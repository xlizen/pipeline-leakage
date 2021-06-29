package com.boyinet.demo.pipelineleakage.service;

import com.boyinet.demo.pipelineleakage.common.R;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * @author lengchunyun
 */
@Service
public class WebSocketService {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public WebSocketService(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public void sendMsg(@Payload R r) {
        String destination = "/topic/";
        simpMessagingTemplate.convertAndSend(destination, r);
    }
}
