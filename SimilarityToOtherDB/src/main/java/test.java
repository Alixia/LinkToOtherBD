import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.tdb.TDBFactory;
import org.getalp.lexsema.similarity.measures.SimilarityMeasure;
import org.getalp.lexsema.similarity.measures.tverski.TverskiIndexSimilarityMeasureBuilder;

import java.io.*;

public class test {

    public static String fileTDB = "/home/bouzigual/cours/M1/StageM1/Langue/dbnary_eng_20170522";
    public static String fileWord = "/home/bouzigual/cours/M1/StageM1/Langue/engWordnet";
    public static String stopWords = "/home/bouzigual/cours/M1/StageM1/Langue/stopWordEng.txt";

    public static void creationDefinition() {
        DBComparaison c = new DBComparaison(fileTDB, fileWord);


        String queryWordDefDBNary = "SELECT distinct ?d \n" +
                "WHERE { \n" +
                " ?p dbnary:describes ?l. \n" +
                " ?l ontolex:sense ?s; dcterms:language lexvo:eng ; ontolex:canonicalForm/ontolex:writtenRep ?w@en. \n" +
                " ?s skos:definition/rdf:value ?d. \n" +
                " } order by ?p ";

        String queryWordDefWordnet = " SELECT distinct ?d \n" +
                "WHERE { \n" +
                " ?p rdfs:label ?w@eng. \n" +
                " ?p wordnet-ontology:gloss ?d \n" +
                " } order by ?p ";

        ParameterizedSparqlString DB1 = new ParameterizedSparqlString();
        DB1.setNsPrefix("eng", "http://kaiko.getalp.org/dbnary/eng/");
        DB1.setNsPrefix("dbnary", "http://kaiko.getalp.org/dbnary#");
        DB1.setNsPrefix("ontolex", "http://www.w3.org/ns/lemon/ontolex#");
        DB1.setNsPrefix("dcterms", "http://purl.org/dc/terms/");
        DB1.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
        DB1.setNsPrefix("lexvo", "http://lexvo.org/id/iso639-3/");
        DB1.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        DB1.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        DB1.setNsPrefix("olia", "http://purl.org/olia/olia.owl#");
        DB1.setNsPrefix("vartrans", "http://www.w3.org/ns/lemon/vartrans#");
        DB1.setNsPrefix("lime", "http://www.w3.org/ns/lemon/lime#");
        DB1.setNsPrefix("synsem", "http://www.w3.org/ns/lemon/synsem#");
        DB1.setNsPrefix("lexinfo", "http://www.lexinfo.net/ontology/2.0/lexinfo#");
        DB1.setNsPrefix("decomp", "http://www.w3.org/ns/lemon/decomp#");
        DB1.setNsPrefix("xs", "http://www.w3.org/2001/XMLSchema#");
        DB1.setCommandText(queryWordDefDBNary);

        ParameterizedSparqlString DB2 = new ParameterizedSparqlString();
        DB2.setNsPrefix("word", "http://wordnet-rdf.princeton.edu/wn31/");
        DB2.setNsPrefix("wordnet-ontology", "http://wordnet-rdf.princeton.edu/ontology#");
        DB2.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        DB2.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        DB2.setNsPrefix("lemon", "http://lemon-model.net/lemon#");
        DB2.setCommandText(queryWordDefWordnet);
        c.createQueryDBNaryWordnet(DB1, DB2, "/home/bouzigual/cours/M1/StageM1/Langue/testDBnary.txt", "/home/bouzigual/cours/M1/StageM1/Langue/testDBnary.txt","/home/bouzigual/cours/M1/StageM1/Langue/RandomWord.txt");

        SimilarityMeasure similarityMeasure = new TverskiIndexSimilarityMeasureBuilder()
                .alpha(1d).beta(1).gamma(1).computeRatio(false).fuzzyMatching(false).normalize(true).regularizeOverlapInput(true).build();

        c.makeLink("/home/bouzigual/cours/M1/StageM1/tests/defDBnary.txt", "/home/bouzigual/cours/M1/StageM1/tests/defWordnet.txt", stopWords, true, 0.7, similarityMeasure, "/home/bouzigual/cours/M1/StageM1/tests/test.txt");

        c.analyseResults("/home/bouzigual/cours/M1/StageM1/tests/test.txt", "/home/bouzigual/cours/M1/StageM1/tests/manualTests.txt", "/home/bouzigual/cours/M1/StageM1/tests/analyse2.txt");

    }

    public static void afficherBD(String data) {
        Dataset dataset = TDBFactory.createDataset(data);
        dataset.begin(ReadWrite.READ);
        Model m = dataset.getDefaultModel();

        StmtIterator stmtIter = m.listStatements(); // liste tous les statements du modèle

        int count = 0;
        int score = 0;

        while (stmtIter.hasNext()) {
            Statement stm = stmtIter.next();
            System.out.println(stm.toString());
            count++;
        }

        System.out.println(score);
        dataset.close();


    }

    public static void stoplist(String sentence) {
        StopWord sw = new StopWord(stopWords);
        String result = sw.deletStopWord(sentence);
        System.out.println(result);
    }

    public static void stemmingTest(String sentence) {
        String newSentence = Stemming_Porter.run(sentence);
        System.out.println(sentence + " -> \n" + newSentence);


    }

