package com.omnichannel.platform.routing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.omnichannel.platform.agent.Agent;
import com.omnichannel.platform.agent.AgentRepository;
import com.omnichannel.platform.agent.AgentStatus;
import com.omnichannel.platform.common.Channel;
import com.omnichannel.platform.common.Skill;
import com.omnichannel.platform.routing.telephony.TelephonyProvider;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;

class RoutingEngineTest {

    private final InteractionRepository interactions = mock(InteractionRepository.class);
    private final AgentRepository agents = mock(AgentRepository.class);
    private final TelephonyProvider telephony = mock(TelephonyProvider.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-01-05T10:00:00Z"), ZoneOffset.UTC);
    private final RoutingProperties properties = new RoutingProperties("longest-idle", Duration.ofSeconds(20));

    private RoutingEngine engine;

    @BeforeEach
    void setUp() {
        engine = new RoutingEngine(interactions, agents, Map.of("longest-idle", new LongestIdleStrategy()),
                properties, telephony, clock, mock(PlatformTransactionManager.class));
    }

    private Interaction queued(Skill skill, int priority) {
        return new Interaction(Channel.CALL, skill, priority, "CUST-1001", null, LocalDateTime.now(clock));
    }

    private Agent readyAgent(String name, Skill skill) {
        Agent agent = new Agent(name, name, Set.of(skill));
        agent.markAvailable(LocalDateTime.now(clock).minusMinutes(10));
        return agent;
    }

    @Test
    void assignsQueuedInteractionToAnAvailableAgentWithTheSkill() {
        Interaction loan = queued(Skill.LOAN, 2);
        Agent priya = readyAgent("priya", Skill.LOAN);
        when(interactions.findByStatusOrderByPriorityAscCreatedAtAsc(InteractionStatus.QUEUED)).thenReturn(List.of(loan));
        when(agents.findReadyWithSkill(eq(AgentStatus.AVAILABLE), eq(Skill.LOAN), any())).thenReturn(List.of(priya));

        engine.dispatch();

        assertThat(loan.getStatus()).isEqualTo(InteractionStatus.ASSIGNED);
        assertThat(priya.getStatus()).isEqualTo(AgentStatus.BUSY);
        verify(telephony).connect(loan, priya);
    }

    @Test
    void interactionStaysQueuedWhenNobodyHasTheSkill() {
        Interaction complaint = queued(Skill.COMPLAINT, 2);
        when(interactions.findByStatusOrderByPriorityAscCreatedAtAsc(InteractionStatus.QUEUED)).thenReturn(List.of(complaint));
        when(agents.findReadyWithSkill(any(), any(), any())).thenReturn(List.of());

        engine.dispatch();

        assertThat(complaint.getStatus()).isEqualTo(InteractionStatus.QUEUED);
        verify(telephony, never()).connect(any(), any());
    }

    @Test
    void queueOrderDecidesWhoGetsTheOnlyAgent() {
        Interaction vip = queued(Skill.CARD, 1);
        Interaction regular = queued(Skill.CARD, 2);
        Agent rahul = readyAgent("rahul", Skill.CARD);
        when(interactions.findByStatusOrderByPriorityAscCreatedAtAsc(InteractionStatus.QUEUED)).thenReturn(List.of(vip, regular));
        when(agents.findReadyWithSkill(eq(AgentStatus.AVAILABLE), eq(Skill.CARD), any()))
                .thenAnswer(inv -> rahul.getStatus() == AgentStatus.AVAILABLE ? List.of(rahul) : List.of());

        engine.dispatch();

        assertThat(vip.getStatus()).isEqualTo(InteractionStatus.ASSIGNED);
        assertThat(regular.getStatus()).isEqualTo(InteractionStatus.QUEUED);
    }

    @Test
    void unknownStrategyFailsAtStartup() {
        RoutingProperties bad = new RoutingProperties("random", Duration.ofSeconds(20));

        assertThatThrownBy(() -> new RoutingEngine(interactions, agents, Map.of("longest-idle", new LongestIdleStrategy()),
                bad, telephony, clock, mock(PlatformTransactionManager.class)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("random");
    }

    @Test
    void telephonyFailureDoesNotUndoTheAssignment() {
        Interaction chat = queued(Skill.GENERAL, 2);
        Agent meera = readyAgent("meera", Skill.GENERAL);
        when(interactions.findByStatusOrderByPriorityAscCreatedAtAsc(InteractionStatus.QUEUED)).thenReturn(List.of(chat));
        when(agents.findReadyWithSkill(any(), any(), any())).thenReturn(List.of(meera));
        org.mockito.Mockito.doThrow(new IllegalStateException("provider down")).when(telephony).connect(any(), any());

        engine.dispatch();

        assertThat(chat.getAssignedAgentId()).isEqualTo(meera.getId());
        assertThat(chat.getStatus()).isEqualTo(InteractionStatus.ASSIGNED);
    }
}
