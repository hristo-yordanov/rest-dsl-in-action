package com.redhat.gpe.route;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redhat.gpe.model.Blog;
import org.apache.camel.*;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.PlainActionFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

public class ElasticSearchService {

    final static Logger LOG = LoggerFactory.getLogger(ElasticSearchService.class);

    public IndexRequest add(@Body Blog body,
                            @Header("indexname") String indexname,
                            @Header("indextype") String indextype,
                            @Header("id") String id) {

        String source = new Gson().toJson(body);
        LOG.info("Id : " + id + ", indexname : " + indexname + ", indextype : " + indextype);
        LOG.info("Source : " + source);

        IndexRequest req = new IndexRequest(indexname, indextype, id);
        req.source(source);
        return req;
    }

    public String findById(@Header("id") String id) {
        return id;
    }

    public String generateResponse(@Body PlainActionFuture future) throws Exception {
        GetResponse getResponse = (GetResponse) future.get();
        String response = getResponse.getSourceAsString();
        if (response == null) {
            LOG.info("No result found for the id - " + getResponse.getId());
            response = emptyFieldsJson("user","title","body","postDate");
        }
        return response;
    }

    /**
     * Generate JSON String using fields passed as parameter and assign the content to an empty string*
     */
    public static String emptyFieldsJson(String... fields) {

        final String DQ = "\"";
        final String DQCOLONDQ = "\": \"";
        final String DQCOMA = "\", ";
        final String PREFIX = "{";
        final String SUFFIX = "}";

        StringBuffer buffer = new StringBuffer();
        buffer.append(PREFIX);

        for (String field : fields) {
            buffer.append(DQ);
            buffer.append(field);
            buffer.append(DQCOLONDQ);
            buffer.append(DQCOMA);
        }
        
        // Remove last coma added
        buffer.setLength(buffer.length() - 2);

        buffer.append(SUFFIX);
        return buffer.toString();
    }

    /**
     * Convert JSON string to pretty print version
     *
     * @param jsonString
     * @return
     */
    public static String toPrettyFormat(String jsonString) {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonString).getAsJsonObject();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }

}