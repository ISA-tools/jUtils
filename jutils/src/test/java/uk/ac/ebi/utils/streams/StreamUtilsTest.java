package uk.ac.ebi.utils.streams;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

/**
 * Tests for {@link StreamUtils}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Jul 2017</dd></dl>
 *
 */
public class StreamUtilsTest
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	/**
	 * Base test
	 */
	@Test
	@SuppressWarnings ( "unchecked" )
	public void testTupleStream ()
	{
		Stream<String>[] streams = new Stream[] {
			Stream.of ( "A", "B", "C" ),
			Stream.of ( "X", "Y", "Z" ),
			Stream.of ( "0", "1", "2" )
		};
		
		String[][] expResults = new String [][] {
			new String [] { "A", "X", "0" },
			new String [] { "B", "Y", "1" },
			new String [] { "C", "Z", "2" }
		};
		
		verifyStreamOfStringArrays ( 
			StreamUtils.tupleStream ( streams ),
			expResults
		);
	}
	
	/**
	 * Tests base streams of uneven sizes
	 */
	@Test
	@SuppressWarnings ( "unchecked" )
	public void testTupleStreamUneven ()
	{
		Stream<String>[] streams = new Stream[] {
			Stream.of ( "A", "B", "C" ),
			Stream.of ( "X", "Y" ),
			Stream.of ( "0", "1", "2" )
		};
		
		String[][] expResults = new String [][] {
			new String [] { "A", "X", "0" },
			new String [] { "B", "Y", "1" }
		};
		
		verifyStreamOfStringArrays ( 
			StreamUtils.tupleStream ( streams ),
			expResults
		);
	}

	/**
	 * Tests parallel result
	 */
	@Test
	@SuppressWarnings ( "unchecked" )
	public void testTupleStreamParallel ()
	{		
		Stream<String>[] streams = new Stream[] {
			Stream.of ( "A", "B", "C", "D", "E", "F" ).parallel (),
			Stream.of ( "Q", "W", "E", "R", "T", "Y" ).parallel (),
			Stream.of ( "0", "1", "2", "3", "4", "5" ).parallel ()
		};
		
		String[][] expResults = new String [][] {
			new String [] { "A", "Q", "0" },
			new String [] { "B", "W", "1" },
			new String [] { "C", "E", "2" },
			new String [] { "D", "R", "3" },
			new String [] { "E", "T", "4" },
			new String [] { "F", "Y", "5" }
		};
		
		verifyStreamOfStringArrays ( 
			StreamUtils.tupleStream ( 0, true, streams ),
			expResults
		);
	}

	
	/**
	 * Tests parallel result with uneven inputs.
	 * 
	 * This cannot work, because the split of arrays happen at e cut points and the tuple spliterator cannot 
	 * split correctly from uneven underlining splittings.
	 * 
	 */
	@Test ( expected = AssertionFailedError.class )
	@SuppressWarnings ( "unchecked" )
	public void testTupleStreamParallelUnven ()
	{		
		Stream<String>[] streams = new Stream[] {
			Stream.of ( "A", "B", "C", "D", "E", "F" ).parallel (),
			Stream.of ( "Q", "W", "E", "R" ).parallel (),
			Stream.of ( "0", "1", "2", "3", "4", "5" ).parallel ()
		};
		
		String[][] expResults = new String [][] {
			new String [] { "A", "Q", "0" },
			new String [] { "B", "W", "1" },
			new String [] { "C", "E", "2" },
			new String [] { "D", "R", "3" }
		};
		
		verifyStreamOfStringArrays ( 
			StreamUtils.tupleStream ( 0, true, streams ),
			expResults
		);
	}
	
	
	private void verifyStreamOfStringArrays ( Stream<String[]> input, String[][] expectedResults )
	{
		Object[] results = input
			.collect ( Collectors.toList () )
			.toArray ();
		
		log.info ( "Results:\n{}\n", Arrays.deepToString ( results ) );
		Assert.assertTrue ( "unexpected resulting array!", Arrays.deepEquals ( results, expectedResults ) );
	}
}
