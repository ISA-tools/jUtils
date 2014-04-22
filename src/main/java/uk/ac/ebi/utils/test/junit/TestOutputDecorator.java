package uk.ac.ebi.utils.test.junit;

import static java.lang.System.out;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;
import org.junit.runners.model.Statement;

/**
 *
 * An output decorator for Junit. Prints a nice header with the test name before the test and a similar trailer 
 * afterward. This can be used in two ways. One is as a {@link TestRule}, pretty much the same way you would use 
 * {@link ExternalResource}. Another option is to add this listener to your runner of choice in order to be invoked 
 * automatically for all the tests run via that runner. For instance, see
 * <a href = 'http://maven.apache.org/plugins/maven-surefire-plugin/examples/junit.html#Using_custom_listeners_and_reporters'>here</a>
 * for an example about Maven/Surefire. Jutils's POM is itself another example of that.
 * 
 * <dl><dt>date</dt><dd>Jan 27, 2012</dd></dl>
 * @author brandizi
 *
 */
public class TestOutputDecorator extends RunListener implements TestRule 
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

	/**
	 * You can use this either as a listener, or a {@link Rule}.
	 */
	public Statement apply ( final Statement base, final Description description )
	{
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				testStarted ( description );
				try {
					base.evaluate();
				} finally {
					testFinished ( description );
				}
			}
		};	
	}

}
