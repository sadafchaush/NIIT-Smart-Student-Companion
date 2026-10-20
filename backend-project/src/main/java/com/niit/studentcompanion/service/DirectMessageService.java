package com.niit.studentcompanion.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.niit.studentcompanion.entity.DirectMessage;
import com.niit.studentcompanion.entity.User;
import com.niit.studentcompanion.repository.DirectMessageRepository;
import com.niit.studentcompanion.repository.UserRepository;

@Service
public class DirectMessageService {

    private final DirectMessageRepository messageRepository;
    private final UserRepository userRepository;

    public DirectMessageService(
            DirectMessageRepository messageRepository,
            UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> getContacts(Long userId) {
        User currentUser = getCampusUser(userId);
        String otherRole = otherCampusRole(currentUser.getRole());

        return userRepository.findAllByRoleIgnoreCase(otherRole).stream()
                .map(user -> new ContactResponse(
                        user.getId(),
                        user.getName(),
                        user.getRole()))
                .toList();
    }

    @Transactional
    public List<MessageResponse> getConversation(
            Long userId,
            Long contactId) {
        User currentUser = getCampusUser(userId);
        User contact = getCampusUser(contactId);
        requireOppositeRoles(currentUser, contact);
        messageRepository.markConversationRead(
                userId,
                contactId,
                LocalDateTime.now());

        return messageRepository.findConversation(userId, contactId).stream()
                .map(DirectMessageService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        getCampusUser(userId);
        return messageRepository.countUnreadForRecipient(userId);
    }

    @Transactional
    public MessageResponse sendMessage(
            Long senderId,
            Long recipientId,
            String content) {
        User sender = getCampusUser(senderId);
        User recipient = getCampusUser(recipientId);
        requireOppositeRoles(sender, recipient);

        if (content == null || content.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Message cannot be empty");
        }
        String trimmedContent = content.trim();
        if (trimmedContent.length() > 2000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Message must be 2000 characters or fewer");
        }

        DirectMessage message = new DirectMessage();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(trimmedContent);
        return toResponse(messageRepository.save(message));
    }

    private User getCampusUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Account not found"));
        normalizeRole(user);
        if (!"STUDENT".equals(user.getRole())
                && !"FACULTY".equals(user.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Messaging is available to students and faculty only");
        }
        return user;
    }

    private String otherCampusRole(String role) {
        return "STUDENT".equals(role) ? "FACULTY" : "STUDENT";
    }

    private void requireOppositeRoles(User first, User second) {
        if (first.getId().equals(second.getId())
                || first.getRole().equalsIgnoreCase(second.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Messages can only be exchanged between a student and faculty member");
        }
    }

    private static void normalizeRole(User user) {
        if (user.getRole() != null) {
            user.setRole(user.getRole().trim().toUpperCase(Locale.ROOT));
        }
    }

    private static MessageResponse toResponse(DirectMessage message) {
        return new MessageResponse(
                message.getId(),
                message.getSender().getId(),
                message.getSender().getName(),
                message.getRecipient().getId(),
                message.getRecipient().getName(),
                message.getContent(),
                message.getSentAt());
    }

    public record ContactResponse(
            Long id,
            String name,
            String role) {
    }

    public record MessageResponse(
            Long id,
            Long senderId,
            String senderName,
            Long recipientId,
            String recipientName,
            String content,
            java.time.LocalDateTime sentAt) {
    }
}
