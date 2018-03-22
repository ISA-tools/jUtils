package uk.ac.ebi.utils.xml;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 Mar 2018</dd></dl>
 *
 */
public class XmlFilterUtilsTest
{
	Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	@Test
	public void testCdataWrapper () throws IOException
	{
		String xml = 
			"<Abstract/>\n" + 
			"<ArticleTitle>test</ArticleTitle>\n" + 
			"<Abstract id = '1' />\n" + 
			"<ArticleTitle id = '2'>test1</ArticleTitle>\n" + 
			"<Abstract foo = '/3' >test2</Abstract>\n" +  
			"<Abstract foo = '/4' />\n"; 
		
		ReaderInputStream xmlin = new ReaderInputStream ( new StringReader ( xml ), "UTF-8" );
		
		InputStream xmlw = XmlFilterUtils.cdataWrapper ( xmlin, "ArticleTitle", "Abstract" );
		String outs = IOUtils.toString ( xmlw, "UTF-8" );
		
		log.info ( "Resulting XML fragment:\n{}", outs );
		
		assertTrue ( "Wrong result for self-closing tag!", outs.startsWith ( "<Abstract/>\n" ) );
		assertTrue ( "Wrong result!", outs.contains ( "<ArticleTitle><![CDATA[test]]></ArticleTitle>\n" ) );
		
		assertTrue ( "Wrong result for self-closing tag + attrib!", outs.contains ( "<Abstract id = '1' />\n" ) );
		assertTrue (
			"Wrong result for tag + attrib!", 
			outs.contains ( "<ArticleTitle id = '2'><![CDATA[test1]]></ArticleTitle>\n" ) 
		);		

		// Tricky case with '/' inside the attrib value
		assertTrue (
			"Wrong result for tag + attrib + '/'!", 
			outs.contains ( "<Abstract foo = '/3' ><![CDATA[test2]]></Abstract>\n" ) 
		);		
		assertTrue (
			"Wrong result for self-closing tag + attrib + '/'!", 
			outs.contains ( "<Abstract foo = '/4' />\n" ) 
		);
	}
}
