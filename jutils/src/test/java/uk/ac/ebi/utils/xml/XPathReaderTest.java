package uk.ac.ebi.utils.xml;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>29 Mar 2018</dd></dl>
 *
 */
public class XPathReaderTest
{
	@Test
	public void testBasics ()
	{
		String xml =
			"<Person id = '123'>\n" + 
			"  <name>John</name>\n" + 
			"  <surname>Smith</surname>\n" + 
			"  <age>28</age>\n" + 
			"</Person>";
		
		XPathReader xr = new XPathReader ( xml );
		
		Assert.assertEquals ( "Wrong name fetched!", "John", xr.readString ( "/Person/name" ) );
		Assert.assertEquals ( "Wrong age fetched!", (Integer) 28, (Integer) xr.readInt ( "/Person/age" ) );
		Assert.assertEquals ( "Wrong id fetched!", (Integer) 123, (Integer) xr.readInt ( "/Person/@id" ) );
	}
}
