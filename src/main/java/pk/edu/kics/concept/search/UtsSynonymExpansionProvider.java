package pk.edu.kics.concept.search;

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

import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import gov.nih.nlm.uts.webservice.content.AtomDTO;
import gov.nih.nlm.uts.webservice.content.TermStringDTO;
import gov.nih.nlm.uts.webservice.content.UtsWsContentController;
import gov.nih.nlm.uts.webservice.content.UtsWsContentControllerImplService;
import gov.nih.nlm.uts.webservice.security.UtsFault_Exception;
import gov.nih.nlm.uts.webservice.security.UtsWsSecurityController;
import gov.nih.nlm.uts.webservice.security.UtsWsSecurityControllerImplService;

/**
 * This {@link SynonymExpansionProvider} uses <a href="https://uts.nlm.nih.gov/">UMLS Terminology
 * Service</a> to look up Concept IDs and identify the synonyms.
 * It uses the <a href="https://github.com/ziy/uts-api">UTS WSDL API 2.0</a>.
 *
 * @see UtsConceptSearchProvider
 * @see CachedUtsSynonymExpansionProvider
 *
 * @author <a href="mailto:ziy@cs.cmu.edu">Zi Yang</a> created on 4/20/15
 */
public class UtsSynonymExpansionProvider
        implements SynonymExpansionProvider {

  private String service;

  private String version;

  private String grantTicket;

  private UtsWsSecurityController securityService;

  private UtsWsContentController contentService;

  private int nthreads;

  private Integer timeout;

  private static final int MAX_RETRY = 5;

  public void initialize() {
	  this.service = "http://umlsks.nlm.nih.gov";
	    this.version = "2017AB";
	    securityService = (new UtsWsSecurityControllerImplService())
	            .getUtsWsSecurityControllerImplPort();
	    String username = "wasimbhalli";
	    String password = "UMLSM@ta1";
      try {
		grantTicket = securityService.getProxyGrantTicket(username, password);
	} catch (UtsFault_Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    contentService = (new UtsWsContentControllerImplService()).getUtsWsContentControllerImplPort();
    nthreads = 100;
    timeout=1000;
  }

  public UtsSynonymExpansionProvider() {
	  initialize();
  }

  public UtsSynonymExpansionProvider(String service, String version, String username,
          String password) throws gov.nih.nlm.uts.webservice.security.UtsFault_Exception {
    this.service = service;
    this.version = version;
    securityService = (new UtsWsSecurityControllerImplService())
            .getUtsWsSecurityControllerImplPort();
    grantTicket = securityService.getProxyGrantTicket(username, password);
    contentService = (new UtsWsContentControllerImplService()).getUtsWsContentControllerImplPort();
  }

  @Override
  public boolean accept(String id) {
    return id != null && id.startsWith("UMLS:");
  }

  @Override
  public Set<String> getSynonyms(String id){
    String umlsId = id.substring(5);
      try {
		return contentService
		          .getConceptAtoms(getSingleUseTicket(), version, umlsId, createContentPsf()).stream()
		          .map(AtomDTO::getTermString).map(TermStringDTO::getName).collect(toSet());
	} catch (gov.nih.nlm.uts.webservice.content.UtsFault_Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (UtsFault_Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return null;
  }

  @Override
  public Map<String, Set<String>> getSynonyms(Collection<String> ids){
    Map<String, Set<String>> id2synonyms = new ConcurrentHashMap<>();
    ExecutorService es = Executors.newFixedThreadPool(nthreads);
    ids.forEach(id -> {
      es.execute(() -> {
        try {
          id2synonyms.put(id, getSynonyms(id));
        } catch (Exception e) {
          e.printStackTrace();
        }
      } );
    } );
    es.shutdown();
      try {
		if (!es.awaitTermination(timeout, TimeUnit.MINUTES)) {
		   System.out.println("Timeout occurs for one or some concept retrieval services.");
		  }
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    return id2synonyms;
  }

  private gov.nih.nlm.uts.webservice.content.Psf createContentPsf() {
    gov.nih.nlm.uts.webservice.content.Psf psf = new gov.nih.nlm.uts.webservice.content.Psf();
    psf.setIncludedLanguage("ENG");
    psf.setPageLn(1000);
    return psf;
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

  public static void main(String[] args) throws Exception {
    UtsSynonymExpansionProvider service = new UtsSynonymExpansionProvider(args[0], args[1], args[2],
            args[3]);
    Set<String> synonyms = service.getSynonyms("UMLS:C1527336");
    synonyms.forEach(System.out::println);
  }

}
