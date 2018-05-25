package pk.edu.kics.question.classification;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import gov.nih.nlm.uts.webservice.security.UtsFault_Exception;
import pk.edu.kics.cache.LingPipeCacheProvider;
import pk.edu.kics.cache.MetaMapCacheProvider;
import pk.edu.kics.cache.TMToolCacheProvider;
import pk.edu.kics.clearnlp.parser.NLPParser;
import pk.edu.kics.concept.search.ConceptSearcher;
import pk.edu.kics.featureextractor.AnswerTypeClassifierTrainer;
import pk.edu.kics.focusextractor.QuestionFocusExtractor;
import pk.edu.kics.utill.Concept;
import pk.edu.kics.utill.ConceptMerger;
import pk.edu.kics.utill.QuestionUtil;
import pk.edu.kics.utill.Token;
public class QuestionClassifier {

	

	public static void main(String[] args) throws UtsFault_Exception, AnalysisEngineProcessException, ClassNotFoundException, IOException {
		// TODO Auto-generated method stub
//		String question="What is the most prominent sequence consensus for the polyadenylation site?";
//		String id="5133b9455274a5fb0700000c";
		
		AnswerTypeClassifierTrainer ans;
		ArrayList<QuestionUtil>qs=new ArrayList<>();
		ArrayList<String>questionids=new ArrayList<>();
		//question.add("What is the most prominent sequence consensus for the polyadenylation site?");
		////question.add("Which extra thyroid tissues have thyrotropin (TSH) receptors?");
		//questionids.add("513f45abbee46bd34c000013");
		
		List<String> question = Files.lines(Paths.get("resource/questions")).map(l -> l).collect(Collectors.toList());
		
		//cs.initialize();
		//Initialize all the cache and the classes 
		NLPParser.initialiaze();
		MetaMapCacheProvider.initialiaze();
		TMToolCacheProvider.initialiaze();
		ConceptSearcher searcher=new ConceptSearcher();
		LingPipeCacheProvider.initialiaze();
		searcher.initialiaze();
		
		for(int i=0;i<question.size();i++) {
	
		List<Token> tokens=new ArrayList<>();
		List<Concept> concepts=new ArrayList<>();
		List<Concept> searchConcepts=new ArrayList<>();

		String focus;
		
		//parse question
		tokens = NLPParser.parseQuestion(question.get(i));
		concepts.addAll(MetaMapCacheProvider.getConcept(question.get(i)));
				
		//tmtool
		try {
			concepts.addAll(TMToolCacheProvider.getConcept(question.get(i)));
		} catch (AnalysisEngineProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		concepts.addAll(LingPipeCacheProvider.getConcept(question.get(i)));
		
		//concepts retrieval
		
		searchConcepts=searcher.ConceptSearch(concepts);
		
		//concepts merging
		concepts=ConceptMerger.merge(concepts);
				
		//focus extraction
		focus=QuestionFocusExtractor.foundFocus(tokens);
		
		//add question utils
		try
		{
		QuestionUtil questionUtil=new QuestionUtil(concepts, tokens, focus,question.get(i),questionids.get(i));
		qs.add(questionUtil);
		}
		catch(Exception ex)
		{
			ex.getMessage();
		}
		
		
		
		
		
	}
		LingPipeCacheProvider.shutdown();	
		MetaMapCacheProvider.shutdown();
		TMToolCacheProvider.shutdown();
		
	    System.out.println("done");
		//AnwerType Classifier
		 ans=new AnswerTypeClassifierTrainer();
		 try {
			ans.process(qs);
			ans.collectionProcessComplete();
		} catch (AnalysisEngineProcessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
	}

}
