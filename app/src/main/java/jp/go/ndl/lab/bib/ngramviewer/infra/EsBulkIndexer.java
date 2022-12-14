package jp.go.ndl.lab.bib.ngramviewer.infra;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class EsBulkIndexer implements Closeable {

    public enum Type {
        INDEX, DELETE;
    }

    private EsDataStore dataStore;
    private int bulkSize;
    private Type type;

    public EsBulkIndexer(EsDataStore dataStore, int bulkSize) {
        this(dataStore, bulkSize, Type.INDEX);
    }

    public EsBulkIndexer(EsDataStore dataStore, int bulkSize, Type type) {
        this.dataStore = dataStore;
        this.bulkSize = bulkSize;
        this.type = type;
    }

    private Map<String, String> map = new HashMap<>();

    public void add(String id, String json) {
        if (StringUtils.isBlank(id) || StringUtils.isBlank(json)) {
            log.warn("invalid input {} {}", id, json);
        } else {
            map.put(id, json);
            if (map.size() >= bulkSize) flush();
        }
    }

    public void add(String id) {
        map.put(id, null);
    }

    public void flush() {
        if (!map.isEmpty()) {
            if (type == Type.DELETE) {
                dataStore.bulkDelete(map.keySet());
            } else {
                dataStore.bulkIndex(map);
            }
            map.clear();
        }
    }

    @Override
    public void close() throws IOException {
        flush();
    }

}
