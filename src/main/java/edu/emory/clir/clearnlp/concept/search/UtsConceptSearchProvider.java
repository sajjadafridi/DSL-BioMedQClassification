package edu.emory.clir.clearnlp.concept.search;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import edu.emory.clir.clearnlp.utill.Concept;
import edu.emory.clir.clearnlp.utill.Type;
import gov.nih.nlm.uts.webservice.content.ConceptDTO;
import gov.nih.nlm.uts.webservice.content.UtsWsContentController;
import gov.nih.nlm.uts.webservice.content.UtsWsContentControllerImplService;
import gov.nih.nlm.uts.webservice.finder.UiLabel;
import gov.nih.nlm.uts.webservice.finder.UtsWsFinderController;
import gov.nih.nlm.uts.webservice.finder.UtsWsFinderControllerImplService;
import gov.nih.nlm.uts.webservice.security.UtsFault_Exception;
import gov.nih.nlm.uts.webservice.security.UtsWsSecurityController;
import gov.nih.nlm.uts.webservice.security.UtsWsSecurityControllerImplService;
import gov.nih.nlm.uts.webservice.semnet.SemanticTypeDTO;
import gov.nih.nlm.uts.webservice.semnet.UtsWsSemanticNetworkController;
import gov.nih.nlm.uts.webservice.semnet.UtsWsSemanticNetworkControllerImplService;

/**
 * This {@link ConceptSearchProvider} uses <a href="https://uts.nlm.nih.gov/">UMLS Terminology
 * Service</a> to look up concept names and identify the {@link Concept}.
 * It uses the <a href="https://github.com/ziy/uts-api">UTS WSDL API 2.0</a> to try different
 * <tt>search type</tt>s in a fixed order: <tt>exact</tt>, <tt>words</tt>,
 * <tt>normalizedString</tt>.
 * If nothing is returned after all, a <tt>None</tt> will be returned.
 *
 * @see UtsSynonymExpansionProvider
 * @see CachedUtsConceptSearchProvider
 *
 * @author <a href="mailto:ziy@cs.cmu.edu">Zi Yang</a> created on 4/4/15
 */
public class UtsConceptSearchProvider implements ConceptSearchProvider {

  private String service;

  private String version;

  private String grantTicket;

  private UtsWsSecurityController securityService;

  private UtsWsFinderController finderService;

  private UtsWsContentController contentService;

  private UtsWsSemanticNetworkController semanticNetworkService;

  private static final String FINDER_TARGET = "atom";

  private static final int MAX_RETRY = 5;

  public void initialize() throws UtsFault_Exception {
    this.service = "http://umlsks.nlm.nih.gov";
    this.version = "2017AB";
    securityService = (new UtsWsSecurityControllerImplService())
            .getUtsWsSecurityControllerImplPort();
    String username = "wasimbhalli";
    String password = "UMLSM@ta1";
    
    grantTicket = securityService.getProxyGrantTicket(username, password);
    finderService = (new UtsWsFinderControllerImplService()).getUtsWsFinderControllerImplPort();
    contentService = (new UtsWsContentControllerImplService()).getUtsWsContentControllerImplPort();
    semanticNetworkService = (new UtsWsSemanticNetworkControllerImplService())
            .getUtsWsSemanticNetworkControllerImplPort();
  }

  public UtsConceptSearchProvider() throws UtsFault_Exception {
	  initialize();
  }

  public UtsConceptSearchProvider(String service, String version, String username, String password)
          throws gov.nih.nlm.uts.webservice.security.UtsFault_Exception {
    this.service = service;
    this.version = version;
    securityService = (new UtsWsSecurityControllerImplService())
            .getUtsWsSecurityControllerImplPort();
    grantTicket = securityService.getProxyGrantTicket(username, password);
    finderService = (new UtsWsFinderControllerImplService()).getUtsWsFinderControllerImplPort();
    contentService = (new UtsWsContentControllerImplService()).getUtsWsContentControllerImplPort();
    semanticNetworkService = (new UtsWsSemanticNetworkControllerImplService())
            .getUtsWsSemanticNetworkControllerImplPort();
  }

