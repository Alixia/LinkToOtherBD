/**
 *
 */
package org.getalp.lexsema.ontology;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * An OWL ontology model wrapper around Jena
 */
public class OWLTBoxModel implements OntologyModel {
    private OntModel model;
    private String propPath = "data" + File.separatorChar + "ontology.properties";
    private Properties properties;

    /**
     * Default constructor
     *
     * @throws IOException When loading the default properties fails (data/ontology.properties)
     */
    public OWLTBoxModel() throws IOException {
        loadProperties();
        createModel(null);
    }

    /**
     * Build an <code>OWLTBoxModel</code> based on an existing model <code>m</code>
     *
     * @param m An existing base ontology model
     * @throws IOException When loading the default properties fails (data/ontology.properties)
     */
    public OWLTBoxModel(Model m) throws IOException {
        loadProperties();
        createModel(m);
    }

    /**
     * Load an OWLTBoxModel with a custom properties path
     *
     * @param propPath The path to the properties file
     * @throws IOException When the properties file at <code>propPath</code> cannot be loaded
     */
    public OWLTBoxModel(String propPath) throws IOException {
        this.propPath = propPath;
        loadProperties();
        createModel(null);
    }

    private void createModel(Model m) {
        if (m == null) {
            model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        } else {
            model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, m);
        }
        if (properties.containsKey("ontologies")) {
            String[] ontologies = properties.getProperty("ontologies").split(",");
            for (String ont : ontologies) {
                model.read(ont.trim());
            }
        }
    }

    /**
     * Load the properties from the properties path
     *
     * @throws IOException When the properties cannot be loaded
     */
    protected void loadProperties() throws IOException {
        properties = new Properties();
        properties.load(new FileInputStream(propPath));
    }

    /**
     * Return a reference to the wrapped Jena <code>OntModel</code>
     *
     * @return
     */
    @Override
    public OntModel getJenaModel() {
        return model;
    }


    @Override
    public Node getNode(String element) {
        return NodeFactory.createURI(getJenaModel().expandPrefix(element));
    }
}