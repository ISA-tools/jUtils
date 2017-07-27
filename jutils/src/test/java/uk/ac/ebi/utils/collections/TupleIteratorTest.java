package uk.ac.ebi.utils.collections;

import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.ORDERED;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.Assert;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>27 Jul 2017</dd></dl>
 *
 */
public class TupleIteratorTest
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	@Test
	public void testBasics ()
	{
		testInput ( 
			new String[][] {
				{ "A", "B", "C" },
				{ "X", "Y", "Z" },
				{ "0", "1", "2" }
		  },
			new String[][] {
				{ "A", "X", "0" },
				{ "B", "Y", "1" },
				{ "C", "Z", "2" }
		  }		
		);
	}
	
	/**
	 * Tests base streams of uneven sizes
	 */
	@Test
	public void testTupleStreamUneven ()
	{
		testInput ( 
			new String[][] {
				{ "A", "B", "C" },
				{ "X", "Y" },
				{ "0", "1", "2" }
			},
			new String[][] {
				{ "A", "X", "0" },
				{ "B", "Y", "1" }
		  }		
		);
	}
	
	private void testInput ( String[][] data, String[][] expectedData )
	{
		@SuppressWarnings ( "unchecked" )
		Iterator<String>[] iterators = new Iterator[] {
			Arrays.asList ( data [ 0 ] ).iterator (),
			Arrays.asList ( data [ 1 ] ).iterator (),
			Arrays.asList ( data [ 2 ] ).iterator (),
		};
				
				
		TupleIterator<String> tuples = TupleIterator.of ( iterators );
		
		Object[] results =
		  StreamSupport.stream ( Spliterators.spliteratorUnknownSize ( tuples, IMMUTABLE | NONNULL | ORDERED ), false )
		  .collect ( Collectors.toList () )
		  .toArray ();
		
		
		log.info ( "Results:\n{}\n", Arrays.deepToString ( results ) );
		Assert.assertTrue ( "unexpected resulting array!", Arrays.deepEquals ( results, expectedData ) );
	}	
	
}
