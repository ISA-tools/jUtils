package uk.ac.ebi.utils.runcontrol;

import java.util.concurrent.Executor;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.utils.runcontrol.StatsExecutorTest.Tester;
import uk.ac.ebi.utils.time.XStopWatch;

/**
 * Tests {@link ChainExecutor}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>6 Oct 2015</dd></dl>
 *
 */
public class ChainedExecutorTest
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );

	@Test
	public void testBasics ()
	{
		Tester tester = new Tester ();
		XStopWatch timer = new XStopWatch ();
		double rate = 50;
		long samplingTime = 500;
		final long testTime = 5000;


		RateLimitedExecutor rateExecutor = new RateLimitedExecutor ( rate );
		StatsExecutor statsExecutor = 
			new StatsExecutor ( this.getClass ().getSimpleName (), samplingTime )
			.setPopUpExceptions ( false ); 
		
		// Calls are first logged by the statistics executor, then considered by the rate limiter.
		// In this case the action passed to the final executor is run after the rate-limiting step, and 
		// the statistics step is run after the action. This is because of the sequence implemented by these 
		// two executors internally. The two executors are run in a nested way.
		Executor executor = new ChainExecutor 
		( 
			rateExecutor, 
				statsExecutor 
		);

		for ( timer.start (); timer.getTime () < testTime; )
			executor.execute ( tester );
		
		double actualRate = 1d * tester.calls.get () / ( testTime / 1000 );
		log.info ( "Calls: {}, Actual Rate: {} call/sec", tester.calls.get (), actualRate );
		Assert.assertTrue ( "Rate was not limited!", actualRate <= rate * 1.30 );		
		
		double failRate = 1d * statsExecutor.getFailedCalls () / statsExecutor.getTotalCalls ();
		log.info ( "Calls: {}, fail rate: {} %", statsExecutor.getTotalCalls (), failRate * 100 );
		Assert.assertTrue ( "Total calls wrong!", statsExecutor.getTotalCalls () > 0 );
		Assert.assertTrue ( "Failed Calls wrong!", Math.abs ( failRate - StatsExecutorTest.FAIL_RATE ) < 0.1 );
	}
}
