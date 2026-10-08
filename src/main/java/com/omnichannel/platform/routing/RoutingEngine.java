package com.omnichannel.platform.routing;

import com.omnichannel.platform.agent.Agent;
import com.omnichannel.platform.agent.AgentRepository;
import com.omnichannel.platform.agent.AgentStatus;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.omnichannel.platform.routing.telephony.TelephonyProvider;

@Service
public class RoutingEngine {

    private static final Logger log = LoggerFactory.getLogger(RoutingEngine.class);

    private final InteractionRepository interactions;
    private final AgentRepository agents;
    private final RoutingStrategy strategy;
    private final RoutingProperties properties;
    private final TelephonyProvider telephony;
    private final Clock clock;
    private final TransactionTemplate tx;
    private final ReentrantLock dispatchLock = new ReentrantLock();

    public RoutingEngine(InteractionRepository interactions, AgentRepository agents,
            Map<String, RoutingStrategy> strategies, RoutingProperties properties, TelephonyProvider telephony,
            Clock clock, PlatformTransactionManager transactionManager) {
        this.interactions = interactions;
        this.agents = agents;
        this.properties = properties;
        this.telephony = telephony;
        this.clock = clock;
        this.tx = new TransactionTemplate(transactionManager);
        this.strategy = strategies.get(properties.strategy());
        if (this.strategy == null) {
            throw new IllegalStateException("Unknown routing.strategy '" + properties.strategy()
                    + "', available: " + strategies.keySet());
        }
        log.info("Routing strategy: {}", properties.strategy());
    }

    public Interaction enqueue(NewInteraction request) {
        Interaction saved = interactions.save(new Interaction(request.channel(), request.skill(),
                request.priority(), request.customerRef(), request.externalRef(), LocalDateTime.now(clock)));
        log.info("Queued interaction {} ({} {} priority {})", saved.getId(), saved.getChannel(), saved.getSkill(),
                saved.getPriority());
        dispatch();
        return interactions.findById(saved.getId()).orElseThrow();
    }

    // sweep every few seconds too, an agent coming back online should not need a new interaction to wake the queue
    @Scheduled(fixedDelay = 3000)
    public void dispatch() {
        // one lock is enough for a single node; with several nodes this would become SELECT ... FOR UPDATE SKIP LOCKED
        dispatchLock.lock();
        try {
            List<Assignment> assignments = tx.execute(status -> assignWaiting());
            if (assignments == null) {
                return;
            }
            for (Assignment a : assignments) {
                connectQuietly(a);
            }
        } finally {
            dispatchLock.unlock();
        }
    }

    private List<Assignment> assignWaiting() {
        List<Assignment> done = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime seenAfter = now.minus(properties.heartbeatTimeout());
        LocalDateTime startOfDay = LocalDate.now(clock).atStartOfDay();

        for (Interaction interaction : interactions.findByStatusOrderByPriorityAscCreatedAtAsc(InteractionStatus.QUEUED)) {
            // no agent for this skill: move on, other skills should not wait behind it
            List<Agent> candidates = agents.findReadyWithSkill(AgentStatus.AVAILABLE, interaction.getSkill(), seenAfter);
            strategy.select(candidates,
                    agent -> interactions.countByAssignedAgentIdAndAssignedAtAfter(agent.getId(), startOfDay))
                    .ifPresent(agent -> {
                        interaction.assignTo(agent.getId(), now);
                        agent.markBusy();
                        done.add(new Assignment(interaction, agent));
                        log.info("Interaction {} -> {}", interaction.getId(), agent.getUsername());
                    });
        }
        return done;
    }

    private void connectQuietly(Assignment a) {
        try {
            telephony.connect(a.interaction(), a.agent());
        } catch (RuntimeException e) {
            log.error("Telephony connect failed for interaction {}", a.interaction().getId(), e);
        }
    }

    private record Assignment(Interaction interaction, Agent agent) {
    }
}
