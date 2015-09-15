package com.programmerdan.minecraft.contraptions.time.unit;

import com.programmerdan.minecraft.contraptions.time.TimeUnit;

public final class Year extends TimeUnit {
	private static final double[] toNanosecond = new double[]{315576000.0, 0.00001};
	private static final double[] toMillisecond = new double[]{3155760.0, 0.0001};
	private static final double[] toMicrosecond = new double[]{315576.0, 0.0001};
	private static final double[] toSecond = new double[]{315576.0, 0.01};
	private static final double[] toTick = new double[]{6311520.0, 0.01};
	private static final double[] toMinute = new double[]{525960.0, 1.0};
	private static final double[] toHour = new double[]{8766.0, 1.0};
	private static final double[] toDay = new double[]{365.25, 1.0};
	private static final double[] toYear = new double[]{1.0, 1.0};
	private static final double[] unknown = new double[]{0.0, 0.0};
	
	@Override
	public UnitType getUnit() {
		return TimeUnit.UnitType.Year;
	}
	
	@Override
	public double[] getRatio(UnitType convert) {
		switch(convert) {
		case Nanosecond: return toNanosecond;
		case Millisecond: return toMillisecond;
		case Microsecond: return toMicrosecond;
		case Second: return toSecond;
		case Tick: return toTick;
		case Minute: return toMinute;
		case Hour: return toHour;
		case Day: return toDay;
		case Year: return toYear;
		default: return unknown; // inconvertible.
		}
	}
	
	public static final Year instance = new Year();
	
	private Year(){}	

	@SuppressWarnings("unchecked")
	public static Year instance() {
		return instance;
	}

}
