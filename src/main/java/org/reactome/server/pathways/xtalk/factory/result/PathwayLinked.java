package org.reactome.server.pathways.xtalk.factory.result;

import org.reactome.server.graph.domain.model.Pathway;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
public class PathwayLinked implements Comparable<PathwayLinked> {

    private Pathway pathway;
    private List<ParticipantRaw> participants;
    private List<Pathway> ancestors;

    public Pathway getPathway() {
        return pathway;
    }

    public List<ParticipantRaw> getParticipants() {
        return participants;
    }

    public List<Pathway> getAncestors() {
        return ancestors == null ? new ArrayList<>() : ancestors;
    }

    public boolean isRelatedTo(PathwayLinked o){
        return getAncestors().contains(o.pathway);
    }

    @Override
    public int compareTo(PathwayLinked o) {
        return this.pathway.compareTo(o.pathway);
    }
}
