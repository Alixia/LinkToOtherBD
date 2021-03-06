package org.getalp.lexsema.io.document.loader;


import org.getalp.lexsema.similarity.*;
import org.getalp.lexsema.util.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Semeval2013Task12CorpusLoader extends CorpusLoaderImpl implements ContentHandler {

    private static final DocumentFactory DOCUMENT_FACTORY = DefaultDocumentFactory.DEFAULT;
    private static final Logger logger = LoggerFactory.getLogger(Semeval2013Task12CorpusLoader.class);

    private boolean inWord;
    private String currentSurfaceForm;
    private String currentPos;
    private String currentLemma;
    private String currentId;
    private Language language;

    private final String path;

    private List<Word> currentPrecedingWords;
    private Sentence currentSentence;
    private Text currentDocument;

    private final String lemmaAttribute;

    public Semeval2013Task12CorpusLoader(String path) {
        this.path = path;
        lemmaAttribute = "lemma";
        init();
    }

    public Semeval2013Task12CorpusLoader(String path, String lemmaAttribute) {
        this.path = path;
        this.lemmaAttribute = lemmaAttribute;
        init();
    }
    
    private void init() {
        inWord = false;
        currentId = "";
        currentLemma = "";
        currentPos = "";
        currentSurfaceForm = "";
        currentPrecedingWords = new ArrayList<>();
    }


    @Override
    public void setDocumentLocator(Locator locator) {

    }

    @Override
    public void startDocument() throws SAXException {

    }

    @Override
    public void endDocument() throws SAXException {

    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {

    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {

    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        switch (localName) {
            case "corpus":
                language = Language.fromCode(atts.getValue("lang"));
                break;
            case "text":
                currentDocument = DOCUMENT_FACTORY.createText(language);
                currentDocument.setId(atts.getValue("id"));
                break;
            case "sentence":
                currentSentence = DOCUMENT_FACTORY.createSentence(atts.getValue("id"));
                currentSentence.setLanguage(language);
                break;
            case "wf":
                currentPos = atts.getValue("pos");
                currentLemma = atts.getValue(lemmaAttribute);
                currentId = "";
                currentPrecedingWords.add(DOCUMENT_FACTORY.createWord("non-target", currentLemma, currentSurfaceForm, currentPos));
                inWord = true;
                break;
            case "instance":
                inWord = true;
                currentPos = atts.getValue("pos");
                currentLemma = atts.getValue(lemmaAttribute);
                currentId = atts.getValue("id");
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (localName) {
            case "text":
                addText(currentDocument);
                break;
            case "sentence":
                currentDocument.addSentence(currentSentence);
                break;
            case "wf":
                inWord = false;
                currentSurfaceForm = "";
                currentId = "";
                currentLemma = "";
                currentPos = "";
                break;
            case "instance":
                inWord = false;
                Word w = DOCUMENT_FACTORY.createWord(currentId, currentLemma, currentSurfaceForm, currentPos);
                for (Word pw : currentPrecedingWords) {
                    w.addPrecedingInstance(pw);
                }
                currentPrecedingWords.clear();
                w.setEnclosingSentence(currentSentence);
                currentSentence.addWord(w);
                currentId = "";
                currentLemma = "";
                currentPos = "";
                currentSurfaceForm = "";
                break;
        }

    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (inWord) {
            for (int i = start; i < start + length; i++) {
                currentSurfaceForm += ch[i];
            }
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {

    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {

    }

    @Override
    public void skippedEntity(String name) throws SAXException {

    }

    @Override
    public void load() {
        try {
        	clearTexts();
        	init();
            XMLReader saxReader = XMLReaderFactory.createXMLReader();
            saxReader.setContentHandler(this);
            saxReader.parse(path);
        } catch (IOException | SAXException t) {
            t.printStackTrace();
            logger.error(t.getLocalizedMessage());
        }
    }

    @Override
    public CorpusLoader loadNonInstances(boolean loadExtra) {
        return this;
    }

}
