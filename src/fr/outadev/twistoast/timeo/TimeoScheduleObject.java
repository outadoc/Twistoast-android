package fr.outadev.twistoast.timeo;

import java.util.Arrays;

/**
 * Used to store a schedule, with its corresponding line, direction, and stop
 * objects.
 * 
 * @author outadoc
 * 
 */
public class TimeoScheduleObject {

	public TimeoScheduleObject(TimeoIDNameObject line, TimeoIDNameObject direction, TimeoIDNameObject stop, String[] schedule) {
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

	@Override
	public String toString() {
		return "TimeoScheduleObject [line=" + line + ", direction=" + direction + ", stop=" + stop + ", schedule="
		        + Arrays.toString(schedule) + "]";
	}

	@Override
	public TimeoScheduleObject clone() {
		return new TimeoScheduleObject((line != null) ? line.clone() : null, (direction != null) ? direction.clone() : null,
		        (stop != null) ? stop.clone() : null, (schedule != null) ? schedule.clone() : null);
	}

	private TimeoIDNameObject line;
	private TimeoIDNameObject direction;
	private TimeoIDNameObject stop;

	private String[] schedule;

}
