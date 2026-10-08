package com.omnichannel.platform.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.omnichannel.platform.common.BusinessRuleException;
import com.omnichannel.platform.common.Channel;
import com.omnichannel.platform.common.Skill;
import com.omnichannel.platform.integration.CustomerDataAdapter;
import com.omnichannel.platform.integration.CustomerDataAdapterFactory;
import com.omnichannel.platform.integration.CustomerProfile;
import com.omnichannel.platform.routing.Interaction;
import com.omnichannel.platform.routing.InteractionRepository;
import com.omnichannel.platform.routing.InteractionStatus;
import com.omnichannel.platform.routing.RoutingEngine;
import com.omnichannel.platform.routing.telephony.TelephonyProvider;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;

class DeskServiceTest {

    private final AgentRepository agents = mock(AgentRepository.class);
    private final InteractionRepository interactions = mock(InteractionRepository.class);
    private final InteractionNoteRepository notes = mock(InteractionNoteRepository.class);
    private final CustomerDataAdapterFactory adapters = mock(CustomerDataAdapterFactory.class);
    private final CustomerDataAdapter adapter = mock(CustomerDataAdapter.class);
    private final RoutingEngine routing = mock(RoutingEngine.class);
    private final TelephonyProvider telephony = mock(TelephonyProvider.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-01-05T10:00:00Z"), ZoneOffset.UTC);

    private DeskService desk;
    private Agent agent;
    private Interaction interaction;

    @BeforeEach
    void setUp() {
        desk = new DeskService(agents, interactions, notes, adapters, routing, telephony, clock,
                mock(PlatformTransactionManager.class));
        agent = new Agent("priya", "Priya Nair", Set.of(Skill.LOAN)) {
            @Override
            public Long getId() {
                return 7L;
            }
        };
        agent.markBusy();
        interaction = new Interaction(Channel.CALL, Skill.LOAN, 2, "CUST-1001", null, LocalDateTime.of(2026, 1, 5, 9, 59));
        interaction.assignTo(7L, LocalDateTime.of(2026, 1, 5, 9, 59, 30));

        when(agents.findById(7L)).thenReturn(Optional.of(agent));
        when(interactions.findById(100L)).thenReturn(Optional.of(interaction));
        when(adapters.current()).thenReturn(adapter);
        when(adapter.type()).thenReturn("core-banking");
    }

    @Test
    void screenPopCarriesTheCustomerFromTheAdapter() {
        when(interactions.findFirstByAssignedAgentIdAndStatus(7L, InteractionStatus.ASSIGNED)).thenReturn(Optional.of(interaction));
        CustomerProfile profile = new CustomerProfile("CUST-1001", "Aarav Sharma", null, null, "PREMIER", "1",
                BigDecimal.TEN, 1, "PLATINUM", "core-banking");
        when(adapter.findById("CUST-1001")).thenReturn(Optional.of(profile));

        DeskState state = desk.state(7L);

        assertThat(state.customer()).isEqualTo(profile);
        assertThat(state.customerError()).isNull();
        verify(agents).touch(any(), any());
    }

    @Test
    void crmOutageIsShownAsAWarningNotAnError() {
        when(interactions.findFirstByAssignedAgentIdAndStatus(7L, InteractionStatus.ASSIGNED)).thenReturn(Optional.of(interaction));
        when(adapter.findById("CUST-1001")).thenThrow(new IllegalStateException("connection refused"));

        DeskState state = desk.state(7L);

        assertThat(state.customer()).isNull();
        assertThat(state.interaction()).isNotNull();
        assertThat(state.customerError()).contains("unavailable");
    }

    @Test
    void idleAgentGetsAnEmptyState() {
        when(interactions.findFirstByAssignedAgentIdAndStatus(7L, InteractionStatus.ASSIGNED)).thenReturn(Optional.empty());

        assertThat(desk.state(7L).interaction()).isNull();
    }

    @Test
    void closingStoresDispositionFreesTheAgentAndWakesTheQueue() {
        desk.close(7L, 100L, Disposition.RESOLVED, "card unblocked");

        assertThat(interaction.getStatus()).isEqualTo(InteractionStatus.CLOSED);
        assertThat(interaction.getDisposition()).isEqualTo(Disposition.RESOLVED);
        assertThat(agent.getStatus()).isEqualTo(AgentStatus.AVAILABLE);
        verify(notes).save(any());
        verify(telephony).hangUp(interaction);
        verify(routing).dispatch();
    }

    @Test
    void cannotCloseSomeoneElsesInteraction() {
        assertThatThrownBy(() -> desk.close(8L, 100L, Disposition.RESOLVED, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not assigned to you");
        verify(routing, never()).dispatch();
    }

    @Test
    void cannotCloseTwice() {
        interaction.close(Disposition.RESOLVED, LocalDateTime.of(2026, 1, 5, 9, 59, 50));

        assertThatThrownBy(() -> desk.close(7L, 100L, Disposition.ESCALATED, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already closed");
    }

    @Test
    void addingANoteToAClosedInteractionIsRejected() {
        interaction.close(Disposition.RESOLVED, LocalDateTime.of(2026, 1, 5, 9, 59, 50));

        assertThatThrownBy(() -> desk.addNote(7L, 100L, "late note")).isInstanceOf(BusinessRuleException.class);
    }
}
