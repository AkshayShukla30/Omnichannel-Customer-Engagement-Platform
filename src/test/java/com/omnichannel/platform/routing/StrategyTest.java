package com.omnichannel.platform.routing;

import static org.assertj.core.api.Assertions.assertThat;

import com.omnichannel.platform.agent.Agent;
import com.omnichannel.platform.common.Skill;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class StrategyTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 5, 10, 0);

    private Agent agent(String username, int idleMinutes) {
        Agent agent = new Agent(username, username, Set.of(Skill.GENERAL));
        agent.markAvailable(NOW.minusMinutes(idleMinutes));
        return agent;
    }

    @Test
    void longestIdlePicksTheAgentWhoWaitedMost() {
        Agent recent = agent("recent", 2);
        Agent veteran = agent("veteran", 45);

        assertThat(new LongestIdleStrategy().select(List.of(recent, veteran), a -> 0)).contains(veteran);
    }

    @Test
    void leastBusyPicksTheAgentWithFewestInteractionsToday() {
        Agent loaded = agent("loaded", 90);
        Agent light = agent("light", 1);
        Map<String, Long> handled = Map.of("loaded", 7L, "light", 2L);

        assertThat(new LeastBusyStrategy().select(List.of(loaded, light), a -> handled.get(a.getUsername())))
                .contains(light);
    }

    @Test
    void leastBusyBreaksTiesByIdleTime() {
        Agent first = agent("first", 5);
        Agent second = agent("second", 30);

        assertThat(new LeastBusyStrategy().select(List.of(first, second), a -> 3)).contains(second);
    }

    @Test
    void noCandidatesMeansNoAgent() {
        assertThat(new LongestIdleStrategy().select(List.of(), a -> 0)).isEmpty();
        assertThat(new LeastBusyStrategy().select(List.of(), a -> 0)).isEmpty();
    }
}
