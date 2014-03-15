package fr.outadev.android.timeo;

/**
 * Associates an ID to a name. Used to associate a line ID with its name, for
 * example.
 * 
 * @author outadoc
 * 
 */
public class TimeoIDNameObject {

	/**
	 * Creates an ID/name object.
	 * 
	 * @param id
	 *            the id of the object
	 * @param name
	 *            the name of the object
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

	@Override
	public TimeoIDNameObject clone() {
		return new TimeoIDNameObject(id, name);
	}

	private String id;
	private String name;

}
