package pk.edu.kics.utill;

import static java.util.stream.Collectors.toMap;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.SerializedName;

public class Question {

	private List<Concept> concepts;
	private List<Token> tokens;
	private String focus;
	private String type;

	// used for gson serialization
	private String question;
	private String qid;
	boolean questionb = false;

	public Question(List<Concept> concepts, List<Token> tokens, String focus, String body, String myId) {
		this.concepts = concepts;
		this.tokens = tokens;
		this.focus = focus;
		this.type = "factoid";
		this.question = body;
		this.qid = myId;
	}

	public Question() {
	}

	public List<ConceptMention> getConceptMentions() {
		List<ConceptMention> ls = new ArrayList<>();
		for (Concept cc : concepts) {
			ls.addAll(cc.getMentions());
		}

		return ls;
	}

	public List<SemanticType> getConceptType() {
		return concepts.stream().map(types -> {
			List<SemanticType> semanticType = types.getTypes();
			return semanticType;

		}).flatMap(List::stream).collect(Collectors.toList());

	}

	
	public void setQuestion(String question) {
		this.question = question;
	}

	public void setQid(String qid) {
		this.qid = qid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Concept> getConcepts() {
		return concepts;
	}

	public void setConcepts(List<Concept> concepts) {
		this.concepts = concepts;
	}

	public List<Token> getTokens() {
		return tokens;
	}

	public void setTokens(List<Token> tokens) {
		this.tokens = tokens;
	}

	public String getFocus() {
		return focus;
	}

	public void setFocus(String focus) {
		this.focus = focus;
	}

	@SerializedName("answer-types")
	private Map<String, Set<String>> answerTypes = new HashMap<>();

	@SerializedName("type-counts")
	private Multiset<String> typeCounts = HashMultiset.create();

	Question(String myqid, String question) {
		this.question = question;
		this.qid = myqid;
	}

	public void addAnswerType(String answer, String type) {
		if (!answerTypes.containsKey(answer)) {
			answerTypes.put(answer, new HashSet<>());
		}
		answerTypes.get(answer).add(type);
		typeCounts.add(type);
	}

	public void addAnswerTypes(String answer, Set<String> types) {
		answerTypes.put(answer, types);
		typeCounts.addAll(types);
	}

	public Question addQuestionAnswerTypes(Question qat) {
		answerTypes.putAll(qat.answerTypes);
		typeCounts.addAll(qat.typeCounts);
		return this;
	}

	public Map<String, Double> getTypeRatios(boolean nullType) {
		double answerCount = answerTypes.size();
		double nullCount = answerTypes.values().stream().filter(Set::isEmpty).count();
		if (nullType) {
			Map<String, Double> typeRatios = typeCounts.entrySet().stream()
					.collect(toMap(Multiset.Entry::getElement, e -> e.getCount() / answerCount));
			typeRatios.put("null", nullCount / answerCount);
			return typeRatios;
		} else {
			Map<String, Double> typeRatios = typeCounts.entrySet().stream()
					.collect(toMap(Multiset.Entry::getElement, e -> e.getCount() / (answerCount - nullCount)));
			return typeRatios;
		}
	}

	public String getQid() {
		return this.qid;
	}

	public String getQuestion() {
		return question;
	}

	public Set<String> getAnswers() {
		return answerTypes.keySet();
	}

	private static class TypeCountsSerializer implements JsonSerializer<Multiset<String>> {

		@Override
		public JsonElement serialize(Multiset<String> src, Type typeOfSrc, JsonSerializationContext context) {
			Map<String, Integer> type2count = src.entrySet().stream()
					.collect(toMap(Multiset.Entry::getElement, Multiset.Entry::getCount));
			return context.serialize(type2count);
		}
	}

	private static class TypeCountsDeserializer implements JsonDeserializer<Multiset<String>> {

		@Override
		public Multiset<String> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			Map<String, Double> type2count = context.deserialize(json, Map.class);
			Multiset<String> ret = HashMultiset.create();
			type2count.entrySet().forEach(entry -> ret.setCount(entry.getKey(), entry.getValue().intValue()));
			return ret;
		}
	}

	public static Gson getGson() {
		return new GsonBuilder().registerTypeAdapter(Multiset.class, new TypeCountsSerializer())
				.registerTypeAdapter(Multiset.class, new TypeCountsDeserializer()).setPrettyPrinting().create();
	}

}
