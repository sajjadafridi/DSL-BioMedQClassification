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
import pk.edu.kics.utill.Concept;
import pk.edu.kics.utill.ConceptMerger;
import pk.edu.kics.utill.IOHelper;
import pk.edu.kics.utill.Question;
import pk.edu.kics.utill.Token;

public class QuestionClassifier {
	
	public static final String[] atGsLabelFiles = { "resource/classifier-data/6b-dev-gslabel-tmtool.json","resource/classifier-data/6b-dev-gslabel-uts.json" };
	public static final String goldenquestion = "resource/phaseb/1b-1-b.json";

	
	public QuestionClassifier()
	{
		
	}

	public static void main(String[] args) throws UtsFault_Exception, AnalysisEngineProcessException,
		ClassNotFoundException, IOException, ParseException {
		Question questionItem;
		AnswerTypeClassifierTrainer ans;

		// List<String> question = Files.lines(Paths.get("resource/questions")).map(l ->
		// l).collect(Collectors.toList());
		ArrayList<Question> question = IOHelper.readGoldQuestions(goldenquestion);
		IOHelper.readLabeledQuestions(atGsLabelFiles);
		NLPParser.initialiaze();
		MetaMapCacheProvider.initialiaze();
		TMToolCacheProvider.initialiaze();
		SynonymsCache.initialiaze();
		ConceptSearcher searcher = new ConceptSearcher();
		LingPipeCacheProvider.initialiaze();
		searcher.initialiaze();

		for (int i = 0; i < question.size(); i++) {

			List<Token> tokens = new ArrayList<>();
			List<Concept> concepts = new ArrayList<>();
			String focus;

			// parse question
			tokens = NLPParser.parseQuestion(question.get(i).getQuestion());
			//metamap service 
			concepts.addAll(MetaMapCacheProvider.getConcept(question.get(i).getQuestion()));

			// tmtool
			try {
				concepts.addAll(TMToolCacheProvider.getConcept(question.get(i).getQuestion()));
			} catch (AnalysisEngineProcessException e) {
				e.printStackTrace();
			}

			concepts.addAll(LingPipeCacheProvider.getConcept(question.get(i).getQuestion()));

			// Synonyms retrieval
			concepts.addAll(SynonymsCache.getConcept(question.get(i).getQuestion(), concepts, searcher));
			// merge the concepts ...
			concepts = ConceptMerger.merge(concepts);
			System.out.println("Concept merged succcessfully.");
			// focus extraction
			focus = QuestionFocusExtractor.foundFocus(tokens);

			// add question utils
			question.get(i).setConcepts(concepts);
			question.get(i).setFocus(focus);
			question.get(i).setTokens(tokens);
		}

		System.out.println("done");
		// AnwerType Classifier
		ans = new AnswerTypeClassifierTrainer();
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
