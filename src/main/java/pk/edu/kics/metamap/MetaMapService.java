package pk.edu.kics.metamap;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;

import gov.nih.nlm.nls.skr.GenericObject;
import pk.edu.kics.metamap.MetaMapObject.Candidate;
import pk.edu.kics.metamap.MetaMapObject.Mapping;
import pk.edu.kics.metamap.MetaMapObject.Phrase;
import pk.edu.kics.metamap.MetaMapObject.Utterance;
import pk.edu.kics.utill.Concept;
import pk.edu.kics.utill.ConceptMention;
import pk.edu.kics.utill.Type;

public class MetaMapService {

	private GenericObject conf;
	private XMLInputFactory xmlInputFactory;
	private Transformer transformer;
	private Unmarshaller unmarshaller;
	
	
	@SuppressWarnings("restriction")
	MetaMapService(String version, String username, String password, String email, boolean silentOnError,
			int priority) {
		conf = createConf(version, username, password, email, false, 0);
		xmlInputFactory = XMLInputFactory.newFactory();
		try {
			transformer = new TransformerFactoryImpl().newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "no");
			unmarshaller = JAXBContext.newInstance(MetaMapObject.class).createUnmarshaller();
		} catch (TransformerConfigurationException | JAXBException e) {
		}
	}

	private static GenericObject createConf(String version, String username, String password, String email,
			boolean silentOnError, int priority) {
		GenericObject conf = new GenericObject(username, password);
		conf.setField("Email_Address", email);
		conf.setField("Batch_Command", "metamap -V USAbase -L 14 -Z " + version + " -E -Av --XMLf");
		conf.setField("SilentEmail", true);
		conf.setField("ESilent", silentOnError);
		conf.setField("RPriority", Integer.toString(priority));
		return conf;
	}

	public List<MetaMapObject> getConcepts(String question) throws JAXBException, IOException {
		List<String> mmoStrings = requestConcepts(question);
		List<MetaMapObject> concepts = new ArrayList<>();
		for (String metaMapObject : mmoStrings) {

			StringReader mmoStringReader = new StringReader(metaMapObject);
			try {
				MetaMapObject mmo = (MetaMapObject) unmarshaller.unmarshal(mmoStringReader);
				concepts.add(mmo);
			} catch (JAXBException e) {

			}
		}
		return concepts;
	}

	@SuppressWarnings("deprecation")
	protected List<String> requestConcepts(String texts) {
		File file = null;
		try {
			file = File.createTempFile("metamap-", ".input");
		} catch (IOException e) {
		}
		try {
			Files.write(formatBody(texts), file, Charsets.UTF_8);
		} catch (IOException e) {
		}
		conf.setFileField("UpLoad_File", file.toString());

		System.out.println("Request ready for {} inputs.\t" + texts);
		String response = conf.handleSubmission();
		System.out.println("\n\n");
		file.deleteOnExit();
		System.out.println("Response received.");
		List<String> mmoStrings = null;
		try {
			mmoStrings = splitResponseByMMO(response);
		} catch (Exception e) {
			System.out.println("Returned: {}" + response);
		}
		return mmoStrings;
	}

	private List<String> splitResponseByMMO(String response) throws XMLStreamException, TransformerException {
		XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(new StringReader(response));
		while (!reader.hasName() || !"MMOs".equals(reader.getLocalName())) {
			reader.next();
		}
		List<String> mmoStrings = new ArrayList<>();
		while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
			StringWriter buffer = new StringWriter();
			transformer.transform(new StAXSource(reader), new StreamResult(buffer));
			mmoStrings.add(buffer.toString());
		}
		return mmoStrings;
	}

	 private static String formatBody(String text) {
	 return text.trim().replaceAll("\\s", " ").replaceAll("–",
	 "-").replaceAll("’", "'").replaceAll("[^\\p{ASCII}]",
	 " ");
	 }

	public static List<Concept> getConcept(String question) throws JAXBException, IOException {

		MetaMapService metamap = new MetaMapService("2017AA", "wasimbhalli", "UMLSM@ta1", "cmwasim@gmail.com", false,
				0);
		List<MetaMapObject> list = metamap.getConcepts(question);
		List<Concept> concepts = new ArrayList<>();
		for (MetaMapObject mmo : list) {
			List<Utterance> us = mmo.getUtterances();
			for (Utterance uss : us) {
				List<Phrase> ps = uss.getPhrases();
				for (Phrase pss : ps) {
					List<Mapping> ms = pss.getMappings();
					for (Mapping mss : ms) {
						List<Candidate> cs = mss.getMappingCandidates();
						for (Candidate css : cs) {
							// create concept
							Concept c = new Concept();
							c.setNames(Arrays.asList(css.getCandidatePreferred(), css.getCandidateMatched()));
							c.setMentions(Arrays.asList(
									new ConceptMention(css.getCandidateMatched(), css.getCandidateScore() / -1000.0)));
							c.setids(Arrays.asList("UMLS:" + css.getCandidateCUI()));
							List<Type> semTypes = new ArrayList<>();
							for (String semType : css.getSemTypes()) {
								semTypes.add(new Type("umls:" + semType, "umls:" + semType));
							}
							c.setTypes(semTypes);
							concepts.add(c);
						}
					}
				}
			}

		}
		return concepts;
	}

	public static void main(String args[]) throws JAXBException, IOException {
		getConcept("What is Cancer?");
	}

}
