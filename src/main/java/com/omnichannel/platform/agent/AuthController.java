package com.omnichannel.platform.agent;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Cookie session login for agents")
public class AuthController {

    private final AgentService agentService;

    public AuthController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/login")
    @Operation(summary = "Log in, marks the agent available")
    public AgentView login(@Valid @RequestBody LoginRequest body, HttpServletRequest request) {
        Agent agent = agentService.login(body.username(), body.password());
        request.getSession(true);
        request.changeSessionId();
        request.getSession().setAttribute(AuthInterceptor.SESSION_KEY, agent.getId());
        return AgentView.from(agent);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Log out, only allowed when no interaction is in progress")
    public void logout(@SessionAttribute(AuthInterceptor.SESSION_KEY) Long agentId, HttpServletRequest request) {
        agentService.changeStatus(agentId, AgentStatus.OFFLINE);
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Current agent")
    public AgentView me(@SessionAttribute(AuthInterceptor.SESSION_KEY) Long agentId) {
        return AgentView.from(agentService.get(agentId));
    }

    public record LoginRequest(@NotBlank @Size(max = 50) String username, @NotBlank @Size(max = 100) String password) {
    }
}
