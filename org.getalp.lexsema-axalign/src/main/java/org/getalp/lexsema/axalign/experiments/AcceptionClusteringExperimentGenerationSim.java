package org.getalp.lexsema.axalign.experiments;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.trickl.cluster.KMeans;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.getalp.lexsema.axalign.cli.org.getalp.lexsema.acceptali.acceptions.SenseCluster;
import org.getalp.lexsema.axalign.cli.org.getalp.lexsema.acceptali.acceptions.SenseClusterer;
import org.getalp.lexsema.axalign.cli.org.getalp.lexsema.acceptali.acceptions.TricklSenseClusterer;
import org.getalp.lexsema.axalign.closure.LexicalResourceTranslationClosure;
import org.getalp.lexsema.axalign.closure.LexicalResourceTranslationClosureImpl;
import org.getalp.lexsema.axalign.closure.generator.TranslationClosureGenerator;
import org.getalp.lexsema.axalign.closure.generator.TranslationClosureGeneratorFactory;
import org.getalp.lexsema.axalign.closure.generator.TranslationClosureSemanticSignatureGenerator;
import org.getalp.lexsema.axalign.closure.generator.TranslationClosureSemanticSignatureGeneratorImpl;
import org.getalp.lexsema.axalign.closure.similarity.PairwiseSimilarityMatrixGenerator;
import org.getalp.lexsema.axalign.closure.similarity.PairwiseSimilarityMatrixGeneratorSim;
import org.getalp.lexsema.io.word2vec.MultilingualSerializedModelWord2VecLoader;
import org.getalp.lexsema.io.word2vec.MultilingualWord2VecLoader;
import org.getalp.lexsema.ontolex.LexicalEntry;
import org.getalp.lexsema.ontolex.LexicalSense;
import org.getalp.lexsema.ontolex.dbnary.DBNary;
import org.getalp.lexsema.ontolex.dbnary.Vocable;
import org.getalp.lexsema.ontolex.dbnary.exceptions.NoSuchVocableException;
import org.getalp.lexsema.ontolex.factories.resource.LexicalResourceFactory;
import org.getalp.lexsema.ontolex.graph.OWLTBoxModel;
import org.getalp.lexsema.ontolex.graph.OntologyModel;
import org.getalp.lexsema.ontolex.graph.storage.JenaTDBStore;
import org.getalp.lexsema.ontolex.graph.storage.StoreHandler;
import org.getalp.lexsema.ontolex.graph.store.Store;
import org.getalp.lexsema.similarity.Sense;
import org.getalp.lexsema.similarity.measures.SimilarityMeasure;
import org.getalp.lexsema.similarity.measures.crosslingual.TranslatorCrossLingualSimilarity;
import org.getalp.lexsema.similarity.measures.word2vec.Word2VecGlossCosineSimilarity;
import org.getalp.lexsema.translation.GoogleWebTranslator;
import org.getalp.lexsema.translation.Translator;
import org.getalp.lexsema.util.Language;
import org.getalp.lexsema.ml.matrix.Matrices;
import org.getalp.lexsema.ml.matrix.factorization.TapkeeNLMatrixFactorization;
import org.getalp.lexsema.ml.matrix.factorization.TapkeeNLMatrixFactorizationFactory;
import org.getalp.lexsema.ml.matrix.filters.Filter;
import org.getalp.lexsema.ml.matrix.filters.MatrixFactorizationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static java.io.File.separator;


public final class AcceptionClusteringExperimentGenerationSim {
    private static final String DB_PATH = String.format("%sVolumes%sRAMDisk", separator, separator);
    private static final String ONTOLOGY_PROPERTIES = String.format("data%sontology.properties", separator);
    private static final File CLOSURE_SAVE_PATH = new File(String.format("..%sdata%sclosure_river", separator, separator));
    private static final String MATRIX_PATH = ".." + separator + "data" + separator + "acception_matrices";
    private static final String SIM_MATRIX_PATH = String.format("%s%ssource.dat", MATRIX_PATH, separator);
    private static final String WORD_2_VEC_MODEL = String.format("..%sdata%sword2vec", separator, separator);
    private static final int NUMBER_OF_TOP_LEVEL_CLUSTERS = 30;
    private static final int SIMILARITY_DIMENSIONS = 20;
    private static final int CL_DIMENSIONS = 20;
    private static final int ENRICHMENT_SIZE = 20;
    /**
     * ainuros@outlook.com account
     */
    private static final String BING_APP_ID = "dbnary_hyper";
    private static final String BING_APP_KEY = "IecT6H4OjaWo3OtH2pijfeNIx1y1bML3grXz/Gjo/+w=";
    private static final int DEPTH = 1;
    private static final Language[] loadLanguages = {
            Language.FRENCH, Language.ENGLISH, Language.ITALIAN, Language.SPANISH,
            Language.PORTUGUESE, Language.BULGARIAN, Language.CATALAN, Language.FINNISH,
            Language.GERMAN, Language.RUSSIAN, Language.GREEK, Language.TURKISH
    };
    private static String dbPath = DB_PATH;


