import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;

import org.getalp.lexsema.similarity.measures.SimilarityMeasure;
import org.getalp.lexsema.similarity.measures.tverski.TverskiIndexSimilarityMeasureBuilder;
import org.getalp.lexsema.similarity.signatures.DefaultSemanticSignatureFactory;
import org.getalp.lexsema.similarity.signatures.SemanticSignature;
import scala.xml.PrettyPrinter;

import java.io.*;
import java.util.ArrayList;

public class DBComparaison {

    private Model modelDB1;
    private Model modelDB2;

    /**
     * @param bdDBNary  location of the bd Dbnary
     * @param bdWordnet location of the bd Wornet
     */
    public DBComparaison(String bdDBNary, String bdWordnet) {

        Dataset datasetDBNary = TDBFactory.createDataset(bdDBNary);
        datasetDBNary.begin(ReadWrite.READ);
        modelDB1 = datasetDBNary.getDefaultModel();

        Dataset datasetWordnet = TDBFactory.createDataset(bdWordnet);
        datasetWordnet.begin(ReadWrite.READ);
        modelDB2 = datasetWordnet.getDefaultModel();

    }

    public void createQueryDBNaryWordnet(ParameterizedSparqlString DB1, ParameterizedSparqlString DB2, String fileDB1, String fileDB2 , String fileRandomWord) {

        try {
            File ffWordnet = new File(fileDB1);
            ffWordnet.createNewFile();
            FileWriter ffsWordnet = new FileWriter(ffWordnet);

            File ffDBnary = new File(fileDB2);
            ffDBnary.createNewFile();
            FileWriter ffsDBnary = new FileWriter(ffDBnary);

            InputStream ips = new FileInputStream(fileRandomWord);
            InputStreamReader ipsr = new InputStreamReader(ips);
            BufferedReader randWord = new BufferedReader(ipsr);

            int indiceRand = 0;
            int i = 0;
            String ligne = randWord.readLine();
            while (ligne != null) {
                String word = ligne;
                DB2.setLiteral("w", word);
                Query queryWordnet = DB2.asQuery();
                QueryExecution qeWordnet = QueryExecutionFactory.create(queryWordnet, modelDB2);
                ResultSet resultsWordnet = qeWordnet.execSelect();

                ffsWordnet.write(word + "\n");
                for (; resultsWordnet.hasNext(); ) {
                    QuerySolution soln = resultsWordnet.nextSolution();
                    Literal name = soln.getLiteral("d");
                    ffsWordnet.write("\t" + name.toString().split("@")[0] + "\n");
                }

                DB1.setLiteral("w", word);
                Query queryDBNary = DB1.asQuery();
                QueryExecution qeDBNary = QueryExecutionFactory.create(queryDBNary, modelDB1);
                ResultSet resultsDBNary = qeDBNary.execSelect();

                ffsDBnary.write(word + "\n");
                for (; resultsDBNary.hasNext(); ) {
                    QuerySolution soln = resultsDBNary.nextSolution();
                    Literal name = soln.getLiteral("d");
                    ffsDBnary.write("\t" + name.toString().split("@")[0] + "\n");
                }


                ligne = randWord.readLine();
            }

            ffsDBnary.close();
            ffsWordnet.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }

    private String simplifySentence(String sentence) {
        String sent = "";
        if (!sentence.equals("")) {
            String[] words = sentence.split("\\W+");
            for (String word : words) {
                sent += word + " ";
            }
            sent = sent.substring(0, sent.length() - 1);
            sent = sent.toLowerCase();
        }
        return sent;
    }

    private String[][] simplifyDefs(String[][] listDB, String stopList, boolean stemming) {
        String[][] simpleListDB = new String[listDB.length][listDB[0].length];
        StopWord sw = null;
        boolean stopWord = false;
        if (stopList != "") {
            sw = new StopWord(stopList);
            stopWord = true;
        }
        int i = 0;
        while (i < simpleListDB.length) {
            int j = 0;
            while (j < simpleListDB[i].length) {
                simpleListDB[i][j] = simplifySentence(listDB[i][j]);
                if (stopWord) {
                    simpleListDB[i][j] = sw.deletStopWord(simpleListDB[i][j]);
                }
                if (stemming) {
                    simpleListDB[i][j] = Stemming_Porter.run(simpleListDB[i][j]);
                }
                j++;
            }
            i++;
        }

        return simpleListDB;
    }

    private String[][] takeListDB(String db) {
        try {
            InputStream ips1 = new FileInputStream(db);
            InputStreamReader ipsr1 = new InputStreamReader(ips1);
            BufferedReader fileDB = new BufferedReader(ipsr1);
            String line = fileDB.readLine();
            ArrayList<ArrayList<String>> listDB = new ArrayList<>();
            int nbMaxDef = 0;
            while (line != null) {
                String[] defs = line.split("\t");
                ArrayList<String> ajout = new ArrayList<>();
                if (defs[0] != "") {
                    ajout.add(defs[0]);
                    while ((line = fileDB.readLine()) != null && (defs = line.split("\t"))[0].equals("")) {
                        int tmp = 1;
                        while (tmp < defs.length && !defs[tmp].equals("")) {
                            ajout.add(defs[tmp]);
                            tmp++;
                        }
                    }
                    listDB.add(ajout);
                    if (nbMaxDef < ajout.size()) {
                        nbMaxDef = ajout.size();
                    }
                } else {
                    line = fileDB.readLine();
                }
            }
            fileDB.close();
            String[][] result = new String[listDB.size()][nbMaxDef];
            for (int i = 0; i < result.length; i++) {
                for (int j = 0; j < result[i].length; j++) {
                    result[i][j] = "";
                }
            }

            int i = 0;
            while (i < listDB.size()) {
                int j = 0;
                ArrayList<String> tmp = listDB.get(i);
                while (j < tmp.size()) {
                    result[i][j] = tmp.get(j);
                    j++;
                }
                i++;
            }
            return result;
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return null;
    }

    public void makeLink(String db1, String db2, String stopList, boolean stemming, double epsilonSimilarity, SimilarityMeasure similarityMeasure, String fileResult) {
        try {
            File ff = new File(fileResult);
            ff.createNewFile();
            FileWriter result = new FileWriter(ff);

            String[][] listDB1 = takeListDB(db1);
            String[][] listDB2 = takeListDB(db2);

            String[][] simpleListDB1 = simplifyDefs(listDB1, stopList, stemming);
            String[][] simpleListDB2 = simplifyDefs(listDB2, stopList, stemming);

            int wordIndexDB1 = 0;
            while (wordIndexDB1 < simpleListDB1.length) {
                //found same word
                int wordIndexDB2 = wordIndexDB1;
                if (!simpleListDB1[wordIndexDB1][0].equals(simpleListDB2[wordIndexDB2][0])) {
                    wordIndexDB2 = 0;
                    while (wordIndexDB2 < simpleListDB2.length && (!simpleListDB2[wordIndexDB2][0].equals(simpleListDB1[wordIndexDB1][0]))) {
                        wordIndexDB2++;
                    }
                    if (wordIndexDB2 == simpleListDB2.length) {
                    }
                }
                result.write(listDB1[wordIndexDB1][0] + "\n");

                //calcul similarity
                Double[][] similarity = new Double[simpleListDB1[wordIndexDB1].length][simpleListDB2[wordIndexDB2].length];
                for (int i = 0; i < similarity.length; i++) {
                    for (int j = 0; j < similarity[i].length; j++) {
                        similarity[i][j] = 0.;
                    }
                }

                int i = 1;
                while (i < simpleListDB1[wordIndexDB1].length && !listDB1[wordIndexDB1][i].equals("")) {
                    if (!simpleListDB1[wordIndexDB1][i].equals("")) {
                        int j = 1;
                        while (j < simpleListDB2[wordIndexDB2].length && !listDB2[wordIndexDB2][j].equals("")) {
                            if (!simpleListDB2[wordIndexDB2][j].equals("")) {
                                SemanticSignature signature1 = DefaultSemanticSignatureFactory.DEFAULT.createSemanticSignature(simpleListDB1[wordIndexDB1][i]);
                                SemanticSignature signature2 = DefaultSemanticSignatureFactory.DEFAULT.createSemanticSignature(simpleListDB2[wordIndexDB2][j]);
                                double sim = similarityMeasure.compute(signature1, signature2);
                                similarity[i][j] = sim;
                            }
                            j++;
                        }
                    }
                    i++;
                }

                //write the result in the fileResult
                boolean[] bd2Found = new boolean[simpleListDB2[wordIndexDB2].length];
                int tmp = 0;
                while (tmp < bd2Found.length) {
                    bd2Found[tmp] = false;
                    tmp++;
                }
                i = 1;
                while (i < similarity.length && !listDB1[wordIndexDB1][i].equals("")) {
                    int j = 1;
                    boolean bd1Found = false;
                    while (j < similarity[i].length && !listDB2[wordIndexDB2][j].equals("")) {
                        if (similarity[i][j] >= epsilonSimilarity) {
                            result.write("\t" + listDB1[wordIndexDB1][i] + "\t" + listDB2[wordIndexDB2][j] + "\n");
                            bd1Found = true;
                            bd2Found[j] = true;
                        }
                        j++;
                    }
                    if (!bd1Found && !listDB1[wordIndexDB1][i].equals("")) {
                        result.write("\t" + listDB1[wordIndexDB1][i] + "\t?\n");
                    }
                    i++;
                }

                int j = 1;
                while (j < bd2Found.length && !simpleListDB2[wordIndexDB2][j].equals("")) {
                    if (!bd2Found[j] && !listDB2[wordIndexDB2][j].equals("")) {
                        result.write("\t?\t" + listDB2[wordIndexDB2][j] + "\n");
                    }
                    j++;
                }
                wordIndexDB1++;
            }
            result.close();
        } catch (Exception e) {
            //System.out.println(e.toString());
        }
    }

    public void analyseResults(String fileAutoLink, String fileManualLink, String fileResult) {
        String[][] autoLink = takeListDB(fileAutoLink);
        String[][] manualLink = takeListDB(fileManualLink);

        int nbErreur = 0;
        int nbLienManquant = 0;
        int nbReussi = 0;

        int wordIndexDB1 = 0;
        while (wordIndexDB1 < autoLink.length) {
            //found same word
            int wordIndexDB2 = wordIndexDB1;
            if (!autoLink[wordIndexDB1][0].equals(manualLink[wordIndexDB2][0])) {
                wordIndexDB2 = 0;
                while (wordIndexDB2 < manualLink.length && (!manualLink[wordIndexDB2][0].equals(autoLink[wordIndexDB1][0]))) {
                    wordIndexDB2++;
                }
            }
            for(int i = 1; i < autoLink[wordIndexDB1].length && !autoLink[wordIndexDB1][i].equals(""); i+=2){
                boolean trouve = false;
                for(int j = 1; j < manualLink[wordIndexDB2].length && !manualLink[wordIndexDB2][j].equals("") ; j+=2){
                    if(autoLink[wordIndexDB1][i].equals(manualLink[wordIndexDB2][j]) && autoLink[wordIndexDB1][i+1].equals(manualLink[wordIndexDB2][j+1])){
                        nbReussi ++;
                        trouve = true;
                        break;
                    }
                }
                if(!trouve){
                    nbErreur++;
                }
            }

            for(int j = 1; j < manualLink[wordIndexDB2].length && !manualLink[wordIndexDB2][j].equals("")  ; j+=2){
                boolean trouve = false;
                for(int i = 1; i < autoLink[wordIndexDB1].length && !autoLink[wordIndexDB1][i].equals(""); i+=2){
                    if(autoLink[wordIndexDB1][i].equals(manualLink[wordIndexDB2][j]) && autoLink[wordIndexDB1][i+1].equals(manualLink[wordIndexDB2][j+1])){
                        trouve = true;
                    }
                }
                if(!trouve){
                    nbLienManquant++;
                }
            }
            wordIndexDB1++;

        }
        try {
            File ff = new File(fileResult);
            ff.createNewFile();
            FileWriter result = new FileWriter(ff);
            result.write("nbReussi : \t" + nbReussi + "\n");
            result.write("nbErreur : \t" + nbErreur + "\n");
            result.write("nbLienManquant : \t" + nbLienManquant);
            result.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }


    }

    public void close() {
        modelDB1.close();
        modelDB2.close();
    }


}

class WordNotFound extends Exception {

    public WordNotFound() {
        System.out.println("These two databases do not have the same words !");
    }

}
