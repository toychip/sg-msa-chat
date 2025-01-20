package com.asyncgate.chat_server.config;

import com.asyncgate.chat_server.util.JwtTokenUtil;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;


@Slf4j
@RequiredArgsConstructor
@Component
public class FilterChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenUtil jwtTokenUtil;

    @Value("${spring.kafka.consumer.state-topic}")
    private String stateTopic;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT.equals(headerAccessor.getCommand())) {
            if (!jwtTokenUtil.validate(Objects.requireNonNull(headerAccessor.getFirstNativeHeader("jwt-token")))) {
                log.info("jwt 검증 실패");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            }
        }
        return message;
    }
//    @Override
//    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
//        switch (Objects.requireNonNull(accessor.getCommand())) {
//            case CONNECT:
//                String session_id = accessor.getSessionId();
//                String user_id = Objects.requireNonNull(accessor.getFirstNativeHeader("user-id"));
//
//                break;
//
//            case DISCONNECT:
//                String sessionId = accessor.getSessionId();
//                break;
//            default:
//                break;
//        }
//    }
}