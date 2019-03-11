package org.reactome.server.pathways.xtalk.factory.result;

import org.reactome.server.graph.domain.model.Compartment;
import org.reactome.server.graph.domain.model.ReferenceEntity;

import java.util.List;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
public class BridgeRaw {

    private ReferenceEntity interactor;
    private Compartment compartment;
    private List<PathwayLinked> pathways;

    public ReferenceEntity getInteractor() {
        return interactor;
    }

    public Compartment getCompartment() {
        return compartment;
    }

    public List<PathwayLinked> getPathways() {
        return pathways;
    }
}
