package pk.edu.kics.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import pk.edu.kics.concept.search.ConceptSearcher;
import pk.edu.kics.utill.Concept;

public class SynonymsCache {
	public static DB db;
	public static NavigableMap<String, List<Concept>> dataMap;

	@SuppressWarnings("unchecked")
	public static void initialiaze() {
		System.out.println("Cache Initialized");
		db = DBMaker.fileDB("resource/cache-map/synonyms-cache.mapdb").checksumHeaderBypass().closeOnJvmShutdown()
				.fileMmapEnable().make();

		dataMap = db.treeMap("resource/cache-map/synonyms-cache.mapdb.t", Serializer.STRING, Serializer.JAVA)
				.createOrOpen();
	}

	public static List<Concept> getConcept(String key, List<Concept> cp,ConceptSearcher cs) {
		List<Concept> concepts = new ArrayList<>();
		if (!dataMap.containsKey(key)) {
			concepts = cs.ConceptSearch(cp);			
			dataMap.put(key, concepts);
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
