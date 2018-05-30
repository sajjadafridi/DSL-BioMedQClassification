package pk.edu.kics.concept.search;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import gov.nih.nlm.uts.webservice.security.UtsFault_Exception;
import pk.edu.kics.utill.Concept;
import pk.edu.kics.utill.TypeUtil;

public class ConceptSearcher {

	//global variables under class
	int i=0;
	private ConceptSearchProvider conceptSearchProvider;
	private SynonymExpansionProvider synonymExpanisonProvider;
	private static final String NAME_NORMALIZATION = " \\(.*?\\)$| \\[.*?\\]$|\\*|\\^";
	public void initialiaze() throws UtsFault_Exception {
		conceptSearchProvider = new UtsConceptSearchProvider();
		synonymExpanisonProvider = new UtsSynonymExpansionProvider();
	}
	
	public List<Concept> ConceptSearch(List<Concept> concepts) {
		
		List<Concept> conceptList= new ArrayList<>();
		try
		{
		List<Concept> missingIdConcepts = concepts.stream().filter(concept -> concept.getids().isEmpty()).collect(toList());
		// retrieving IDs
		System.out.println("Retrieving IDs for " + missingIdConcepts.size() + " concepts.");
		for(;i<missingIdConcepts.size();i++) {
			Optional<Concept> response = conceptSearchProvider.search(missingIdConcepts.get(i).getConceptPreferredName());
			response.ifPresent(c -> {
				missingIdConcepts.set(i, TypeUtil.mergeConcept(missingIdConcepts.get(i), c));
			});   
		}
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
		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		return conceptList;
	}
}
