/*
 * Open Advancement Question Answering (OAQA) Project Copyright 2016 Carnegie Mellon University
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package pk.edu.kics.tmtool;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import pk.edu.kics.utill.Concept;
import pk.edu.kics.utill.ConceptMention;
import pk.edu.kics.utill.Type;

import static java.util.stream.Collectors.toList;

/**
 * A utility class for {@link PubAnnotation}, which includes
 * <ul>
 * <li>escaping and normalizing input text for sending {@link PubAnnotation}
 * object through the {@link TmToolConceptProvider} service via
 * {@link #normalizeText(String)},</li>
 * <li>constructing {@link PubAnnotation} data from texts in a batch via
 * {@link #convertTextsToPubAnnotations(List)},</li>
 * <li>and converting the returned
 * {@link edu.cmu.lti.oaqa.bioqa.providers.kb.PubAnnotation.Denotation}s to
 * {@link Concept}s via {@link #convertDenotationsToConcepts(JCas, Collection)}.
 * </li>
 * </ul>
 *
 * @see PubAnnotation
 * @see TmToolConceptProvider
 *
 * @author <a href="mailto:ziy@cs.cmu.edu">Zi Yang</a> created on 3/20/16.
 */
class PubAnnotationConvertUtil {

	// private static CharMatcher nonAscii = CharMatcher.ASCII.negate();

	private static Pattern diacriticalMarksPattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

	static String normalizeText(String text) {
		String ret = new String(text);
		// replace spaces, double-quotes, percentage, ® with spaces
		ret = ret.replaceAll("[\\s\"%®]", " ");
		// replace vbar with 'I' for "Deiodinase type || (DIO2)"
		ret = ret.replaceAll("\\|", "I");
		// replace multiplication mark '×' with 'x'
		ret = ret.replaceAll("×", "x");
		// sharp-s to beta
		ret = ret.replaceAll("ß", "β");
		// replace '·' with '.'
		ret = ret.replaceAll("·", ".");
		// remove '±' with '+'
		ret = ret.replaceAll("±", "+");
		// remove ending whitespaces
		ret = ret.replaceAll("\\s+$", "");
		// remove non ASCII characters
		// ret = nonAscii.replaceFrom(ret, ' ');
		// replace diacritical marks plus symbols that look alike, see
		// http://stackoverflow.com/questions/20674577/how-to-compare-unicode-characters-that-look-alike
		ret = Normalizer.normalize(ret, Normalizer.Form.NFKD);
		ret = diacriticalMarksPattern.matcher(ret).replaceAll("");
		return ret;
	}

	static PubAnnotation[] convertTextsToPubAnnotations(List<String> normalizedTexts) {
		return IntStream.range(0, normalizedTexts.size())
				.mapToObj(i -> new PubAnnotation("_DB_", String.valueOf(i), normalizedTexts.get(i)))
				.toArray(PubAnnotation[]::new);
	}

	static List<Concept> convertDenotationsToConcepts(String jcas, Collection<PubAnnotation.Denotation> denotations) {
		return denotations.stream().filter(denotation -> enclosedByText(jcas, denotation))
				.map(denotation -> convertDenotationToConcept(jcas, denotation)).collect(toList());
	}

	private static boolean enclosedByText(String question, PubAnnotation.Denotation denotation) {
		return denotation.getSpan().getBegin() < question.length();
	}

	public static Concept convertDenotationToConcept(String jcas, PubAnnotation.Denotation denotation) {

		String[] objSegs = denotation.getObj().split(":", 2);
		Concept c = new Concept();
		try {
			c.setTypes(Arrays.asList(new Type("tmtool:" + objSegs[0], "tmtool:" + objSegs[0])));
			c.setMentions(Arrays.asList(new ConceptMention(
					jcas.substring(denotation.getSpan().getBegin(), denotation.getSpan().getEnd()), 0.0)));
			// c.setmentions((Map<String,
			// Double>)ImmutableMap.<String,Double>builder().put(jcas.substring(denotation.getSpan().getBegin(),
			// denotation.getSpan().getEnd()),0.0).build());
			c.setNames(Arrays.asList(jcas.substring(denotation.getSpan().getBegin(), denotation.getSpan().getEnd())));
			c.setids(Arrays.asList(Collections.singletonList(objSegs[1]).get(0)));

		} catch (Exception ex) {
			ex.getMessage();
		}
		return c;
	}

	/*
	 * private static ConceptMention convertSpanToConceptMention(JCas jcas,
	 * PubAnnotation.Span span) { return TypeFactory.createConceptMention(jcas,
	 * span.getBegin(), span.getEnd()); }
	 */

	public static void main(String[] args) {
		String orig = "ΔBP: 12.3±";
		System.out.println((int) orig.charAt(9));
		String ret = Normalizer.normalize(orig, Normalizer.Form.NFD);
		System.out.println((int) ret.charAt(9));
	}

}
