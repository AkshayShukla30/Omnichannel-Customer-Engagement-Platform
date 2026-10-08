package com.omnichannel.platform.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.omnichannel.platform.common.BusinessRuleException;
import com.omnichannel.platform.common.Skill;
import com.omnichannel.platform.common.UnauthorizedException;
import com.omnichannel.platform.routing.RoutingEngine;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;

class AgentServiceTest {

    private final AgentRepository agents = mock(AgentRepository.class);
    private final RoutingEngine routing = mock(RoutingEngine.class);
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final Clock clock = Clock.fixed(Instant.parse("2026-01-05T10:00:00Z"), ZoneOffset.UTC);

    private AgentService service;
    private Agent priya;

    @BeforeEach
    void setUp() {
        service = new AgentService(agents, encoder, routing, clock, mock(PlatformTransactionManager.class));
        priya = new Agent("priya", "Priya Nair", Set.of(Skill.LOAN)) {
            @Override
            public Long getId() {
                return 1L;
            }

            @Override
            public String getPasswordHash() {
                return encoder.encode("agent123");
            }
        };
        when(agents.findByUsername("priya")).thenReturn(Optional.of(priya));
        when(agents.findById(1L)).thenReturn(Optional.of(priya));
    }

    @Test
    void loginMarksOfflineAgentAvailableAndWakesTheQueue() {
        Agent agent = service.login("priya", "agent123");

        assertThat(agent.getStatus()).isEqualTo(AgentStatus.AVAILABLE);
        assertThat(agent.getLastSeenAt()).isEqualTo(LocalDateTime.of(2026, 1, 5, 10, 0));
        verify(routing).dispatch();
    }

    @Test
    void wrongPasswordAndUnknownUserGetTheSameError() {
        when(agents.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login("priya", "nope")).isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid username or password");
        assertThatThrownBy(() -> service.login("ghost", "agent123")).isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid username or password");
        verify(routing, never()).dispatch();
    }

    @Test
    void busyAgentCannotGoOffline() {
        priya.markBusy();

        assertThatThrownBy(() -> service.changeStatus(1L, AgentStatus.OFFLINE))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void busyCannotBeSetByHand() {
        assertThatThrownBy(() -> service.changeStatus(1L, AgentStatus.BUSY)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void goingAvailableTriggersDispatch() {
        service.changeStatus(1L, AgentStatus.AVAILABLE);

        assertThat(priya.getStatus()).isEqualTo(AgentStatus.AVAILABLE);
        verify(routing).dispatch();
    }
}
