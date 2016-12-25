package conghaodng.demo.profinder.utils;

public class ListMeetData {
	private String person_avatar, person_FBID, person_email, person_name, content, tag, doc, img, location, date, tel, time, person_rating;
    private int person_id, iField, iSubject, iSex, id_chsg, from_chsg;
 
    public ListMeetData() {
    }
 
    public ListMeetData(String person_avatar, String person_FBID, String person_email, String person_name, String content, String tag, 
    		String doc, String img, String location, String date, String tel, String time, 
    		int person_id, String person_rating, int iField, int iSubject, int iSex, int id_chsg, int aFrom_chsg) {
    	this.person_avatar = person_avatar;
        this.person_FBID = person_FBID;
        this.person_email = person_email;
        this.person_name = person_name;
        this.content = content;
        this.tag = tag;
        this.doc = doc;
        this.img = img;
        this.location = location;
        this.date = date;
        this.tel = tel;
        this.time = time;
        this.person_id = person_id;
        this.person_rating = person_rating;
        this.iField = iField;
        this.iSubject = iSubject;
        this.iSex = iSex;
        this.id_chsg = id_chsg;
        this.from_chsg = aFrom_chsg;
    }

	public int getFrom_chsg() {
		return from_chsg;
	}

	public void setFrom_chsg(int from_chsg) {
		this.from_chsg = from_chsg;
	}

	public String getPerson_avatar() {
		return person_avatar;
	}

	public void setPerson_avatar(String person_avatar) {
		this.person_avatar = person_avatar;
	}

	public String getPerson_FBID() {
		return person_FBID;
	}

	public void setPerson_FBID(String person_FBID) {
		this.person_FBID = person_FBID;
	}

	public String getPerson_email() {
		return person_email;
	}

	public void setPerson_email(String person_email) {
		this.person_email = person_email;
	}

	public String getPerson_name() {
		return person_name;
	}

	public void setPerson_name(String person_name) {
		this.person_name = person_name;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getDoc() {
		return doc;
	}

	public void setDoc(String doc) {
		this.doc = doc;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public int getPerson_id() {
		return person_id;
	}

	public void setPerson_id(int person_id) {
		this.person_id = person_id;
	}

	public String getPerson_rating() {
		return person_rating;
	}

	public void setPerson_rating(String person_rating) {
		this.person_rating = person_rating;
	}

	public int getiField() {
		return iField;
	}

	public void setiField(int iField) {
		this.iField = iField;
	}

	public int getiSubject() {
		return iSubject;
	}

	public void setiSubject(int iSubject) {
		this.iSubject = iSubject;
	}

	public int getiSex() {
		return iSex;
	}

	public void setiSex(int iSex) {
		this.iSex = iSex;
	}

	public int getId_chsg() {
		return id_chsg;
	}

	public void setId_chsg(int id_chsg) {
		this.id_chsg = id_chsg;
	}

}
