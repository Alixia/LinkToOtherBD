
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.TDBFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Tools {

    public static void printTab1(Object[] s){
        for(int i = 0; i < s.length; i++){
                System.out.println(s[i].toString());
        }
    }

    public static void printTab2(Object[][] s){
        for(int i = 0; i < s.length; i++){
            for(int j = 0; j < s[0].length; j++){
                System.out.print(s[i][j].toString() + "\t");
            }
            System.out.println();
        }
    }

    public static void printTab2withOp(Object[][] s, String op){
        for(int i = 0; i < s.length; i++){
            for(int j = 0; j < s[0].length; j++){
                System.out.print(s[i][j].toString() + op);
            }
            System.out.println();
        }
    }

    /**
     *
     * @param fileAllWord           file that we search the words
     * @param fileRandomWord        file that we write for take nbWord
     * @param sizeOfFileAllWord     size of fileallword
     * @param nbWord                nb word that we want to analyse (nbWord < sizeofFileAllWord)
     */
    public static void takeRandomWord(String fileAllWord, String fileRandomWord, int sizeOfFileAllWord, int nbWord){

        Dataset dataset = TDBFactory.createDataset("/home/bouzigual/cours/M1/StageM1/Langue/engWordnet");
        dataset.begin(ReadWrite.READ);
        Model m = dataset.getDefaultModel();

        ParameterizedSparqlString pss = new ParameterizedSparqlString();

        pss.setNsPrefix("word", "http://wordnet-rdf.princeton.edu/wn31/");
        pss.setNsPrefix("wordnet-ontology", "http://wordnet-rdf.princeton.edu/ontology#");
        pss.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        pss.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        pss.setNsPrefix("lemon", "http://lemon-model.net/lemon#");


        pss.setCommandText(" SELECT distinct ?p ?d \n" +
                "WHERE { \n" +
                " ?p rdfs:label ?w@eng. \n" +
                " ?p wordnet-ontology:gloss ?d \n" +
                " } order by ?p ");



        Random r = new Random();
        int[] tabRand = new int[nbWord+500];
        for(int i=0; i<nbWord+500; i++){
            tabRand[i] = r.nextInt(sizeOfFileAllWord);
        }
        Arrays.sort(tabRand);

        try {
            File ff = new File(fileRandomWord);
            ff.createNewFile();
            FileWriter randomWord = new FileWriter(ff);

            InputStream ips = new FileInputStream(fileAllWord);
            InputStreamReader ipsr = new InputStreamReader(ips);
            BufferedReader allWord = new BufferedReader(ipsr);

            String ligne = allWord.readLine();
            int i = 0;
            int j = 0;
            boolean notFound = false;
            while ((ligne !=null) && (i<tabRand.length)){
                if(j == tabRand[i] || notFound){
                    String word = ligne.split("@")[0];
                    pss.setLiteral("w", word);
                    Query queryWordnet = pss.asQuery();
                    QueryExecution qeWordnet = QueryExecutionFactory.create(queryWordnet, m);
                    ResultSet resultsWordnet = qeWordnet.execSelect();
                    if(resultsWordnet.hasNext()) {
                        notFound = false;
                        randomWord.write(word + "\n");
                    }else{
                        notFound = true;
                    }
                    i++;
                }
                ligne = allWord.readLine();
                j++;
            }

            allWord.close();
            randomWord.close();
        }catch(Exception e) {
            System.out.println(e.toString());
        }

    }

}
