package com.omnichannel.platform.agent;

import java.time.LocalDateTime;

public record NoteView(Long id, String body, LocalDateTime createdAt) {

    static NoteView from(InteractionNote note) {
        return new NoteView(note.getId(), note.getBody(), note.getCreatedAt());
    }
}
