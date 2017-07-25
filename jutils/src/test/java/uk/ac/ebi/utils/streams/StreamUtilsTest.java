package uk.ac.ebi.utils.streams;

import java.util.Arrays;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.Assert;

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
		
		
		Object[] results = StreamUtils
			.tupleStream ( streams )
			.collect ( Collectors.toList () )
			.toArray ();
		
		log.info ( "Results:\n{}\n", Arrays.deepToString ( results ) );
		Assert.assertTrue ( "unexpected resulting array!", Arrays.deepEquals ( results, expResults ) );
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
			new String [] { "B", "Y", "1" },
		};
		
		
		Object[] results = StreamUtils
			.tupleStream ( streams )
			.collect ( Collectors.toList () )
			.toArray ();
		
		log.info ( "Results:\n{}\n", Arrays.deepToString ( results ) );
		Assert.assertTrue ( "unexpected resulting array!", Arrays.deepEquals ( results, expResults ) );
	}

	/**
	 * Tests parallel result
	 */
	@Test
	@SuppressWarnings ( "unchecked" )
	public void testTupleStreamParallel ()
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
		
		
		Object[] results = StreamUtils
			.tupleStream ( Spliterator.ORDERED, true, streams )
			.collect ( Collectors.toList () )
			.toArray ();
		
		log.info ( "Results:\n{}\n", Arrays.deepToString ( results ) );
		Assert.assertTrue ( "unexpected resulting array!", Arrays.deepEquals ( results, expResults ) );
	}


}
