import * as Axios from "axios";
import * as Config from "config";
import { Ngramyear } from "domain/ngramyear";
const BASE_URL = Config.BASE_PATH + "api/";

/*export function search(s: string): Axios.AxiosPromise<Ngramyear[]> {
  let fd = new FormData();
  fd.append("bib", s);
  return Axios.default.post<Ngramyear[]>(BASE_URL + "search", fd);
}*/
export interface SearchResult<T> {
  list: T[];
  hit: number;
  from: number;
};

export function search(keywords: string,size:number=null,from:number=null,groupstr: string=null): Axios.AxiosPromise<SearchResult<Ngramyear>> {
  var resstring=BASE_URL + "search?keyword=" +keywords;
  if(size!==null){
    resstring+="&size="+size;
  }
  if(from!==null){
    resstring+="&from="+from;
  }
  if(groupstr!==null){
    resstring+="&groupstr="+groupstr;
  }
  return Axios.default.get<SearchResult<Ngramyear>>(encodeURI(resstring));
}
export function downloadurl(keywords: string,groupstr: string=null): string {
  var resstring=BASE_URL + "download?keyword=" +keywords+"&size=10000";
  if(groupstr!==null){
    resstring+="&groupstr="+groupstr;
  }
  return encodeURI(resstring);
}
