package org.getalp.lexsema.similarity.signatures;

import org.getalp.lexsema.similarity.measures.SimilarityMeasure;
import org.getalp.lexsema.similarity.signatures.symbols.*;
import org.getalp.lexsema.util.Language;
import org.getalp.lexsema.util.VectorOperation;

import java.util.*;
import java.util.stream.Collectors;

class VectorizedSemanticSignatureImpl implements VectorizedSemanticSignature {
    private static final double DEFAULT_WEIGHT = 1d;
    private final List<VectorizedSemanticSymbol> symbols;
    private Language language;

    @Override
    public Language getLanguage() {
        return language;
    }

    @Override
    public void setLanguage(Language language) {
        this.language = language;
    }

    VectorizedSemanticSignatureImpl() {
        this(new ArrayList<>());
    }

    VectorizedSemanticSignatureImpl(List<VectorizedSemanticSymbol> symbols) {
        this.symbols = Collections.unmodifiableList(symbols);
    }

    @Override
    public double computeSimilarityWith(SimilarityMeasure measure, SemanticSignature other, Map<String, SemanticSignature> relatedA, Map<String, SemanticSignature> relatedB) {
        if (other != null) {
            return measure.compute(this, other, relatedA, relatedB);
        } else {
            return 0;
        }
    }

    @Override
    public VectorizedSemanticSignature copy() {
        return new VectorizedSemanticSignatureImpl(symbols);
    }

    @Override
    public int size() {
        return symbols.size();
    }
    
    @Override
    public void addSymbol(String symbol, double weight) {
        symbols.add(DefaultSemanticSymbolFactory.DEFAULT_FACTORY.createVectorizedSemanticSymbol(VectorOperation.to_vector(symbol), weight));
    }

    @Override
    public void addSymbol(String symbol) {
        addSymbol(symbol, DEFAULT_WEIGHT);
    }

    @Override
    public void addSymbols(List<SemanticSymbol> symbols) {
        for (SemanticSymbol ss : symbols) {
            addSymbol(ss);
        }
    }

    @Override
    public void addSymbolString(List<String> symbolString, List<Double> weights) {
        for (int i = 0; i < Math.min(symbolString.size(), weights.size()); i++) {
            addSymbol(symbolString.get(i), weights.get(i));
        }
    }

    @Override
    public void addSymbolString(List<String> symbolString) {
        for (String aString : symbolString) {
            addSymbol(aString, 1.0);
        }
    }

    @Override
    public List<Double> getWeights() {
        List<Double> weights = new ArrayList<>();
        for (SemanticSymbol ss : this) {
            weights.add(ss.getWeight());
        }
        return weights;
    }

    @Override
    public List<String> getStringSymbols() {
        List<String> stringSymbols = new ArrayList<>();
        for (SemanticSymbol ss : this) {
            stringSymbols.add(ss.getSymbol());
        }
        return stringSymbols;
    }

    @Override
    public List<SemanticSymbol> getSymbols() {
        return symbols.stream().map((VectorizedSemanticSymbol symbol) -> (SemanticSymbol)symbol).collect(Collectors.toList());
    }

    @Override
    public Iterator<SemanticSymbol> iterator() {
        final Collection<SemanticSymbol> stringSymbols = new ArrayList<>();
        for(VectorizedSemanticSymbol semanticSymbol: symbols){
            stringSymbols.add(DefaultSemanticSymbolFactory.DEFAULT_FACTORY.createSemanticSymbol(semanticSymbol.getSymbol(), semanticSymbol.getWeight()));
        }
        return stringSymbols.iterator();
    }

    @Override
    public SemanticSignature appendSignature(SemanticSignature other) {
        for (SemanticSymbol ss : other) {
            addSymbol(ss);
        }
        return this;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (SemanticSymbol semanticSymbol : symbols) {
            stringBuilder.append(String.format(" %s", semanticSymbol.getSymbol()));
        }
        return stringBuilder.toString();
    }

    @Override
    public SemanticSignature mergeSignatures(SemanticSignature other) {
        final SemanticSignature semanticSymbols = new VectorizedSemanticSignatureImpl(symbols);
        return semanticSymbols.appendSignature(other);
    }

    @Override
    public SemanticSymbol getSymbol(int index) {
        return symbols.get(index);
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public void addSymbol(SemanticSymbol symbol) {
        if(symbol instanceof VectorizedSemanticSymbol){
            symbols.add((VectorizedSemanticSymbol) symbol);
        } else {
            addSymbol(symbol.getSymbol(), symbol.getWeight());
        }
    }
    
    @Override
    public List<VectorizedSemanticSymbol> getVectorizedSymbols() {
        return Collections.unmodifiableList(symbols);
    }

}
