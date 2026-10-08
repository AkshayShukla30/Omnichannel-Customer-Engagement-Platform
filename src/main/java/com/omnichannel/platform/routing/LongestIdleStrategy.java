package com.omnichannel.platform.routing;

import com.omnichannel.platform.agent.Agent;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.ToLongFunction;
import org.springframework.stereotype.Component;

@Component("longest-idle")
public class LongestIdleStrategy implements RoutingStrategy {

    @Override
    public Optional<Agent> select(List<Agent> candidates, ToLongFunction<Agent> handledToday) {
        return candidates.stream()
                .min(Comparator.comparing(Agent::getIdleSince, Comparator.nullsLast(Comparator.<LocalDateTime>naturalOrder())));
    }
}
