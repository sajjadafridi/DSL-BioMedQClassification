package edu.emory.clir.clearnlp.question.classification;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import edu.emory.clir.clearnlp.cache.LingPipeCacheProvider;
import edu.emory.clir.clearnlp.cache.MetaMapCacheProvider;
import edu.emory.clir.clearnlp.cache.TMToolCacheProvider;
import edu.emory.clir.clearnlp.concept.search.ConceptSearcher;
import edu.emory.clir.clearnlp.focusextractor.QuestionFocusExtractor;
import edu.emory.clir.clearnlp.tutorial.NLPDecodeTutorial;
import edu.emory.clir.clearnlp.utill.Concept;
import edu.emory.clir.clearnlp.utill.ConceptMerger;
import edu.emory.clir.clearnlp.utill.QuestionUtil;
import edu.emory.clir.clearnlp.utill.Token;
import edu.emory.clir.featureextractor.AnswerTypeClassifierTrainer;
import gov.nih.nlm.uts.webservice.security.UtsFault_Exception;
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
		NLPDecodeTutorial.initialiaze();
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
		tokens = NLPDecodeTutorial.parseQuestion(question.get(i));
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
