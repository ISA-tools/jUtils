package uk.ac.ebi.utils.test.junit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.Rule;
import org.junit.rules.ExternalResource;


/**
 * A JPA {@link EntityManager} provider to be used in test classes as {@link Rule} 
 * (please see JUnit documentation for details). Before starting a test method in the test class using an instance
 * of this provider, the EM will be initialised and it will be closed after the test is run (so, there will be 
 * one instantiation per test method). The class is supposed to be instantiated with some EM factory.
 * 
 * <h2>Typical Usage</h2>
 * 
 * <pre>
 * public class ExampleTest
 * {
 *   {@literal @}Rule
 *   public TestEntityMgrProvider emProvider = new TestEntityMgrProvider ( &lt;your EntityManagerFactory here&gt; );
 *   
 *   {@literal @}Test
 *   public void testMethod () {
 *     EntityManager em = emProvider.getEntityManager ();
 *     &lt;use em here&gt;
 *   }
 * }
 * </pre>
 *
 * <dl><dt>date</dt><dd>Dec 13, 2011</dd></dl>
 * @author Marco Brandizi
 *
 */
public class TestEntityMgrProvider extends ExternalResource
{
	private final EntityManagerFactory emf;
	private EntityManager entityManager;
	
	/**
	 * Pass me a factory that I can use to create entity managers.
	 */
	public TestEntityMgrProvider ( EntityManagerFactory emf )
	{
		super ();
		this.emf = emf;
	}

	/**
	 * The last EM created by {@link #newEntityManager()}. This is created by {@link #before()} and disposed by {@link #after()}.
	 */
	public EntityManager getEntityManager () {
		return entityManager;
	}

	/**
	 * Uses the factor passed to constructor to create a new {@link EntityManager}, via {@link #newEntityManager()}, 
	 * makes it available via {@link #getEntityManager()}. This is supposed to be done before each test method in a 
	 * test class where an instance of this class is annotated with {@link Rule}.
	 * 
	 */
	@Override
	protected void before () throws Throwable {
		newEntityManager ();
	}

	/**
	 * Shuts down (flushes, closes, etc) the entity manager created by {@link #before()}. This is supposed to be called 
	 * by a test class via the {@link Rule} annotation. 
	 * 
	 */
	@Override
	protected void after ()
	{
  	EntityManager em = getEntityManager ();
    if ( em != null && em.isOpen() ) em.close();
	}

	/**
	 * Disposes (via {@link #after()}) the previously created entity manager, if any, and creates a new entity manager, 
	 * using the factory passed to the constructor. The method is public and returns the newly-created EM, cause sometimes
	 * you need to re-create a new EM in the middle of a test (eg, long tests that creates memory leaks).
	 *  
	 */
	public EntityManager newEntityManager () {
		after(); return entityManager = emf.createEntityManager ();
	}
}
