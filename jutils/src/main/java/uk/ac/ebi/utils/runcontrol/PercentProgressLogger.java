package uk.ac.ebi.utils.runcontrol;

/**
 * <p>It's like {@link ProgressLogger} but reports percentages, given a max value that you set initially.</p> 
 * 
 * <p>Note that the {@link #getProgressReportAction() prog report action} receives the progress percentages, not
 * the absolute values.</p>
 * 
 * <p>Instead, {@link #update(long)} and its variants receives an absolute increment, as this is  to be used with the 
 * {@link #getProgressResolution() progressResolution property}.</p>
 * 
 * <p>{@link #getProgress()} reports the absolute progress too, while {@link #getPercentProgress()} is provided as 
 * a shortcut to the percent progress.</p>
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 May 2019</dd></dl>
 *
 */
public class PercentProgressLogger extends ProgressLogger
{
	private long maxProgress;
	
	public PercentProgressLogger ( String logMessageTemplate, long maxProgress, long progressResolutionPercent )
	{
		super ( logMessageTemplate, progressResolutionPercent );
		this.maxProgress = maxProgress;
	}

	public PercentProgressLogger ( String logMessageTemplate, long maxProgress )
	{
		this ( logMessageTemplate, maxProgress, 10 );
	}

	public PercentProgressLogger ( long maxProgress )
	{
		this ( "{}% done", maxProgress );
	}

	@Override
	protected void progressReport ( long oldProgress, long newProgress )
	{
		int oldPercent = (int) Math.round ( 100d * oldProgress / this.maxProgress );
		int newPercent = (int) Math.round ( 100d * newProgress / this.maxProgress );
		super.progressReport ( oldPercent, newPercent );
	}

	/**
	 * Every progress is reported in integer percentage points calculated with respect to this max 
	 * value. Methods like {@link #update(long)} should keep updating with the absolute progress and
	 * then {@link #progressReport(long, long)} will convert everything as needed.
	 */
	public long getMaxProgress ()
	{
		return maxProgress;
	}

	public void setMaxProgress ( long maxProgress )
	{
		this.maxProgress = maxProgress;
	}

	/**
	 * Uses {@link #getProgress() the absolute progress} and {@link #getMaxProgress()} to 
	 * report the 0-100 progress 
	 */
	public double getPercentProgress ()
	{
		return 100d * this.getProgress () / this.maxProgress;
	}
	
}
