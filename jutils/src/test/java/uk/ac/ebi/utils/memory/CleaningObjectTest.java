package uk.ac.ebi.utils.memory;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>31 Mar 2020</dd></dl>
 *
 */
public class CleaningObjectTest
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	private class SelfDisposingObject extends CleaningObject
	{
		@Override
		public void close ()
		{
			log.info ( "close() invoked for {}!", this.getClass ().getSimpleName () );
			System.err.println ( "close() invoked for " + this.getClass ().getSimpleName () + "!" );
		}
	}
	
	@Test @Ignore ( "To be completed" )
	public void testBasics ()
	{
		@SuppressWarnings ( { "resource", "unused" } )
		SelfDisposingObject obj = new SelfDisposingObject ();
		// TODO: we need something to trigger the disposal, for the moment we just check 
		// the logging output.
	}
}
