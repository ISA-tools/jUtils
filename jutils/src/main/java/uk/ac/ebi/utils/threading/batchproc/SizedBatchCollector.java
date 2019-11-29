package uk.ac.ebi.utils.threading.batchproc;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A batch collector such that the {@link #batchReadyFlag() batch ready flag} can be based on a 
 * {@link #batchSizer() batch size} and a {@link #maxBatchSize() max size parameter}.    
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>23 Nov 2019</dd></dl>
 *
 */
public interface SizedBatchCollector<B> extends BatchCollector<B>
{
	public abstract Function<B,Long> batchSizer ();
	
	@Override
	public default Predicate<B> batchReadyFlag ()
	{
		return b -> this.batchSizer ().apply ( b ) >= this.maxBatchSize ();
	}

	public default long maxBatchSize () {
		return 1000;
	}	
}
