package uk.ac.ebi.utils.runcontrol;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.utils.time.XStopWatch;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>5 Oct 2015</dd>
 *
 */
public class StatsInvokerTest
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
		StatsInvoker invoker = new StatsInvoker ( "JUnit Test", samplingTime );
		Tester tester = new Tester ();

		XStopWatch timer = new XStopWatch ();
		final long testTime = 2000 - samplingTime / 10; // Avoids to finish just after the reset of internal stats
		
		for ( timer.start (); timer.getTime () < testTime; )
			invoker.run ( tester );

		double failRate = 1d * invoker.getFailedCalls () / invoker.getTotalCalls ();
		log.info ( "Calls: {}, fail rate: {} %", invoker.getTotalCalls (), failRate * 100 );
		Assert.assertTrue ( "Total calls wrong!", invoker.getTotalCalls () > 0 );
		Assert.assertTrue ( "Failed Calls wrong!", Math.abs ( failRate - FAIL_RATE ) < 0.1 );
	}
	
	@Test
	public void testMultiThreading () throws InterruptedException
	{
		long samplingTime = 500;
		final Tester tester = new Tester ();
		final long testTime = 2000 - samplingTime / 10; // Avoids to finish just after the reset of internal stats
		final StatsInvoker invoker = new StatsInvoker ( "JUnit Multi-Thread Test", samplingTime );
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
					for ( timer.start (); timer.getTime () < testTime; ) {
						invoker.run ( tester );
					}
				}
			});
			threads [ i ].start ();
		}
		
		for ( Thread thread: threads ) thread.join ();
				
		double failRate = 1d * invoker.getFailedCalls () / invoker.getTotalCalls ();
		log.info ( "Calls: {}, fail rate: {} %", invoker.getTotalCalls (), failRate * 100 );
		Assert.assertTrue ( "Multi-thread total calls wrong!", invoker.getTotalCalls () > 0  );
		Assert.assertTrue ( "Multi-thread failed calls wrong!", Math.abs ( failRate - FAIL_RATE ) < 0.1 );
	}	
}
