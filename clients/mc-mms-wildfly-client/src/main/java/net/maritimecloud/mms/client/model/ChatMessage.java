package net.maritimecloud.mms.client.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.Date;

/**
 * A chat message
 */
@Entity
@NamedQueries({
    @NamedQuery(name  = "ChatMessage.selectAll", query = "select cm from ChatMessage cm")
})
public class ChatMessage {

    @Id
    @GeneratedValue
    private Long id;

    Date date;
    String severity;
    String msg;

    public ChatMessage() {
        this.date = new Date();
    }

    public ChatMessage(String severity, String msg) {
        this();
        this.severity = severity;
        this.msg = msg;
    }

    public Long getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
