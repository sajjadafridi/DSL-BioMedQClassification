package pk.edu.kics.utill;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import com.google.common.base.CharMatcher;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;

public class ConceptMerger {

	private static final String UUID_PREFIX = "__UUID__";

	@SuppressWarnings("deprecation")
	private static CharMatcher alphaNumeric = CharMatcher.JAVA_LETTER_OR_DIGIT;

	public static List<Concept> merge(List<Concept> concepts) {
		// preserve concept fields
		Set<String> uuids = new HashSet<>();
		SetMultimap<String, String> uuid2ids = HashMultimap.create();
		SetMultimap<String, String> uuid2names = HashMultimap.create();
		SetMultimap<String, String> uuid2uris = HashMultimap.create();
		SetMultimap<String, ConceptMention> uuid2mentions = HashMultimap.create();
		SetMultimap<String, List<String>> uuid2types = HashMultimap.create();
		for (Concept con : concepts) {
			String uuid = UUID_PREFIX + UUID.randomUUID().toString();
			uuids.add(uuid);
			uuid2ids.putAll(uuid, con.getIds());
			uuid2names.putAll(uuid, con.getNames());
			uuid2mentions.putAll(uuid, con.getMentions());
			// also remove duplicated concept type entries
			con.getTypes().forEach(type -> uuid2types.put(uuid, Concept.toTypeList(type)));
		}
		// connectivity detection for merging
		UndirectedGraph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
		uuids.forEach(graph::addVertex);
		uuid2ids.values().forEach(graph::addVertex);
		uuid2ids.entries().forEach(entry -> graph.addEdge(entry.getKey(), entry.getValue()));
		uuid2names.values().stream().map(ConceptMerger::nameKey).forEach(graph::addVertex);
		uuid2names.entries().forEach(entry -> graph.addEdge(entry.getKey(), nameKey(entry.getValue())));

		ConnectivityInspector<String, DefaultEdge> ci = new ConnectivityInspector<>(graph);
		Multiset<Integer> mergedSizes = HashMultiset.create();

		List<Concept> mergedConcepts = ci.connectedSets().stream().map(subgraph -> {
			Set<String> cuuids = subgraph.stream().filter(str -> str.startsWith(UUID_PREFIX)).collect(toSet());
			List<String> ids = cuuids.stream().map(uuid2ids::get).flatMap(Set::stream).filter(Objects::nonNull)
					.distinct().collect(toList());
			List<String> names = cuuids.stream().map(uuid2names::get).flatMap(Set::stream).filter(Objects::nonNull)
					.distinct().collect(toList());
			List<Type> types = cuuids.stream().map(uuid2types::get).flatMap(Set::stream).filter(Objects::nonNull)
					.distinct().map(type -> parseTypeList(type)).collect(toList());
			List<ConceptMention> mentions = cuuids.stream().map(uuid2mentions::get).flatMap(Set::stream)
					.filter(Objects::nonNull).collect(toList());
			mergedSizes.add(cuuids.size());
			Concept cp = new Concept();
			cp.setids(ids);
			cp.setNames(names);
			cp.setMentions(mentions);
			cp.setTypes(types);
			return cp;
		}).collect(toList());

		mergedConcepts.forEach(c -> c.getNames());
		// LOG.info("Merged concepts from {} concepts.", mergedSizes);
		// if (LOG.isDebugEnabled()) {
		// mergedConcepts.stream().map(TypeUtil::toString).forEachOrdered(c ->
		// LOG.debug(" - {}", c));

		return mergedConcepts;
	}

	private static String nameKey(String name) {
		return alphaNumeric.retainFrom(name.toLowerCase());
	}

	/*
	 * private static List<String> toTypeList(ConceptType type) { return
	 * Arrays.asList(type.getId(), type.getName(), type.getAbbreviation()); }
	 */

	private static Type parseTypeList(List<String> type) {
		return new Type(type.get(0), type.get(1));
	}

}
