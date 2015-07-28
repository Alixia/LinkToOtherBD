package org.getalp.lexsema.wsd.experiments;

import java.io.File;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.getalp.lexsema.io.document.loader.Semeval2007CorpusLoader;
import org.getalp.lexsema.io.document.loader.CorpusLoader;
import org.getalp.lexsema.io.resource.LRLoader;
import org.getalp.lexsema.io.resource.dictionary.DictionaryLRLoader;
import org.getalp.lexsema.similarity.Document;
import org.getalp.lexsema.similarity.measures.lesk.AnotherLeskSimilarity;
import org.getalp.lexsema.wsd.configuration.Configuration;
import org.getalp.lexsema.wsd.method.*;
import org.getalp.lexsema.wsd.score.*;

import com.google.common.math.DoubleMath;

public class TestOnSimilarityMeasures
{
    private static class Result
    {
        public Result(double[] scores, long[] times)
        { 
            this.scores = scores; 
            this.meanScore = getMean(this.scores);
            this.times = times; 
            this.meanTime = getMean(this.times);
        }
        public double[] scores;
        public double meanScore;
        public long[] times;
        public long meanTime;
    }
    
    private static MannWhitneyUTest mannTest = new MannWhitneyUTest();
    
    public static void main(String[] args) throws Exception
    {
        //Result res1 = getScores("../data/lesk_dict/dict_semeval2007task7_stopwords_stemming_semcor");
        //Result res2 = getScores("../data/lesk_dict/dict_semeval2007task7_stopwords_stemming_dso");
        Result res4 = getScores("../data/lesk_dict/dict_semeval2007task7_stopwords_stemming_semcor_dso_wordnetglosstag");
        Result res3 = getScores("../data/lesk_dict/dict_semeval2007task7_stopwords_stemming_semcor_dso");

        //System.out.println("Mean Scores Semcor : " + res1.meanScore);
        //System.out.println("Mean Scores DSO : " + res2.meanScore);
        System.out.println("Mean Scores Semcor + DSO : " + res3.meanScore);
        System.out.println("Mean Scores Semcor + DSO + WordnetGlossTag : " + res4.meanScore);

        //System.out.println("Mean Times Semcor : " + res1.meanTime);
        //System.out.println("Mean Times DSO : " + res2.meanTime);
        System.out.println("Mean Times Semcor + DSO : " + res3.meanTime);
        System.out.println("Mean Times Semcor + DSO + WordnetGlossTag : " + res4.meanTime);
        
        //System.out.println("MWUTest Semcor / DSO : " + mannTest.mannWhitneyUTest(res1.scores, res2.scores));
        //System.out.println("MWUTest Semcor / Semcor + DSO : " + mannTest.mannWhitneyUTest(res1.scores, res3.scores));
        //System.out.println("MWUTest DSO / Semcor + DSO : " + mannTest.mannWhitneyUTest(res2.scores, res3.scores));
        System.out.println("MWUTest Semcor + DSO / Semcor + DSO + WordnetGlosstag : " + mannTest.mannWhitneyUTest(res3.scores, res4.scores));
        
        //double[] scores = getScores("../data/lesk_dict/dict_semeval2007task7");
        //double[] scoresStopWords = getScores("../data/lesk_dict/dict_semeval2007task7_stopwords");
        //double[] scoresStemming = getScores("../data/lesk_dict/dict_semeval2007task7_stemming");
        //double[] scoresStopWordsStemming = getScores("../data/lesk_dict/all/dict_semeval2007task7_stopwords_stemming");

        //double[] scoresSemCor = getScores("../data/lesk_dict/dict_semeval2007task7");
        //double[] scoresStopWordsSemCor = getScores("../data/lesk_dict/dict_semeval2007task7_stopwords");
        //double[] scoresStemmingSemCor = getScores("../data/lesk_dict/dict_semeval2007task7_stemming");
        //double[] scoresStopWordsStemmingSemCor = getScores("../data/lesk_dict/all/dict_semeval2007task7_stopwords_stemming_semcor");

        //double meanScores = getMean(scores);
        //double meanScoresStopWords = getMean(scoresStopWords);
        //double meanScoresStemming = getMean(scoresStemming);
        //double meanScoresStopWordsStemming = getMean(scoresStopWordsStemming);

        //double meanScoresSemCor = getMean(scoresSemCor);
        //double meanScoresStopWordsSemCor = getMean(scoresStopWordsSemCor);
        //double meanScoresStemmingSemCor = getMean(scoresStemmingSemCor);
        //double meanScoresStopWordsStemmingSemCor = getMean(scoresStopWordsStemmingSemCor);
        
        //System.out.println("Mean Scores : " + meanScores);
        //System.out.println("Mean Scores StopWords : " + meanScoresStopWords);
        //System.out.println("Mean Scores Stemming : " + meanScoresStemming);
        //System.out.println("Mean Scores StopWords Stemming: " + meanScoresStopWordsStemming);

        //System.out.println("Mean Scores SemCor : " + meanScoresSemCor);
        //System.out.println("Mean Scores StopWords SemCor : " + meanScoresStopWordsSemCor);
        //System.out.println("Mean Scores Stemming SemCor: " + meanScoresStemmingSemCor);
        //System.out.println("Mean Scores StopWords Stemming SemCor: " + meanScoresStopWordsStemmingSemCor);
        
        //System.out.println("MWUTest between Scores And Scores StopWords : " + mannTest.mannWhitneyUTest(scores, scoresStopWords));
        //System.out.println("MWUTest between Scores And Scores Stemming : " + mannTest.mannWhitneyUTest(scores, scoresStemming));
        //System.out.println("MWUTest between Scores And Scores StopWords And Stemming : " + mannTest.mannWhitneyUTest(scores, scoresStopWordsStemming));
        //System.out.println("MWUTest between Scores StopWords And Scores Stemming : " + mannTest.mannWhitneyUTest(scoresStopWords, scoresStemming));
        //System.out.println("MWUTest between Scores StopWords And Scores StopWords And Stemming : " + mannTest.mannWhitneyUTest(scoresStopWords, scoresStopWordsStemming));
        //System.out.println("MWUTest between Scores Stemming And Scores StopWords And Stemming : " + mannTest.mannWhitneyUTest(scoresStemming, scoresStopWordsStemming));
        
        //System.out.println("MWUTest between Scores StopWords Stemming And Scores StopWords Stemming SemCor : " + mannTest.mannWhitneyUTest(scoresStopWordsStemming, scoresStopWordsStemmingSemCor));
    }
    
