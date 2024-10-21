package org.example.data;

public class WebsocketMessage {
    private String message;
    private String user;
    private WebsocketMessageType type;

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {

        this.message = message;
    }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public WebsocketMessageType getType() {
        return type;
    }
    public void setType(WebsocketMessageType type) {
        this.type = type;
    }
}
