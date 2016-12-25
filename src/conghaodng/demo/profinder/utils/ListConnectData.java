package conghaodng.demo.profinder.utils;

public class ListConnectData {
	private String username1, username2, field, subject, content, date;
    private int idConnect;
 
    public ListConnectData() {
    }
 
    public ListConnectData(int idConnect, String username1, String username2, String field, String subject, String content, String date) {
        this.field = field;
        this.subject = subject;
        this.username1 = username1;
        this.username2 = username2;
        this.content = content;
        this.date = date;
    }

	public String getUsername1() {
		return username1;
	}

	public void setUsername1(String username1) {
		this.username1 = username1;
	}

	public String getUsername2() {
		return username2;
	}

	public void setUsername2(String username2) {
		this.username2 = username2;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public int getIdConnect() {
		return idConnect;
	}

	public void setIdConnect(int idConnect) {
		this.idConnect = idConnect;
	}

    
}
