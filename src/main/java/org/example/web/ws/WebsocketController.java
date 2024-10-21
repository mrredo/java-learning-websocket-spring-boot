package org.example.web.ws;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.data.WebsocketMessage;
import org.example.data.WebsocketPayload;
import org.example.data.WebsocketSession;
import org.example.data.WebsocketUser;
import org.example.data.util.WebsocketUserUtil;
import org.example.web.ws.path.message.HandleMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class WebsocketController extends TextWebSocketHandler {
    private final ConcurrentHashMap<String, WebsocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        URI uri = session.getUri();
        if (uri == null || uri.getQuery() == null) {
            closeSessionWithMessage(session, "Name query is not present");
            return;
        }

        // Extract query parameters
        Map<String, String> queryParams = Arrays.stream(uri.getQuery().split("&"))
                .map(param -> param.split("="))
                .collect(Collectors.toMap(
                        param -> param[0],
                        param -> param.length > 1 ? param[1] : ""
                ));

        // Extract the "name" parameter
        String name = queryParams.get("name");
        if (name == null || name.isEmpty()) {
            closeSessionWithMessage(session, "Name query is not present");
            return;
        }

        // Validate username
        if (!WebsocketUserUtil.ValidUsername(name)) {
            closeSessionWithMessage(session, "Username invalid");
            return;
        }

        synchronized (this) {
            // Check if a session already exists for the user and close the old session
            if (sessions.containsKey(name)) {
                session.sendMessage(new TextMessage(String.format("Account with this name %s already exists", name)));
                session.close(CloseStatus.BAD_DATA);
                return;
//                WebsocketSession oldSession = sessions.get(name);
//                if (oldSession.getSession().isOpen()) {
//                    // Notify and close the old session
//                    oldSession.getSession().sendMessage(new TextMessage("A new connection has been established. Closing the old connection."));
//                    oldSession.getSession().close(CloseStatus.NORMAL);
//                }
//                sessions.remove(name);
            }

            // Add the new session

            sessions.put(name, new WebsocketSession(session, new WebsocketUser(name)));
            sendNamesListToAllSessions();

            System.out.println("Connected: " + session.getId() + "\nName: " + name);
        }

    }
    public void sendNamesListToAllSessions() throws Exception {
        // Step 1: Collect the list of names from all active sessions
        List<String> names = sessions.entrySet().stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Step 2: Create a WebsocketPayload object and set the names as the payload
        WebsocketPayload<List<String>> payload = new WebsocketPayload<>();
        payload.setPayload(names);  // Set the names list
        payload.setPath("/namesList");  // Set a relevant path

        // Step 3: Convert the WebsocketPayload object to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = objectMapper.writeValueAsString(payload);

        // Step 4: Create a TextMessage to be sent
        TextMessage textMessage = new TextMessage(jsonPayload);

        // Step 5: Send the message to all active WebSocket sessions
        for (WebsocketSession wsSession : sessions.values()) {
            WebSocketSession session = wsSession.getSession();  // Get the actual WebSocketSession
            if (session.isOpen()) {  // Only send if the session is open
                session.sendMessage(textMessage);
            }
        }
    }
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Remove the session when it is closed
        sessions.values().removeIf(s -> s.getSession().equals(session));
        System.out.println("Disconnected: " + session.getId());
    }



    // Helper method to close session and send a message
    private void closeSessionWithMessage(WebSocketSession session, String message) throws Exception {
        session.sendMessage(new TextMessage(message));
        session.close(CloseStatus.BAD_DATA);
    }
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        WebsocketPayload payload = null;


        try {
            // Attempt to parse the message as JSON payload
            payload = objectMapper.readValue(message.getPayload(), WebsocketPayload.class);
        } catch (Exception e) {
            // If it fails to parse, treat it as a simple text message and broadcast it
            System.out.println("Message is not a valid JSON payload, echoing as text message.");
            broadcastMessage(session, "Echo: " + message.getPayload());
            return;  // Exit after handling the raw text message
        }

        // Handle based on the parsed payload
        switch (payload.getPath()) {
            case "/message":
                WebsocketPayload<WebsocketMessage> msg = null;
                try {
                    // Parse the message payload as a WebsocketMessage type
                    msg = objectMapper.readValue(message.getPayload(), new TypeReference<WebsocketPayload<WebsocketMessage>>() {});
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                assert msg != null;
                // Handle the message according to your custom logic
                new HandleMessage().HandleMessagePath(sessions, session, msg.getPayload());
                break;
                case "/user":
                    break;
            default:
                System.out.println("Unknown path: " + payload.getPath());
                break;
        }
    }

    private void broadcastMessage(WebSocketSession session, String message) throws Exception {
        // Send the message to all connected users
        for (WebsocketSession wsSession : sessions.values()) {
            if (wsSession.getSession().isOpen()) {
                wsSession.getSession().sendMessage(new TextMessage(message));
            }
        }
    }


}



