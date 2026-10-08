package com.omnichannel.platform.routing;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/simulator")
@Tag(name = "Simulator", description = "Generates incoming calls and chats so the demo works without telephony")
public class SimulatorController {

    private final SimulatorService simulator;

    public SimulatorController(SimulatorService simulator) {
        this.simulator = simulator;
    }

    @PostMapping("/interactions")
    @Operation(summary = "Create random (or fully specified) incoming interactions and route them")
    public List<InteractionView> generate(@Valid @RequestBody(required = false) SimulatorRequest request) {
        return simulator.generate(request);
    }
}
