package org.javasimon;

public abstract interface Meter
extends Simon
{
public abstract Meter mark();

public abstract Meter mark(long paramLong);

public abstract void start();

public abstract long getCount();

public abstract double getMeanRate();
public abstract double getInstantRate();

public abstract double getOneMinuteRate();

public abstract double getPeakRate();

public abstract MeterSample sample();

public abstract MeterSample sampleIncrement(Object paramObject);

public abstract MeterSample sampleIncrementNoReset(Object paramObject);
}
