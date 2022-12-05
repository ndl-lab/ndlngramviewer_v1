package jp.go.ndl.lab.bib.ngramviewer.service;


import jp.go.ndl.lab.bib.ngramviewer.domain.Ngramyear;
import jp.go.ndl.lab.bib.ngramviewer.infra.EsDataStore;
import jp.go.ndl.lab.bib.ngramviewer.infra.EsDataStoreFactory;
import jp.go.ndl.lab.bib.ngramviewer.infra.EsSearchQuery;
import jp.go.ndl.lab.bib.ngramviewer.infra.EsSearchResult;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class NgramService {
	public final EsDataStore<Ngramyear> ngramStore;
    public NgramService(EsDataStoreFactory  storeFactory) {
    	ngramStore = storeFactory.build(Ngramyear.class);
    }
    @Autowired
    AnalyzeService as;
    public EsSearchResult<Ngramyear> search(EsSearchQuery q) {
    	SearchSourceBuilder ssb = new SearchSourceBuilder();
    	ssb.from(q.from == null ? 0 : q.from);
        ssb.size(q.size == null ? 200 : q.size);
        ssb.sort("count",SortOrder.DESC);
        BoolQueryBuilder kwq;
        for(int i=0;i<q.keyword.size();i++) {
        	q.keyword.set(i, as.normalizeString(q.keyword.get(i)));
        }
        if(q.enableregex) {
        	if(q.reverseregex) {
        		kwq= EsSearchQuery.createKeywordRegexQuery(q.keyword, Arrays.asList("normalizedkeyword"));
        	}else {
        		kwq= EsSearchQuery.createKeywordRegexQuery(q.keyword, Arrays.asList("ngramkeyword"));
        	}
        }else {
        	kwq= EsSearchQuery.createKeywordBoolQuery(q.keyword, Arrays.asList("ngramkeyword"));
        }
        ssb.query(kwq);
        ObjectMapper mapper = new ObjectMapper();
        EsSearchResult<Ngramyear> baseResult=ngramStore.search(ssb);
        if(q.groupid.size()>0) {
        	ArrayList<Ngramyear> tmphitlist=(ArrayList<Ngramyear>) baseResult.list;	
	        for(ArrayList<Integer>group:q.groupid) {
	        	ArrayList<String> mergekeywordlist=new ArrayList<String>();
	        	int mergecount=0;
	        	TreeMap<Integer, Integer>mergemap=new TreeMap<Integer, Integer>();
	        	for(int idx: group) {
	        		if(baseResult.list.size()>idx) {
	        			Ngramyear mergetarget=baseResult.list.get(idx);
	        			mergekeywordlist.add(mergetarget.ngramkeyword);
	        			try {
							TreeMap<Integer, Integer> tmpmap= mapper.readValue(mergetarget.ngramyearjson, new TypeReference<TreeMap<Integer, Integer>>(){});
							tmpmap.forEach((k, v) -> mergemap.merge(k, v, (v1, v2) -> v1 + v2));
						} catch (JsonMappingException e) {
							e.printStackTrace();
						} catch (JsonProcessingException e) {
							e.printStackTrace();
						}
	        			mergecount+=mergetarget.count;
	        		}
	        	}
	        	if(mergekeywordlist.size()>0) {
	        		String mergekeyword= String.join("=",mergekeywordlist);
	        		JSONObject ngramyearjson =  new JSONObject(mergemap);
	        		Ngramyear mergeng=new Ngramyear(mergekeyword,mergekeyword,mergecount,ngramyearjson.toString());
	        		tmphitlist.add(mergeng);
	        	}
	        }
	        Collections.sort( tmphitlist, new Comparator<Ngramyear>(){
	            @Override
	            public int compare(Ngramyear a, Ngramyear b){
	              return b.count - a.count;
	            }
	        });
	        baseResult.hit=tmphitlist.size();
	        baseResult.list=tmphitlist;
        }
        baseResult.from = q.from;
        return baseResult;
    }
}
