package jp.go.ndl.lab.bib.ngramviewer.batch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jp.go.ndl.lab.bib.ngramviewer.Application;
import jp.go.ndl.lab.bib.ngramviewer.tools.GenericEsClient;

@Slf4j
@Component("create-index")
@Profile({Application.MODE_BATCH})
@Lazy
public class CreateIndexBatch extends AbstractBatch {

    public static void main(String[] args) throws Throwable {
        Application.main(Application.MODE_BATCH, "create-index", "ng-ngramyear");
    }

    @Autowired
    private GenericEsClient esClient;
    private Path base = Paths.get("config", "index");

    @Override
    public void run(String[] params) {
        try {
            if (params[0].equals("all")) {
                createAllIndex();
            } else {
                createIndex(params[0], base.resolve(params[0] + ".json"), true);
            }
        } catch (Exception e) {
            log.error("{}", e);
        }

    }

    private void createAllIndex() throws IOException {
        try {
            log.info("try delete all");
            esClient.issueDelete("ng*");
        } catch (Exception e) {
            log.error("{}", e);
        }
        Files.list(base).forEach(path -> {
            try {
                createIndex(path.getFileName().toString().replace(".json", ""), path, false);
            } catch (Exception ex) {
                log.error("error {} {}", path);
                log.error("", ex);
            }
        });
    }

    private void createIndex(String indexName, Path file, boolean delete) {
        try {
            if (delete) {
                log.info("try delete {}", indexName);
                esClient.issueDelete(indexName);
            }
        } catch (Exception e) {
//            log.error("{}", e);
        }
        try {
            log.info("try create {}", indexName);
            String result = esClient.issue("PUT", indexName, file);
            log.info("{}->{}", file, result);
        } catch (Exception e) {
            log.error("{}", e);
        }
    }

}
