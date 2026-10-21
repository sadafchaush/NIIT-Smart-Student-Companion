package com.niit.studentcompanion.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.niit.studentcompanion.entity.DirectMessage;

public interface DirectMessageRepository
        extends JpaRepository<DirectMessage, Long> {

    boolean existsBySender_IdOrRecipient_Id(Long senderId, Long recipientId);

    @Query("""
            SELECT message
            FROM DirectMessage message
            JOIN FETCH message.sender
            JOIN FETCH message.recipient
            WHERE (message.sender.id = :userId
                   AND message.recipient.id = :contactId)
               OR (message.sender.id = :contactId
                   AND message.recipient.id = :userId)
            ORDER BY message.sentAt ASC
            """)
    List<DirectMessage> findConversation(
            @Param("userId") Long userId,
            @Param("contactId") Long contactId);

    @Query("SELECT COUNT(message) FROM DirectMessage message "
            + "WHERE message.recipient.id = :recipientId "
            + "AND message.readAt IS NULL")
    long countUnreadForRecipient(
            @Param("recipientId") Long recipientId);

    @Modifying
    @Query("UPDATE DirectMessage message "
            + "SET message.readAt = :readAt "
            + "WHERE message.recipient.id = :recipientId "
            + "AND message.sender.id = :senderId "
            + "AND message.readAt IS NULL")
    int markConversationRead(
            @Param("recipientId") Long recipientId,
            @Param("senderId") Long senderId,
            @Param("readAt") LocalDateTime readAt);
}
