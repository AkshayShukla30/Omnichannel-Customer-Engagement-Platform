package com.omnichannel.platform.agent;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InteractionNoteRepository extends JpaRepository<InteractionNote, Long> {

    List<InteractionNote> findByInteractionIdOrderByCreatedAtAsc(Long interactionId);
}
