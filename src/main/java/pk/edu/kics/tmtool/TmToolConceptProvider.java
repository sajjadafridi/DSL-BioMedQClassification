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

package pk.edu.kics.tmtool;

import com.google.common.collect.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import pk.edu.kics.utill.Concept;

import org.apache.http.HttpResponse;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import java.io.IOException;
import java.util.*;
import org.apache.commons.io.IOUtils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

public class TmToolConceptProvider {

	private static final String URL_PREFIX = "https://www.ncbi.nlm.nih.gov/CBBresearch/Lu/Demo/RESTful/tmTool.cgi/";

	protected Set<String> triggers = ImmutableSet.of("DNorm");

	private static HttpClientBuilder clientBuilder = HttpClientBuilder.create().disableRedirectHandling()
			.setServiceUnavailableRetryStrategy(new ServiceUnavailableRetryStrategy() {

				@Override
				public boolean retryRequest(HttpResponse response, int executionCount, HttpContext context) {
					int statusCode = response.getStatusLine().getStatusCode();
					return statusCode == 404 || statusCode == 501;
				}

				@Override
				public long getRetryInterval() {
					return 1000L;
				}
			});

	private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

	public void setTriggers(Set<String> triggers) {
		this.triggers = triggers;
	}

	// public List<Concept> getConcepts(ArrayList<String> question)
	// {
	// return getConcepts(Collections.singletonList(question));
	// }

	public List<Concept> getConcepts(List<String> question1) throws AnalysisEngineProcessException {
		// send request
		List<String> normalizedTexts = question1.stream().map(PubAnnotationConvertUtil::normalizeText)
				.collect(toList());
		ListMultimap<Integer, PubAnnotation.Denotation> index2denotations = Multimaps
				.synchronizedListMultimap(ArrayListMultimap.create());
		ExecutorService es = Executors.newCachedThreadPool();
		for (String trigger : triggers) {
			es.submit(() -> {
				try {
					List<String> denotationStrings = requestConcepts(normalizedTexts, trigger);
					assert denotationStrings.size() == question1.size();
					for (int i = 0; i < question1.size(); i++) {
						PubAnnotation.Denotation[] denotations = gson.fromJson(denotationStrings.get(i),
								PubAnnotation.Denotation[].class);
						index2denotations.putAll(i, Arrays.asList(denotations));
					}
				} catch (Exception e) {
					throw TmToolConceptProviderException.unknownException(trigger, e);
				}
			});
		}
		es.shutdown();
		try {
			boolean status = es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			if (!status) {
				throw new AnalysisEngineProcessException();
			}
		} catch (InterruptedException e) {
			throw new AnalysisEngineProcessException(e);
		}
		// convert denotation strings
		List<Concept> concepts = new ArrayList<>();
		for (int i = 0; i < question1.size(); i++) {
			String body = question1.get(i);
			List<PubAnnotation.Denotation> denotations = index2denotations.get(i);
			try {
				System.out.println(denotations.toString());
					concepts=PubAnnotationConvertUtil.convertDenotationsToConcepts(body, denotations);
				// concepts.addAll(PubAnnotationConvertUtil.convertDenotationsToConcepts(jcas,
				// denotations));
			} catch (StringIndexOutOfBoundsException e) {
				System.out.println(e.getMessage());
			}
		}
		return concepts;
	}

	protected List<String> requestConcepts(List<String> normalizedTexts, String trigger)
			throws AnalysisEngineProcessException {
		PubAnnotation[] inputs = PubAnnotationConvertUtil.convertTextsToPubAnnotations(normalizedTexts);
		String request = gson.toJson(inputs, PubAnnotation[].class);
		String response = null;
		try {
			response = submitText(trigger, request);
		} catch (IOException e) {
			System.out.println("System exception");
		}
		PubAnnotation[] outputs = gson.fromJson("[" + response + "]", PubAnnotation[].class);
		List<PubAnnotation> sortedOutputs = Arrays.stream(outputs)
				.sorted(Comparator.comparing(pa -> Integer.parseInt(pa.getSourceid()))).collect(toList());
		List<String> denotationStrings = sortedOutputs.stream().map(PubAnnotation::getDenotations).map(gson::toJson)
				.collect(toList());
		if (denotationStrings.size() != normalizedTexts.size()) {
			throw TmToolConceptProviderException.unequalVolume(trigger, normalizedTexts.size(),
					denotationStrings.size());
		}
		for (int i = 0; i < normalizedTexts.size(); i++) {
			String sentText = normalizedTexts.get(i);
			String recvText = PubAnnotationConvertUtil.normalizeText(sortedOutputs.get(i).getText());
			if (sentText.length() != recvText.length()) {
				throw TmToolConceptProviderException.unequalTextLength(trigger, sentText, recvText);
			}
			// if (sentText.equals(recvText)) {
			// throw TmToolConceptProviderException.textChanged(trigger, sentText,
			// recvText);
			// }
		}
		return denotationStrings;
	}

	private static String submitText(String trigger, String text) throws IOException {
		CloseableHttpClient client = clientBuilder.build();
		HttpPost post = new HttpPost(URL_PREFIX + trigger + "/Submit/");
		post.setEntity(new StringEntity(text));
		HttpResponse response = client.execute(post);
		String session = IOUtils.toString(response.getEntity().getContent());
		HttpGet get = new HttpGet(URL_PREFIX + session + "/Receive/");
		response = client.execute(get);
		return IOUtils.toString(response.getEntity().getContent());
	}

	public static void main(String args[]) throws AnalysisEngineProcessException {
		String s = "Which antibody is implicated in the Bickerstaff's Brainstem encephalitis?";
		List<String> myList = new ArrayList<String>(Arrays.asList(s.split(",")));
		TmToolConceptProvider test = new TmToolConceptProvider();
		test.getConcepts(myList);
	}
	
	public static List<Concept> getConcept(String question) throws AnalysisEngineProcessException {
		TmToolConceptProvider test = new TmToolConceptProvider();
		return test.getConcepts(Arrays.asList(question));
	}

}
