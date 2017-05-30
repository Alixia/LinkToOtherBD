/**
 * This class implement the porter algorithme, use in the stemming of word
 */
public class Stemming_Porter {

    /**
     *
     * @param c
     * @return true if the paramater is a vowelle (a, e, i, o, u)
     */
    private static boolean voyelleNormal(char c) {
        return c == 'a' || c == 'A' || c == 'e' || c == 'E' || c == 'i' || c == 'I' || c == 'o' || c == 'O' || c == 'u' || c == 'U';
    }

    /**
     *
     * @param c
     * @param prec
     * @returntrue if the parameter c is a vowelle (a, e, i, o, u) and test if y is a vowelle (if prec is a consonne)
     */
    private static boolean voyelle(char c, char prec){
        if(voyelleNormal(c)){
            return true;
        }else{
            if(c == 'y' || c == 'Y'){
                if(prec != '\0'){
                    if(! voyelleNormal(prec)){
                        return true;
                    }
                }
            }
            return false;
        }
    }


    /**
     *
     * @param word
     * @return the mesure of the word
     */
    private static int mesure(String word){
        int mesure = 0;
        int i = 0;
        while(i<word.length()){
            char c = word.charAt(i);
            char prec = (i>0) ? word.charAt(i-1) : '\0';

            //avancer jusqu'à la première séquence voyelle/consonne
            while( !voyelle(c, prec)){
                i++;
                if(i>=word.length()){
                    break;
                }
                c = word.charAt(i);
                prec = (i>0) ? word.charAt(i-1) : '\0';
            }

            //sequence de voyelle
            boolean voyelleTrouve = false;
            while( i<word.length() && voyelle(c, prec)){
                voyelleTrouve = true;
                i++;
                if(i>=word.length()){
                    break;
                }
                c = word.charAt(i);
                prec = (i>0) ? word.charAt(i-1) : '\0';
            }

            //sequence de consonne
            boolean consonneTrouve = false;
            if(voyelleTrouve){
                while( i<word.length() && !voyelle(c, prec)){
                    consonneTrouve = true;
                    i++;
                    if(i>=word.length()){
                        break;
                    }
                    c = word.charAt(i);
                    prec = (i>0) ? word.charAt(i-1) : '\0';

                }
            }

            if(consonneTrouve){
                mesure++;
            }
        }
        return mesure;
    }

    /**
     * test if the word countain a vowelle
     * @param word
     * @return
     */
    private static boolean contientVoyelle(String word){
        char c;
        char prec;
        for(int i = 0; i<word.length(); i++){
            c = word.charAt(i);
            prec = (i>0) ? word.charAt(i-1) : '\0';
            if(voyelle(c, prec)){
                return true;
            }
        }
        return false;
    }

