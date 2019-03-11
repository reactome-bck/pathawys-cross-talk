package org.reactome.server.pathways.xtalk.factory.result;

import org.reactome.server.graph.domain.model.PhysicalEntity;
import org.reactome.server.graph.domain.model.ReferenceEntity;

import java.util.List;

public class ParticipantRaw {

    private ReferenceEntity referenceEntity;
    private List<PhysicalEntity> physicalEntities;
    private Double score;

    public ReferenceEntity getReferenceEntity() {
        return referenceEntity;
    }

    public List<PhysicalEntity> getPhysicalEntities() {
        return physicalEntities;
    }

    public Double getScore() {
        return score;
    }
}
