package conghaodng.demo.profinder.utils;

public class ListFieldData {
	private String field_name, field_img;
    private int field_id;
 
    public ListFieldData() {
    }
 
    public ListFieldData(String field_name, String field_img, int field_id) {
        this.field_name = field_name;
        this.field_img = field_img;
        this.field_id = field_id;
    }

	public String getField_name() {
		return field_name;
	}

	public void setField_name(String field_name) {
		this.field_name = field_name;
	}

	public String getField_img() {
		return field_img;
	}

	public void setField_img(String field_img) {
		this.field_img = field_img;
	}

	public int getField_id() {
		return field_id;
	}

	public void setField_id(int field_id) {
		this.field_id = field_id;
	}

	
}
