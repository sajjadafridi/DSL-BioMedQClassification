package pk.edu.kics.utill;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class IOHelper {
	public static Map<String, Set<String>> qid2labels;

	// read labeled questions

	public static void readLabeledQuestions(String[] atGsLabelFiles) {
		// labels for training instances
		Gson gson = Question.getGson();
		Collection<Question> qats = Arrays.stream(atGsLabelFiles).map(atGsLabelFile -> {

			Reader reader = null;
			try {
				reader = new InputStreamReader(new FileInputStream(new File(atGsLabelFile)));
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return gson.fromJson(reader, Question[].class);
		}).flatMap(Arrays::stream)
				.collect(toMap(Question::getQid, Function.identity(), Question::addQuestionAnswerTypes)).values();
		boolean nullType = false;
		float typeRatioThreshold = 0.5f;
		qid2labels = qats.stream().collect(toMap(Question::getQid, qat -> qat.getTypeRatios(nullType).entrySet()
				.stream().filter(e -> e.getValue() >= typeRatioThreshold).map(Map.Entry::getKey).collect(toSet())));

		/*
		 * Map<String, Object> question2labels =
		 * qats.stream().collect(toMap(QuestionAnswerTypes::getQid, qat ->
		 * qat.getTypeRatios(nullType).entrySet().stream() .filter(e -> e.getValue() >=
		 * typeRatioThreshold).map(Map.Entry::getKey) .collect(toSet()) ));
		 */

	}

	// read Golden Question Files
	public static ArrayList<Question> readGoldQuestions(String path) throws IOException, ParseException {
		ArrayList<Question> batchQuestions = new ArrayList<>();
		new GsonBuilder().disableHtmlEscaping().create();
		IOHelper ioHelper = new IOHelper();
		batchQuestions.addAll(ioHelper.readTestFile(path));
		return batchQuestions;
	}

	public ArrayList<Question> readTestFile(String path) throws IOException, ParseException {
		// symenticGroupType("resources\\Mappings\\symGroup2013.txt");
		String body = null, id = null, type = null;
		ArrayList<Question> questions = new ArrayList<>();

		try {
			Question item;
			FileReader reader = new FileReader(path);
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
			JSONArray JSONQuestions = (JSONArray) jsonObject.get("questions");
			int count=0;
			for (int i = 0; i < JSONQuestions.size(); i++) {
				item = new Question();
				JSONObject questionObject = (JSONObject) JSONQuestions.get(i);
				if (questionObject.get("id") != null)
					id = questionObject.get("id").toString();
				if (questionObject.get("body") != null)
					body = (String) questionObject.get("body");
				if (questionObject.get("type") != null) {
					String ty= (String) questionObject.get("type");
					item.setQid(id);
					item.setQuestion(body);
					item.setType(type);
					if ( ty.equals("factoid") || ty.equals("list"))
					{
						type = (String) questionObject.get("type");
						count++;
						questions.add(item);

					}
				}
				
			}
			//System.out.println(count);
		} catch (org.json.simple.parser.ParseException e) {
			System.err.println("Error Parsing the file");
		} catch (IOException ex) {
			System.err.println("Unable to find the file");
		}

		return questions;
	}

}
