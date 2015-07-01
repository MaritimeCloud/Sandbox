package net.maritimecloud.mms.client;

import dma.messaging.MaritimeText;
import dma.messaging.MaritimeTextingNotificationSeverity;
import org.vertx.java.core.json.JsonObject;

import java.io.IOException;

/**
 * A chat message
 */
public class ChatMessage {

    public enum Severity {
        MESSAGE,
        SAFETY,
        WARNING,
        ALERT;
    }

    private String senderId;
    private String receiverId;
    private Severity severity;
    private String msg;

    /**
     * Constructor
     */
    public ChatMessage() {
    }

    /** Converts an MMS chat message to a ChatMessage */
    public static ChatMessage fromMms(String senderId, String receiverId, MaritimeText message) {
        ChatMessage msg = new ChatMessage();
        msg.setMsg(message.getMsg());
        msg.setSeverity(Severity.valueOf(message.getSeverity().getName()));
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        return msg;
    }

    /** Converts this ChatMessage to an MMS chat message */
    public MaritimeText toMms() {
        MaritimeText message = new MaritimeText();
        message.setMsg(getMsg());
        try {
            message.setSeverity(MaritimeTextingNotificationSeverity.SERIALIZER.from(getSeverity().name()));
        } catch (IOException e) {
            // Should never happen!
            e.printStackTrace();
        }
        return message;
    }

    /** Converts a JSON object to a ChatMessage */
    public static ChatMessage fromJson(JsonObject json) {
        ChatMessage msg = new ChatMessage();
        msg.setSenderId(String.valueOf(json.getNumber("senderId").intValue()));
        msg.setReceiverId(String.valueOf(json.getNumber("receiverId").intValue()));
        msg.setSeverity(Severity.valueOf(json.getObject("parameters").getString("severity")));
        msg.setMsg(json.getObject("parameters").getString("msg"));
        return msg;
    }

    /** Converts this ChatMessage to JSON */
    public String toJson() {
        JsonObject json = new JsonObject();
        json.putNumber("receiverId", Integer.parseInt(receiverId));
        json.putNumber("senderId", Integer.parseInt(senderId));
        json.putString("endpointType", "dma.messaging.MaritimeTextingService.sendMessage");

        JsonObject params = new JsonObject();
        params.putString("severity", severity.name());
        params.putString("msg", msg);
        json.putObject("parameters", params);
        return json.toString();
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "senderId='" + senderId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", severity=" + severity +
                ", msg='" + msg + '\'' +
                '}';
    }

    // *** Getters and Setters

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
