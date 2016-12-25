package conghaodng.demo.profinder.chat.utils;

import java.io.Serializable;

public class MessageData  implements Serializable {
    String id, message, createdAt;
    Sender sender;
    int fileType;
 
    public MessageData() {
    }
 
    public MessageData(String id, String message, String createdAt, Sender sender, int fileType) {
        this.id = id;
        this.message = message;
        this.createdAt = createdAt;
        this.sender = sender;
        this.fileType = fileType;
    }
 
    public int getFileType() {
		return fileType;
	}

	public void setFileType(int fileType) {
		this.fileType = fileType;
	}

	public String getId() {
        return id;
    }
 
    public void setId(String id) {
        this.id = id;
    }
 
    public String getMessage() {
        return message;
    }
 
    public void setMessage(String message) {
        this.message = message;
    }
 
    public String getCreatedAt() {
        return createdAt;
    }
 
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
 
    public Sender getSender() {
        return sender;
    }
 
    public void setSender(Sender s) {
        this.sender = s;
    }
}