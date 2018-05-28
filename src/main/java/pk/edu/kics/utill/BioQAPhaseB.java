package pk.edu.kics.utill;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BioQAPhaseB {

	@SuppressWarnings("unchecked")
	public static void main(String args[]) throws IOException, ParseException, org.json.simple.parser.ParseException {
		/*ArrayList<PhaseBQuestion> batchQuestions = new ArrayList<>();
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		BioQAPhaseB bioQAPhaseB = new BioQAPhaseB();
		batchQuestions.addAll(bioQAPhaseB.readTestFile("resource/classifier-data/questions2gold-lat.json"));*/
		
	}
	
	public static ArrayList<QuestionUtil> readQuestions() throws IOException, ParseException {
		ArrayList<QuestionUtil> batchQuestions = new ArrayList<>();
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		BioQAPhaseB bioQAPhaseB = new BioQAPhaseB();
		batchQuestions.addAll(bioQAPhaseB.readTestFile("resource/classifier-data/questions2gold-lat.json"));
		
		return batchQuestions;
	}

	public static void writeJsonFile(JSONObject questions) {
		try {
			File file = new File("questions2gold-lats.json");
			file.createNewFile();
			FileWriter fileWriter = new FileWriter(file);
			System.out.println("Writing JSON object to file");
			System.out.println("-----------------------");
			System.out.print(questions);
			fileWriter.write(questions.toString());
			fileWriter.flush();
			fileWriter.close();
		} catch (Exception ex) {
		}
	}

	public ArrayList<QuestionUtil> readTestFile(String path) throws IOException, ParseException {
		// symenticGroupType("resources\\Mappings\\symGroup2013.txt");
		String body = null, id = null, type = null;
		ArrayList<PhaseBQuestion> questions = new ArrayList<>();
		ArrayList<QuestionUtil> questionsBioasq=new ArrayList<>();

		try {
			PhaseBQuestion item = null;
			FileReader reader = new FileReader(path);
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
			JSONArray JSONQuestions = (JSONArray) jsonObject.get("questions");

			for (int i = 0; i < JSONQuestions.size(); i++) {
				item = new PhaseBQuestion();
				JSONObject questionObject = (JSONObject) JSONQuestions.get(i);
				if (questionObject.get("id") != null)
					id = questionObject.get("id").toString();
				if (questionObject.get("body") != null)
					body = (String) questionObject.get("body");
				if (questionObject.get("type") != null)
					type = (String) questionObject.get("type");
				if (questionObject.get("concepts") != null)
					item.setConcepts(getConcepts(questionObject.get("concepts")));

				QuestionUtil itemb=new QuestionUtil();
				item.setId(id);
				item.setBody(body);
				item.setType(type);
				
				itemb.setBody(item.getBody());
				itemb.setId(item.getId());
				itemb.setType(item.getType());
				
				item.setDocuments(getDocuments(questionObject.get("documents")));
				item.setSnippets(getSnippets(questionObject.get("snippets")));

				questions.add(item);
				questionsBioasq.add(itemb);
			}
		} catch (org.json.simple.parser.ParseException e) {
			System.err.println("Error Parsing the file");
		} catch (IOException ex) {
			System.err.println("Unable to find the file");
		}

		return questionsBioasq;
	}

	private ArrayList<String> getConcepts(Object conceptsObject)
			throws ParseException, org.json.simple.parser.ParseException {

		ArrayList<String> conceptsList = new ArrayList<String>();
		if (conceptsObject != null) {
			JSONParser parser = new JSONParser();
			JSONArray concepts = (JSONArray) parser.parse(conceptsObject.toString());
			for (int j = 0; j < concepts.size(); j++) {
				Object concept = concepts.get(j);
				if (concept != null) {
					conceptsList.add((String) concept);
				}
			}
		}

		return conceptsList;
	}

	private ArrayList<String> getDocuments(Object documentsObject)
			throws ParseException, org.json.simple.parser.ParseException {

		ArrayList<String> documentsList = new ArrayList<String>();
		if (documentsObject != null) {
			JSONParser parser = new JSONParser();
			JSONArray documents = (JSONArray) parser.parse(documentsObject.toString());
			for (int j = 0; j < documents.size(); j++) {
				Object concept = documents.get(j);
				if (concept != null) {
					documentsList.add((String) concept);
				}
			}
		}

		return documentsList;
	}

	private ArrayList<Snippet> getSnippets(Object snippetsObject)
			throws ParseException, FileNotFoundException, IOException, org.json.simple.parser.ParseException {

		ArrayList<Snippet> snippetList = new ArrayList<Snippet>();

		if (snippetsObject != null) {
			JSONParser parser = new JSONParser();
			JSONArray snippets = (JSONArray) parser.parse(snippetsObject.toString());
			for (int j = 0; j < snippets.size(); j++) {
				JSONObject snippet = (JSONObject) snippets.get(j);
				if (snippet != null) {
					Snippet snippy = new Snippet();
					snippy.beginSection = snippet.get("beginSection").toString();
					snippy.document = snippet.get("document").toString();
					snippy.endSection = snippet.get("endSection").toString();
					snippy.offsetInBeginSection = Integer.parseInt(snippet.get("offsetInBeginSection").toString());
					snippy.offsetInEndSection = Integer.parseInt(snippet.get("offsetInEndSection").toString());
					snippy.text = snippet.get("text").toString();
					snippetList.add(snippy);
				}
			}
		}

		return snippetList;
	}

}