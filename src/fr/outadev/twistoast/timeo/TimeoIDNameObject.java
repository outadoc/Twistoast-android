package fr.outadev.twistoast.timeo;

public class TimeoIDNameObject {

	/**
	 * Create an ID/name object. Used to associate a line ID with its name, for example.
	 * 
	 * @param id the id of the object
	 * @param name the name of the object
	 * @see TimeoRequestObject
	 * @see TimeoScheduleObject
	 */
	public TimeoIDNameObject(String id, String name) {
		this.id = id;
		this.name = name;
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
	
	@Override
	public String toString() {
		return name;
	}

	private String id;
	private String name;

}
