package com.omnichannel.platform.routing;

import com.omnichannel.platform.common.Channel;
import com.omnichannel.platform.common.Skill;
import java.time.LocalDateTime;

public record InteractionView(Long id, Channel channel, Skill skill, int priority, String customerRef,
        InteractionStatus status, LocalDateTime createdAt, LocalDateTime assignedAt) {

    public static InteractionView from(Interaction i) {
        return new InteractionView(i.getId(), i.getChannel(), i.getSkill(), i.getPriority(), i.getCustomerRef(),
                i.getStatus(), i.getCreatedAt(), i.getAssignedAt());
    }
}
