package uk.ac.ebi.utils.threading.batchproc;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>23 Nov 2019</dd></dl>
 *
 */
public interface ItemizedSizedBatchCollector<B,E> 
	extends ItemizedBatchCollector<B, E>, SizedBatchCollector<B>
{
	// Doesn't need anything, it's just a signature
}
