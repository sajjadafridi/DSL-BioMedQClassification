package pk.edu.kics.utill;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class PhaseBQuestion {

	private String body;
	private List<String> documents;
	private List<String> concepts;
	private String type;
	private String id;
	private List<Snippet> snippets;

	public PhaseBQuestion() {
		documents = new ArrayList<>();
		concepts = new ArrayList<>();
		snippets = new ArrayList<>();
	}

	public void setConcepts(List<String> concepts) {
		this.concepts = concepts;
	}
	
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public List<String> getDocuments() {
		return documents;
	}

	public void setDocuments(List<String> documents) {
		this.documents = documents;
	}

	public List<String> getConcepts() {
		return concepts;
	}

	public void Concepts(List<String> concepts) {
		this.concepts = concepts;
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

	public List<Snippet> getSnippets() {
		return snippets;
	}
	public void setSnippets(ArrayList<Snippet> snippets) {
		this.snippets = snippets;
	}


}
