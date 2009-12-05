package test;

import java.io.File;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.neo4j.api.core.EmbeddedNeo;
import org.neo4j.api.core.NeoService;

import thewebsemantic.IndexedNeo;
import thewebsemantic.PersistenceManager;

public class TestTravelDomain {
	
	static IndexedNeo neo;

	static String[][] statedata = {
			{"TX", "Texas"},
			{"NY", "New York"},
			{"CA", "California"}
	};
	
	static String[][] citydata = {
			{"Dallas", "TX"},
			{"Austin", "TX"},
			{"San Antonio", "TX"},
			{"New York", "NY"},
			{"San Francisco", "CA"},
			{"San Diego", "CA"},
			{"Los Angeles", "CA"},
	};
	
	@BeforeClass
	public static void setup() {
		deleteDirectory(new File("neo_store"));
		neo = new IndexedNeo( new EmbeddedNeo("neo_store") );
		createStates();
		createCities();
	}
	
	private static void createCities() {
		PersistenceManager pm = new PersistenceManager(neo);
		for (String[] row : citydata) {
			City c = new City();
			c.setName(row[0]);
			State s = pm.getSingle(State.class, "statecode", row[1]);
			c.setState(s);
			s.getCities().add(c);
			pm.persist(c);
		}
		pm.close();
		
	}

	private static void createStates() {
		PersistenceManager pm = new PersistenceManager(neo);
		for (String[] row : statedata) {
			State s = new State();
			s.setCode(row[0]);
			s.setName(row[1]);
			pm.persist(s);
		}
		pm.close();
		
	}

	@AfterClass
	public static void teardown() {
		neo.shutdown();
	}
	
	@Test
	public void basic() {
		PersistenceManager pm = new PersistenceManager(neo);
		Collection<State> states = pm.get(State.class);
		assertEquals(3, states.size());
		Collection<City> cities = pm.get(City.class);
		assertEquals(7, cities.size());
		State texas = pm.getSingle(State.class, State.STATE_CODE_IDX, "TX");
		assertNotNull(texas);
		assertEquals(3, texas.getCities().size());		
	}
	
	@Test
	public void timeline() {
		PersistenceManager pm = new PersistenceManager(neo);

		Calendar c = Calendar.getInstance();
		c.add(Calendar.HOUR, -1);
		String[] stcodes = {"NM", "CO", "AK"};
		for (String string : stcodes) {
			State s = new State();
			s.setCode(string);
			pm.persist(s);
		}
		
		Collection<State> states = pm.getSince(State.class,c.getTime());
		assertEquals(6, states.size());
		
	}
	
	static public boolean deleteDirectory(File path) {
	    if( path.exists() ) {
	      File[] files = path.listFiles();
	      for(int i=0; i<files.length; i++) {
	         if(files[i].isDirectory()) {
	           deleteDirectory(files[i]);
	         }
	         else {
	           files[i].delete();
	         }
	      }
	    }
	    return( path.delete() );
	  }
	
}
