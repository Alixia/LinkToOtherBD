/*
 * Carrot2 project.
 *
 * Copyright (C) 2002-2015, Dawid Weiss, Stanisław Osiński.
 * All rights reserved.
 *
 * Refer to the full license file "carrot2.LICENSE"
 * in the root folder of the repository checkout or at:
 * http://www.carrot2.org/carrot2.LICENSE
 */

package org.getalp.lexsema.ml.matrix.factorization;

import cern.colt.matrix.tdouble.DoubleMatrix2D;

/**
 * {@link LocalNonnegativeMatrixFactorization} factory.
 */
@SuppressWarnings("deprecation")
public class LocalNonnegativeMatrixFactorizationFactory extends
        IterativeMatrixFactorizationFactory {
    public MatrixFactorization factorize(DoubleMatrix2D A) {
        LocalNonnegativeMatrixFactorization factorization = new LocalNonnegativeMatrixFactorization(
                A);
        factorization.setK(k);
        factorization.setMaxIterations(maxIterations);
        factorization.setStopThreshold(stopThreshold);
        factorization.setSeedingStrategy(createSeedingStrategy());
        factorization.setOrdered(ordered);

        factorization.compute();

        return factorization;
    }
}