package uk.ac.ebi.utils.runcontrol;

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
 * <dl><dt>Date:</dt><dd>6 Oct 2015</dd>
 *
 */
public class ChainedWrapperInvokerTest
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


		RateLimitedInvoker rateInvoker = new RateLimitedInvoker ( rate );
		StatsInvoker statsInvoker = new StatsInvoker ( "ChainedInvokerTest", samplingTime )
			.setPopUpExceptions ( false ); 
		
		ChainedWrappedInvoker<Boolean> invoker = new ChainedWrappedInvoker<Boolean> ( 
			rateInvoker, 
				statsInvoker 
		);

		for ( timer.start (); timer.getTime () < testTime; )
			invoker.run ( tester );
		
		double actualRate = 1d * tester.calls.get () / ( testTime / 1000 );
		log.info ( "Calls: {}, Actual Rate: {} call/sec", tester.calls.get (), actualRate );
		Assert.assertTrue ( "Rate was not limited!", actualRate <= rate * 1.30 );		
		
		double failRate = 1d * statsInvoker.getFailedCalls () / statsInvoker.getTotalCalls ();
		log.info ( "Calls: {}, fail rate: {} %", statsInvoker.getTotalCalls (), failRate * 100 );
		Assert.assertTrue ( "Total calls wrong!", statsInvoker.getTotalCalls () > 0 );
		Assert.assertTrue ( "Failed Calls wrong!", Math.abs ( failRate - StatsInvokerTest.FAIL_RATE ) < 0.1 );
	}
}
