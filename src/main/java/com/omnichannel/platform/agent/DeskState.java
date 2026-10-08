package com.omnichannel.platform.agent;

import com.omnichannel.platform.integration.CustomerProfile;
import com.omnichannel.platform.routing.InteractionView;
import java.util.List;

public record DeskState(AgentView agent, InteractionView interaction, CustomerProfile customer,
        String customerError, List<NoteView> notes) {

    static DeskState idle(AgentView agent) {
        return new DeskState(agent, null, null, null, List.of());
    }
}
