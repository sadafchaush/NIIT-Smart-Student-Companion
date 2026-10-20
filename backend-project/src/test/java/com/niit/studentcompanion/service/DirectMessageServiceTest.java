package com.niit.studentcompanion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.niit.studentcompanion.entity.DirectMessage;
import com.niit.studentcompanion.entity.User;
import com.niit.studentcompanion.repository.DirectMessageRepository;
import com.niit.studentcompanion.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class DirectMessageServiceTest {

    @Mock
    private DirectMessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void sendsTrimmedMessageFromStudentToFaculty() {
        User student = user(1L, "Student", "STUDENT");
        User faculty = user(2L, "Faculty", "FACULTY");
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(userRepository.findById(2L)).thenReturn(Optional.of(faculty));
        when(messageRepository.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DirectMessageService service =
                new DirectMessageService(messageRepository, userRepository);
        DirectMessageService.MessageResponse response =
                service.sendMessage(1L, 2L, "  Hello faculty  ");

        assertEquals(1L, response.senderId());
        assertEquals(2L, response.recipientId());
        assertEquals("Hello faculty", response.content());
        verify(messageRepository).save(org.mockito.ArgumentMatchers.argThat(
                message -> message.getSender().getId().equals(1L)
                        && message.getRecipient().getId().equals(2L)));
    }

    @Test
    void rejectsMessagesBetweenAccountsWithSameRole() {
        User firstStudent = user(1L, "First student", "STUDENT");
        User secondStudent = user(2L, "Second student", "STUDENT");
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(firstStudent));
        when(userRepository.findById(2L))
                .thenReturn(Optional.of(secondStudent));

        DirectMessageService service =
                new DirectMessageService(messageRepository, userRepository);

        assertThrows(
                ResponseStatusException.class,
                () -> service.sendMessage(1L, 2L, "Not allowed"));
    }

    @Test
    void rejectsBlankMessages() {
        User student = user(1L, "Student", "STUDENT");
        User faculty = user(2L, "Faculty", "FACULTY");
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(userRepository.findById(2L)).thenReturn(Optional.of(faculty));

        DirectMessageService service =
                new DirectMessageService(messageRepository, userRepository);

        assertThrows(
                ResponseStatusException.class,
                () -> service.sendMessage(1L, 2L, "  "));
    }

    @Test
    void marksIncomingConversationMessagesReadWhenConversationIsOpened() {
        User student = user(1L, "Student", "STUDENT");
        User faculty = user(2L, "Faculty", "FACULTY");
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(userRepository.findById(2L)).thenReturn(Optional.of(faculty));
        when(messageRepository.findConversation(1L, 2L))
                .thenReturn(Collections.emptyList());

        DirectMessageService service =
                new DirectMessageService(messageRepository, userRepository);

        service.getConversation(1L, 2L);

        verify(messageRepository).markConversationRead(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(2L),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void returnsUnreadMessageCountForCampusUser() {
        User student = user(1L, "Student", "STUDENT");
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(messageRepository.countUnreadForRecipient(1L)).thenReturn(3L);

        DirectMessageService service =
                new DirectMessageService(messageRepository, userRepository);

        assertEquals(3L, service.getUnreadCount(1L));
    }

    private User user(Long id, String name, String role) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(name.toLowerCase().replace(' ', '.') + "@niit.edu");
        user.setPassword("not-used");
        user.setRole(role);
        return user;
    }
}
