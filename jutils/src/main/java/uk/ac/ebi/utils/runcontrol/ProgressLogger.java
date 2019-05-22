package uk.ac.ebi.utils.runcontrol;

import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jext.Logger;
import uk.org.lidalia.slf4jext.LoggerFactory;

/**
 * Reports (using the logger) the progress of some process, represented by a long number.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 May 2019</dd></dl>
 *
 */
public class ProgressLogger
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	private long progress;
	private long progressResolution = 1000;
	
	private String logMessageTemplate = "{} items processed";
	private Level loggingLevel = Level.INFO;
		
		
	public ProgressLogger ( String logMessageTemplate, long progressResolution )
	{
		this.logMessageTemplate = logMessageTemplate;
		this.progressResolution = progressResolution;
	}

	public ProgressLogger ( long progressResolution ) {
		this.progressResolution = progressResolution;
	}


	public ProgressLogger ()
	{
	}

	/**
	 * Updates the current progress and possibly generates a new log message, according to {@link #getProgressResolution()}, as
	 */
	public void update ( long newProgress )
	{
		this.progressReport ( this.progress, newProgress );
		synchronized ( this ) {
			this.progress = newProgress;
		}
	}

	/**
	 * Wrapper of {@link #update(long)}
	 */
	public void updateWithIncrement ( long increment )
	{
		this.update ( this.progress + increment );
	}

	/**
	 * {@link #updateWithIncrement() increments} by 1
	 */
	public void updateWithIncrement ()
	{
		this.updateWithIncrement ( 1 );
	}
		
	/**
	 * Invoked by {@link #update(long)}, decides if we have to log the new progress and possibly does it.
	 */
	protected void progressReport ( long oldProgress, long newProgress )
	{
		long delta = newProgress - oldProgress;
		if ( delta < progressResolution ) return;  
		log.log ( loggingLevel, logMessageTemplate, newProgress );
	}
	
	/**
	 * When {@link #update(long)} and similar methods increment an old value, a logging message is generated
	 * if the new progress is equal or greater than the resolution.
	 */
	public long getProgressResolution ()
	{
		return progressResolution;
	}

	public void setProgressResolution ( long progressResolution )
	{
		this.progressResolution = progressResolution;
	}

	/**
	 * Default "{} items processed". This is passed to the underlining logger 
	 */
	public String getLogMessageTemplate ()
	{
		return logMessageTemplate;
	}

	public void setLogMessageTemplate ( String logMessageTemplate )
	{
		this.logMessageTemplate = logMessageTemplate;
	}

	/**
	 * The logging messages are generated with this level. Default is {@link Level#INFO}.
	 */
	public Level getLoggingLevel ()
	{
		return loggingLevel;
	}

	public void setLoggingLevel ( Level loggingLevel )
	{
		this.loggingLevel = loggingLevel;
	}

	/**
	 * The progress is updated by {@link #update(long)} and similar methods.
	 */
	public long getProgress ()
	{
		return progress;
	}
}
