package com.supervision.livraisons.repository;

import com.supervision.livraisons.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByNocdeOrderBySentAtAsc(Integer nocde);

    @Query("SELECT DISTINCT m.nocde FROM ChatMessage m")
    List<Integer> findDistinctNocde();
}
