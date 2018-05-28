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

package pk.edu.kics.featureextractor;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gson.Gson;

import pk.edu.kics.utill.QuestionUtil;

public class AnswerTypeClassifierTrainer {
	
	private FeatureConstructorProvider featureConstructor;

	private ClassifierProvider classifier;

	private Map<String, Set<String>> qid2labels;

	private String cvPredictFile;

	private List<Map<String, Double>> trainX;

	private List<Collection<String>> trainY;

	private List<String> qids;

	private int limit;

	private static ClassifierProvider.ResampleType RESAMPLE_TYPE = ClassifierProvider.ResampleType.NONE;

	public AnswerTypeClassifierTrainer() {

		try {
			initialize();
		} catch (ResourceInitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void initialize() throws ResourceInitializationException {

		// feature constructor and classifier
		//featureConstructor = new FeatureConstructorProviderImpl();

		classifier = new LibLinearProvider();
		classifier.initialize();
		// labels for training instances
		String[] atGsLabelFiles = { "resource/classifier-data/4b-dev-gslabel-tmtool.json", "resource/classifier-data/4b-dev-gslabel-uts.json" };
		Gson gson = QuestionAnswerTypes.getGson();
		Collection<QuestionAnswerTypes> qats = Arrays.stream(atGsLabelFiles).map(atGsLabelFile -> {
				
			Reader reader = null;
			try {
				reader = new InputStreamReader(new FileInputStream(new File(atGsLabelFile)));
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return gson.fromJson(reader, QuestionAnswerTypes[].class);
		}).flatMap(Arrays::stream).collect(
				toMap(QuestionAnswerTypes::getQid, Function.identity(), QuestionAnswerTypes::addQuestionAnswerTypes))
				.values();
		boolean nullType = false;
		float typeRatioThreshold = 0.5f;
		qid2labels = qats.stream()
				.collect(toMap(QuestionAnswerTypes::getQid, qat -> qat.getTypeRatios(nullType).entrySet().stream()
						.filter(e -> e.getValue() >= typeRatioThreshold).map(Map.Entry::getKey).collect(toSet())));
		// /* question2labels = qats.stream().collect(toMap(QuestionAnswerTypes::getQid,
		// qat -> qat.getTypeRatios(nullType).entrySet().stream()
		// .filter(e -> e.getValue() >= typeRatioThreshold).map(Map.Entry::getKey)
		// .collect(toSet())
		// ));
		// */
		// // cv file
		cvPredictFile = "resource/cv-predict-file";
		trainX = new ArrayList<>();
		trainY = new ArrayList<>();
		if (cvPredictFile != null) {
			qids = new ArrayList<>();
		}
		limit = 1;
		featureConstructor = new FeatureConstructorProviderImpl();

	}

	public void process(ArrayList<QuestionUtil> question) throws AnalysisEngineProcessException {
		for(int i=0;i<10;i++) {
		
		Map<String, Double> features = featureConstructor.constructFeatures(question.get(i));
		trainX.add(features);
		String qid = question.get(i).getId();
		
		trainY.add(qid2labels.get(qid));
		if (cvPredictFile != null) {
			qids.add(qid);
		}

		System.out.println(features.toString());
		}    
	
	}
	
	public void collectionProcessComplete() throws AnalysisEngineProcessException {

		if (cvPredictFile != null) {
			try (BufferedWriter bw = Files.newWriter(new File(cvPredictFile), Charsets.UTF_8)) {
				Set<Double> f1s = new HashSet<>();
				List<List<String>> results = classifier.crossTrainPredictMultiLabel(trainX, trainY, RESAMPLE_TYPE,
						limit);
				for (int i = 0; i < qids.size(); i++) {
					String qid = qids.get(i);
					List<String> predLabels = results.get(i);
					// calculate f1
					Set<String> gsLabels = qid2labels.get(qid);
					f1s.add(2.0 * Sets.intersection(gsLabels, ImmutableSet.copyOf(predLabels)).size()
							/ (gsLabels.size() + predLabels.size()));
					// write to file
					bw.write(qid + "\t" + predLabels.stream().collect(joining(";")) + "\n");
				}
				f1s.stream().mapToDouble(Double::doubleValue).average()
						.ifPresent(f1 -> System.out.println("Micro F1: {}" + f1));
				bw.close();
			} catch (IOException e) {
				throw new AnalysisEngineProcessException(e);
			}
		}
		System.out.println("Train classifier");
		// changed CV to false, as a "micro f1" will be calculated if the cvPredictFile
		// is specifie
		classifier.trainMultiLabel(trainX, trainY, RESAMPLE_TYPE, false);
	}

}
