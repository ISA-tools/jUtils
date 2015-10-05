package uk.ac.ebi.utils.runcontrol;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>5 Oct 2015</dd>
 *
 */
public interface WrappedInvoker<WV>
{
	public WV run ( Runnable action );
}
