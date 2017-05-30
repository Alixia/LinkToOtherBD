import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.TDBFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class ComparaisonBDs {

    private Model modelDB1;
    private Model modelDB2;

    /**
     *
     * @param bdDBNary          location of the bd Dbnary
     * @param bdWordnet         location of the bd Wornet
     */
    public ComparaisonBDs(String bdDBNary, String bdWordnet){

        Dataset datasetDBNary = TDBFactory.createDataset(bdDBNary);
        datasetDBNary.begin(ReadWrite.READ);
        modelDB1 = datasetDBNary.getDefaultModel();

        Dataset datasetWordnet = TDBFactory.createDataset(bdWordnet);
        datasetWordnet.begin(ReadWrite.READ);
        modelDB2 = datasetWordnet.getDefaultModel();

    }

    public void createQueryDBNaryWordnet( String fileRandomWord){
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

        ParameterizedSparqlString DBNary = new ParameterizedSparqlString();
        DBNary.setNsPrefix("eng", "http://kaiko.getalp.org/dbnary/eng/");
        DBNary.setNsPrefix("dbnary", "http://kaiko.getalp.org/dbnary#");
        DBNary.setNsPrefix("ontolex", "http://www.w3.org/ns/lemon/ontolex#");
        DBNary.setNsPrefix("dcterms", "http://purl.org/dc/terms/");
        DBNary.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
        DBNary.setNsPrefix("lexvo", "http://lexvo.org/id/iso639-3/");
        DBNary.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        DBNary.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        DBNary.setNsPrefix("olia", "http://purl.org/olia/olia.owl#");
        DBNary.setNsPrefix("vartrans", "http://www.w3.org/ns/lemon/vartrans#");
        DBNary.setNsPrefix("lime", "http://www.w3.org/ns/lemon/lime#");
        DBNary.setNsPrefix("synsem", "http://www.w3.org/ns/lemon/synsem#");
        DBNary.setNsPrefix("lexinfo", "http://www.lexinfo.net/ontology/2.0/lexinfo#");
        DBNary.setNsPrefix("decomp", "http://www.w3.org/ns/lemon/decomp#");
        DBNary.setNsPrefix("xs", "http://www.w3.org/2001/XMLSchema#");
        DBNary.setCommandText(queryWordDefDBNary);

        ParameterizedSparqlString Wordnet = new ParameterizedSparqlString();
        Wordnet.setNsPrefix("word", "http://wordnet-rdf.princeton.edu/wn31/");
        Wordnet.setNsPrefix("wordnet-ontology", "http://wordnet-rdf.princeton.edu/ontology#");
        Wordnet.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        Wordnet.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        Wordnet.setNsPrefix("lemon", "http://lemon-model.net/lemon#");
        Wordnet.setCommandText(queryWordDefWordnet);


        try {
            File ffWordnet = new File("/home/bouzigual/cours/M1/StageM1/Langue/testWordnet.txt");
            ffWordnet.createNewFile();
            FileWriter ffsWordnet = new FileWriter(ffWordnet);

            File ffDBnary = new File("/home/bouzigual/cours/M1/StageM1/Langue/testDBnary.txt");
            ffDBnary.createNewFile();
            FileWriter ffsDBnary = new FileWriter(ffDBnary);

            InputStream ips = new FileInputStream(fileRandomWord);
            InputStreamReader ipsr = new InputStreamReader(ips);
            BufferedReader randWord = new BufferedReader(ipsr);

            int indiceRand = 0;
            int i =0;
            String ligne = randWord.readLine();
            while(ligne != null){
                String word = ligne;
                Wordnet.setLiteral("w", word);
                Query queryWordnet = Wordnet.asQuery();
                QueryExecution qeWordnet = QueryExecutionFactory.create(queryWordnet, modelDB2);
                ResultSet resultsWordnet = qeWordnet.execSelect();

                ffsWordnet.write(word + "\n");
                for( ; resultsWordnet.hasNext() ; ){
                    QuerySolution soln = resultsWordnet.nextSolution() ;
                    Literal name = soln.getLiteral("d");
                    ffsWordnet.write( "\t"+ name.toString().split("@")[0] + "\n");
                }

                DBNary.setLiteral("w", word);
                Query queryDBNary = DBNary.asQuery();
                QueryExecution qeDBNary = QueryExecutionFactory.create(queryDBNary, modelDB1);
                ResultSet resultsDBNary = qeDBNary.execSelect();

                ffsDBnary.write(word + "\n");
                for( ; resultsDBNary.hasNext() ; ){
                    QuerySolution soln = resultsDBNary.nextSolution() ;
                    Literal name = soln.getLiteral("d");
                    ffsDBnary.write("\t" + name.toString().split("@")[0] + "\n");
                }


                ligne = randWord.readLine();
            }

            ffsDBnary.close();
            ffsWordnet.close();
        }catch(Exception e) {
            System.out.println(e.toString());
        }

    }

    private String simplifySentence(String sentence){
        String sent = "";
        String[] words = sentence.split("\\W+");
        for(String word : words){
            sent += word + " ";
        }
        sent = sent.substring(0, sent.length()-1);
        sent = sent.toLowerCase();
        return sent;
    }

    private String[][] simplifyDefs(String[][] listDB, String stopList, boolean stemming){
        String[][] simpleListDB = new String[listDB.length][listDB[0].length];
        StopWord sw = null;
        boolean stopWord = false;
        if(stopList != ""){
            sw = new StopWord(stopList);
            stopWord = true;
        }
        int i = 0;
        while(i < listDB.length){
            int j = 0;
            while (j < listDB[0].length){
                simpleListDB[i][j] = simplifySentence(listDB[i][j]);
                if(stopWord){
                    simpleListDB[i][j] = sw.deletStopWord(simpleListDB[i][j]);
                }
                if(stemming){
                    simpleListDB[i][j] = Stemming_Porter.run(simpleListDB[i][j]);
                }
                j++;
            }
            i++;
        }

        return simpleListDB;
    }

    private String[][] takeListDB(String db){
        try{
            InputStream ips1 = new FileInputStream(db);
            InputStreamReader ipsr1 = new InputStreamReader(ips1);
            BufferedReader fileDB = new BufferedReader(ipsr1);
            String line = "";
            ArrayList<ArrayList<String>> listDB = new ArrayList<>();
            int nbMaxDef = 0;
            while(line!=null && (line = fileDB.readLine()) != null){
                String[] defs = line.split("\t");
                ArrayList<String> ajout = new ArrayList<>();
                if(defs[0] != ""){
                    ajout.add(defs[0]);
                    while((line = fileDB.readLine()) != null && (defs = line.split("\t"))[0].equals("")){
                        ajout.add(defs[1]);
                    }
                    listDB.add(ajout);
                    if(nbMaxDef < ajout.size()){
                        nbMaxDef = ajout.size();
                    }
                }
            }
            fileDB.close();
            String[][] result = new String[listDB.size()][nbMaxDef];
            int i = 0;
            while(i < listDB.size()){
                int j = 0;
                ArrayList<String> tmp = listDB.get(i);
                while(j < tmp.size()){
                    result[i][j] = tmp.get(j);
                    j++;
                }
                i++;
            }
            return result;
        }catch(Exception e) {
            System.out.println(e.toString());
        }
        return null;
    }

    public void makeLink(String db1, String db2, String stopList, boolean stemming){
        String[][] listDB1 = takeListDB(db1);
        String[][] listDB2 = takeListDB(db2);

        String[][] simpleListDB1 = simplifyDefs(listDB1, stopList, stemming);
        String[][] simpleListDB2 = simplifyDefs(listDB2, stopList, stemming);

        int i = 0;
        while( i < simpleListDB1.length){
        }

    }

    public void close() {
        modelDB1.close();
        modelDB2.close();
    }


}
