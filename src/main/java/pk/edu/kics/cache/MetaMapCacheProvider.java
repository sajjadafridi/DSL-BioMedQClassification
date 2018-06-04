package pk.edu.kics.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import javax.xml.bind.JAXBException;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import pk.edu.kics.metamap.MetaMapService;
import pk.edu.kics.utill.Concept;

public class MetaMapCacheProvider
{
	public static DB db;
	public static NavigableMap<String, List<Concept>> dataMap;
	@SuppressWarnings("unchecked")
	public static void initialiaze() {
		System.out.println("Metamapcache Initialized");
		db = DBMaker.fileDB("resource/cache-map/metamap-cache.mapdb").checksumHeaderBypass().closeOnJvmShutdown()
				.fileMmapEnable().make();

		dataMap = db.treeMap("resource/cache-map/metamap-cache.mapdb.t", Serializer.STRING, Serializer.JAVA).createOrOpen();
	}
	public static List<Concept> getConcept(String key) 
	{	
		List<Concept> concepts = new ArrayList<>();
		if (!dataMap.containsKey(key)) {
			
			try {
				concepts.addAll(MetaMapService.getConcept(key));
				dataMap.put(key, concepts);
			} catch (JAXBException | IOException e) {
				e.printStackTrace();
			}
			// Commit and close
			db.commit();

		} else
			concepts = dataMap.get(key);
	
		return concepts;

	}

	public static void shutdown() {
		db.close();
	}

}