    private static Result getScores(String dict) throws Exception
    {
        int n = 100;
        double[] scores = new double[n];
        long[] times = new long[n];
        LRLoader lrloader = new DictionaryLRLoader(new File(dict), true);

        CorpusLoader dl = new Semeval2007CorpusLoader("../data/senseval2007_task7/test/eng-coarse-all-words.xml");
        dl.load();
        for (Document d : dl) lrloader.loadSenses(d);

        ConfigurationScorer scorer = new ConfigurationScorerWithCache(new AnotherLeskSimilarity());
        //ConfigurationScorer scorer = new MultiThreadConfigurationScorerWithCache(new AnotherLeskSimilarity());

        SemEval2007Task7PerfectConfigurationScorer perfectScorer = new SemEval2007Task7PerfectConfigurationScorer();
        
        int iterations = 100000;
        double minLevyLocation = 1;
        double maxLevyLocation = 5;
        double minLevyScale = 0.5;
        double maxLevyScale = 1.5;
        
        //CuckooSearchDisambiguator cuckooDisambiguator = new CuckooSearchDisambiguator(new StopCondition(StopCondition.Condition.SCORERCALLS, iterations), levyLocation, levyScale, nestsNumber, destroyedNests, scorer, false);
        MultiThreadCuckooSearch cuckooDisambiguator = new MultiThreadCuckooSearch(iterations, minLevyLocation, maxLevyLocation, minLevyScale, maxLevyScale, scorer, false);

        for (int i = 0 ; i < n ; i++)
        {
            System.out.print("" + i + " ");
            System.out.flush();
            scores[i] = 0;
            int j = 0;
            long startTime = System.currentTimeMillis();
            for (Document d : dl)
            {
                System.out.print("(" + d.getId() + ") ");
                System.out.flush();
                Configuration c = cuckooDisambiguator.disambiguate(d);
                scores[i] += perfectScorer.computeScore(d, c);
                j++;
            }
            long endTime = System.currentTimeMillis();
            times[i] = (endTime - startTime);
            scores[i] /= ((double) j);
            System.out.println("score : " + scores[i] + " ; time : " + times[i]);
        }
        System.out.println();
        cuckooDisambiguator.release();
        return new Result(scores, times);
    }
    
    private static double getMean(double[] array)
    {
        return DoubleMath.mean(array);
    }
    
    private static long getMean(long[] array)
    {
        return (long) DoubleMath.mean(array);
    }
}