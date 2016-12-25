package conghaodng.demo.profinder.utils;

public class ListSbjData {
	private String sbj_name, sbj_img;
    private int sbj_id, sbj_cID, sbj_fID;
 
    public ListSbjData() {
    }
 
    public ListSbjData(String sbj_name, String sbj_img, int sbj_id, int sbj_cID, int sbj_fID) {
        this.sbj_name = sbj_name;
        this.sbj_img = sbj_img;
        this.sbj_id = sbj_id;
        this.sbj_cID = sbj_cID;
        this.sbj_fID = sbj_fID;
    }

	public String getSbj_name() {
		return sbj_name;
	}

	public void setSbj_name(String sbj_name) {
		this.sbj_name = sbj_name;
	}

	public String getSbj_img() {
		return sbj_img;
	}

	public void setSbj_img(String sbj_img) {
		this.sbj_img = sbj_img;
	}

	public int getSbj_id() {
		return sbj_id;
	}

	public void setSbj_id(int sbj_id) {
		this.sbj_id = sbj_id;
	}

	public int getSbj_cID() {
		return sbj_cID;
	}

	public void setSbj_cID(int sbj_cID) {
		this.sbj_cID = sbj_cID;
	}

	public int getSbj_fID() {
		return sbj_fID;
	}

	public void setSbj_fID(int sbj_fID) {
		this.sbj_fID = sbj_fID;
	}

}
