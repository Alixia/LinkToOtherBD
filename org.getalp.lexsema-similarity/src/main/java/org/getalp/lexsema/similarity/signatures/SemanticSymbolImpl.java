package org.getalp.lexsema.similarity.signatures;

import com.sun.istack.internal.NotNull;
import lombok.Data;

@Data
public class SemanticSymbolImpl implements SemanticSymbol {
    private String symbol;
    private Double weight;

    public SemanticSymbolImpl(String symbol, double weight) {
        this.weight = weight;
        this.symbol = symbol;
    }

    @Override
    public int compareTo(@NotNull SemanticSymbol o) {
        int symbolCmp = symbol.compareTo(o.getSymbol());
        if (symbolCmp == 0) {
            symbolCmp = weight.compareTo(o.getWeight());
        }
        return symbolCmp;
    }
}