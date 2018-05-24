package edu.emory.clir.clearnlp.concept.search;

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

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import edu.emory.clir.clearnlp.utill.Concept;
import edu.emory.clir.clearnlp.utill.TypeUtil;
import gov.nih.nlm.uts.webservice.security.UtsFault_Exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link JCasAnnotator_ImplBase} assumes an input {@link JCas} contains a
 * collection of identified {@link Concept}s, but some of them have not been
 * associated with a {@link Concept} in a knowledge, i.e. no Concept ID or
 * synonym. The goal of this class is to use a {@link ConceptSearchProvider} to
 * look up the concept name, and a {@link SynonymExpansionProvider} to find the
 * synonyms of the concept.
 *
 * @author <a href="mailto:ziy@cs.cmu.edu">Zi Yang</a> created on 4/19/15
 */
public class ConceptSearcher {

	//global variables under class
	int i=0;
	private ConceptSearchProvider conceptSearchProvider;
	private SynonymExpansionProvider synonymExpanisonProvider;

	private static final String NAME_NORMALIZATION = " \\(.*?\\)$| \\[.*?\\]$|\\*|\\^";
	private static final Logger LOG = LoggerFactory.getLogger(ConceptSearcher.class);

	public void initialiaze() throws UtsFault_Exception {
		conceptSearchProvider = new UtsConceptSearchProvider();
		synonymExpanisonProvider = new UtsSynonymExpansionProvider();
	}
	
	public List<Concept> ConceptSearch(List<Concept> concepts) {
		
		List<Concept> conceptList= new ArrayList<>();
		List<Concept> missingIdConcepts = concepts.stream().filter(concept -> concept.getids().isEmpty()).collect(toList());
		// retrieving IDs
		System.out.println("Retrieving IDs for " + missingIdConcepts.size() + " concepts.");
		
		for(;i<missingIdConcepts.size();i++) {
			Optional<Concept> response = conceptSearchProvider.search(missingIdConcepts.get(i).getConceptPreferredName());
			response.ifPresent(c -> {
				missingIdConcepts.set(i, TypeUtil.mergeConcept(missingIdConcepts.get(i), c));
			});   
		}
		/*for (Concept concept : missingIdConcepts) {
			Optional<Concept> response = conceptSearchProvider.search(concept.getConceptPreferredName());
			Concept cm;
			response.ifPresent(c -> {
				cm=TypeUtil.mergeConcept(concept, c);
			});        
		}*/
		int j=0;
		for (int k = 0; k < concepts.size(); k++) {
			if(concepts.get(k).getids().isEmpty())
			{
				concepts.set(k, missingIdConcepts.get(j));
				j++;
			}
		}
		// retrieving synonyms (names)
		System.out.println("Retrieving synonyms for " + concepts.size() + " concepts.");
		Map<String, Concept> id2concept = new HashMap<>();
		for (Concept concept : concepts) {
			concept.getids().stream().filter(synonymExpanisonProvider::accept)
					.forEach(id -> id2concept.put(id, concept));
		}
		Map<String, Set<String>> id2synonyms = synonymExpanisonProvider.getSynonyms(id2concept.keySet());
		for (Map.Entry<String, Concept> entry : id2concept.entrySet()) {
			String id = entry.getKey();
			Concept concept = entry.getValue();
			List<String> names = Stream.concat(concept.getNames().stream(), id2synonyms.get(id).stream())
					.filter(Objects::nonNull).map(name -> name.replaceAll(NAME_NORMALIZATION, "")).distinct()
					.collect(toList());
			concept.setNames(names);
			conceptList.add(concept);
			}
		if (LOG.isDebugEnabled()) {
			// concepts.stream().map(TypeUtil::toString).forEachOrdered(c -> LOG.debug(" -
			// {}", c));
		}
		return conceptList;
	}
}
