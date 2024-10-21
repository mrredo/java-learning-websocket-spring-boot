package org.example.web.ws.path.message;

import org.example.data.WebsocketMessage;
import org.example.data.WebsocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HandleMessage {
    public void HandleMessagePath(ConcurrentHashMap<String, WebsocketSession> sessions, WebSocketSession session, WebsocketMessage message) throws IOException {
        switch (message.getType()) {
            case Private:
                try {
                    handlePrivate(sessions, session, message);

                } catch (Exception e) {
                    e.printStackTrace();
                    session.sendMessage(new TextMessage("Failed to handle private message"));
                }
                break;
            case All:
                try {
                    handlePublic(sessions, message);

                } catch (Exception e) {
                    e.printStackTrace();
                    session.sendMessage(new TextMessage("Failed to handle public message"));
                }
        }
    }
    private void handlePrivate(ConcurrentHashMap<String, WebsocketSession> sessions, WebSocketSession session, WebsocketMessage message) throws IOException {
        if(session.getId().equals(sessions.get(message.getUser()).getSession().getId())) {
            session.sendMessage(new TextMessage(message.getMessage()));
            return;
        }
        session.sendMessage(new TextMessage(message.getMessage()));
        sessions.get(message.getUser()).getSession().sendMessage(new TextMessage(message.getMessage()));
    }
    private void handlePublic(ConcurrentHashMap<String, WebsocketSession> sessions, WebsocketMessage message) throws IOException {
        for (Map.Entry<String, WebsocketSession> entry : sessions.entrySet()) {
            String k = entry.getKey();
            WebsocketSession v = entry.getValue();
            v.getSession().sendMessage(new TextMessage(message.getMessage()));
        }
    }

}
