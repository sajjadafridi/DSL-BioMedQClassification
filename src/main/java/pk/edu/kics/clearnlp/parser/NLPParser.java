/**
 * Copyright 2015, Emory University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pk.edu.kics.clearnlp.parser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.emory.clir.clearnlp.component.AbstractComponent;
import edu.emory.clir.clearnlp.component.mode.dep.DEPConfiguration;
import edu.emory.clir.clearnlp.component.utils.GlobalLexica;
import edu.emory.clir.clearnlp.component.utils.NLPUtils;
import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.tokenization.AbstractTokenizer;
import edu.emory.clir.clearnlp.util.IOUtils;
import edu.emory.clir.clearnlp.util.lang.TLanguage;
import pk.edu.kics.utill.Token;

/**
 * @author Jinho D. Choi ({@code jinho.choi@emory.edu})
 */
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
					    //token.setHead(new Token(dnode.getWordForm(),dnode.getLemma(),dnode.getPOSTag(),dnode.getLabel()));
					getHeadToken(dnode,token);
					QcFeature.add(token);
					//token.iamtoken();
				}
				//nlp.processRaw (new FileInputStream(sampleRaw) , IOUtils.createBufferedPrintStream(sampleRaw +".cnlp"));
				//nlp.processLine(new FileInputStream(sampleLine), IOUtils.createBufferedPrintStream(sampleLine+".cnlp"));
				
				//InputStream[] in  = new InputStream[]{new FileInputStream(sampleRaw), new FileInputStream(sampleRaw)};
				//PrintStream[] out = new PrintStream[]{IOUtils.createBufferedPrintStream(sampleRaw+".0.cnlp"), IOUtils.createBufferedPrintStream(sampleRaw+".1.cnlp")};
				//nlp.processMultiThreads(in, out);
			}
			catch (Exception e) {e.printStackTrace();}
	      return QcFeature;
		}
		//final String sampleRaw  = "src/main/resources/samples/sample-raw.txt";
		//final String sampleLine = "src/main/resources/samples/sample-line.txt";	

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

