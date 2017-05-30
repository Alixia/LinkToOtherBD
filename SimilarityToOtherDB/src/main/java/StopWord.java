import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class StopWord {

    private String file ;
    private ArrayList<String> stopList ;

    public StopWord(String fichier) {
        this.file = fichier;
        stopList = new ArrayList<>();
        init();
    }

    /**
     * save the stoplist in a arraylist
     */
    private void init(){
        try{
            InputStream ips = new FileInputStream(file);
            InputStreamReader ipsr = new InputStreamReader(ips);
            BufferedReader br = new BufferedReader(ipsr);
            String ligne;
            while ((ligne = br.readLine())!=null){
                stopList.add(ligne);
            }
            br.close();
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }

    /**
     * delete the stopword in the sentence take in paramater
     * @param sentence
     * @return
     */
    public String deletStopWord(String sentence){
        String[] listMot = sentence.split(" ");
        for(int i=0; i<listMot.length; i++){
            for(String stopW : stopList){
                if(listMot[i].equals(stopW)){
                    listMot[i] = "";
                }
            }
        }

        String result = "";
        for(int i=0; i<listMot.length-1; i++){
            if(!listMot[i].equals("")){
                result += listMot[i] + " ";
            }
        }
        if(listMot.length >= 1){
            result += listMot[listMot.length-1];
        }

        return result;
    }

}
