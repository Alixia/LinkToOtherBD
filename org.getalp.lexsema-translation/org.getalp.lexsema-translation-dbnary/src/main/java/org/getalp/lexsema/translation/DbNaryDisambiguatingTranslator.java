package org.getalp.lexsema.translation;

import org.getalp.lexsema.io.resource.LRLoader;
import org.getalp.lexsema.io.resource.dbnary.DBNaryLoaderImpl;
import org.getalp.lexsema.io.text.TextProcessor;
import org.getalp.lexsema.ontolex.LexicalEntry;
import org.getalp.lexsema.ontolex.LexicalSenseImpl;
import org.getalp.lexsema.ontolex.dbnary.DBNary;
import org.getalp.lexsema.ontolex.dbnary.Translation;
import org.getalp.lexsema.ontolex.dbnary.Vocable;
import org.getalp.lexsema.ontolex.dbnary.exceptions.NoSuchVocableException;
import org.getalp.lexsema.similarity.Document;
import org.getalp.lexsema.similarity.Sense;
import org.getalp.lexsema.similarity.Text;
import org.getalp.lexsema.similarity.Word;
import org.getalp.lexsema.util.Language;
import org.getalp.lexsema.wsd.configuration.Configuration;
import org.getalp.lexsema.wsd.method.Disambiguator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Pattern;

public class DbNaryDisambiguatingTranslator implements Translator {

    private static final Logger logger = LoggerFactory.getLogger(DbNaryDisambiguatingTranslator.class);
    private static final Pattern PUNCT_PATTERN = Pattern.compile("\\p{Punct}");
    private static final Pattern BRAKETS_PATTERN = Pattern.compile("[\\[\\]]+");
    private static final Pattern COMP_PATTERN = Pattern.compile("''comp.");
    private final Disambiguator disambiguator;
    //private final SnowballStemmer snowballStemmer;
    private final Collection<String> sourceStopList;
    private final Collection<String> targetStopList;
    private final DBNary dbNary;
    private final TextProcessor textProcessor;

    public DbNaryDisambiguatingTranslator(DBNary dbNary, TextProcessor textProcessor, Disambiguator disambiguator, Collection<String> sourceStopList, Collection<String> targetStopList) {
        this.dbNary = dbNary;
        this.textProcessor = textProcessor;
        this.disambiguator = disambiguator;
        this.sourceStopList = Collections.unmodifiableCollection(sourceStopList);
        this.targetStopList = Collections.unmodifiableCollection(targetStopList);
    }

    @Override
    public String translate(String source, Language sourceLanguage, Language targetLanguage) {
        StringBuilder outputBuilder = new StringBuilder();
        LRLoader lrLoader = null;
        try {
            lrLoader = new DBNaryLoaderImpl(dbNary, sourceLanguage).loadDefinitions(true);
            Text sentence = textProcessor.process(filterInput(source), "");
            loadSenses(lrLoader, sentence);
            Configuration result = disambiguator.disambiguate(sentence);
            //noinspection LawOfDemeter
            for (int i = 0; i < result.size(); i++) {
                outputBuilder.append(String.format("%s ", getWordTranslation(i, result, sentence, sourceLanguage, targetLanguage)));
            }
        } catch (IOException e) {
            logger.error("IO {}", e.getLocalizedMessage());
        } catch (InvocationTargetException e) {
            logger.error("Invoke {}", e.getLocalizedMessage());
        } catch (NoSuchMethodException e) {
            logger.error(e.getLocalizedMessage());
        } catch (ClassNotFoundException e) {
            logger.error("Class not found: {}", e.getLocalizedMessage());
        } catch (InstantiationException e) {
            logger.error("Cannot instantiate{}", e.getLocalizedMessage());
        } catch (IllegalAccessException e) {
            logger.error("Illegal access{}", e.getLocalizedMessage());
        }
        return outputBuilder.toString();
    }

    private String filterInput(CharSequence input) {
        return PUNCT_PATTERN.matcher(input).replaceAll(" ");
    }
    private String filterOutput(CharSequence output){
        return COMP_PATTERN.matcher(PUNCT_PATTERN.matcher(BRAKETS_PATTERN.matcher(output).replaceAll("")).replaceAll(" ")).replaceAll("");
    }

    @Override
    public void close() {
    }

    private void loadSenses(LRLoader lrLoader, Document document) {
        lrLoader.loadSenses(document);
    }

    private String getWordTranslation(int index, Configuration c, Document d, Language sourceLanguage, Language targetLanguage) {
        StringBuilder outputBuilder = new StringBuilder();
        int selectedSense = c.getAssignment(index);
        List<Translation> translations = null;
        Collection<String> uniqueTranslations = new TreeSet<>();
        String lemma = getWordLemma(d.getWord(0, index));
        if (selectedSense >= 0 && !targetStopList.contains(lemma)) {
            Sense sense = getAssignedSense(d, index, selectedSense);
            if (sense != null) {
                translations = getDBNarySenseTranslation(sense, targetLanguage);
            }
        }
        if (translations == null || translations.isEmpty()) {
            try {
                Vocable v = dbNary.getVocable(getWordLemma(d.getWord(0, index)), sourceLanguage);
                translations = getDBNaryTranslation(v, targetLanguage);
            } catch (NoSuchVocableException e) {
                logger.error(e.getLocalizedMessage());
            }
        }
        if (translations != null) {
            for (Translation translation : translations) {
                if (translation.getLanguage() == targetLanguage) {
                    uniqueTranslations.add(translation.getWrittenForm());
                }
            }
            for (String t : uniqueTranslations) {
                if (!targetStopList.contains(t)) {
                    //snowballStemmer.setCurrent(t);
                    //outputBuilder.append(snowballStemmer.stem()).append(" ");
                    outputBuilder.append(filterOutput(t)).append(" ");
                }
            }
        }
        return outputBuilder.toString();
    }

    private String getWordLemma(Word w) {
        return w.getLemma();
    }

    private List<Translation> getDBNaryTranslation(Vocable v, Language targetLanguage) {
        List<Translation> translations = new ArrayList<>();
        List<LexicalEntry> lexicalEntries = dbNary.getLexicalEntries(v);
        for (LexicalEntry lexicalEntry : lexicalEntries) {
            translations.addAll(dbNary.getTranslations(lexicalEntry, targetLanguage));
        }
        return translations;
    }

    private List<Translation> getDBNarySenseTranslation(Sense sense, Language targetLanguage) {
        List<Translation> translations = new ArrayList<>();
        translations.addAll(dbNary.getTranslations(new LexicalSenseImpl(dbNary,sense.getId(),null,""), targetLanguage));
        return translations;
    }

    private Sense getAssignedSense(Document document, int index, int senseIndex) {
        if (document.getSenses(index).size() > index) {
            return document.getSenses(index).get(senseIndex);
        } else {
            return null;
        }
    }

}
