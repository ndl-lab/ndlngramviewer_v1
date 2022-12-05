package jp.go.ndl.lab.bib.ngramviewer.batch;

import jp.go.ndl.lab.bib.ngramviewer.Application;
import jp.go.ndl.lab.bib.ngramviewer.domain.Ngramyear;
import jp.go.ndl.lab.bib.ngramviewer.infra.EsBulkIndexer;
import jp.go.ndl.lab.bib.ngramviewer.service.AnalyzeService;
import jp.go.ndl.lab.bib.ngramviewer.service.NgramService;
import jp.go.ndl.lab.common.utils.IDUtils;
import jp.go.ndl.lab.common.utils.LabFileUtils;
import lombok.extern.slf4j.Slf4j;


import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map.Entry;

@Component(IndexFromGzipBatch.BATCH_NAME)
@Profile({Application.MODE_BATCH})
@Slf4j
@Lazy
public class IndexFromGzipBatch extends AbstractBatch{
	public static final String BATCH_NAME = "index-gzip";
	public static void main(String[] args) throws Throwable {
        Application.main(Application.MODE_BATCH, IndexFromGzipBatch.BATCH_NAME);
    }
	@Autowired
    private NgramService ns;
	@Autowired
	private AnalyzeService az;
	
	private static final int BULK_INDEX_SIZE = 50000;
	
	@Override
	public void run(String[] params) {
		ObjectMapper mapper = new ObjectMapper();
		Path gzippath= Paths.get(params[0]);
		int cnt=0;
		try (EsBulkIndexer ngramIndexer = new EsBulkIndexer(ns.ngramStore, BULK_INDEX_SIZE)) {
			try (LabFileUtils.DataReader reader = LabFileUtils.gDataReader(gzippath)) {
	            for (String[] data : reader) {
	            	cnt++;
	            	String keyString = data[0];
	                String json = data[2].replace("\'", "\"");
	                Map<Integer, Integer> kvp = null;
	                try {
	                	kvp=mapper.readValue(json, new TypeReference<Map<Integer, Integer>>(){});
	            	} catch (Exception e) {
	            		e.printStackTrace();
	            	}
	                if(kvp.size()==1||keyString.length()>15)continue;
	                String hashid=IDUtils.md5HashId(keyString);
		            String ngramkeyword=keyString;
		            String normalizedkeyword=keyString;//az.normalizeString(keyString);
		            Map<Integer, Integer> yearmap = new TreeMap<Integer, Integer>();
		            int sum=0;
		            for (Entry<Integer, Integer> entry :kvp.entrySet()) {
		            	int yearval=entry.getKey();
		            	int intval=entry.getValue();
		            	yearmap.put(yearval, intval);
		            	sum+=intval;
		            }
		            boolean confident=true;
		            if(sum<=3)continue;
		            else if(sum<=10)confident=false;
		            JSONObject ngramyearjson =  new JSONObject(yearmap);
		            Ngramyear nyear=new Ngramyear(ngramkeyword,normalizedkeyword,sum,ngramyearjson.toString(),confident);
		            ngramIndexer.add(hashid, mapper.writeValueAsString(nyear));
		            if(cnt%1000000==0) {
		            	log.info("count: {}",cnt);
		            }
	            }
			}
        } catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
	}
}
