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





public interface ConceptSearchProvider  {

  Optional<Concept> search(String string);

  default Optional<Concept> search( String string, String searchType) throws AnalysisEngineProcessException {
    return search( string, searchType, 1).stream().findFirst();
  }

  List<Concept> search(String string, String searchType, int hits)
          throws AnalysisEngineProcessException;

}
