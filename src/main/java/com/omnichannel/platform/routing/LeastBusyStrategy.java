package com.omnichannel.platform.routing;

import com.omnichannel.platform.agent.Agent;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.ToLongFunction;
import org.springframework.stereotype.Component;

@Component("least-busy")
public class LeastBusyStrategy implements RoutingStrategy {

    // idle time breaks ties so a fresh start does not always pick the same agent
    @Override
    public Optional<Agent> select(List<Agent> candidates, ToLongFunction<Agent> handledToday) {
        Comparator<Agent> byIdle = Comparator.comparing(Agent::getIdleSince,
                Comparator.nullsLast(Comparator.<LocalDateTime>naturalOrder()));
        return candidates.stream()
                .min(Comparator.comparingLong(handledToday).thenComparing(byIdle));
    }
}
