package pk.edu.kics.utill;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuestionUtil {

	private List<Concept> concepts;
	private List<Token> tokens;
	private String focus;
	private String body;
	private String type;
	private String id;
	
	public QuestionUtil(List<Concept> concepts, List<Token> tokens, String focus,String bo,String myId) {
		super();
		this.concepts = concepts;
		this.tokens = tokens;
		this.focus = focus;
		this.type="factoid";
		this.body=bo;
		this.id=myId;
	}
	
	public List<ConceptMention> getConceptMentions()
	{
		//return concepts.stream().map(concept -> getConceptMentions()).flatMap(List::stream).collect(Collectors.toList());
		
		
		
		List<ConceptMention>ls=new ArrayList<>();
		for (Concept cc : concepts) {
			
			
			ls.addAll(cc.getMentions());
			
			
		}
	
		return ls;
		
		
		
		
		
		
	}
	
	public List<Type> getConceptType()
	{
		return concepts.stream().map(types ->  
		{
			List<Type> type = types.getTypes();
			return type;
			
		}).flatMap(List::stream).collect(Collectors.toList());
		
	}

	public String getType() {
		return type;
	}



	public void setType(String type) {
		this.type = type;
	}



	public String getId() {
		return id;
	}



	public void setId(String id) {
		this.id = id;
	}



	public String getBody() {
		return body;
	}



	public void setBody(String body) {
		this.body = body;
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
	
	
}
