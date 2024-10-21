package org.example.data;

import org.springframework.web.socket.WebSocketSession;

public class WebsocketSession {
    private WebSocketSession session;
    private WebsocketUser user;
    public WebsocketSession(WebSocketSession session, WebsocketUser user) {
        this.session = session;
        this.user = user;
    }

    public WebsocketUser getUser() {
        return user;
    }
    public WebSocketSession getSession() {
        return session;
    }

    public void setSession(WebSocketSession session) {
        this.session = session;
    }
    public void setUser(WebsocketUser user) {
        this.user = user;
    }
}
