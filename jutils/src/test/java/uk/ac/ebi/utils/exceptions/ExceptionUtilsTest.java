package uk.ac.ebi.utils.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>28 Aug 2018</dd></dl>
 *
 */
public class ExceptionUtilsTest
{
	@Test
	public void testBuildEx ()
	{
		RuntimeException ex = ExceptionUtils.buildEx ( 
			RuntimeException.class, "A test exception with param having value: %.1f param", 10.5 
		);
		
		Assert.assertTrue ( "Unexpected message!", ex.getMessage ().contains ( "10.5 param" ) );
	}

	@Test
	public void testBuildExCause ()
	{
		RuntimeException ex = ExceptionUtils.buildEx ( 
			RuntimeException.class, new IOException (), 
			"A test exception with param having value: %.1f param", 
			10.5 
		);
		
		Assert.assertTrue ( "Unexpected message!", ex.getMessage ().contains ( "10.5 param" ) );
		Assert.assertTrue ( "Unexpected cause!", ex.getCause () instanceof IOException );
	}

	@Test ( expected = Exception.class )
	public void testThrowEx () throws Exception
	{
		ExceptionUtils.throwEx ( Exception.class, "Test Exception" );
	}

	@Test ( expected = RuntimeException.class )
	public void testThrowExUnchecked ()
	{
		ExceptionUtils.throwEx ( 
			RuntimeException.class, new IOException (), "Test Exception with %s param", "foo" 
		);
	}
	
	@Test
	public void testExceptionWithoutCause ()
	{
		NumberFormatException ex = new NumberFormatException ( "Exception that doesn't accept a cause" );
		String msgTpl = "Parent Exception. Child message is: %s";
		NumberFormatException parentEx = ExceptionUtils.buildEx (
			ex, msgTpl, ex.getMessage ()
		);
		
		assertNull ( "Cause isn't null!", parentEx.getCause () );
		assertEquals ( "Wrong parent's messsage!", String.format ( msgTpl, ex.getMessage () ), parentEx.getMessage () );
	}
}
