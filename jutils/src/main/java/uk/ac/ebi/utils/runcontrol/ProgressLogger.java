package uk.ac.ebi.utils.runcontrol;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;

import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jext.Logger;
import uk.org.lidalia.slf4jext.LoggerFactory;

/**
 * Reports (using a logger) the progress of some process, represented by a long number.
 * The progress-update operations in this class are thread-safe.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 May 2019</dd></dl>
 *
 */
public class ProgressLogger
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	private long progress = 0;
	private long progressResolution = 1000;
	
	private String logMessageTemplate = "{} items processed";
	private Level loggingLevel = Level.INFO;
		
	private BiConsumer<Long, Long> progressReportAction =
		(oldProgress, newProgress) -> log.log ( loggingLevel, logMessageTemplate, newProgress );

	
	private ReadWriteLock lock = new ReentrantReadWriteLock ();
		
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

	
	public void reset () {
		this.update ( 0 );
	}
	
	/**
	 * Updates the current progress and possibly generates a new log message, according to {@link #getProgressResolution()}.
	 * The new progress should be greater than the existing one, except for the value 0, which resets the logger for a new
	 * run of items. 
	 * 
	 */
	public void update ( long newProgress )
	{
		long oldProgress;
		
		Lock wlock = lock.writeLock ();
		wlock.lock ();
		try
		{
			oldProgress = this.progress;
			this.progress = newProgress;
		}
		finally { 
			wlock.unlock ();
		}
		if ( newProgress == 0 ) return;
		
		this.progressReport ( oldProgress, newProgress );
	}

	/**
	 * Wrapper of {@link #update(long)}
	 */
	public void updateWithIncrement ( long increment )
	{
		Lock wlock = lock.writeLock ();
		wlock.lock ();
		try {
			this.update ( this.progress + increment );
		}
		finally { 
			wlock.unlock (); 
		}
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
	 * If yes, it invokes {@link #getProgressReportAction()}.
	 */
	protected void progressReport ( long oldProgress, long newProgress )
	{
		Lock rlock = lock.readLock ();
		rlock.lock ();
		try {
			long oldCheckPt = oldProgress / progressResolution;
			long newCheckPt = newProgress / progressResolution;
			if ( newCheckPt - oldCheckPt == 0 ) return;
		}
		finally {
			rlock.unlock ();
		}
		progressReportAction.accept ( oldProgress, newProgress );
	}
	
	/**
	 * When {@link #update(long)} and similar methods increment an old value, a logging message is generated
	 * if the difference between new and old progress is &gt;= the resolution.
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

	/**
	 * <p>This is invoked when the progress reaches a multiple of {@link #getProgressResolution()}, as per
	 * {@link #update(long)} implementation. The bi-consumer receives the before and after-update progresses so far.</p>
	 * 
	 * <p>The default action logs with {@link #getLogMessageTemplate()} and {@link #getLoggingLevel()}.
	 * Typically, you will want to use such default action and then chain yours via {@link BiConsumer#andThen(BiConsumer)}
	 * (or the other way around, whatever you prefer). BEWARE that, without such a chaining, you'll lose (replace) the logging
	 * step. Consider {@link #appendProgressReportAction(BiConsumer)} instead of the regular setter.</p>
	 * 
	 */
	public BiConsumer<Long, Long> getProgressReportAction ()
	{
		return progressReportAction;
	}

	/**
	 * @see #appendProgressReportAction(BiConsumer).
	 */
	public void setProgressReportAction ( BiConsumer<Long, Long> progressReportAction )
	{
		this.progressReportAction = progressReportAction;
	}
	
	/**
	 * Facility to append the action to the {@link #getProgressReportAction()}. This can be useful when 
	 * you want to add something to the default logging action. @see {@link #getProgressReportAction()}
	 */
	public void appendProgressReportAction ( BiConsumer<Long, Long> progressReportAction )
	{
		BiConsumer<Long, Long> currentAction = this.getProgressReportAction ();
		if ( currentAction == null ) {
			this.setProgressReportAction ( progressReportAction );
			return;
		}
		this.setProgressReportAction ( currentAction.andThen ( progressReportAction ) );
	}
}
