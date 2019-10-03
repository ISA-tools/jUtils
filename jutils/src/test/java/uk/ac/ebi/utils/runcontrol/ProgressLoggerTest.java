package uk.ac.ebi.utils.runcontrol;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.Assert;

/**
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 May 2019</dd></dl>
 *
 */
public class ProgressLoggerTest
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	@Test
	public void testProgressLogger ()
	{
		// Stdout redirection
		PrintStream outBkp = System.out;
		ByteArrayOutputStream outBuf = new ByteArrayOutputStream ();
		System.setOut ( new PrintStream ( outBuf ) );

		ProgressLogger progTracker = new ProgressLogger ( 100 );
		progTracker.update ( 100 );
		progTracker.updateWithIncrement (); // 101
		progTracker.updateWithIncrement ( 100 ); // 201
		progTracker.updateWithIncrement ( 50 ); // 251
		
		for ( int i = 1; i <= 49; i++ ) progTracker.updateWithIncrement (); // 300 

		System.setOut ( outBkp );  // restore the original output
		
		String outStr = outBuf.toString ();
		
		log.info ( "Output from the progress logger:\n\n-------------------\n{}-------------------\n", outStr );
		
		Assert.assertTrue ( "100 not reported!", outStr.contains ( "100 items processed" ) );
		Assert.assertFalse ( "101 shouldn't be reported!", outStr.contains ( "101 items processed" ) );
		Assert.assertTrue ( "201 not reported!", outStr.contains ( "201 items processed" ) );
		Assert.assertFalse ( "101 shouldn't be reported!", outStr.contains ( "251 items processed" ) );
		Assert.assertFalse ( "270 shouldn't be reported!", outStr.contains ( "270 items processed" ) );
		Assert.assertTrue ( "300 not reported!", outStr.contains ( "300 items processed" ) );
	}


	@Test
	public void testPercentProgressLogger ()
	{
		// Stdout redirection
		PrintStream outBkp = System.out;
		ByteArrayOutputStream outBuf = new ByteArrayOutputStream ();
		System.setOut ( new PrintStream ( outBuf ) );

		PercentProgressLogger progTracker = new PercentProgressLogger ( "{}% of items processed", 1000 );
		progTracker.update ( 100 );
		progTracker.updateWithIncrement ( 50 ); // 150
		progTracker.updateWithIncrement ( 300 ); // 450
		progTracker.updateWithIncrement ( 5 ); // 455
		progTracker.updateWithIncrement ( 545 ); // 1000

		System.setOut ( outBkp );  // restore the original output
		
		String outStr = outBuf.toString ();
		
		log.info ( "Output from the progress logger:\n\n-------------------\n{}-------------------\n", outStr );
		
		Assert.assertTrue ( "10% not reported!", outStr.contains ( "10% of items processed" ) );
		Assert.assertFalse ( "15% shouldn't be reported!", outStr.contains ( "15% of items processed" ) );
		Assert.assertTrue ( "45% not reported!", outStr.contains ( "45% of items processed" ) );
		Assert.assertTrue ( "100% not reported!", outStr.contains ( "100% of items processed" ) );
	}
	
	@Test
	public void testCustomReportAction ()
	{
		// Stdout redirection
		PrintStream outBkp = System.out;
		ByteArrayOutputStream outBuf = new ByteArrayOutputStream ();
		System.setOut ( new PrintStream ( outBuf ) );
		
		PercentProgressLogger progTracker = new PercentProgressLogger ( "{}% of items processed", 1000 );
		progTracker.appendProgressReportAction ( 
			(oldp, newp) -> log.info ( "custom progress report action: {}%", newp )
		);
		progTracker.update ( 100 );

		System.setOut ( outBkp );  // restore the original output
		
		String outStr = outBuf.toString ();
		
		log.info ( "Output from the progress logger:\n\n-------------------\n{}-------------------\n", outStr );
		
		Assert.assertTrue ( "10% not reported!", outStr.contains ( "10% of items processed" ) );
		Assert.assertTrue ( "custom 10% not reported!", outStr.contains ( "custom progress report action: 10%" ) );
	}
	
}
