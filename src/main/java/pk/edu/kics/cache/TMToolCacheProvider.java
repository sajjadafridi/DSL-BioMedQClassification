package pk.edu.kics.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import pk.edu.kics.tmtool.TmToolConceptProvider;
import pk.edu.kics.utill.Concept;

public class TMToolCacheProvider
{
	public static DB db;
	public static NavigableMap<String, List<Concept>> dataMap;
	
	
	@SuppressWarnings("unchecked")
	public static void initialiaze() {
		System.out.println("TmToolCache is Initialized");
		db = DBMaker.fileDB("resource/cache-map/TmTool-cache.mapdb").checksumHeaderBypass().closeOnJvmShutdown()
				.fileMmapEnable().make();

		dataMap = db.treeMap("resource/cache-map/TmTool-cache.mapdb.t", Serializer.STRING, Serializer.JAVA).createOrOpen();
	}
	public static List<Concept> getConcept(String key) throws AnalysisEngineProcessException 
	{	
		List<Concept> concepts = new ArrayList<>();
		if (!dataMap.containsKey(key)) 
		{
			concepts.addAll(TmToolConceptProvider.getConcept(key));
			dataMap.put(key, concepts);
			db.commit();

		} 
		else
			concepts = dataMap.get(key);
	
		return concepts;

	}

	public static void shutdown() {
		db.close();
	}

}
