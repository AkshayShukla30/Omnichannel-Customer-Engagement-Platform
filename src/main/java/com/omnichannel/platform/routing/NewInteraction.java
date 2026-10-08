package com.omnichannel.platform.routing;

import com.omnichannel.platform.common.Channel;
import com.omnichannel.platform.common.Skill;

public record NewInteraction(Channel channel, Skill skill, int priority, String customerRef, String externalRef) {
}
