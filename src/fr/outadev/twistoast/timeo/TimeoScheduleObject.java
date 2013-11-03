package fr.outadev.twistoast.timeo;

public class TimeoScheduleObject {

	public TimeoScheduleObject(TimeoIDNameObject line,
			TimeoIDNameObject direction, TimeoIDNameObject stop,
			String[] schedule) {
		this.line = line;
		this.direction = direction;
		this.stop = stop;
		this.schedule = schedule;
	}

	public TimeoIDNameObject getLine() {
		return line;
	}

	public void setLine(TimeoIDNameObject line) {
		this.line = line;
	}

	public TimeoIDNameObject getDirection() {
		return direction;
	}

	public void setDirection(TimeoIDNameObject direction) {
		this.direction = direction;
	}

	public TimeoIDNameObject getStop() {
		return stop;
	}

	public void setStop(TimeoIDNameObject stop) {
		this.stop = stop;
	}

	public String[] getSchedule() {
		return schedule;
	}

	public void setSchedule(String[] schedule) {
		this.schedule = schedule;
	}

	private TimeoIDNameObject line;
	private TimeoIDNameObject direction;
	private TimeoIDNameObject stop;

	private String[] schedule;

}
