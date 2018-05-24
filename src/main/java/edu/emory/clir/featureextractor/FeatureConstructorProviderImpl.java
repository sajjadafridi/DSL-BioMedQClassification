/*
 * Open Advancement Question Answering (OAQA) Project Copyright 2016 Carnegie Mellon University
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package edu.emory.clir.featureextractor;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.UimaContextAdmin;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceManager;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.resource.metadata.ResourceMetaData;
import org.apache.uima.util.Logger;

import com.google.common.io.Resources;

import edu.emory.clir.clearnlp.utill.ConceptMention;
import edu.emory.clir.clearnlp.utill.QuestionUtil;
import edu.emory.clir.clearnlp.utill.Token;
import edu.emory.clir.clearnlp.utill.Type;

public class FeatureConstructorProviderImpl implements FeatureConstructorProvider {

	private  List<List<String>> quantityQuestionPhrases;

	public FeatureConstructorProviderImpl() {
		
		try {
			initialize();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean initialize() throws MalformedURLException {
		URL quantityQuestionWordsPath = new File("resource/classifier-data/quantity-question-words").toURI().toURL();
		try {
			quantityQuestionPhrases = Resources.readLines(quantityQuestionWordsPath, UTF_8)
					.stream().map(String::trim).map(line -> Arrays.asList(line.split(" "))).collect(toList());
			System.out.println(quantityQuestionPhrases.toString());
		} catch (IOException e) {

		}
		return true;
	}

	public Map<String, Double> constructFeatures(QuestionUtil question) {

		Map<String, Double> features = new HashMap<>();
		// question type
		features.put("question-type:" + question.getType(), 1.0);
		// cmention
		List<ConceptMention> cmentions = question.getConceptMentions();
		for (ConceptMention cmention : cmentions) {
			double score = cmention.getScore();
			if (Double.isNaN(score))
				score = 1.0;
			for (Type st : question.getConceptType()) {
				String semTypeAbbr = st.getAbb();
				String semType = "concept-type:" + semTypeAbbr;
				features.put(semType, score);
				String semTypePrefix = "concept-type-prefix:" + semTypeAbbr.split(":", 2)[0];
				features.put(semTypePrefix, score);
				if (!features.containsKey(semType) || features.get(semType) < score) {
					features.put(semType, score);
				}
				// here is problem
				/*
				 * Token token = TypeUtil.getHeadTokenOfAnnotation(cmention); String
				 * semTypeDepLabel ="concept-type:" + semTypeAbbr + "/dependency-label:" +
				 * token.getDepLabel(); if (!features.containsKey(semTypeDepLabel) ||
				 * features.get(semTypeDepLabel) < score) { features.put(semTypeDepLabel,
				 * score); } String semTypeHeadDepLabel = "concept-type:" + semTypeAbbr +
				 * "/head-dependency-label:" + (token.getHead() == null ? "null" :
				 * token.getHead().getDepLabel()); features.put(semTypeHeadDepLabel, score);
				 */ }
		}
		// token
		List<Token> tokens = question.getTokens();
		for (Token token : tokens) {
			features.put("lemma:" + token.getLemma(), 1.0);
		}
		features.put("first-lemma:" + tokens.get(0).getLemma(), 1.0);
		features.put("last-lemma:" + tokens.get(tokens.size() - 1).getLemma(), 1.0);
		// focus
		String focus = question.getFocus();
		if (focus != null) {
			features.put("focus:" + focus, 1.0);
		}
		List<String> lemmas = tokens.stream().map(Token::getLemma).collect(toList());
		boolean choice = (lemmas.get(0).equals("do") || lemmas.get(0).equals("be")) && lemmas.contains("or");
		features.put("choice", choice ? 1d : 0d);
		boolean quantity = quantityQuestionPhrases.stream().map(phrase -> Collections.indexOfSubList(lemmas, phrase))
				.filter(index -> index >= 0).findAny().isPresent();
		features.put("quantity", quantity ? 1.0 : 0.0);
		return features;
	}

	@Override
	public ResourceMetaData getMetaData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResourceManager getResourceManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Logger getLogger() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLogger(Logger aLogger) {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public UimaContext getUimaContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UimaContextAdmin getUimaContextAdmin() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
			throws ResourceInitializationException {
		// TODO Auto-generated method stub
		return false;
	}

	
	
}
