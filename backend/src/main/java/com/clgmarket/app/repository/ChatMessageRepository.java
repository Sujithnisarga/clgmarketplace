package com.clgmarket.app.repository;

import com.clgmarket.app.entity.ChatMessage;
import com.clgmarket.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE (m.sender = :u1 AND m.receiver = :u2) OR (m.sender = :u2 AND m.receiver = :u1) ORDER BY m.createdAt ASC")
    List<ChatMessage> findConversation(User u1, User u2);

    @Query("SELECT DISTINCT CASE WHEN m.sender = :user THEN m.receiver ELSE m.sender END FROM ChatMessage m WHERE m.sender = :user OR m.receiver = :user")
    List<User> findChatPartners(User user);
}
