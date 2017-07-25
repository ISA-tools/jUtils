package uk.ac.ebi.utils.runcontrol;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.utils.time.XStopWatch;

/**
 * Tests for {@link StatsExecutor}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>5 Oct 2015</dd></dl>
 *
 */
public class StatsExecutorTest
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	public static final double FAIL_RATE = 0.03;
	public static final long TASK_MAX_TIME = 10;
	
	
	public static class Tester implements Runnable 
	{
		public AtomicInteger calls = new AtomicInteger ( 0 );
		public double failRate = FAIL_RATE;
		private Logger log = LoggerFactory.getLogger ( this.getClass () );

		public void run ()
		{
			if ( RandomUtils.nextDouble ( 0, 1 ) < failRate ) throw new RuntimeException ( "Failed on Purpose" );
			long runningTime = RandomUtils.nextLong ( 0, TASK_MAX_TIME + 1 );
			XStopWatch timer = new XStopWatch ();
			for ( timer.start (); timer.getTime () < runningTime; );

			log.trace ( "Call #{}", calls.incrementAndGet () );
		}
	}

	@Test
	public void testBasics ()
	{
		long samplingTime = 500;
		StatsExecutor executor = new StatsExecutor ( "JUnit Test", samplingTime ).setPopUpExceptions ( false );

		Tester tester = new Tester ();

		XStopWatch timer = new XStopWatch ();
		final long testTime = 3000;
		
		for ( timer.start (); timer.getTime () <= testTime; )
			executor.execute ( tester );

		double failRate = 1d * executor.getLastFailedCalls () / executor.getLastTotalCalls ();
		
		// each call requires TASK_MAX_TIME/2, except those that fails, which needs almost no time
		// (so, we have the failRate factor).
		double expectedCalls = samplingTime / ( ( 1 - failRate ) * TASK_MAX_TIME / 2d );
		
		log.info ( "Calls: {}, fail rate: {} %", executor.getLastTotalCalls (), failRate * 100 );
		log.info ( "Expected Calls: {}", expectedCalls );
		
		Assert.assertTrue ( "Total calls wrong!", 
			Math.abs ( executor.getLastTotalCalls () / expectedCalls - 1 ) < 0.2 
		);
		Assert.assertTrue ( "Failed Calls wrong!", Math.abs ( failRate - FAIL_RATE ) < 0.1 );
	}
	
	@Test
	public void testMultiThreading () throws InterruptedException
	{
		long samplingTime = 500;
		final Tester tester = new Tester ();
		final long testTime = 2000;
		final StatsExecutor executor = new StatsExecutor ( "JUnit Multi-Thread Test", samplingTime ).setPopUpExceptions ( false );
		final int nthreads = 3;
		
		Thread[] threads = new Thread [ nthreads ];
		for ( int i = 0; i < nthreads; i++ )
		{
			threads [ i ] = new Thread ( new Runnable() 
			{
				@Override
				public void run ()
				{
					XStopWatch timer = new XStopWatch ();
					for ( timer.start (); timer.getTime () <= testTime; ) {
						executor.execute ( tester );
					}
				}
			});
			threads [ i ].start ();
		}
		
		for ( Thread thread: threads ) thread.join ();
				
		double failRate = 1d * executor.getLastFailedCalls () / executor.getLastTotalCalls ();

		// each call requires TASK_MAX_TIME/2 on average, except those that fails, which needs almost no time
		// (so, we have the 1-failRate factor).
		double expectedCalls = nthreads * samplingTime / ( ( 1 - failRate ) * TASK_MAX_TIME / 2d );
		
		
		log.info ( "Calls: {}, fail rate: {} %", executor.getLastTotalCalls (), failRate * 100 );
		log.info ( "Expected Calls: {}", expectedCalls );
		
		// Keep this delta high, CI environments are often busy and erratic
		Assert.assertTrue ( "Multi-thread Total calls wrong!", 
			Math.abs ( executor.getLastTotalCalls () / expectedCalls - 1 ) < 0.35 
		);
		Assert.assertTrue ( "Multi-thread failed calls wrong!", Math.abs ( failRate - FAIL_RATE ) < 0.1 );
	}	
}
