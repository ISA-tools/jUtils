package uk.ac.ebi.utils.threading.batchproc;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>23 Nov 2019</dd></dl>
 *
 */
public class BatchCollectorImpl<B> implements BatchCollector<B> 
{
	private Supplier<B> batchFactory;
	private Predicate<B> batchReadyFlag;

	
	public BatchCollectorImpl ()
	{
	}

	@Override
	public Supplier<B> batchFactory ()
	{
		return this.batchFactory;
	}

	@Override
	public Predicate<B> batchReadyFlag ()
	{
		return this.batchReadyFlag;
	}

}
