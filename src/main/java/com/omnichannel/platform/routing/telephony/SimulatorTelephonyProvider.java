package com.omnichannel.platform.routing.telephony;

import com.omnichannel.platform.agent.Agent;
import com.omnichannel.platform.routing.Interaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "telephony.provider", havingValue = "simulator", matchIfMissing = true)
public class SimulatorTelephonyProvider implements TelephonyProvider {

    private static final Logger log = LoggerFactory.getLogger(SimulatorTelephonyProvider.class);

    @Override
    public void connect(Interaction interaction, Agent agent) {
        log.info("[sim] bridged {} {} to {}", interaction.getChannel(), interaction.getId(), agent.getUsername());
    }

    @Override
    public void hangUp(Interaction interaction) {
        log.info("[sim] released interaction {}", interaction.getId());
    }
}
