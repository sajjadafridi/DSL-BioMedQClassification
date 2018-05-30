package pk.edu.kics.utill;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Concept implements Serializable  {
	
	private static final long serialVersionUID = -1297329388717145620L;
	private List<String> names;
	private List<String> ids;
	private List<SemanticType> semanticTypes;
	private List<ConceptMention> mentions;

	public Concept() {
		super();
		names = new ArrayList<>();
		ids = new ArrayList<>();
		semanticTypes = new ArrayList<>();
		mentions = new ArrayList<>();
	}

	public List<String> getNames() {
		return names;
	}

	public void setNames(List<String> names) {
		this.names = names;
	}

	public List<String> getids() {
		return ids;
	}

	public void setids(List<String> ids) {
		this.ids = ids;
	}

	public List<SemanticType> getTypes() {
		return semanticTypes;
	}

	public void setTypes(List<SemanticType> semanticTypes) {
		this.semanticTypes = semanticTypes;
	}

	public List<String> getIds() {
		return ids;
	}

	public void setIds(List<String> ids) {
		this.ids = ids;
	}

	public List<ConceptMention> getMentions() {
		return mentions;
	}

	public void setMentions(List<ConceptMention> mentions) {
		this.mentions = mentions;
	}
	
	public static List<String> toTypeList(SemanticType semanticType){
		return Arrays.asList(semanticType.getName(),semanticType.getAbb());
	}
	
	public String getConceptPreferredName() {
		return names.get(0);
	}

}
