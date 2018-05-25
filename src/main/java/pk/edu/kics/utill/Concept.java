package pk.edu.kics.utill;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Concept implements Serializable  {
	
	private static final long serialVersionUID = -1297329388717145620L;
	private List<String> names;
	private List<String> ids;
	private List<Type> types;
	private List<ConceptMention> mentions;

	public Concept() {
		super();
		names = new ArrayList<>();
		ids = new ArrayList<>();
		types = new ArrayList<>();
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

	public List<Type> getTypes() {
		return types;
	}

	public void setTypes(List<Type> types) {
		this.types = types;
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
	
	public static List<String> toTypeList(Type type){
		return Arrays.asList(type.getName(),type.getAbb());
	}
	
	public String getConceptPreferredName() {
		return names.get(0);
	}

}
