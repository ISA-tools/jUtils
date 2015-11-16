package uk.ac.ebi.utils.runcontrol;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.utils.runcontrol.StatsExecutorTest.Tester;
import uk.ac.ebi.utils.time.XStopWatch;

/**
 * Tests DynamicRateExecutor.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>19 Oct 2015</dd></dl>
 *
 */
public class DynamicRateExecutorTest
{
	private long testTime = 3000;
	private double rate = 50;
	private XStopWatch timer = new XStopWatch ();

	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	private class TestExecutor extends DynamicRateExecutor
	{
		@Override
		protected double setNewRate () {
			return 1d * timer.getTime () / testTime <= 0.5d ? rate : rate / 2;
		}
	}
	
	@Test
	public void testBasics ()
	{
		Tester tester = new Tester ();
		tester.failRate = -1;
		
		TestExecutor executor = new TestExecutor ();
		for ( timer.start (); timer.getTime () < testTime; )
			executor.execute ( tester );
		
		double actualRate = 1d * tester.calls.get () / ( testTime / 1000d );
		log.info ( "Calls: {}, Actual Rate: {} call/sec", tester.calls.get (), actualRate );
		Assert.assertTrue ( "Rate was not limited correctly!", actualRate <= ( ( rate + rate / 2 ) / 2 ) * 1.05 );
	}
}
