package com.omnichannel.platform.routing.telephony;

import com.omnichannel.platform.agent.Agent;
import com.omnichannel.platform.routing.Interaction;

public interface TelephonyProvider {

    void connect(Interaction interaction, Agent agent);

    void hangUp(Interaction interaction);
}
