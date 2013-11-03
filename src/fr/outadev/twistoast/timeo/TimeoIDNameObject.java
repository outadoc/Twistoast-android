package fr.outadev.twistoast.timeo;

public class TimeoIDNameObject {

	public TimeoIDNameObject(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String toString() {
		return name;
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

	private String id;
	private String name;

}
