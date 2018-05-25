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

package pk.edu.kics.focusextractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import com.google.common.collect.ImmutableSet;

import pk.edu.kics.clearnlp.parser.NLPParser;
import pk.edu.kics.utill.Token;

public class QuestionFocusExtractor {

	private static final String ROOT_DEP_LABEL = "root";

	private static final String DEP_DEP_LABEL = "dep";

	private static final Set<String> NOUN_POS_TAGS = ImmutableSet.of("NN", "NNP", "NNS", "NNPS");

	Token tokenFocus = null;

	public static String foundFocus(List<Token> tokens) 
	{
		try {
			for (Token token : tokens) {
				if (token.getHead() != null && token.getHead().getDepLabel().equals(ROOT_DEP_LABEL)
						&& !token.getDepLabel().equals(DEP_DEP_LABEL) && NOUN_POS_TAGS.contains(token.getPos())) {
					return token.getLemma();
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;

	}

	public static void main(String[] args) throws AnalysisEngineProcessException {
		QuestionFocusExtractor focus = new QuestionFocusExtractor();
		String body = "What symptoms characterize the Muenke syndrome?";
		ArrayList<Token> qtoken = NLPParser.parseQuestion(body);
		focus.foundFocus(qtoken);

	}
}
