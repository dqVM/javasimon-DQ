package org.javasimon;

public class MeterSample
extends Sample
{
private double oneMinuteRate;
private double meanRate;
private double counter;
private double peakRate;

public String toString()
{
  StringBuilder sb = new StringBuilder();
  sb.append("CounterSample{");
  if (getName() != null) {
    sb.append("name=").append(getName()).append(", ");
  }
  sb.append("count=").append(this.counter);
  sb.append("meanRate=").append(this.meanRate);
  sb.append("oneMinuteRate=").append(this.oneMinuteRate);
  sb.append("peakRate=").append(this.peakRate);
  
  toStringCommon(sb);
  return sb.toString();
}

public String simonToString()
{
  return "Simon Meter: current Count=" + this.counter + ", meanRate=" + this.meanRate + ",PeakRate=" + this.peakRate + simonToStringCommon();
}

public double getOneMinuteRate()
{
  return this.oneMinuteRate;
}

public void setOneMinuteRate(double oneMinuteRate)
{
  this.oneMinuteRate = oneMinuteRate;
}

public double getMeanRate()
{
  return this.meanRate;
}

public void setMeanRate(double meanRate)
{
  this.meanRate = meanRate;
}

public double getCounter()
{
  return this.counter;
}

public void setCounter(double counter)
{
  this.counter = counter;
}

public void setPeakRate(double peakRate)
{
  this.peakRate = peakRate;
}

public double getPeakRate()
{
  return this.peakRate;
}
}

