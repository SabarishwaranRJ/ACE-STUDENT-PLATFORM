package com.aceteamup.config;

import com.aceteamup.entity.Student;
import com.aceteamup.repository.StudentRepository;
import com.aceteamup.security.JwtUtil;
import com.aceteamup.security.UserDetailsServiceImpl;
import com.aceteamup.service.WorkspaceSecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthChannelInterceptor.class);
    private static final Pattern TEAM_DESTINATION_PATTERN = Pattern.compile("^/(?:topic/team|app/chat\\.[^/]+)/(\\d+)(?:/.*)?$");
    private static final Pattern CONV_DESTINATION_PATTERN = Pattern.compile("^/(?:topic/conversation|app/dm\\.[^/]+)/(\\d+)(?:/.*)?$");
    private static final Pattern NOTIF_DESTINATION_PATTERN = Pattern.compile("^/topic/notifications/(\\d+)$");

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final StudentRepository studentRepository;
    private final WorkspaceSecurityService workspaceSecurityService;
    private final com.aceteamup.repository.ConversationMemberRepository conversationMemberRepository;

    public WebSocketAuthChannelInterceptor(JwtUtil jwtUtil,
                                           UserDetailsServiceImpl userDetailsService,
                                           StudentRepository studentRepository,
                                           WorkspaceSecurityService workspaceSecurityService,
                                           com.aceteamup.repository.ConversationMemberRepository conversationMemberRepository) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.studentRepository = studentRepository;
        this.workspaceSecurityService = workspaceSecurityService;
        this.conversationMemberRepository = conversationMemberRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            authenticateConnect(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(command)) {
            authorizeSubscription(accessor);
        } else if (StompCommand.SEND.equals(command)) {
            authorizeSend(accessor);
        }

        return message;
    }

    private void authenticateConnect(StompHeaderAccessor accessor) {
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        String token = null;

        if (authHeaders != null && !authHeaders.isEmpty()) {
            String header = authHeaders.get(0);
            if (header.startsWith("Bearer ")) {
                token = header.substring(7);
            } else {
                token = header;
            }
        } else {
            // Also check 'token' header
            List<String> rawTokens = accessor.getNativeHeader("token");
            if (rawTokens != null && !rawTokens.isEmpty()) {
                token = rawTokens.get(0);
            }
        }

        if (token != null && !token.isBlank()) {
            try {
                String email = jwtUtil.extractEmail(token);
                if (email != null && jwtUtil.validateToken(token, email)) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    accessor.setUser(authentication);
                    log.info("WebSocket connection authenticated for user: {}", email);
                    return;
                }
            } catch (Exception e) {
                log.warn("WebSocket authentication failed: {}", e.getMessage());
            }
        }
        log.warn("WebSocket connect attempted without valid authorization token");
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) return;

        Matcher teamMatcher = TEAM_DESTINATION_PATTERN.matcher(destination);
        if (teamMatcher.find()) {
            Long teamId = Long.parseLong(teamMatcher.group(1));
            String email = accessor.getUser() != null ? accessor.getUser().getName() : null;

            if (email == null) {
                throw new AccessDeniedException("Unauthenticated subscription attempt to: " + destination);
            }

            Student student = studentRepository.findByEmail(email).orElse(null);
            if (student == null || !workspaceSecurityService.isStudentMemberOfTeam(student.getStudentId(), teamId)) {
                log.warn("Denied subscription for {} to unauthorized team destination {}", email, destination);
                throw new AccessDeniedException("Access denied: You are not an active member of team " + teamId);
            }
            return;
        }

        Matcher convMatcher = CONV_DESTINATION_PATTERN.matcher(destination);
        if (convMatcher.find()) {
            Long convId = Long.parseLong(convMatcher.group(1));
            String email = accessor.getUser() != null ? accessor.getUser().getName() : null;

            if (email == null) {
                throw new AccessDeniedException("Unauthenticated subscription attempt to: " + destination);
            }

            Student student = studentRepository.findByEmail(email).orElse(null);
            if (student == null || !conversationMemberRepository.existsByConversationConversationIdAndStudentStudentId(convId, student.getStudentId())) {
                log.warn("Denied subscription for {} to unauthorized conversation destination {}", email, destination);
                throw new AccessDeniedException("Access denied: You are not a participant in conversation " + convId);
            }
            return;
        }

        Matcher notifMatcher = NOTIF_DESTINATION_PATTERN.matcher(destination);
        if (notifMatcher.find()) {
            Long targetStudentId = Long.parseLong(notifMatcher.group(1));
            String email = accessor.getUser() != null ? accessor.getUser().getName() : null;

            if (email == null) {
                throw new AccessDeniedException("Unauthenticated subscription attempt to: " + destination);
            }

            Student student = studentRepository.findByEmail(email).orElse(null);
            if (student == null || !student.getStudentId().equals(targetStudentId)) {
                log.warn("Denied subscription for {} to unauthorized notification destination {}", email, destination);
                throw new AccessDeniedException("Access denied: You can only subscribe to your own notifications");
            }
        }
    }

    private void authorizeSend(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) return;

        Matcher teamMatcher = TEAM_DESTINATION_PATTERN.matcher(destination);
        if (teamMatcher.find()) {
            Long teamId = Long.parseLong(teamMatcher.group(1));
            String email = accessor.getUser() != null ? accessor.getUser().getName() : null;

            if (email == null) {
                throw new AccessDeniedException("Unauthenticated send attempt to: " + destination);
            }

            Student student = studentRepository.findByEmail(email).orElse(null);
            if (student == null || !workspaceSecurityService.isStudentMemberOfTeam(student.getStudentId(), teamId)) {
                log.warn("Denied send for {} to unauthorized team destination {}", email, destination);
                throw new AccessDeniedException("Access denied: You are not an active member of team " + teamId);
            }
            return;
        }

        Matcher convMatcher = CONV_DESTINATION_PATTERN.matcher(destination);
        if (convMatcher.find()) {
            Long convId = Long.parseLong(convMatcher.group(1));
            String email = accessor.getUser() != null ? accessor.getUser().getName() : null;

            if (email == null) {
                throw new AccessDeniedException("Unauthenticated send attempt to: " + destination);
            }

            Student student = studentRepository.findByEmail(email).orElse(null);
            if (student == null || !conversationMemberRepository.existsByConversationConversationIdAndStudentStudentId(convId, student.getStudentId())) {
                log.warn("Denied send for {} to unauthorized conversation destination {}", email, destination);
                throw new AccessDeniedException("Access denied: You are not a participant in conversation " + convId);
            }
        }
    }
}

