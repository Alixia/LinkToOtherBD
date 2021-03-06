package org.getalp.lexsema.translation;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.getalp.lexsema.util.Language;
import org.getalp.lexsema.util.dataitems.Pair;
import org.getalp.lexsema.util.dataitems.PairImpl;
import org.getalp.lexsema.util.rest.RestfulQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaiduAPITranslator implements Translator {
    private static Logger logger = LoggerFactory.getLogger(BaiduAPITranslator.class);

    private String key;

    public BaiduAPITranslator(String key) {
        this.key = key;
    }

    @Override
    public String translate(String source, Language sourceLanguage, Language targetLanguage) {
        List<Pair<String,String>> parameters = new ArrayList<>();
        parameters.add(new PairImpl<>("client_id", key));
        parameters.add(new PairImpl<>("from", sourceLanguage.getISO2Code()));
        parameters.add(new PairImpl<>("to", targetLanguage.getISO2Code()));
        parameters.add(new PairImpl<>("q", source));
        try {
            URLConnection urlConnection = RestfulQuery.restfulQuery("http://openapi.baidu.com/public/2.0/bmt/translate",parameters);
            String response = RestfulQuery.getRequestOutput(urlConnection);
            JsonObject object = JSON.parse(response);
            response = object.get("trans_result").getAsArray().get(0).getAsObject().get("dst").getAsString().value();
            return response;
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage());
        }
        return "";
    }

    @Override   
    public void close() {

    }

}
