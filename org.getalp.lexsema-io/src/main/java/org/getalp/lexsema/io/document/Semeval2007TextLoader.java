package org.getalp.lexsema.io.document;


import org.getalp.lexsema.similarity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;

@SuppressWarnings({"BooleanParameter", "ClassWithTooManyFields"})
public class Semeval2007TextLoader extends TextLoaderImpl implements ContentHandler {

    private Logger logger = LoggerFactory.getLogger(Semeval2007TextLoader.class);

    private boolean inWord;
    private boolean loadExtra;
    private String currentSurfaceForm;
    private String currentPos;
    private String currentLemma;
    private String currentId;
    private String extraWords;

    private String path;


    private Sentence currentSentence;
    private Text currentDocument;

    public Semeval2007TextLoader(String path) {
        inWord = false;
        this.path = path;
        currentId = "";
        currentLemma = "";
        currentPos = "";
        currentSurfaceForm = "";
        extraWords = "";
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
            case "text":
                currentDocument = new TextImpl();
                currentDocument.setId(atts.getValue("id"));
                break;
            case "sentence":
                currentSentence = new SentenceImpl(atts.getValue("id"));
                break;
            case "instance":
                inWord = true;
                currentPos = atts.getValue("pos");
                currentLemma = atts.getValue("lemma");
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
            case "instance":
                inWord = false;
                Word w = new WordImpl(currentId, currentLemma, currentSurfaceForm, currentPos);

                if (loadExtra) {
                    for (String e : extraWords.trim().split(System.getProperty("line.separator"))) {
                        if (!e.isEmpty()) {
                            w.addPrecedingInstance(new WordImpl("non-target", "", e, ""));
                        }
                    }
                }

                w.setEnclosingSentence(currentSentence);
                currentSentence.addWord(w);
                currentId = "";
                currentLemma = "";
                currentPos = "";
                currentSurfaceForm = "";
                extraWords = "";
                break;
        }

    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (inWord) {
            for (int i = start; i < start + length; i++) {
                currentSurfaceForm += ch[i];
            }
        } else {
            for (int i = start; i < start + length; i++) {
                extraWords += ch[i];
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
            XMLReader saxReader = XMLReaderFactory.createXMLReader();
            saxReader
                    .setContentHandler(this);
            saxReader.parse(path);
        } catch (IOException | SAXException t) {
            logger.error(t.getLocalizedMessage());
        }
    }


    @Override
    public TextLoader loadNonInstances(boolean loadExtra) {
        this.loadExtra = loadExtra;
        return this;
    }


}
