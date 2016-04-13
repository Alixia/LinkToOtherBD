package org.getalp.lexsema.ws.wsdforsmt;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.getalp.lexsema.io.resource.dictionary.DictionaryLRLoader;
import org.getalp.lexsema.similarity.Document;
import org.getalp.lexsema.similarity.DocumentImpl;
import org.getalp.lexsema.similarity.Sense;
import org.getalp.lexsema.similarity.Word;
import org.getalp.lexsema.similarity.WordImpl;
import org.getalp.lexsema.similarity.measures.lesk.IndexedLeskSimilarity;
import org.getalp.lexsema.ws.core.WebServiceServlet;
import org.getalp.lexsema.wsd.configuration.Configuration;
import org.getalp.lexsema.wsd.method.Disambiguator;
import org.getalp.lexsema.wsd.method.MultiThreadCuckooSearch;
import org.getalp.lexsema.wsd.score.ConfigurationScorer;
import org.getalp.lexsema.wsd.score.ConfigurationScorerWithCache;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.ISenseEntry;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class WSDForSMTWebService1  extends WebServiceServlet
{
    private static StanfordCoreNLP stanford = null;
    
    private static DictionaryLRLoader dictionary = null;
    
    private static Dictionary wordnet = null;
    
    private static Disambiguator disambiguator = null;
    
    protected void handle(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        setHeaders(request, response);
        loadStanford();
        loadDictionary();
        loadWordnet();
        loadDisambiguator();
        
        String rawText = request.getParameter("input");

        System.out.println("Got the following input of size " + rawText.length() + " characters:");
        System.out.println(rawText);

        System.out.println("Parsing input...");
        Document txt = rawToText(rawText);
        System.out.println("Parsed " + txt.size() + " words");

        System.out.println("Loading senses...");
        dictionary.loadSenses(txt);

        System.out.println("Disambiguating...");
        Configuration c = disambiguator.disambiguate(txt);
        disambiguator.release();

        String[] outputArray = new String[c.size()];
        for (int i = 0 ; i < c.size() ; i++) 
        {
            int assignment = c.getAssignment(i);
            List<Sense> senses =txt.getSenses(i);
            if (assignment < 0 || assignment >= senses.size()) 
            {
                outputArray[i] = "0";
            } 
            else 
            {
                String senseID = senses.get(assignment).getId();
                String offset = getWordnetOffset(senseID);
                if (offset.equals("0") && senseID.contains("%5"))
                {
                    senseID = senseID.replace("%5", "%3");
                    offset = getWordnetOffset(senseID);
                }
                if (offset.equals("0") && senseID.contains("%3"))
                {
                    senseID = senseID.replace("%3", "%5");
                    offset = getWordnetOffset(senseID);
                }
                outputArray[i] = offset;
                if (offset.equals("0"))
                {
                    System.out.println("Warning : no sense found for " + senseID);
                }
            }
            System.out.println("Word " + i + " : \"" + txt.getWord(i).getSurfaceForm() + "\" {" + txt.getWord(i).getLemma() + "%" + txt.getWord(i).getPartOfSpeech() + "} [" + outputArray[i] + "]");
        }
        String output = Arrays.toString(outputArray);
        System.out.println("Writing output of size " + output.length() + "...");
        response.getWriter().print(output);
        response.getWriter().close();
    }

    private String getWordnetOffset(String senseID)
    {
        Iterator<ISenseEntry> senseIterator = wordnet.getSenseEntryIterator();
        while (senseIterator.hasNext())
        {
            ISenseEntry sense = senseIterator.next();
            if (sense.getSenseKey().toString().equals(senseID)) 
            {
                return String.format("%08d", sense.getOffset()) + sense.getPOS().getTag();
            }
        }
        return "0";
    }
    
    private void setHeaders(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException
    {        
        request.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "*");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
    }

    private static synchronized void loadStanford()
    {
        if (stanford != null) return;
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        props.put("tokenize.options", "untokenizable=allKeep");
        stanford = new StanfordCoreNLP(props);
    }

    private static synchronized void loadDictionary() throws Exception
    {
        if (dictionary != null) return;
        dictionary = new DictionaryLRLoader(new FileInputStream("/home/viall/current/data/lesk_dict/all/dict_all_stopwords_stemming_semcor_wordnetglosstag_250"), true);
    }

    private static synchronized void loadWordnet() throws Exception
    {
        if (wordnet != null) return;
        wordnet = new Dictionary(new File("/home/viall/current/data/wordnet/3.0/dict"));
        wordnet.open();
    }
    
    private static synchronized void loadDisambiguator()
    {
        if (disambiguator != null) return;
        ConfigurationScorer scorer = new ConfigurationScorerWithCache(new IndexedLeskSimilarity());
        int iterations = 100000;
        double minLevyLocation = 1;
        double maxLevyLocation = 5;
        double minLevyScale = 0.5;
        double maxLevyScale = 1.5;
        disambiguator = new MultiThreadCuckooSearch(iterations, minLevyLocation, maxLevyLocation, minLevyScale, maxLevyScale, scorer, false);    
    }

    private static Document rawToText(String raw)
    {
        Document txt = new DocumentImpl();
        Annotation document = new Annotation(raw);
        stanford.annotate(document);
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences) 
        {
            for (CoreLabel token: sentence.get(TokensAnnotation.class))
            {
                String lemma = token.getString(LemmaAnnotation.class);
                String surfaceForm = token.originalText();
                String pos = token.getString(PartOfSpeechAnnotation.class);
                Word word = new WordImpl("", lemma, surfaceForm, pos);
                txt.addWord(word);
            }
        }
        return txt;
    }

}