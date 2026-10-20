package com.niit.studentcompanion.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.niit.studentcompanion.service.DirectMessageService;
import com.niit.studentcompanion.service.DirectMessageService.ContactResponse;
import com.niit.studentcompanion.service.DirectMessageService.MessageResponse;

@RestController
@RequestMapping("/messages")
public class DirectMessageController {

    private final DirectMessageService messageService;

    public DirectMessageController(DirectMessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/contacts")
    public ResponseEntity<List<ContactResponse>> getContacts(
            HttpServletRequest request) {
        return ResponseEntity.ok(
                messageService.getContacts(authenticatedUserId(request)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(HttpServletRequest request) {
        return ResponseEntity.ok(
                messageService.getUnreadCount(authenticatedUserId(request)));
    }

    @GetMapping("/{contactId}")
    public ResponseEntity<List<MessageResponse>> getConversation(
            @PathVariable Long contactId,
            HttpServletRequest request) {
        return ResponseEntity.ok(messageService.getConversation(
                authenticatedUserId(request),
                contactId));
    }

    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            @RequestBody MessageRequest body,
            HttpServletRequest request) {
        if (body.recipientId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A valid recipient is required");
        }

        MessageResponse message = messageService.sendMessage(
                authenticatedUserId(request),
                body.recipientId(),
                body.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    private Long authenticatedUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object userId = session == null
                ? null
                : session.getAttribute("authenticatedUserId");
        if (userId instanceof Long id) {
            return id;
        }
        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Please sign in to use messages");
    }

    public record MessageRequest(Long recipientId, String content) {
    }
}
