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

package edu.emory.clir.clearnlp.lingpipe;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.util.Streams;
import com.google.common.collect.ImmutableMap;

import edu.emory.clir.clearnlp.utill.Concept;
import edu.emory.clir.clearnlp.utill.ConceptMention;
import edu.emory.clir.clearnlp.utill.Type;

public class Lingpipe {

	private static Chunker chunker;
	Concept c = null;
	public boolean initialize() throws ClassNotFoundException, IOException {

		try (ObjectInputStream ois = new ObjectInputStream(
				getClass().getResourceAsStream(new String("/ne-en-bio-genia.TokenShapeChunker")))) {
			chunker = (Chunker) ois.readObject();
			Streams.closeQuietly(ois);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return true;
	}

	public Concept getConcepts(String text) throws AnalysisEngineProcessException {

		Chunking chunking = chunker.chunk(text);
		chunking.chunkSet().stream().forEach(chunk -> {
			c = new Concept();
			c.setNames(Arrays.asList(text.substring(chunk.start(), chunk.end())));
			System.out.println(chunk.start() + ":" + chunk.end());
			c.setTypes(Arrays.asList(new Type("lingpipe:" + chunk.type(), "lingpipe:" + chunk.type())));
			c.setMentions(Arrays.asList(new ConceptMention(text.substring(chunk.start(), chunk.end()), 0.0)));
		});
		return c;
	}

	public static void main(String[] args) throws Exception {
		Lingpipe test = new Lingpipe();
		test.initialize();
		test.getConcepts("Which antibody is implicated in the Bickerstaff's Brainstem encephalitis?");
	}
	
	public static Concept getConcept(String question) throws ClassNotFoundException, IOException, AnalysisEngineProcessException{
		Lingpipe test = new Lingpipe();
		test.initialize();
		return test.getConcepts(question);
	}
}