    private static int numberOfTopLevelClusters = NUMBER_OF_TOP_LEVEL_CLUSTERS;
    private static int similarityDimensions = SIMILARITY_DIMENSIONS;
    private static int clDimensions = CL_DIMENSIONS;
    private static int enrichmentSize = ENRICHMENT_SIZE;
    private static String word2VecModel = WORD_2_VEC_MODEL;
    private static final Logger logger = LoggerFactory.getLogger(AcceptionClusteringExperimentGenerationSim.class);


    private AcceptionClusteringExperimentGenerationSim() {
        System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true");
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
    }


    public static void main(String... args) throws IOException, NoSuchVocableException {
        try {

            loadProperties();

            logger.info("Generating or Loading Closure...");
            Set<Sense> closureSet = generateTranslationClosureWithSignatures(instantiateDBNary());

            logger.info("Loading Word2VecImpl...");
            MultilingualWord2VecLoader word2VecLoader = new MultilingualSerializedModelWord2VecLoader();
            word2VecLoader.loadGoogle(new File(WORD_2_VEC_MODEL), true);

            long matrix_time = System.currentTimeMillis();

            SimilarityMeasure similarityMeasure = createSimilarityMeasure(word2VecLoader.getWordVectors(Language.ENGLISH));

            Translator translator = new GoogleWebTranslator();


            //SignatureEnrichment signatureEnrichment = new Word2VecSignatureEnrichment(null, enrichmentSize);

            SimilarityMeasure crossLingualSimilarity =
                    new TranslatorCrossLingualSimilarity(similarityMeasure, translator, null);
            /*SimilarityMeasure crossLingualSimilarity =
                    new TranslatorCrossLingualSimilarity(similarityMeasure, translator, signatureEnrichment);*/
            /*CrossLingualSimilarity crossLingualSimilarity =
                    new TranslatorCrossLingualSimilarity(similarityMeasure, translator);*/

            logger.info("Generating matrix...");
            PairwiseSimilarityMatrixGenerator matrixGenerator =
                    new PairwiseSimilarityMatrixGeneratorSim(crossLingualSimilarity, closureSet, "newcross2");
            matrixGenerator.generateMatrix();

            logger.info("Clustering...");
            SenseClusterer clusterer = new TricklSenseClusterer(new KMeans());
            DoubleMatrix2D inputData = matrixGenerator.getScoreMatrix();
            inputData.normalize();

            //Filter filter = new MatrixFactorizationFilter(new NonnegativeMatrixFactorizationKLFactory(),20);
            Filter filter2 = new MatrixFactorizationFilter(new TapkeeNLMatrixFactorizationFactory(TapkeeNLMatrixFactorization.Method.HLLE, Paths.get("..", "tapkee-nle-server")), clDimensions);

            //filter.apply(inputData);
            filter2.apply(inputData);

            List<SenseCluster> clusters = clusterer.cluster(inputData, numberOfTopLevelClusters, new ArrayList<>(closureSet));

            for (SenseCluster sc : clusters) {
                logger.info(sc.toString());
            }

            createMatrixDirectories(matrix_time);
            writeSourceMatrix(matrix_time, matrixGenerator.getScoreMatrix());

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                InstantiationException | ClassNotFoundException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    private static void loadProperties() {
        final Properties properties = new Properties();
        try (InputStream props = AcceptionClusteringExperimentGenerationSim.class.getResourceAsStream(String.format("%sacceptali.properties", separator))) {
            if (props != null) {
                properties.load(props);
                if (properties.containsKey("acceptali.config.tdbPath")) {
                    dbPath = properties.getProperty("acceptali.config.tdbPath");
                    logger.info(String.format("[CONFIG] Loaded tdbPath=%s", dbPath));
                }
                if (properties.containsKey("acceptali.config.numberOfClusters")) {
                    numberOfTopLevelClusters = Integer.valueOf(properties.getProperty("acceptali.config.numberOfClusters"));
                    logger.info(String.format("[CONFIG] Loaded numberOfClusters=%d", numberOfTopLevelClusters));
                }
                if (properties.containsKey("acceptali.config.similarityDimensions")) {
                    similarityDimensions = Integer.valueOf(properties.getProperty("acceptali.config.similarityDimensions"));
                    logger.info(String.format("[CONFIG] Loaded similarityDimensions=%d", similarityDimensions));
                }
                if (properties.containsKey("acceptali.config.clDimensions")) {
                    clDimensions = Integer.valueOf(properties.getProperty("acceptali.config.clDimensions"));
                    logger.info(String.format("[CONFIG] Loaded clDimensions=%d", clDimensions));
                }
                if (properties.containsKey("acceptali.config.enrichmentSize")) {
                    enrichmentSize = Integer.valueOf(properties.getProperty("acceptali.config.enrichmentSize"));
                    logger.info(String.format("[CONFIG] Loaded enrichmentSize=%d", enrichmentSize));
                }
                if (properties.containsKey("acceptali.config.word2vecModel")) {
                    word2VecModel = properties.getProperty("acceptali.config.word2vecModel");
                    logger.info(String.format("[CONFIG] Loaded word2vecModel=%s", word2VecModel));
                }
            } else {
                logger.info("No acceptali.properties in the classpath, using default configuration.");
            }
        } catch (IOException ignored) {
            logger.info("No acceptali.properties in the classpath, using default configuration.");
        }
    }


    @SuppressWarnings({"LawOfDemeter", "MagicNumber", "FeatureEnvy"})
    private static SimilarityMeasure createSimilarityMeasure(WordVectors word2Vec) {
        /*return new TverskiIndexSimilarityMeasureMatrixImplBuilder()
                .computeRatio(true)
                .alpha(1d)
                .beta(0.5d)
                .gamma(0.5d)
                .fuzzyMatching(true)
                .isDistance(true)
                .matrixScorer(new SumMatrixScorer())
                .setDistance(new ScaledLevenstein())
                        //.filter(new NormalizationFilter())
                        //.filter(new MatrixFactorizationFilter(new TapkeeNLMatrixFactorizationFactory(Method.HLLE)))
                .build();*/
        return new Word2VecGlossCosineSimilarity(word2Vec, true);

    }

    private static Set<Sense> generateTranslationClosureWithSignatures(DBNary dbNary) throws NoSuchVocableException {
        LexicalResourceTranslationClosure<LexicalSense> closure;

        if (CLOSURE_SAVE_PATH.exists()) {
            TranslationClosureGenerator gtc = TranslationClosureGeneratorFactory.createFileGenerator(dbNary, CLOSURE_SAVE_PATH.getAbsolutePath());
            closure = generateLexicalSenseClosure(gtc, DEPTH);
        } else {
            Vocable v = dbNary.getVocable("river", Language.ENGLISH);
            List<LexicalEntry> ventries = dbNary.getLexicalEntries(v);
            if (ventries.isEmpty()) {
                closure = new LexicalResourceTranslationClosureImpl();
            } else {
                TranslationClosureGenerator gtc = TranslationClosureGeneratorFactory.createVocablePOSGenerator(v, "http://www.lexinfo.net/ontology/2.0/lexinfo#noun", dbNary);
                closure = generateLexicalSenseClosure(gtc, DEPTH);
            }
        }
        TranslationClosureSemanticSignatureGenerator semanticSignatureGenerator =
                new TranslationClosureSemanticSignatureGeneratorImpl();
        Set<Sense> sigClosure = semanticSignatureGenerator.generateSemanticSignatures(closure);
        logger.info(sigClosure.toString());
        return sigClosure;
    }

    private static LexicalResourceTranslationClosure<LexicalSense> generateLexicalSenseClosure(TranslationClosureGenerator ctg, int degree) {
        return ctg.generateClosure(degree);
    }

    private static DBNary instantiateDBNary() throws IOException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Store vts = new JenaTDBStore(dbPath);
        StoreHandler.registerStoreInstance(vts);
        //StoreHandler.DEBUG_ON = true;
        OntologyModel tBox = new OWLTBoxModel(ONTOLOGY_PROPERTIES);
        // Creating DBNary wrapper
        return (DBNary) LexicalResourceFactory.getLexicalResource(DBNary.class, tBox, loadLanguages);
    }

    private static void writeSourceMatrix(long matrix_time, DoubleMatrix2D matrix) {
        try (PrintWriter pw = new PrintWriter(String.format("%s%s%d%ssource.dat", MATRIX_PATH, separator, matrix_time, separator))) {
            Matrices.matrixCSVWriter(pw, matrix);
            pw.close();
        } catch (FileNotFoundException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    private static void createMatrixDirectories(long matrix_time) {
        File dir = new File(MATRIX_PATH + separator + matrix_time);
        if (!dir.exists()) {
            boolean result = dir.mkdirs();
            if (!result) {
                logger.error("Cannot create {}{}{}", MATRIX_PATH, separator, matrix_time);
            }
        }
    }

}
