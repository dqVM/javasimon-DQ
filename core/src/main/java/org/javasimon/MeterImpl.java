package org.javasimon;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


public class MeterImpl
  extends AbstractSimon
  implements Meter
{
  private long startTime;
  private long counter;
  private long incrementSum;
  private double peakRate;
  private AtomicLong lastTick;
  private static final long TICK_INTERVAL = TimeUnit.SECONDS.toNanos(5L); // tick every 5 seconds 
  private final EWMA m1Rate = EWMA.oneMinuteEWMA();
  private EvolvingEvent event;
  //private 
  
  private static enum EvolvingEvent
  {
    Increase,  Hold,  Decrease;
    private EvolvingEvent() {}
  }
  
  MeterImpl(String name, Manager manager)
  {
    super(name, manager);
    start();
  }
  
  public void start()
  {
    this.startTime = this.manager.nanoTime();
    this.lastTick = new AtomicLong(this.startTime);
    this.counter = 0L;
    this.peakRate = 0.0D;
    this.event = EvolvingEvent.Hold;
  }
  
  public Meter mark()
  {
    return mark(1L);
  }
  
  public Meter mark(long inc)
  {
    if (!this.enabled) {
      return this;
    }
    long now = this.manager.milliTime();
    MeterSample sample;
    synchronized (this)
    {
      increasePrivate(inc, now);
      updateIncrementalSimonsIncrease(inc, now);
      sample = sampleIfCallbacksNotEmpty();
    }
    switch (this.event)
    {
	    case Increase: 
	      this.manager.callback().onMeterIncrease(this, inc, sample);
	      break;
	    case Decrease: 
	      this.manager.callback().onMeterDecrease(this, inc, sample);
	      break;
    }

   
    
    return this;
  }
  
  private MeterSample sampleIfCallbacksNotEmpty()
  {
    if (!this.manager.callback().callbacks().isEmpty()) {
      return sample();
    }
    return null;
  }
  
  private void increasePrivate(long inc, long now)
  {
    updateUsages(now);
    setIncrementSum(getIncrementSum() + inc);
    
    this.counter += inc;
    tickIfNecessary();
    this.m1Rate.update(inc);
  }
  
  private void updateIncrementalSimonsIncrease(long inc, long now)
  {
    Collection<Simon> simons = incrementalSimons();
    if (simons != null) {
      for (Simon simon : simons) {
        ((MeterImpl)simon).increasePrivate(inc, now);
      }
    }
  }
  
  public long getCount()
  {
    return this.counter;
  }
  
  private void tickIfNecessary()
  {
    long oldTick = this.lastTick.get();
    long newTick = this.manager.nanoTime();
    long age = newTick - oldTick;
    boolean ticked = false;
    event=EvolvingEvent.Hold;
    if (age > TICK_INTERVAL)
    {
      long newIntervalStartTick = newTick - age % TICK_INTERVAL;
      if (this.lastTick.compareAndSet(oldTick, newIntervalStartTick))
      {
        long requiredTicks = age / TICK_INTERVAL;
        ticked = true;
        for (long i = 0L; i < requiredTicks; i += 1L) {
          this.m1Rate.tick();
        }
      }
    }
    if (ticked)
    {
    	//update the peakRate
    	this.peakRate=this.m1Rate.getPeakRate(TimeUnit.SECONDS);
    	double diff=-this.m1Rate.getInstantRate(TimeUnit.SECONDS)-this.m1Rate.getRate(TimeUnit.SECONDS);
    	if(diff>1E-2){
    		event=EvolvingEvent.Increase;
    	}
 
    	if(diff<-1*1E-2){
    		event=EvolvingEvent.Decrease;
    	}
    }
  }
  
  public double getMeanRate()
  {
    if (this.counter == 0L) {
      return 0.0D;
    }
    double elapsed = this.manager.nanoTime() - this.startTime;
    
    return getCount() / elapsed * TimeUnit.SECONDS.toNanos(1L);
  }
  
  public double getOneMinuteRate()
  {
   tickIfNecessary();
    return this.m1Rate.getRate(TimeUnit.SECONDS);
  }
  
  public synchronized MeterSample sample()
  {
    MeterSample sample = new MeterSample();
    
    sample.setCounter(this.counter);
    sample.setPeakRate(this.peakRate);
    sample.setMeanRate(getMeanRate());
    sampleCommon(sample);
    return sample;
  }
  
  public MeterSample sampleIncrement(Object key)
  {
    return null;
  }
  
  public MeterSample sampleIncrementNoReset(Object key)
  {
    return null;
  }
  
  public long getIncrementSum()
  {
    return this.incrementSum;
  }
  
  public void setIncrementSum(long incrementSum)
  {
    this.incrementSum = incrementSum;
  }
  
  public synchronized String toString()
  {
    return "Simon Meter: current Count=" + getCount() + ", meanRate=" + getMeanRate() + "events/second" + ", OneMinuteRate=" + getOneMinuteRate() + "events/second" + ",PeakRate=" + getPeakRate() + "events/second" 
    		+"InstantRate="+getInstantRate()+"events/second"+ super.toString();
  }
  
  public double getPeakRate()
  {
    return m1Rate.getPeakRate(TimeUnit.SECONDS);
  }
  
  public double getInstantRate(){
	  return m1Rate.getInstantRate(TimeUnit.SECONDS);
  }
}

