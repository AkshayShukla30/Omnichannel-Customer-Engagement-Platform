package com.omnichannel.platform.routing;

import com.omnichannel.platform.agent.Agent;
import java.util.List;
import java.util.Optional;
import java.util.function.ToLongFunction;

public interface RoutingStrategy {

    Optional<Agent> select(List<Agent> candidates, ToLongFunction<Agent> handledToday);
}
