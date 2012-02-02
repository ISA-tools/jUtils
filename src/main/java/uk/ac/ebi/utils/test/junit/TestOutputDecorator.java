package uk.ac.ebi.utils.test.junit;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;
import static java.lang.System.out;

/**
 *
 * An output decorator for Junit. Prints a nice header with the test name before the test and a similar trailer 
 * afterward. This can be added to your runner of choice in order to be invoked automatically. For instance, see
 * <a href = 'http://maven.apache.org/plugins/maven-surefire-plugin/examples/junit.html#Using_custom_listeners_and_reporters'>here</a>
 * for an example about Maven/Surefire. And of course another example is in the Jutils POM.
 * 
 * <dl><dt>date</dt><dd>Jan 27, 2012</dd></dl>
 * @author brandizi
 *
 */
public class TestOutputDecorator extends RunListener
{
	@Override
	public void testStarted ( Description description ) throws Exception
	{
		String label = MessageFormat.format ( " {0} ", description.getDisplayName () );
		out.println ( "\n\n     " + StringUtils.center ( label, 110, "=-" ) );
	}

	@Override
	public void testFinished ( Description description ) throws Exception
	{
		String label = MessageFormat.format ( " /end: {0} ", description.getDisplayName () );
		out.println ( "    " + StringUtils.center ( label, 110, "=-" ) + "\n" );
	}
	
}
