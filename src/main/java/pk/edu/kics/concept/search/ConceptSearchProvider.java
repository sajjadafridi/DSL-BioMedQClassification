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

package pk.edu.kics.concept.search;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import pk.edu.kics.utill.Concept;

import java.util.List;
import java.util.Optional;

/**
 * <p>
 * An implementation of this interface can look up a concept (given by its name) in an ontology, and
 * return the most relevant {@link Concept}.
 * </p>
 * <p>
 * In comparison, {@link ConceptProvider} is often used before a {@link ConceptSearchProvider},
 * which identifies the concepts from a plain text.
 * The concepts identified from {@link ConceptProvider} do not have to be existing entries in any
 * ontology, since some {@link ConceptProvider}s make "guess" based on their morphological
 * structures.
 * </p>
 *
 * @see ConceptProvider
 *
 * @author <a href="mailto:ziy@cs.cmu.edu">Zi Yang</a> created on 4/4/15
 */





public interface ConceptSearchProvider  {

  Optional<Concept> search(String string);

  default Optional<Concept> search( String string, String searchType) throws AnalysisEngineProcessException {
    return search( string, searchType, 1).stream().findFirst();
  }

  List<Concept> search(String string, String searchType, int hits)
          throws AnalysisEngineProcessException;

}
