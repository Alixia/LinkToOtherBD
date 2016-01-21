package org.getalp.lexsema.wsd.experiments;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;

import org.getalp.lexsema.io.annotresult.SemevalWriter;
import org.getalp.lexsema.io.document.loader.CorpusLoader;
import org.getalp.lexsema.io.document.loader.Semeval2007CorpusLoader;
import org.getalp.lexsema.io.resource.LRLoader;
import org.getalp.lexsema.io.resource.dictionary.DictionaryLRLoader;
import org.getalp.lexsema.similarity.Document;
import org.getalp.lexsema.similarity.measures.lesk.*;
import org.getalp.lexsema.wsd.configuration.Configuration;
import org.getalp.lexsema.wsd.method.*;
import org.getalp.lexsema.wsd.score.*;

public class CuckooSearchDisambiguation
{
    public static void main(String[] args) throws Exception
    {
        int iterations = 50000;
        double minLevyLocation = 1;
        double maxLevyLocation = 5;
        double minLevyScale = 0.5;
        double maxLevyScale = 1.5;
         
        if (args.length >= 1) iterations = Integer.valueOf(args[0]);
        /*
        if (args.length >= 2) levyLocation = Double.valueOf(args[1]);
        if (args.length >= 3) levyScale = Double.valueOf(args[2]);
        if (args.length >= 4) nestsNumber = Integer.valueOf(args[3]);
        if (args.length >= 5) destroyedNests = Integer.valueOf(args[4]);
        */
        /*
        System.out.println("Parameters value : " +
                           "<scorer calls = " + iterations + "> " +
                           "<levy location = " + levyLocation + "> " +
                           "<levy scale = " + levyScale + "> " +
                           "<nests number = " + nestsNumber + "> " +
                           "<destroyed nests = " + destroyedNests + "> ");
        */
        long startTime = System.currentTimeMillis();

        CorpusLoader dl = new Semeval2007CorpusLoader(new FileInputStream("../data/senseval2007_task7/test/eng-coarse-all-words.xml"));
        
        LRLoader lrloader = new DictionaryLRLoader(new FileInputStream("../data/lesk_dict/dict_semeval2007task7_embeddings.xml"), false);

        //ConfigurationScorer scorer = new SemEval2007Task7PerfectConfigurationScorer();
        ConfigurationScorer scorer = new ConfigurationScorerWithCache(new AnotherLeskSimilarity());
        ConfigurationScorer perfectScorer = new SemEval2007Task7PerfectConfigurationScorer();
        //ConfigurationScorer scorer = new MultiThreadConfigurationScorerWithCache(new AnotherLeskSimilarity());
        //ConfigurationScorer scorer = new MatrixConfigurationScorer(new AnotherLeskSimilarity(), new SumMatrixScorer(),Runtime.getRuntime().availableProcessors());
        //CuckooSearchDisambiguator cuckooDisambiguator = new CuckooSearchDisambiguator(new StopCondition(StopCondition.Condition.SCORERCALLS, iterations), levyLocation, levyScale, nestsNumber, destroyedNests, scorer, true);
        MultiThreadCuckooSearch cuckooDisambiguator = new MultiThreadCuckooSearch(iterations, minLevyLocation, maxLevyLocation, minLevyScale, maxLevyScale, scorer, true);
        
        System.out.println("Loading texts...");
        dl.load();

        for (Document d : dl)
        {
            System.out.println("Starting document " + d.getId());
            
            System.out.println("Loading senses...");
            lrloader.loadSenses(d);

            cuckooDisambiguator.scorePlotWriter = new PrintWriter("../cuckoo_score_plot_" + d.getId() + ".dat");
            cuckooDisambiguator.perfectScorePlotWriter = new PrintWriter("../cuckoo_perfect_score_plot_" + d.getId() + ".dat");
            cuckooDisambiguator.perfectScorer = perfectScorer;
            System.out.println("Disambiguating...");
            Configuration c = cuckooDisambiguator.disambiguate(d);
            System.out.println("Final score : " + perfectScorer.computeScore(d, c));
            System.out.println("Writing results...");
            SemevalWriter sw = new SemevalWriter("../" + d.getId() + ".ans");
            sw.write(d, c.getAssignments());
            
            System.out.println("Done!");
        }
        
        cuckooDisambiguator.release();
        
        long endTime = System.currentTimeMillis();
        System.out.println("Total time elapsed in execution of Cuckoo Search Algorithm is : ");
        System.out.println((endTime - startTime) + " ms.");
        System.out.println(((endTime - startTime) / 1000l) + " s.");
        System.out.println(((endTime - startTime) / 60000l) + " m.");
    }
}
