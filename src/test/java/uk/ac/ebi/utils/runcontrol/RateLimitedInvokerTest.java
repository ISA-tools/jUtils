package uk.ac.ebi.utils.runcontrol;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.utils.runcontrol.StatsInvokerTest.Tester;
import uk.ac.ebi.utils.time.XStopWatch;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>5 Oct 2015</dd>
 *
 */
public class RateLimitedInvokerTest
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	@Test
	public void testBasics () 
	{
		Tester tester = new Tester ();
		tester.failRate = -1;
		XStopWatch timer = new XStopWatch ();
		long testTime = 3000;
		double rate = 50;
		RateLimitedInvoker invoker = new RateLimitedInvoker ( rate );
		
		for ( timer.start (); timer.getTime () < testTime; )
			invoker.run ( tester );
		
		double actualRate = 1d * tester.calls.get () / ( testTime / 1000 );
		log.info ( "Calls: {}, Actual Rate: {} call/sec", tester.calls.get (), actualRate );
		Assert.assertTrue ( "Rate was not limited!", actualRate <= rate * 1.05 );
	}
	
	@Test
	public void testMultiThreading () throws InterruptedException
	{
		final Tester tester = new Tester ();
		final long testTime = 10 * 1000;
		double rate = 10;
		final RateLimitedInvoker invoker = new RateLimitedInvoker ( rate );
		final int nthreads = 5;
		
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
		
		double actualRate = 1d * tester.calls.get () / ( testTime / 1000 );
		log.info ( "Calls: {}, Actual Rate: {} call/sec", tester.calls.get (), actualRate );

		// Rather imprecise when running for low time 
		Assert.assertTrue ( "Multi-thread rate was not limited!", actualRate <= rate * 1.30 );
	}
}