  @Override
  public Optional<Concept> search(String string){
    Optional<Concept> optional = null;
	try {
		optional = search(string, "exact");
    if (!optional.isPresent()) {
      int index = string.indexOf(" (");
      if (index > 0) {
        optional = search(string.substring(0, index), "words");
      }
    }
    if (!optional.isPresent()) {
      optional = search(string, "words");
    }
    if (!optional.isPresent()) {
      optional = search(string, "normalizedString");
    }
	} catch (AnalysisEngineProcessException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return optional;
  }

  @Override
  public List<Concept> search(String string, String searchType, int hits) {
    List<UiLabel> results = null;
      try {
		results = finderService.findConcepts(getSingleUseTicket(), version, FINDER_TARGET, string,
		          searchType, createFinderPsf(hits));
	} catch (gov.nih.nlm.uts.webservice.finder.UtsFault_Exception | UtsFault_Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    List<Concept> concepts = new ArrayList<>();
    for (UiLabel result : results) {
      ConceptDTO concept = null;
        try {
			concept = contentService.getConcept(getSingleUseTicket(), version, result.getUi());
		} catch (gov.nih.nlm.uts.webservice.content.UtsFault_Exception | UtsFault_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      List<Type> types = new ArrayList<>();
      for (String semanticTypeId : concept.getSemanticTypes()) {
        SemanticTypeDTO semType = null;
          try {
			semType = semanticNetworkService.getSemanticType(getSingleUseTicket(), version,
			          semanticTypeId);
		} catch (gov.nih.nlm.uts.webservice.semnet.UtsFault_Exception | UtsFault_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        types.add(new Type("umls:" + semType.getUi(), "umls:" + semType.getValue(),
                        "umls:" + semType.getAbbreviation()));
      }
      concepts.add(createConcept(concept.getDefaultPreferredName(),
              "UMLS:" + concept.getUi(), types));
    }
    return concepts;
  }

  private Concept createConcept(String name, String ids, List<Type> types) {
	  Concept concept=new Concept();
	  concept.setIds(Arrays.asList(ids));
	  concept.setNames(Arrays.asList(name));
	  concept.setTypes(types);
	  return concept;
  }
  private String getSingleUseTicket()
          throws gov.nih.nlm.uts.webservice.security.UtsFault_Exception {
    int retries = 0;
    while (true) {
      try {
        return securityService.getProxyTicket(grantTicket, service);
      } catch (gov.nih.nlm.uts.webservice.security.UtsFault_Exception e) {
        if (++retries == MAX_RETRY) throw e;
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e1) {
          e1.printStackTrace();
        }
      }
    }
  }

  private static gov.nih.nlm.uts.webservice.finder.Psf createFinderPsf(int hits) {
    gov.nih.nlm.uts.webservice.finder.Psf psf = new gov.nih.nlm.uts.webservice.finder.Psf();
    psf.setPageLn(hits);
    return psf;
  }

  public static void main(String[] args) throws Exception {
    /*UtsConceptSearchProvider service = new UtsConceptSearchProvider(args[0], args[1], args[2],
            args[3]);
    JCas jcas = JCasFactory.createJCas();
    Optional<Concept> concept = service.search(jcas, "NBEAL2", "words");
    if (concept.isPresent()) {
      System.out.println(TypeUtil.getConceptPreferredName(concept.get()) + " "
              + TypeUtil.getFirstConceptId(concept.get()));
      TypeUtil.getConceptTypes(concept.get())
              .stream().map(semanticType -> " - " + semanticType.getName() + " ["
                      + semanticType.getId() + ", " + semanticType.getAbbreviation() + "]")
              .forEach(System.out::println);*/
    }
}
