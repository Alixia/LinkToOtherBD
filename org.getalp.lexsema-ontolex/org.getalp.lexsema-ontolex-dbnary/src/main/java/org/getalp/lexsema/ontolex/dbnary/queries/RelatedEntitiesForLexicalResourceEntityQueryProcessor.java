package org.getalp.lexsema.ontolex.dbnary.queries;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.Var;
import org.getalp.lexsema.ontolex.*;
import org.getalp.lexsema.ontolex.dbnary.Vocable;
import org.getalp.lexsema.ontolex.dbnary.relations.DBNaryRelationType;
import org.getalp.lexsema.ontolex.factories.entities.LexicalResourceEntityFactory;
import org.getalp.lexsema.ontolex.queries.ARQSelectQueryImpl;
import org.getalp.lexsema.ontolex.queries.AbstractQueryProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This query processor implements a query that retrieves all {@code LexicalSense}s for a
 * given {@code LexicalEntry}.
 */
public final class RelatedEntitiesForLexicalResourceEntityQueryProcessor extends AbstractQueryProcessor<LexicalResourceEntity> {

    private static final String ENTRY_RESULT_VAR = "rel";
    private static final String TYPE_RESULT_VAR = "relType";
    private final LexicalResourceEntity entity;

    LexicalResourceEntityFactory lexicalResourceEntityFactory;
    private final DBNaryRelationType relationType;


    public RelatedEntitiesForLexicalResourceEntityQueryProcessor(LexicalResource lexicalResource,
                                                                 LexicalResourceEntity entity, DBNaryRelationType relationType) {
        super(lexicalResource.getGraph());
        lexicalResourceEntityFactory = lexicalResource.getLexicalResourceEntityFactory();
        this.entity = entity;
        this.relationType = relationType;
        initialize();
    }

    @Override
    protected final void defineQuery() {
        setQuery(new ARQSelectQueryImpl());
        addTriple(entity.getNode(),
                getNode(relationType.getURI()),
                Var.alloc(ENTRY_RESULT_VAR));
        addTriple(Var.alloc(ENTRY_RESULT_VAR),
                getNode("rdf:type"),
                Var.alloc(TYPE_RESULT_VAR));
        addResultVar(ENTRY_RESULT_VAR);
        addResultVar(TYPE_RESULT_VAR);
    }

    private LexicalResourceEntity getEntity(String uri, String type, LexicalResourceEntity parent, Map<String, String> parameters) {
        Class<? extends LexicalResourceEntity> targetEntityClass;
        if (type.contains("LexicalEntry")) {
            targetEntityClass = LexicalEntry.class;
        } else if (type.contains("Vocable")) {
            targetEntityClass = Vocable.class;
        } else if (type.contains("LexicalSense")) {
            targetEntityClass = LexicalSense.class;
        } else {
            return null;
        }
        return lexicalResourceEntityFactory.getEntity(targetEntityClass, uri, parent, parameters);
    }

    @Override
    public List<LexicalResourceEntity> processResults() {
        List<LexicalResourceEntity> entries = new ArrayList<>();
        while (hasNextResult()) {
            QuerySolution qs = nextSolution();
            RDFNode resultUri = qs.get(ENTRY_RESULT_VAR);
            RDFNode type = qs.get(TYPE_RESULT_VAR);
            LexicalResourceEntity retrievedEntity = getEntity(resultUri.toString(), type.toString(), null, null);
            if (retrievedEntity != null) {
                entries.add(retrievedEntity);
            }
        }
        return entries;
    }
}
