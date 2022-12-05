package jp.go.ndl.lab.bib.ngramviewer.service;

import java.util.List;

import org.elasticsearch.client.indices.AnalyzeRequest;
import org.springframework.stereotype.Service;

import jp.go.ndl.lab.bib.ngramviewer.domain.Ngramyear;
import jp.go.ndl.lab.bib.ngramviewer.infra.EsDataStore;
import jp.go.ndl.lab.bib.ngramviewer.infra.EsDataStoreFactory;

@Service
public class AnalyzeService {
	public final EsDataStore<Ngramyear> ngramyearStore;
	public AnalyzeService(EsDataStoreFactory storeFactory) {
		 	ngramyearStore= storeFactory.build(Ngramyear.class);
    }
	 public String normalizeString(String querystr) {
			AnalyzeRequest request = AnalyzeRequest.buildCustomAnalyzer("ng-ngramyear","whitespace")
					.addCharFilter("normalize_char_filter") 
	    		    .build(querystr);
			List<String> response=ngramyearStore.normalize(request);
	    	return response.get(0);
	 }
}
