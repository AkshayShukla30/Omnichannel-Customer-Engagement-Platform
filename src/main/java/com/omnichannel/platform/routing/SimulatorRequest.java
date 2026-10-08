package com.omnichannel.platform.routing;

import com.omnichannel.platform.common.Channel;
import com.omnichannel.platform.common.Skill;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record SimulatorRequest(
        @Min(1) @Max(50) Integer count,
        Skill skill,
        Channel channel,
        @Min(1) @Max(3) Integer priority,
        @Size(max = 50) String customerRef) {
}