    public static void analyse() {
        DBComparaison c = new DBComparaison(fileTDB, fileWord);
        String pathTest = "/home/bouzigual/cours/M1/StageM1/tests/";
        String pathLink = "/home/bouzigual/cours/M1/StageM1/tests/Link/";
        String pathAnalyse = "/home/bouzigual/cours/M1/StageM1/tests/Analyse/";

        int maxnbReussi = 0;
        int minnbReussi = 500;
        int maxnbErreur = 0;
        int minnbErreur = 500;
        int maxnbManquant = 0;
        int minnbManquant = 500;

        for (double alpha = 0.25; alpha <= 1; alpha += 0.25) {
            for (double beta = 0; beta <= 1; beta += 0.25) {
                for (double gamma = 0; gamma <= 1; gamma += 0.25) {
                    for (double epsilon = 0.05; epsilon <= 1; epsilon += 0.2) {
                        String n = "Tversky_" + alpha + "_" + beta + "_" + gamma + "_" + epsilon;
                        String[] name = {n + "_true_true", n + "_true_false", n + "_false_true", n + "_false_false"};
                        for (int i = 0; i < 4; i++) {
                            boolean[] test = {true, false};
                            String link = pathLink + "autoLink" + name[i] + ".txt";
                            String analyse = pathAnalyse + "analyse" + name[i] + ".txt";
                            for (boolean t1 : test) {
                                for (boolean t2 : test) {
                                    for (boolean t3 : test) {
                                        for (boolean t4 : test) {
                                            for (boolean t5 : test) {
                                                for (boolean t6 : test) {
                                                    for (boolean t7 : test) {
                                                        for (boolean t8 : test) {
                                                            for (boolean t9 : test) {
                                                                for (boolean t10 : test) {
                                                                    SimilarityMeasure similarityMeasure = new TverskiIndexSimilarityMeasureBuilder()
                                                                            .alpha(alpha).beta(beta).gamma(gamma).quadraticWeighting(t1).extendedLesk(t2).randomInit(t3).regularizeOverlapInput(t4).optimizeOverlapInput(t5).regularizeRelations(t6).optimizeRelations(t7).isDistance(t8).fuzzyMatching(t9).normalize(t10).build();
                                                                    if (i == 0) {
                                                                        c.makeLink(pathTest + "defDBnary.txt", pathTest + "defWordnet.txt", stopWords, true, epsilon, similarityMeasure, link);
                                                                    } else if (i == 1) {
                                                                        c.makeLink(pathTest + "defDBnary.txt", pathTest + "defWordnet.txt", stopWords, false, epsilon, similarityMeasure, link);
                                                                    } else if (i == 2) {
                                                                        c.makeLink(pathTest + "defDBnary.txt", pathTest + "defWordnet.txt", "", true, epsilon, similarityMeasure, link);
                                                                    } else if (i == 3) {
                                                                        c.makeLink(pathTest + "defDBnary.txt", pathTest + "defWordnet.txt", "", false, epsilon, similarityMeasure, link);
                                                                    }
                                                                    c.analyseResults(link, pathTest + "manualTests.txt", analyse);

                                                                    try {
                                                                        InputStream ips = new FileInputStream(analyse);
                                                                        InputStreamReader ipsr = new InputStreamReader(ips);
                                                                        BufferedReader analyses = new BufferedReader(ipsr);
                                                                        int nbReussi = Integer.parseInt(analyses.readLine().split("\t")[1]);
                                                                        int nbErreur = Integer.parseInt(analyses.readLine().split("\t")[1]);
                                                                        int nbManquant = Integer.parseInt(analyses.readLine().split("\t")[1]);
                                                                        if (nbReussi >= 216) {
                                                                            System.out.println(analyse + "   " + t1 + "   " + t2 + "   " + t3 + "   " + t4 + "   " + t5 + "   " + t6 + "   " + t7 + "   " + t8 + "   " + t9 + "   " + t10 + "     " + nbReussi + "   " + nbErreur + "    " + nbManquant);
                                                                        }
                                                                        maxnbReussi = Math.max(maxnbReussi, nbReussi);
                                                                        maxnbErreur = Math.max(maxnbErreur, nbErreur);
                                                                        maxnbManquant = Math.max(maxnbManquant, nbManquant);

                                                                        minnbReussi = Math.min(minnbReussi, nbReussi);
                                                                        minnbErreur = Math.min(minnbErreur, nbErreur);
                                                                        minnbManquant = Math.min(minnbManquant, nbManquant);

                                                                        analyses.close();
                                                                    } catch (Exception e) {
                                                                        //System.out.println(e.toString());
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("reussi : " + maxnbReussi + ", " + minnbReussi);
        System.out.println("erreur : " + maxnbErreur + ", " + minnbErreur);
        System.out.println("manquant : " + maxnbManquant + ", " + minnbManquant);

    }


    public static void main(String[] arg) {

        try{
            if(arg[0].equals("-def")){
                creationDefinition();
            }else if(arg[0].equals("-test")){
                analyse();
            }else if(arg[0].equals("-stop")){
                stoplist(arg[1]);
            }else if(arg[0].equals("-lemm")){
                stemmingTest(arg[1]);
            }else if(arg[0].equals("-print")){
                afficherBD(arg[1]);
            }
        }catch (Exception e) {
            System.out.println("usage : \n  -def to create the definition in the 2 data bases\n"+
                            "  -test to create all test with tversky mesure if we have already do the definitions \n" +
                            "  -stop \"sentence\" to test the stop word algoithme \n"+
                            "  -lemm \"sentence\" to test the lemmatisation algorithm\n"+
                            "  -print \"path\" to print the data base with the path \n"+
                            "check if the path to the different document is correct");
        }
    }

}
