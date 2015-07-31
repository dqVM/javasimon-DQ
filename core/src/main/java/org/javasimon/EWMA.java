package org.javasimon;

import java.util.concurrent.TimeUnit;
import java.util.function.DoubleToLongFunction;


public class EWMA
{
  private static final int INTERVAL = 5;
  private static final double SECONDS_PER_MINUTE = 60.0D;
  private static final int ONE_MINUTE = 1;
//  private static final int FIVE_MINUTES = 5;
//  private static final int FIFTEEN_MINUTES = 15;
  private static final double M1_ALPHA = 1.0D - Math.exp(-0.08333333333333333D);
  private static final double M5_ALPHA = 1.0D - Math.exp(-0.016666666666666666D);
  private static final double M15_ALPHA = 1.0D - Math.exp(-0.005555555555555555D);
  private volatile boolean initialized = false;
  private volatile double rate = 0.0D;
  private final LongAdder uncounted = new LongAdder();
  private final double alpha;
  private final double interval;
  private volatile double peakRate = 0.0D;
  private volatile double cRate=0.0D;
  
  public static EWMA oneMinuteEWMA()
  {
    return new EWMA(M1_ALPHA, 5L, TimeUnit.SECONDS);
  }
  
//  public static EWMA fiveMinuteEWMA()
//  {
//    return new EWMA(M5_ALPHA, 5L, TimeUnit.SECONDS);
//  }
//  
//  public static EWMA fifteenMinuteEWMA()
//  {
//    return new EWMA(M15_ALPHA, 5L, TimeUnit.SECONDS);
//  }
  
  public EWMA(double alpha, long interval, TimeUnit intervalUnit)
  {
    this.interval = intervalUnit.toNanos(interval);
    this.alpha = alpha;
  }
  
  public void update(long n)
  {
    this.uncounted.add(n);
  }
  
  public void tick()
  {
    long count = this.uncounted.sumThenReset();
    double instantRate = count / this.interval;
    // get instance rate
    this.cRate=instantRate;
    if(this.initialized){
    	this.rate+=this.alpha*(instantRate-this.rate);
    }else{
    	this.rate=instantRate;
    	this.initialized=true;
    	this.peakRate=instantRate;
    }
    
    
//    if (instantRate > this.peakRate) {
//      this.peakRate = instantRate;
//    }
//    if (this.initialized)
//    {
//      this.rate += this.alpha * (instantRate - this.rate);
//    }
//    else
//    {
//      this.rate = instantRate;
//      this.initialized = true;
//      this.peakRate = instantRate;
//    }
  }
  
  public double getRate(TimeUnit rateUnit)
  {
    return this.rate * rateUnit.toNanos(1L);
  }
  
  
  public double getInstantRate(TimeUnit rateUnit)
  {
    return this.cRate * rateUnit.toNanos(1L);
  }
  
  public double getPeakRate(TimeUnit rateUnit)
  {
    return this.peakRate * rateUnit.toNanos(1L);
  }
}

