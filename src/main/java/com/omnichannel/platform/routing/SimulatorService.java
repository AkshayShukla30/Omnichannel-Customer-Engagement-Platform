package com.omnichannel.platform.routing;

import com.omnichannel.platform.common.Channel;
import com.omnichannel.platform.common.Skill;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;

@Service
public class SimulatorService {

    private final RoutingEngine routing;
    private final SimulatorProperties properties;

    public SimulatorService(RoutingEngine routing, SimulatorProperties properties) {
        this.routing = routing;
        this.properties = properties;
    }

    public List<InteractionView> generate(SimulatorRequest request) {
        SimulatorRequest req = request == null ? new SimulatorRequest(null, null, null, null, null) : request;
        int count = req.count() == null ? 1 : req.count();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        List<InteractionView> created = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Skill skill = req.skill() != null ? req.skill() : Skill.values()[random.nextInt(Skill.values().length)];
            Channel channel = req.channel() != null ? req.channel() : Channel.values()[random.nextInt(Channel.values().length)];
            int priority = req.priority() != null ? req.priority() : (random.nextInt(5) == 0 ? 1 : 2);
            List<String> refs = properties.customerRefs();
            String customerRef = req.customerRef() != null ? req.customerRef() : refs.get(random.nextInt(refs.size()));
            created.add(InteractionView.from(routing.enqueue(new NewInteraction(channel, skill, priority, customerRef, null))));
        }
        return created;
    }
}
