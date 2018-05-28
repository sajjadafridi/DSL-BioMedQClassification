package pk.edu.kics.clearnlp.parser;

import java.util.ArrayList;
import java.util.List;
import edu.emory.clir.clearnlp.component.AbstractComponent;
import edu.emory.clir.clearnlp.component.mode.dep.DEPConfiguration;
import edu.emory.clir.clearnlp.component.utils.GlobalLexica;
import edu.emory.clir.clearnlp.component.utils.NLPUtils;
import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.tokenization.AbstractTokenizer;
import edu.emory.clir.clearnlp.util.lang.TLanguage;
import pk.edu.kics.utill.Token;

public class NLPParser
{
	private AbstractComponent[] components;
	private AbstractTokenizer   tokenizer;
	static NLPParser nlp;
	
	public static void initialiaze() {
		nlp   = new NLPParser(TLanguage.ENGLISH);
		}
	
	public NLPParser(TLanguage language)
	{
		tokenizer  = NLPUtils.getTokenizer(TLanguage.ENGLISH);
		components = getBioinformaticsModels(TLanguage.ENGLISH);
		
	}

	
	public AbstractComponent[] getBioinformaticsModels(TLanguage language)
	{
		// initialize global lexicons
		List<String> paths = new ArrayList<>();
		paths.add("brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt.xz");
		GlobalLexica.initDistributionalSemanticsWords(paths);
		
		// initialize statistical models
		AbstractComponent morph = NLPUtils.getMPAnalyzer(language);
		AbstractComponent pos = NLPUtils.getPOSTagger   (language, "bioinformatics-en-pos.xz");
		AbstractComponent dep = NLPUtils.getDEPParser   (language, "bioinformatics-en-dep.xz", new DEPConfiguration("root"));
		
		return new AbstractComponent[]{pos, morph, dep};
	}
	
	public DEPTree toDEPTree(String line)
	{
		List<String> tokens = tokenizer.tokenize(line);
		System.out.println(tokens.toString());
		DEPTree tree = new DEPTree(tokens);
		
		for (AbstractComponent component : components)
			component.process(tree);
		
		return tree;
	}
	
	public static void getHeadToken(DEPNode dnode,Token token)
	{
		if(dnode!=null)
		{
		    token.setHead(new Token(dnode.getWordForm(),dnode.getLemma(),dnode.getPOSTag(),dnode.getLabel()));
		    getHeadToken(dnode.getHead(), token.getHead());
		}
			
	}

	
	public static ArrayList<Token> parseQuestion(String question)
	{
		ArrayList<Token> QcFeature = new ArrayList<>();
		
	      try 
			{
				DEPTree tree = nlp.toDEPTree(question);
				for (int i = 1; i < tree.size(); i++) 
					
					// retrieve each question token, lemma and part of speech
				{
					Token token = new Token(tree.get(i).getWordForm(), tree.get(i).getLemma(), tree.get(i).getPOSTag(), tree.get(i).getLabel());
				    DEPNode dnode = tree.get(i).getHead();
					   
					getHeadToken(dnode,token);
					QcFeature.add(token);
					
				}

			}
			catch (Exception e) {e.printStackTrace();}
	      return QcFeature;
		}


public static void main(String args[]) {
	initialiaze();
 	String body="Which antibody is implicated in the Bickerstaff's brainstem encephalitis?";
 	parseQuestion(body);
	//NLPDecodeTutorial nlp   = new NLPDecodeTutorial(TLanguage.ENGLISH);
 	//DEPTree tree = nlp.toDEPTree("What is cancer?");
	//System.out.println(tree.toString()+"\n");
 	
 	//nlp.processRaw (new FileInputStream(body) , IOUtils.createBufferedPrintStream("good" +".cnlp"));
}
}

