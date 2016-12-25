package conghaodng.demo.profinder.chat.utils;

import java.io.Serializable;

public class ChatRoomData implements Serializable {
    String id, name, lastMessage, timestamp;
    int unreadCount;
 
    public ChatRoomData() {
    }
 
    public ChatRoomData(String id, String name, String lastMessage, String timestamp, int unreadCount) {
        this.id = id;
        this.name = name;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.unreadCount = unreadCount;
    }
 
    public String getId() {
        return id;
    }
 
    public void setId(String id) {
        this.id = id;
    }
 
    public String getName() {
        return name;
    }
 
    public void setName(String name) {
        this.name = name;
    }
 
    public String getLastMessage() {
        return lastMessage;
    }
 
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
 
    public int getUnreadCount() {
        return unreadCount;
    }
 
    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
 
    public String getTimestamp() {
        return timestamp;
    }
 
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}