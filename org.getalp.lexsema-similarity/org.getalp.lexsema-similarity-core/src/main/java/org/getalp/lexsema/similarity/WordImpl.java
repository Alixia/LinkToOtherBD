package org.getalp.lexsema.similarity;

import com.hp.hpl.jena.graph.Node;
import org.getalp.lexsema.ontolex.LexicalEntry;
import org.getalp.lexsema.ontolex.LexicalResource;
import org.getalp.lexsema.ontolex.LexicalResourceEntity;
import org.getalp.lexsema.ontolex.NullLexicalEntry;
import org.getalp.lexsema.ontolex.graph.OntologyModel;
import org.getalp.lexsema.util.Language;

import java.util.*;

class WordImpl implements Word {
    private final String id;
    private final String surfaceForm;
    private String textPos;
    private LexicalEntry lexicalEntry = new NullLexicalEntry();
    private String semanticTag = "";
    private Sentence enclosingSentence = new NullSentence();
    private final List<Word> precedingNonInstances = new ArrayList<>();
    private String lemma;
    private final int begin;
    private final int end;
    private final List<Sense> senses = new ArrayList<>();

    WordImpl(String id, String lemma, String surfaceForm, String pos) {
        this.id = id;
        this.lemma = lemma;
        this.surfaceForm = surfaceForm;
        textPos = pos;
        begin = 0;
        if (surfaceForm != null) {
            end = surfaceForm.length();
        } else {
            end = 0;
        }
    }

    WordImpl(String id, String lemma, String surfaceForm, String pos, int begin, int end) {
        this.id = id;
        this.lemma = lemma;
        this.surfaceForm = surfaceForm;
        textPos = pos;
        this.begin = begin;
        this.end = end;
    }


    @Override
    public void addPrecedingInstance(Word precedingNonInstance) {
        precedingNonInstances.add(precedingNonInstance);
    }

    @Override
    public Sentence getEnclosingSentence() {
        return enclosingSentence;
    }

    @Override
    public void setEnclosingSentence(Sentence enclosingSentence) {
        this.enclosingSentence = enclosingSentence;
    }

    @Override
    public void setLexicalEntry(LexicalEntry le) {
        lexicalEntry = le;
    }

    @Override
    public String getLemma() {
        if (lexicalEntry.isNull()) {
            return lemma;
        } else {
            return lexicalEntry.getLemma();
        }
    }

    @Override
    public void setLemma(String lemma) {
        if (lexicalEntry.isNull()) {
            lexicalEntry.setLemma(lemma);
        } else {
            this.lemma = lemma;
        }
    }

    @Override
    public String getPartOfSpeech() {
        if (lexicalEntry.isNull()) {
            return textPos;
        } else {
            return lexicalEntry.getPartOfSpeech();
        }
    }

    @Override
    public void setPartOfSpeech(String partOfSpeech) {
        if(lexicalEntry.isNull()){
            textPos = partOfSpeech;
        } else {
            lexicalEntry.setPartOfSpeech(partOfSpeech);
        }
    }

    @Override
    public int getNumber() {
        return lexicalEntry.getNumber();
    }

    @Override
    public void setNumber(int number) {
        lexicalEntry.setNumber(number);
    }

    @Override
    public LexicalResource getLexicalResource() {
        return lexicalEntry.getLexicalResource();
    }

    @Override
    public OntologyModel getOntologyModel() {
        return lexicalEntry.getOntologyModel();
    }

    @Override
    public Node getNode() {
        return lexicalEntry.getNode();
    }

    @Override
    public LexicalResourceEntity getParent() {
        return lexicalEntry.getParent();

    }

    @Override
    public Language getLanguage() {
        return lexicalEntry.getLanguage();
    }

    @Override
    public void setLanguage(Language language) {

            lexicalEntry.setLanguage(language);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getSurfaceForm() {
        return surfaceForm;
    }

    @Override
    public Iterator<Sense> iterator() {
        return senses.iterator();
    }

    @Override
    public String getSenseAnnotation() {
        return semanticTag;
    }

    @Override
    public void setSemanticTag(String semanticTag) {
        this.semanticTag = semanticTag;
    }

    @Override
    public int compareTo(LexicalResourceEntity o) {
        final Node node = o.getNode();
        return id.compareTo(node.toString());
    }

    @Override
    public String toString() {
        if (lexicalEntry.isNull()) {
            return String.format("Word|%s#%s|", lemma, textPos);
        } else {
            return lexicalEntry.toString();
        }
    }

    @Override
    public int getBegin() {
        return begin;
    }

    @Override
    public int getEnd() {
        return end;
    }

    @Override
    public Iterable<Word> precedingNonInstances() {
        return Collections.unmodifiableList(precedingNonInstances);
    }

    @Override
    public void loadSenses(Collection<Sense> senses) {
        senses.stream().forEachOrdered(this.senses::add);
    }

    @Override
    public boolean isNull() {
        return false;
    }
}