package com.omnichannel.platform.agent;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

@RestController
@RequestMapping("/api/desk")
@Tag(name = "Agent Desktop", description = "What the agent sees: current interaction, screen pop, notes, disposition")
public class DeskController {

    private final DeskService desk;
    private final AgentService agentService;

    public DeskController(DeskService desk, AgentService agentService) {
        this.desk = desk;
        this.agentService = agentService;
    }

    @GetMapping("/state")
    @Operation(summary = "Poll for the assigned interaction and customer screen pop (also works as the agent heartbeat)")
    public DeskState state(@SessionAttribute(AuthInterceptor.SESSION_KEY) Long agentId) {
        return desk.state(agentId);
    }

    @PostMapping("/status")
    @Operation(summary = "Switch between AVAILABLE and OFFLINE")
    public AgentView status(@SessionAttribute(AuthInterceptor.SESSION_KEY) Long agentId,
            @Valid @RequestBody StatusRequest body) {
        return AgentView.from(agentService.changeStatus(agentId, body.status()));
    }

    @PostMapping("/interactions/{id}/notes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a note to the interaction")
    public NoteView addNote(@SessionAttribute(AuthInterceptor.SESSION_KEY) Long agentId, @PathVariable Long id,
            @Valid @RequestBody NoteRequest body) {
        return desk.addNote(agentId, id, body.text());
    }

    @PostMapping("/interactions/{id}/close")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Set a disposition and close the interaction, the agent becomes available again")
    public void close(@SessionAttribute(AuthInterceptor.SESSION_KEY) Long agentId, @PathVariable Long id,
            @Valid @RequestBody CloseRequest body) {
        desk.close(agentId, id, body.disposition(), body.note());
    }

    public record StatusRequest(@NotNull AgentStatus status) {
    }

    public record NoteRequest(@NotBlank @Size(max = 2000) String text) {
    }

    public record CloseRequest(@NotNull Disposition disposition, @Size(max = 2000) String note) {
    }
}
