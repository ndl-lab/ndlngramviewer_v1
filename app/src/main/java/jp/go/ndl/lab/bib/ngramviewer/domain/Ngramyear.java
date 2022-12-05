package jp.go.ndl.lab.bib.ngramviewer.domain;


import com.fasterxml.jackson.annotation.JsonInclude;

import jp.go.ndl.lab.bib.ngramviewer.infra.EsData;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Ngramyear  implements EsData{
	public String id;
    public Long version;
    
    public Ngramyear(String keyword, String normalizedkeyword,int count, String ngramyearjson,boolean confident) {
        this.ngramkeyword = keyword;
        this.normalizedkeyword=normalizedkeyword;
        this.ngramyearjson=ngramyearjson;
        this.count = count;
        this.confident=confident;
    }
    public Ngramyear(String keyword, String normalizedkeyword,int count, String ngramyearjson) {
        this.ngramkeyword = keyword;
        this.normalizedkeyword=normalizedkeyword;
        this.ngramyearjson=ngramyearjson;
        this.count = count;
        this.confident=true;
    }
    public Ngramyear(String keyword,String normalizedkeyword, int count) {
        this.ngramkeyword = keyword;
        this.normalizedkeyword=normalizedkeyword;
        this.ngramyearjson= "";
        this.count = count;
    }
    public Ngramyear() {
    }

    public String ngramkeyword;
    public String normalizedkeyword;
    public String ngramyearjson;
    public int count;
    public boolean confident;
}
