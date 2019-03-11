package org.reactome.server.pathways.xtalk.model;

import org.reactome.server.graph.domain.model.Compartment;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.domain.model.PhysicalEntity;
import org.reactome.server.graph.domain.model.ReferenceEntity;
import org.reactome.server.pathways.xtalk.factory.result.ParticipantRaw;

import java.util.List;
import java.util.Objects;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
public class Participant {

    private Pathway pathway;
    private ReferenceEntity referenceEntity;
    private Compartment compartment;
    private List<PhysicalEntity> physicalEntities;
    private Double score;

    public Participant(Pathway pathway, ParticipantRaw interactor, Compartment compartment) {
        this.pathway = pathway;
        this.referenceEntity = interactor.getReferenceEntity();
        this.compartment = compartment;
        this.physicalEntities = interactor.getPhysicalEntities();
        this.score = interactor.getScore();
    }

    public Pathway getPathway() {
        return pathway;
    }

    public ReferenceEntity getReferenceEntity() {
        return referenceEntity;
    }

    public Compartment getCompartment() {
        return compartment;
    }

    public List<PhysicalEntity> getPhysicalEntities() {
        return physicalEntities;
    }

    public Double getScore() {
        return score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participant that = (Participant) o;
        return Objects.equals(referenceEntity, that.referenceEntity) &&
                Objects.equals(compartment, that.compartment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(referenceEntity, compartment);
    }

    @Override
    public String toString() {
        return "Participant{" +
                "referenceEntity=" + referenceEntity.getDisplayName() +
                ", compartment=" + compartment.getDisplayName() +
                ", score=" + score +
                '}';
    }
}
