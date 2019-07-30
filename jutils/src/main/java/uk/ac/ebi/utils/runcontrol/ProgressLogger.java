package uk.ac.ebi.utils.runcontrol;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jext.Logger;
import uk.org.lidalia.slf4jext.LoggerFactory;

/**
 * Reports (using the logger) the progress of some process, represented by a long number.
 * The progress update operations in this class are thread-safe.
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

	/**
	 * Updates the current progress and possibly generates a new log message, according to {@link #getProgressResolution()}, as
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
	 */
	protected void progressReport ( long oldProgress, long newProgress )
	{
		Lock rlock = lock.readLock ();
		rlock.lock ();
		try {
			long oldCheckPt = oldProgress / progressResolution;
			long newCheckPt = newProgress / progressResolution;
			if ( newCheckPt - oldCheckPt == 0 ) return;
			log.log ( loggingLevel, logMessageTemplate, newProgress );
		}
		finally {
			rlock.unlock ();
		}
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
}
