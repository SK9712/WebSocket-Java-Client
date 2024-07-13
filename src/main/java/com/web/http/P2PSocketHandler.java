package com.web.http;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class P2PSocketHandler extends TextWebSocketHandler {

    List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
            throws IOException {
        if(message.getPayload().equalsIgnoreCase("pong"))
            System.out.println("Connection Established from client: " + session.getId());
        else {
            for (WebSocketSession webSocketSession : sessions) {
                System.out.println("Sending message from client: " + session.getId() + " to all connected clients");
                System.out.println("Message: " + message.getPayload());
                if (webSocketSession.isOpen() && !session.getId().equals(webSocketSession.getId())) {
                    webSocketSession.sendMessage(message);
                }
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("LocalAddress: " + session.getLocalAddress());
        System.out.println("RemoteAddress: " + session.getRemoteAddress());
        session.sendMessage(new TextMessage("Ping"));
        sessions.add(session);
    }
}