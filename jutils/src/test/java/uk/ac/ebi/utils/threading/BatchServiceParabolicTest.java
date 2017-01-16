package uk.ac.ebi.utils.threading;

import static java.lang.System.out;
import static junit.framework.Assert.assertTrue;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests the {@link BatchService} with the {@link PoolSizeTuner}, to verify that it reaches the optimal pool size. 
 * In order to do that runs foo threads, which sleep for a time that depends on the current thread size according
 * to a convex parabolic function.
 * 
 * More precisely, each thread runs for a time T defined as a * n ^ 2 + b * n + c + r(N), where:
 * <ul> 
 *   <li>n is the current thread pool size</li>
 *   <li>a, b, c are computed so that 1) T is minimum for the value nmin (an initial test
 *   parameter) and equal to tmin (another parameter) and 2) T(1) = t1 (yet another parameter)</li>
 *   <li>r(N) is a random number, distributed uniformly between -N and +N (a parameter)</li> 
 * </ul>
 * 
 * <dl><dt>date</dt><dd>9 Oct 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class BatchServiceParabolicTest
{
	private static final int nmin = 100;
	private static final int tmin = 20; // msec
	private static final int t1 = 1000; // msec
	
	private static final double a = 1.0 * ( tmin - t1 ) / ( nmin * nmin - 2 * nmin * nmin - 1 + 2 * nmin );
	private static final double b = -2.0 * a * nmin;
	private static final double c = 1.0 * t1 - a + 2.0 * a * nmin;
	private static final int N = 3;
		
	private static class TestTask extends BatchServiceTask
	{
		private int T;
		
		public TestTask ( String name, int n ) 
		{
			super ( name );
			T = (int) Math.round ( a * n * n + b * n + c + RandomUtils.nextInt ( -N, +N ) );
			if ( T < 0 ) T = 0;
			this.name += " (" + T + "ms )";
		}
		
		public void run ()
		{
			try
			{
				Thread.sleep ( T );
			} 
			catch ( InterruptedException ex ) {
				ex.printStackTrace();
			}
		}

		@Override
		public String getName ()
		{
			return super.getName ();
		}
	}

	@Test @Ignore ( "Too time-consuming, normally disabled" )
	public void testOptimization ()
	{
		out.printf ( "a = %f, b = %f, c = %f\n", a, b, c );
		BatchService<TestTask> service = new BatchService<TestTask> ( 20 );

		PoolSizeTuner tuner = service.getPoolSizeTuner ();
		tuner.setPeriodMSecs ( 10*1000 );
		tuner.setMaxThreads ( 200 );
		tuner.setMinThreads ( 1 );
		tuner.setMaxThreadIncr ( 25 );
		tuner.setMinThreadIncr ( 2 );

		long t0 = System.currentTimeMillis ();

		for ( int i = 1; System.currentTimeMillis () - t0 < 3 * 60 * 1000; i++ )
		{
			service.submit ( new  TestTask ( "TestTask:" + i, tuner.getThreadPoolSize () ) );
		}
		
		service.waitAllFinished ();
		
		int sz = tuner.getThreadPoolSize ();
		out.println ( "It got tuned to " + sz );
		
		float rsz = 1f * sz / nmin;
		assertTrue ( "Optimisation failed!", rsz > 0.9 && rsz < 1.1 );
	}

}
