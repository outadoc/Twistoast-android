package fr.outadev.twistoast.timeo;

/**
 * Contains the data necessary to make a call to the API.
 * 
 * @author outadoc
 * 
 */
public class TimeoRequestObject {

	public TimeoRequestObject(String line, String direction, String stop) {
		this.line = line;
		this.direction = direction;
		this.stop = stop;
	}

	public TimeoRequestObject(String line, String direction) {
		this.line = line;
		this.direction = direction;
	}

	public TimeoRequestObject(String line) {
		this.line = line;
	}

	public TimeoRequestObject() {

	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getStop() {
		return stop;
	}

	public void setStop(String stop) {
		this.stop = stop;
	}

	private String line;
	private String direction;
	private String stop;

}
