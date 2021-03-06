package org.getalp.lexsema.wsd.method.sequencial.entrydisambiguators;

import com.wcohen.ss.ScaledLevenstein;
import com.wcohen.ss.api.StringDistance;
import org.getalp.lexsema.similarity.Document;
import org.getalp.lexsema.similarity.Sense;
import org.getalp.lexsema.similarity.Word;
import org.getalp.lexsema.similarity.measures.SimilarityMeasure;
import org.getalp.lexsema.similarity.signatures.DefaultSemanticSignatureFactory;
import org.getalp.lexsema.similarity.signatures.SemanticSignature;
import org.getalp.lexsema.wsd.configuration.Configuration;
import org.getalp.lexsema.wsd.method.sequencial.parameters.SimplifiedLeskParameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;


public class SimplifiedLeskLexicalEntryDisambiguator extends SequentialLexicalEntryDisambiguator {

    SimplifiedLeskParameters params;
    SimilarityMeasure similarityMeasure;

    public SimplifiedLeskLexicalEntryDisambiguator(Configuration c, Document d, SimilarityMeasure sim, SimplifiedLeskParameters params, int start, int end, int currentIndex) {
        super(c, d, start, end, currentIndex);
        this.params = params;
        similarityMeasure = sim;
    }

    @Override
    public void run() {
        try {
            Collection<String> context;
            if (params.isOnlyUniqueWords()) {
                context = new TreeSet<String>();
            } else {
                context = new ArrayList<String>();
            }

            StringDistance sl = new ScaledLevenstein();
            for (int j = getStart(); j < getEnd(); j++) {
                if (params.isIncludeTarget() || !params.isIncludeTarget() && j != getCurrentIndex()) {
                    Word cw = getDocument().getWord(0, j);
                        for (Word prevWord : cw.precedingNonInstances()) {
                            String lemma = prevWord.getLemma();
                            if (lemma != null && !lemma.isEmpty()) {
                                context.add(lemma);
                            } else {
                                context.add(prevWord.getSurfaceForm());
                            }
                        }
                        context.add(cw.getSurfaceForm());
                    }
            }
            SemanticSignature lcontext = DefaultSemanticSignatureFactory.DEFAULT.createSemanticSignature();
            lcontext.addSymbolString(new ArrayList<>(context));
            int index;
            int minIndex = -1;
            int maxIndex = -1;
            double minValue;
            double prevMinValue;
            double maxValue;
            double prevMaxValue;

            minValue = Double.MAX_VALUE;
            prevMinValue = 0;

            maxValue = -Double.MAX_VALUE;
            prevMaxValue = 0;


            List<Sense> senses = getSenses(0, getCurrentIndex());
            for (int s = 0; s < senses.size(); s++) {
                double score = lcontext.computeSimilarityWith(similarityMeasure, senses.get(s).getSemanticSignature(), null, null);
                //System.err.println(score);
                if (score <= minValue) {
                    prevMinValue = minValue;
                    minValue = score;
                    minIndex = s;
                }
                if (score >= maxValue) {
                    prevMaxValue = maxValue;
                    maxValue = score;
                    maxIndex = s;
                }
            }
            double range = maxValue - minValue;
            if (!params.isAllowTies() && (params.isMinimize() && Math.abs(prevMinValue - minValue) < params.getDeltaThreshold() ||
                    !params.isMinimize() && Math.abs(prevMaxValue - maxValue) < params.getDeltaThreshold())) {
                index = -1;
            } else if (params.isMinimize()) {
                index = minIndex;
            } else {
                index = maxIndex;
            }

            if (params.isFallbackFS() && index == -1) {
                index = 0;
            }
            getConfiguration().setSense(getCurrentIndex(), index);
            getConfiguration().setConfidence(getCurrentIndex(), 1d);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
