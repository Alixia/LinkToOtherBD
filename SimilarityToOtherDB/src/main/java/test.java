import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.tdb.TDBFactory;

import java.io.*;

public class test {

    public static String fileTDB = "/home/bouzigual/cours/M1/StageM1/Langue/dbnary_eng_20170522";
    public static String fileWord = "/home/bouzigual/cours/M1/StageM1/Langue/engWordnet";
    public static String stopWords = "/home/bouzigual/cours/M1/StageM1/Langue/stopWordEng.txt";

    public static void creationBD() {
        Dataset dataset = TDBFactory.createDataset(fileWord);
        dataset.begin(ReadWrite.READ);
        Model m = dataset.getDefaultModel();

        ParameterizedSparqlString pss = new ParameterizedSparqlString();

        /*
        pss.setCommandText("SELECT distinct ?p ?l ?s ?d \n" +
                " WHERE { \n" +
                " ?p dbnary:describes ?l. \n" +
                " ?l ontolex:sense ?s; dcterms:language lexvo:eng. \n" +
                " ?s skos:definition/rdf:value ?d. \n" +
                " } order by ?p");
        */

/*
        pss.setCommandText("SELECT distinct ?p ?l ?w \n" +
                "WHERE { \n" +
                " ?p dbnary:describes ?l. \n" +
                " ?l ontolex:sense ?s; dcterms:language lexvo:eng ; ontolex:canonicalForm/ontolex:writtenRep ?w\n" +
                " } order by ?w ");

        pss.setNsPrefix("eng", "http://kaiko.getalp.org/dbnary/eng/");
        pss.setNsPrefix("dbnary", "http://kaiko.getalp.org/dbnary#");
        pss.setNsPrefix("ontolex", "http://www.w3.org/ns/lemon/ontolex#");
        pss.setNsPrefix("dcterms", "http://purl.org/dc/terms/");
        pss.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
        pss.setNsPrefix("lexvo", "http://lexvo.org/id/iso639-3/");
        pss.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        pss.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        pss.setNsPrefix("olia", "http://purl.org/olia/olia.owl#");
        pss.setNsPrefix("vartrans", "http://www.w3.org/ns/lemon/vartrans#");
        pss.setNsPrefix("lime", "http://www.w3.org/ns/lemon/lime#");
        pss.setNsPrefix("synsem", "http://www.w3.org/ns/lemon/synsem#");
        pss.setNsPrefix("lexinfo", "http://www.lexinfo.net/ontology/2.0/lexinfo#");
        pss.setNsPrefix("decomp", "http://www.w3.org/ns/lemon/decomp#");
        pss.setNsPrefix("xs", "http://www.w3.org/2001/XMLSchema#");

*/

        pss.setNsPrefix("word", "http://wordnet-rdf.princeton.edu/wn31/");
        pss.setNsPrefix("wordnet-ontology", "http://wordnet-rdf.princeton.edu/ontology#");
        pss.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        pss.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        pss.setNsPrefix("lemon", "http://lemon-model.net/lemon#");


        pss.setCommandText(" SELECT distinct ?p ?d \n" +
                "WHERE { \n" +
                " ?p rdfs:label \"sou'easter\"@eng. \n" +
                " ?p wordnet-ontology:gloss ?d \n" +
                " } order by ?p ");



/*
        try {
            File ff = new File("/home/bouzigual/cours/M1/StageM1/Langue/testeeeeeeeeeeee.txt");
            ff.createNewFile();
            FileWriter randomWord = new FileWriter(ff);

*/

        System.out.println(pss.toString());

        Query query = pss.asQuery();

        QueryExecution qe = QueryExecutionFactory.create(query, m);
        ResultSet results = qe.execSelect();
        ResultSetFormatter.out(System.out,results,query) ;
/*
       for ( ; results.hasNext() ; ) {
                QuerySolution soln = results.nextSolution() ;
                Literal name = soln.getLiteral("w");
                randomWord.write(name.toString() + "\n");
                System.out.println(name.toString());
       }

        dataset.close();
        randomWord.close();
    }catch(Exception e) {
        System.out.println(e.toString());
    }
*/
    }

    public static void afficherBD(){
        Dataset dataset = TDBFactory.createDataset(fileWord);
        dataset.begin(ReadWrite.READ);
        Model m = dataset.getDefaultModel();

        StmtIterator stmtIter = m.listStatements() ; // liste tous les statements du modèle

        int count = 0;
        int score = 0;

        while(stmtIter.hasNext()){
            Statement stm = stmtIter.next() ;
            //System.out.println(stm.toString());
            //if((count = count%100000) == 0) {
                System.out.println(stm.toString());
              //  score++;
            //}
            count++;
        }

        System.out.println(score);
        dataset.close();



    }

    public static void stoplist(){
        StopWord sw = new StopWord(stopWords);
        String phrase = "";
        String result = sw.deletStopWord(phrase);
        System.out.println(result);
    }

    public static void stemmingTest(){
        String sentence = "chated";
        String newSentence = Stemming_Porter.run(sentence);
        System.out.println(sentence + " -> \n" + newSentence);


    }

    public static void test(){

        try {
            InputStream ips = new FileInputStream("/home/bouzigual/cours/M1/StageM1/Langue/testDBnary.txt");
            InputStreamReader ipsr = new InputStreamReader(ips);
            BufferedReader allWord = new BufferedReader(ipsr);

            String ligne;
            int count = 0;
            while((ligne=allWord.readLine()) != null){
                if(!ligne.split("\t")[0].equals("")){
                    count++;
                }
            }
            System.out.println(count);
            allWord.close();
        }catch(Exception e) {
            System.out.println(e.toString());
        }
    }
    public static void main(String[] arg) {

        //afficherBD();
        //creationBD();
        //Tools.takeRandomWord("/home/bouzigual/cours/M1/StageM1/Langue/ListDefs.txt", "/home/bouzigual/cours/M1/StageM1/Langue/RandomWord.txt", 413729, 100);
        //stemmingTest();
        test();
        //ComparaisonBDs c = new ComparaisonBDs(fileTDB, fileWord);
        //c.createQueryDBNaryWordnet("/home/bouzigual/cours/M1/StageM1/Langue/RandomWord.txt");

    }

}
