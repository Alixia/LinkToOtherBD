package org.getalp.lexsema.ml.tensor.opertor;

import org.nd4j.linalg.api.ndarray.INDArray;

@FunctionalInterface
public interface UnaryTensorOperator {
    INDArray operator(INDArray tensor);
}
