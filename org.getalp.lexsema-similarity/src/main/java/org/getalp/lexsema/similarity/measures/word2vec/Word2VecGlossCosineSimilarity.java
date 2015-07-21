package org.getalp.lexsema.similarity.measures.word2vec;


import org.deeplearning4j.models.word2vec.Word2Vec;
import org.getalp.lexsema.similarity.measures.SimilarityMeasure;
import org.getalp.lexsema.similarity.signatures.SemanticSignature;
import org.getalp.lexsema.similarity.signatures.symbols.SemanticSymbol;
import org.getalp.ml.matrix.MatrixUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Word2VecGlossCosineSimilarity implements SimilarityMeasure {



    private static Logger logger = LoggerFactory.getLogger(Word2VecGlossCosineSimilarity.class);
    private Word2Vec word2Vec;
    private boolean useCentroids;

    public Word2VecGlossCosineSimilarity(Word2Vec word2Vec, boolean useCentroids) {
        this.word2Vec = word2Vec;
        this.useCentroids = useCentroids;
    }

    @Override
    public double compute(SemanticSignature sigA, SemanticSignature sigB, Map<String, SemanticSignature> relatedSignaturesA, Map<String, SemanticSignature> relatedSignaturesB) {
        INDArray sigASignatureMatrix = generateSignatureMatrix(sigA);
        INDArray sigBSignatureMatrix = generateSignatureMatrix(sigB);

        double totalSim = 0d;
        if(!useCentroids) {
            for (int i = 0; i < sigASignatureMatrix.rows(); i++) {
                INDArray row1 = sigASignatureMatrix.getRow(i);
                deNanVector(row1);
                for (int j = 0; j < sigBSignatureMatrix.rows(); j++) {
                    INDArray row2 = sigBSignatureMatrix.getRow(j);
                    deNanVector(row2);
                    double fin = Transforms.cosineSim(row1, row2.transpose());
                    if (Double.isNaN(fin)) {
                        fin = -1;
                    }
                    totalSim += 1 - Math.acos(fin) / Math.PI;
                }
            }
            totalSim /= sigASignatureMatrix.rows() + sigBSignatureMatrix.rows();
        } else {
            INDArray averageA = MatrixUtils.getColumnWiseSumVector(sigASignatureMatrix);
            INDArray averageB = MatrixUtils.getColumnWiseSumVector(sigBSignatureMatrix);
            double fin = Transforms.cosineSim(averageA, averageB.transpose());
            if (Double.isNaN(fin)) {
                fin = -1;
            }
            totalSim = 1 - Math.acos(fin) / Math.PI;
        }
        return totalSim;
    }

    private void deNanVector(INDArray toDenan){
        for(int i=0;i<toDenan.columns();i++){
            Double v = toDenan.getDouble(i);
            if(v.isNaN()){
                toDenan.putScalar(i,0d);
            }
        }
    }

    private INDArray generateSignatureMatrix(SemanticSignature semanticSignature){
        INDArray sigASignatureMatrix = null;
        int size= semanticSignature.size();
        int currentRow = 0;
        for(SemanticSymbol symbol : semanticSignature) {
            INDArray vector = word2Vec.getWordVectorMatrix(symbol.getSymbol());
            if(sigASignatureMatrix ==null){
                sigASignatureMatrix = Nd4j.create(size,vector.columns());
            }
            try {
                sigASignatureMatrix.putRow(currentRow, vector);
            } catch (Exception e){
                //e.printStackTrace();
            }
            currentRow++;
            //logger.info("Processed "+symbol.getSymbol());
        }
        return  sigASignatureMatrix;
    }

    @Override
    public double compute(SemanticSignature sigA, SemanticSignature sigB) {
        return compute(sigA,sigB,null,null);
    }

}