    /**
     * test if the word end by a double consonne
     * @param word
     * @return
     */
    private static boolean consonneDoublee(String word) {
        char c;
        char prec;
        if (word.length() >= 2){
            c = word.charAt(word.length()-1);
            prec = word.charAt(word.length()-2);
            if (!voyelle(c, prec)) {
                if(c == prec) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * test if the word end by consonne vowelle consonne, and if the last charactere is not w, x, y
     * @param word
     * @return
     */
    private static boolean endcvc(String word){
        if(word.length() >= 3){
            char c1 = word.charAt(word.length()-3);
            char v = word.charAt(word.length()-2);
            char c2 = word.charAt(word.length()-1);
            char prec = (word.length()-4>=0) ? word.charAt(word.length()-4) : '\0';
            if(c2!='w' && c2!='x' && c2!='y') {
                if (!voyelle(c1, prec) && voyelle(v, c1) && !voyelle(c2, v)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * test if the suffix of the word is the same that the paramater suffix
     * @param word
     * @param suffix
     * @return
     */
    private static boolean suffixIdentique(String word, String suffix){
        if(word.length() >= suffix.length()){
            for(int i=1; i<=suffix.length(); i++){
                if(suffix.charAt(suffix.length()-i) != word.charAt(word.length()-i)){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * implement the step 1a to the run algorithm
     * @param word
     * @return the word change by the current step
     */
    private static String step1a(String word){
        String newWord = word;

        if(suffixIdentique(word, "sses")){
            newWord = word.substring(0, word.length() -2);
        } else if(suffixIdentique(word, "ies")){
            newWord = word.substring(0, word.length() -2);
        }else if(suffixIdentique(word, "ss")){
                newWord = word;
        }else if(suffixIdentique(word, "s")){
                newWord = word.substring(0, word.length() -1);
        }

        return newWord;
    }

    //TODO
    /**
     *
     * @param word
     * @return
     */
    private static String rajouterE(String word){
        String newWord = word;

        if(suffixIdentique(word, "at") || suffixIdentique(word, "bl") || suffixIdentique(word, "iz")){
            newWord = word + "e";
        }

        return newWord;
    }

    //TODO
    /**
     *
     * @param word
     * @return
     */
    private static String enleverConsonneDouble(String word){
        String newWord = word;

        if(consonneDoublee(word) && !suffixIdentique(word, "l") && !suffixIdentique(word, "s") && !suffixIdentique(word, "z")){
            newWord = word.substring(0, word.length()-1);
        }

        return newWord;
    }

    /**
     * implement the step 1b to the run algorithm
     * @param word
     * @return the word change by the current step
     */
    private static String step1b (String word){
        String newWord = word;
        if(suffixIdentique(word, "eed")){
            if(mesure(word.substring(0, word.length()-3)) > 0){
                newWord = word.substring(0, word.length()-1);
            }
        }else if(suffixIdentique(word, "ed")){
            if(contientVoyelle(word.substring(0, word.length()-2))){
                newWord = word.substring(0, word.length()-2);
            }
        }else if(suffixIdentique(word, "ing")){
            if(contientVoyelle(word.substring(0, word.length()-3))){
                newWord = word.substring(0, word.length()-3);
            }
        }
        newWord = rajouterE(newWord);
        newWord = enleverConsonneDouble(newWord);
        if(endcvc(newWord) && (mesure(newWord)==1)){
            newWord = newWord + "e";
        }

        return newWord;
    }

    /**
     * implement the step 1c to the run algorithm
     * @param word
     * @return the word change by the current step
     */
    private static String step1c(String word){

        String newWord = word;

        if(contientVoyelle(word.substring(0, word.length()-1)) && (word.charAt(word.length()-1) == 'y')){
            newWord = word.substring(0, word.length()-1);
            newWord += "i";
        }

        return newWord;
    }

    /**
     * implement the step 2 to the run algorithm
     * @param word
     * @return the word change by the current step
     */
    private static String step2 (String word){

        String newWord = word;

        if(suffixIdentique(word, "ational") && mesure(word.substring(0, word.length()-7)) > 0){
            newWord = word.substring(0, word.length()-5) + "e";
        }else if(suffixIdentique(word, "tional") && mesure(word.substring(0, word.length()-6)) > 0){
            newWord = word.substring(0, word.length()-2);
        }else if(suffixIdentique(word, "enci") && mesure(word.substring(0, word.length()-4)) > 0){
            newWord = word.substring(0, word.length()-1) + "e";
        }else if(suffixIdentique(word, "anci") && mesure(word.substring(0, word.length()-4)) > 0){
            newWord = word.substring(0, word.length()-1) + "e";
        }else if(suffixIdentique(word, "izer") && mesure(word.substring(0, word.length()-4)) > 0){
            newWord = word.substring(0, word.length()-1);
        }else if(suffixIdentique(word, "abli") && mesure(word.substring(0, word.length()-4)) > 0){
            newWord = word.substring(0, word.length()-1) + "e";
        }else if(suffixIdentique(word, "alli") && mesure(word.substring(0, word.length()-4)) > 0){
            newWord = word.substring(0, word.length()-2);
        }else if(suffixIdentique(word, "entli") && mesure(word.substring(0, word.length()-5)) > 0){
            newWord = word.substring(0, word.length()-2);
        }else if(suffixIdentique(word, "eli") && mesure(word.substring(0, word.length()-3)) > 0){
            newWord = word.substring(0, word.length()-2);
        }else if(suffixIdentique(word, "ousli") && mesure(word.substring(0, word.length()-5)) > 0){
            newWord = word.substring(0, word.length()-2);
        }else if(suffixIdentique(word, "ization") && mesure(word.substring(0, word.length()-7)) > 0){
            newWord = word.substring(0, word.length()-5) + "e";
        }else if(suffixIdentique(word, "ation") && mesure(word.substring(0, word.length()-5)) > 0){
            newWord = word.substring(0, word.length()-3) + "e";
        }else if(suffixIdentique(word, "ator") && mesure(word.substring(0, word.length()-4)) > 0){
            newWord = word.substring(0, word.length()-2) + "e";
        }else if(suffixIdentique(word, "alism") && mesure(word.substring(0, word.length()-5)) > 0){
            newWord = word.substring(0, word.length()-3);
        }else if(suffixIdentique(word, "iveness") && mesure(word.substring(0, word.length()-7)) > 0){
            newWord = word.substring(0, word.length()-4);
        }else if(suffixIdentique(word, "fulness") && mesure(word.substring(0, word.length()-7)) > 0){
            newWord = word.substring(0, word.length()-4);
        }else if(suffixIdentique(word, "ousness") && mesure(word.substring(0, word.length()-7)) > 0){
            newWord = word.substring(0, word.length()-4);
        }else if(suffixIdentique(word, "aliti") && mesure(word.substring(0, word.length()-5)) > 0){
            newWord = word.substring(0, word.length()-3);
        }else if(suffixIdentique(word, "iviti") && mesure(word.substring(0, word.length()-5)) > 0){
            newWord = word.substring(0, word.length()-3) + "e";
        }else if(suffixIdentique(word, "biliti") && mesure(word.substring(0, word.length()-6)) > 0){
            newWord = word.substring(0, word.length()-3) + "e";
        }

        return newWord;
    }

    /**
     * implement the step 3 to the run algorithm
     * @param word
     * @return the word change by the current step
     */
    private static String step3 (String word){

        String newWord = word;

        if(suffixIdentique(word, "icate") && mesure(word.substring(0, word.length()-5)) > 0){
            newWord = word.substring(0, word.length()-3);
        }else if(suffixIdentique(word, "ative") && mesure(word.substring(0, word.length()-5)) > 0){
            newWord = word.substring(0, word.length()-5);
        }else if(suffixIdentique(word, "alize") && mesure(word.substring(0, word.length()-5)) > 0){
            newWord = word.substring(0, word.length()-3);
        }else if(suffixIdentique(word, "iciti") && mesure(word.substring(0, word.length()-5)) > 0){
            newWord = word.substring(0, word.length()-3);
        }else if(suffixIdentique(word, "ical") && mesure(word.substring(0, word.length()-4)) > 0){
            newWord = word.substring(0, word.length()-2);
        }else if(suffixIdentique(word, "ful") && mesure(word.substring(0, word.length()-3)) > 0){
            newWord = word.substring(0, word.length()-3);
        }else if(suffixIdentique(word, "ness") && mesure(word.substring(0, word.length()-4)) > 0){
            newWord = word.substring(0, word.length()-4);
        }

        return newWord;
    }

    /**
     * implement the step 4 to the run algorithm
     * @param word
     * @return the word change by the current step
     */    private static String step4 (String word){

        String newWord = word;

        if(suffixIdentique(word, "al") && mesure(word.substring(0, word.length()-2)) > 1){
            newWord = word.substring(0, word.length()-2);
        }else if(suffixIdentique(word, "ance") && mesure(word.substring(0, word.length()-4)) > 1){
            newWord = word.substring(0, word.length()-4);
        }else if(suffixIdentique(word, "ence") && mesure(word.substring(0, word.length()-4)) > 1){
            newWord = word.substring(0, word.length()-4);
        }else if(suffixIdentique(word, "er") && mesure(word.substring(0, word.length()-2)) > 1){
            newWord = word.substring(0, word.length()-2);
        }else if(suffixIdentique(word, "ic") && mesure(word.substring(0, word.length()-2)) > 1){
            newWord = word.substring(0, word.length()-2);
        }else if(suffixIdentique(word, "able") && mesure(word.substring(0, word.length()-4)) > 1){
            newWord = word.substring(0, word.length()-4);
        }else if(suffixIdentique(word, "ible") && mesure(word.substring(0, word.length()-4)) > 1){
            newWord = word.substring(0, word.length()-4);
        }else if(suffixIdentique(word, "ant") && mesure(word.substring(0, word.length()-3)) > 1){
            newWord = word.substring(0, word.length()-3);
        }else if(suffixIdentique(word, "ement") && mesure(word.substring(0, word.length()-5)) > 1){
            newWord = word.substring(0, word.length()-5);
        }else if(suffixIdentique(word, "ment") && mesure(word.substring(0, word.length()-4)) > 1){
            newWord = word.substring(0, word.length()-4);
        }else if(suffixIdentique(word, "ent") && mesure(word.substring(0, word.length()-3)) > 1){
            newWord = word.substring(0, word.length()-3);
        }else if(suffixIdentique(word, "ion") && (( (word.substring(0, word.length()-3).charAt(word.substring(0, word.length()-3).length()) == 't') ) || (word.substring(0, word.length()-3).charAt(word.substring(0, word.length()-3).length())) == 's') && mesure(word.substring(0, word.length()-3)) > 1){
            newWord = word.substring(0, word.length()-3);
        }else if(suffixIdentique(word, "ou") && mesure(word.substring(0, word.length()-2)) > 1){
            newWord = word.substring(0, word.length()-2);
        }else if(suffixIdentique(word, "ism") && mesure(word.substring(0, word.length()-3)) > 1){
            newWord = word.substring(0, word.length()-3);
        }else if(suffixIdentique(word, "ate") && mesure(word.substring(0, word.length()-3)) > 1){
            newWord = word.substring(0, word.length()-3);
        }else if(suffixIdentique(word, "iti") && mesure(word.substring(0, word.length()-3)) > 1){
            newWord = word.substring(0, word.length()-3);
        }else if(suffixIdentique(word, "ous") && mesure(word.substring(0, word.length()-3)) > 1){
            newWord = word.substring(0, word.length()-3);
        }else if(suffixIdentique(word, "ive") && mesure(word.substring(0, word.length()-3)) > 1){
            newWord = word.substring(0, word.length()-3);
        }else if(suffixIdentique(word, "ize") && mesure(word.substring(0, word.length()-3)) > 1){
            newWord = word.substring(0, word.length()-3);
        }

        return newWord;
    }

    /**
     * implement the step 5a to the run algorithm
     * @param word
     * @return the word change by the current step
     */
    private static String step5a (String word){

        String newWord = word;

        if(suffixIdentique(word, "e") && (mesure(word.substring(0, word.length()-1)) >1) && endcvc(word.substring(0, word.length()-1))){
            newWord = word.substring(0, word.length()-1);
        }

        return newWord;
    }

    /**
     * implement the step 5b to the run algorithm
     * @param word
     * @return the word change by the current step
     */
    private static String step5b (String word){

        String newWord = word;

        if((mesure(word) > 1) && (consonneDoublee(word)) && (suffixIdentique(word, "l"))){
            newWord = word.substring(0, word.length()-1);
        }

        return newWord;
    }

    /**
     * execut the Porter algorithm to the paramater, can be used with a word or a sentence
     * @param sentence
     * @return the word or the sentence change by the Porter algorithm
     */
    public static String run(String sentence){
        String[] words = sentence.split(" ");
        for(int i = 0; i<words.length; i++){
            words[i] = step1a(words[i]);
            words[i] = step1b(words[i]);
            words[i] = step1c(words[i]);
            words[i] = step2(words[i]);
            words[i] = step3(words[i]);
            words[i] = step4(words[i]);
            words[i] = step5a(words[i]);
            words[i] = step5b(words[i]);
        }
        String newSentence = "";
        for(String s : words){
            newSentence += s + " ";
        }
        if(newSentence.length()>=1){
            newSentence =  newSentence.substring(0, newSentence.length() -1);
        }
        return newSentence;
    }

}
