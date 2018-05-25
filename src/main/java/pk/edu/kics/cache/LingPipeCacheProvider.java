package pk.edu.kics.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import pk.edu.kics.lingpipe.Lingpipe;
import pk.edu.kics.utill.Concept;

public class LingPipeCacheProvider
{
	public static DB db;
	public static NavigableMap<String, List<Concept>> dataMap;
	
	
	@SuppressWarnings("unchecked")
	public static void initialiaze() {
		System.out.println("Cache Initialized");
		db = DBMaker.fileDB("resource/cache-map/lingpipe-cache.mapdb").checksumHeaderBypass().closeOnJvmShutdown()
				.fileMmapEnable().make();

		dataMap = db.treeMap("resource/cache-map/lingpipe-cache.mapdb.t", Serializer.STRING, Serializer.JAVA).createOrOpen();
	}
	public static List<Concept> getConcept(String question) throws AnalysisEngineProcessException, ClassNotFoundException 
	{	
		List<Concept> concepts = new ArrayList<>();
		if (!dataMap.containsKey(question)) {
			
			try {
				concepts.add(Lingpipe.getConcept(question));
				dataMap.put(question, concepts);
			} catch (IOException e) {
				e.printStackTrace();
			}
	
			db.commit();

		} else
			concepts = dataMap.get(question);
	
		return concepts;

	}

	public static void shutdown() {
		db.close();
	}

}
