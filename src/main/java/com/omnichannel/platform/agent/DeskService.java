package com.omnichannel.platform.agent;

import com.omnichannel.platform.common.BusinessRuleException;
import com.omnichannel.platform.common.NotFoundException;
import com.omnichannel.platform.integration.CustomerDataAdapterFactory;
import com.omnichannel.platform.integration.CustomerProfile;
import com.omnichannel.platform.routing.Interaction;
import com.omnichannel.platform.routing.InteractionRepository;
import com.omnichannel.platform.routing.InteractionStatus;
import com.omnichannel.platform.routing.InteractionView;
import com.omnichannel.platform.routing.RoutingEngine;
import com.omnichannel.platform.routing.telephony.TelephonyProvider;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

@Service
public class DeskService {

    private static final Logger log = LoggerFactory.getLogger(DeskService.class);

    private final AgentRepository agents;
    private final InteractionRepository interactions;
    private final InteractionNoteRepository notes;
    private final CustomerDataAdapterFactory adapters;
    private final RoutingEngine routing;
    private final TelephonyProvider telephony;
    private final Clock clock;
    private final TransactionTemplate tx;

    public DeskService(AgentRepository agents, InteractionRepository interactions, InteractionNoteRepository notes,
            CustomerDataAdapterFactory adapters, RoutingEngine routing, TelephonyProvider telephony, Clock clock,
            PlatformTransactionManager transactionManager) {
        this.agents = agents;
        this.interactions = interactions;
        this.notes = notes;
        this.adapters = adapters;
        this.routing = routing;
        this.telephony = telephony;
        this.clock = clock;
        this.tx = new TransactionTemplate(transactionManager);
    }

    public DeskState state(Long agentId) {
        agents.touch(agentId, LocalDateTime.now(clock));
        Agent agent = agents.findById(agentId).orElseThrow(() -> new NotFoundException("Agent not found"));
        AgentView agentView = AgentView.from(agent);

        Optional<Interaction> current = interactions.findFirstByAssignedAgentIdAndStatus(agentId, InteractionStatus.ASSIGNED);
        if (current.isEmpty()) {
            return DeskState.idle(agentView);
        }
        Interaction interaction = current.get();

        CustomerProfile customer = null;
        String customerError = null;
        try {
            customer = adapters.current().findById(interaction.getCustomerRef()).orElse(null);
            if (customer == null) {
                customerError = "Customer " + interaction.getCustomerRef() + " was not found in "
                        + adapters.current().type();
            }
        } catch (RuntimeException e) {
            log.warn("CRM lookup failed for {}: {}", interaction.getCustomerRef(), e.getMessage());
            customerError = "Customer data is unavailable right now (" + adapters.current().type() + ")";
        }

        List<NoteView> noteViews = notes.findByInteractionIdOrderByCreatedAtAsc(interaction.getId()).stream()
                .map(NoteView::from).toList();
        return new DeskState(agentView, InteractionView.from(interaction), customer, customerError, noteViews);
    }

    public NoteView addNote(Long agentId, Long interactionId, String text) {
        return tx.execute(status -> {
            requireOpenAndOwned(agentId, interactionId);
            InteractionNote saved = notes.save(new InteractionNote(interactionId, agentId, text.trim(), LocalDateTime.now(clock)));
            return NoteView.from(saved);
        });
    }

    public void close(Long agentId, Long interactionId, Disposition disposition, String finalNote) {
        LocalDateTime now = LocalDateTime.now(clock);
        Interaction closed = tx.execute(status -> {
            Interaction interaction = requireOpenAndOwned(agentId, interactionId);
            if (StringUtils.hasText(finalNote)) {
                notes.save(new InteractionNote(interactionId, agentId, finalNote.trim(), now));
            }
            interaction.close(disposition, now);
            agents.findById(agentId).orElseThrow(() -> new NotFoundException("Agent not found")).markAvailable(now);
            return interaction;
        });
        log.info("Agent {} closed interaction {} as {}", agentId, interactionId, disposition);

        try {
            telephony.hangUp(closed);
        } catch (RuntimeException e) {
            log.warn("Telephony hang up failed for interaction {}: {}", interactionId, e.getMessage());
        }
        routing.dispatch();
    }

    private Interaction requireOpenAndOwned(Long agentId, Long interactionId) {
        Interaction interaction = interactions.findById(interactionId)
                .orElseThrow(() -> new NotFoundException("Interaction " + interactionId + " not found"));
        if (!agentId.equals(interaction.getAssignedAgentId())) {
            throw new BusinessRuleException("Interaction " + interactionId + " is not assigned to you");
        }
        if (interaction.getStatus() != InteractionStatus.ASSIGNED) {
            throw new BusinessRuleException("Interaction " + interactionId + " is already closed");
        }
        return interaction;
    }
}
