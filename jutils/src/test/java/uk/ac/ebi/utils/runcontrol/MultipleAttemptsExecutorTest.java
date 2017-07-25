package uk.ac.ebi.utils.runcontrol;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for {@link MultipleAttemptsExecutor}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>9 Oct 2015</dd></dl>
 *
 */
public class MultipleAttemptsExecutorTest
{
	Logger log = LoggerFactory.getLogger ( this.getClass () );

	@Rule
 	public ExpectedException thrown = ExpectedException.none();
	
	int runCt = 0;
	
	/**
	 * Terminates at first attempt, no failure.
	 */
	@Test
	public void testWithoutEx ()
	{
		MultipleAttemptsExecutor executor = new MultipleAttemptsExecutor ( 
			3, 0, 1000, RuntimeException.class 
		);
		
		executor.execute ( new Runnable() 
		{
			@Override
			public void run () {
				log.info ( "Hello, World" );
				runCt++;
			}
		});
		
		assertEquals ( "Run count is wrong!", 1, runCt );
	}
	
	/**
	 * Fails twice, then succeeds. 
	 */
	@Test
	public void testThirdAttempt ()
	{
		MultipleAttemptsExecutor executor = new MultipleAttemptsExecutor ( 
			3, 500, 4000, RuntimeException.class 
		);

		executor.execute ( new Runnable() 
		{
			@Override
			public void run () 
			{
				if ( ++runCt <= 2 ) throw new IllegalStateException ( "On-purpose exception #" + runCt );
				log.info ( "Hello, World" );
			}
		});

		assertEquals ( "Run count is wrong!", 3, runCt );
	}

	/**
	 * Fails twice, never succeeds, terminates with an exception.
	 */
	@Test
	public void testFail ()
	{
		MultipleAttemptsExecutor executor = new MultipleAttemptsExecutor ( 
			2, 0, 1000, RuntimeException.class 
		);

		thrown.expect ( IllegalStateException.class );
		executor.execute ( new Runnable() 
		{
			@Override
			public void run () {
				throw new IllegalStateException ( "On-purpose exception #" + (++runCt) );
			}
		});
	}

}
