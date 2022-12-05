package jp.go.ndl.lab.bib.ngramviewer.batch;

import jp.go.ndl.lab.bib.ngramviewer.Application;
import jp.go.ndl.lab.bib.ngramviewer.domain.Ngramyear;
import jp.go.ndl.lab.bib.ngramviewer.infra.EsBulkIndexer;
import jp.go.ndl.lab.bib.ngramviewer.service.AnalyzeService;
import jp.go.ndl.lab.bib.ngramviewer.service.NgramService;
import jp.go.ndl.lab.common.utils.IDUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.whitbeck.rdbparser.*;

@Component(ParseRDBBatch.BATCH_NAME)
@Profile({Application.MODE_BATCH})
@Lazy
public class ParseRDBBatch extends AbstractBatch{
	public static final String BATCH_NAME = "parse-rdb";
	public static void main(String[] args) throws Throwable {
        Application.main(Application.MODE_BATCH, ParseRDBBatch.BATCH_NAME);
    }
	@Autowired
    private NgramService ns;
	@Autowired
	private AnalyzeService az;
	
	private static final int BULK_INDEX_SIZE = 50000;
	
	@Override
	public void run(String[] params) {
		Path rdbpath= Paths.get(params[0]);
		try (EsBulkIndexer ngramIndexer = new EsBulkIndexer(ns.ngramStore, BULK_INDEX_SIZE)) {
			try (RdbParser parser = new RdbParser(rdbpath)) {
			      Entry e;
			      ObjectMapper mapper = new ObjectMapper();
			      while ((e = parser.readNext()) != null) {
				    	  switch (e.getType()) {
						          case SELECT_DB:
							            System.out.println("Processing DB: " + ((SelectDb)e).getId());
							            System.out.println("------------");
							            break;
						          case EOF:
							            System.out.print("End of file. Checksum: ");
							            for (byte b : ((Eof)e).getChecksum()) {
							            	System.out.print(String.format("%02x", b & 0xff));
							            }
							            System.out.println();
							            System.out.println("------------");
							            break;
						          case KEY_VALUE_PAIR:
							            KeyValuePair kvp = (KeyValuePair)e;
							            int freqcount=kvp.getFreq();
							            if(freqcount<4) break;
							            String keyString=new String(kvp.getKey(), Charset.forName("UTF-8"));
							            String hashid=IDUtils.md5HashId(keyString);
							            String ngramkeyword=keyString;
							            String analyzedkeyword=az.normalizeString(keyString);
							            Map<Integer, Integer> yearmap = new TreeMap<Integer, Integer>();
							            int cnt=0;
							            int yearval=0;
							            int sum=0;
							            for (byte[] val : kvp.getValues()) {
							            	String stval=new String(val, Charset.forName("UTF-8"));
							            	int intval=Integer.parseInt(stval);
							            	if(cnt%2==0) {
							            		yearval=intval;
							            	}else {
							            		sum+=intval;
							            		yearmap.put(yearval, intval);
							            	}
							            	cnt++;
							            }
							            if(yearmap.size()==1)break;
							            JSONObject ngramyearjson =  new JSONObject(yearmap);
							            //System.out.println(ngramyearjson);
							            Ngramyear nyear=new Ngramyear(ngramkeyword,analyzedkeyword,sum,ngramyearjson.toString());
							            ngramIndexer.add(hashid, mapper.writeValueAsString(nyear));
							            break;
							      default:
							    	  System.out.println("Not Found:"+e.getType());
						    }
			      }
			} catch (IOException e1) {
				e1.printStackTrace();
			}
        } catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
	}
}
