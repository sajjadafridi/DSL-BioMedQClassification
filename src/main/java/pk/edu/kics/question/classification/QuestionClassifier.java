package pk.edu.kics.question.classification;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import gov.nih.nlm.uts.webservice.security.UtsFault_Exception;
import pk.edu.kics.cache.LingPipeCacheProvider;
import pk.edu.kics.cache.MetaMapCacheProvider;
import pk.edu.kics.cache.SynonymsCache;
import pk.edu.kics.cache.TMToolCacheProvider;
import pk.edu.kics.clearnlp.parser.NLPParser;
import pk.edu.kics.concept.search.ConceptSearcher;
import pk.edu.kics.featureextractor.AnswerTypeClassifierTrainer;
import pk.edu.kics.focusextractor.QuestionFocusExtractor;
import pk.edu.kics.utill.BioQAPhaseB;
import pk.edu.kics.utill.Concept;
import pk.edu.kics.utill.ConceptMerger;
import pk.edu.kics.utill.QuestionUtil;
import pk.edu.kics.utill.Token;
public class QuestionClassifier {

	

	public static void main(String[] args) throws UtsFault_Exception, AnalysisEngineProcessException, ClassNotFoundException, IOException, ParseException {
		// TODO Auto-generated method stub
//		String question="What is the most prominent sequence consensus for the polyadenylation site?";
//		String id="5133b9455274a5fb0700000c";
		QuestionUtil questionUtil;
		AnswerTypeClassifierTrainer ans;
		//ArrayList<QuestionUtil>qs=new ArrayList<>();
		//question.add("What is the most prominent sequence consensus for the polyadenylation site?");
		////question.add("Which extra thyroid tissues have thyrotropin (TSH) receptors?");
		//questionids.add("513f45abbee46bd34c000013");
		
		//List<String> question = Files.lines(Paths.get("resource/questions")).map(l -> l).collect(Collectors.toList());
		ArrayList<QuestionUtil> question=BioQAPhaseB.readQuestions();
		
		//cs.initialize();
		//Initialize all the caches
		NLPParser.initialiaze();
		MetaMapCacheProvider.initialiaze();
		TMToolCacheProvider.initialiaze();
		SynonymsCache.initialiaze();
		ConceptSearcher searcher=new ConceptSearcher();
		LingPipeCacheProvider.initialiaze();
		searcher.initialiaze();
		
		for(int i=0;i<10;i++) {
	
		List<Token> tokens=new ArrayList<>();
		List<Concept> concepts=new ArrayList<>();
		List<Concept> searchConcepts=new ArrayList<>();

		String focus;
		
		//parse question
		tokens = NLPParser.parseQuestion(question.get(i).getBody());
		concepts.addAll(MetaMapCacheProvider.getConcept(question.get(i).getBody()));
				
		//tmtool
		try {
			concepts.addAll(TMToolCacheProvider.getConcept(question.get(i).getBody()));
		} catch (AnalysisEngineProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		concepts.addAll(LingPipeCacheProvider.getConcept(question.get(i).getBody()));
				
		//concepts retrieval 
		concepts.addAll(SynonymsCache.getConcept(question.get(i).getBody(),concepts,searcher));
		//merge the concepts ...
		concepts = ConceptMerger.merge(concepts);
		
		//focus extraction
		focus=QuestionFocusExtractor.foundFocus(tokens);
		
		//add question utils
		question.get(i).setConcepts(concepts);
		question.get(i).setFocus(focus);
		question.get(i).setTokens(tokens);
	}
		
		
		
	    System.out.println("done");
		//AnwerType Classifier
		 ans=new AnswerTypeClassifierTrainer();
		 try {
			ans.process(question);
			ans.collectionProcessComplete();
		} catch (AnalysisEngineProcessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		 LingPipeCacheProvider.shutdown();	
			MetaMapCacheProvider.shutdown();
			TMToolCacheProvider.shutdown();
	}

}
