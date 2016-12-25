package conghaodng.demo.profinder.utils;

public class ListChsgData {
	private String field, subject, tag;
    private int id, iCat, iLearn;
 
    public ListChsgData() {
    }
 
    public ListChsgData(String field, String subject, String tag, int aId, int aILearn, int aICat) {
        this.field = field;
        this.subject = subject;
        this.tag = tag;
        this.id = aId;
        this.iLearn = aILearn;
        this.iCat = aICat;
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

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public int getId() {
        return id;
    }
 
    public void setId(int aId) {
        this.id = aId;
    }
    
    public int getILearn() {
        return iLearn;
    }
 
    public void setILearn(int aILearn) {
        this.iLearn = aILearn;
    }
    
    public int getICat() {
        return iCat;
    }
 
    public void setICat(int aICat) {
        this.iCat = aICat;
    }
}
