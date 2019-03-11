package org.reactome.server.pathways.xtalk.model;

import org.reactome.server.graph.domain.model.Compartment;
import org.reactome.server.graph.domain.model.ReferenceEntity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
public class Bridge implements Comparable<Bridge> {

    private ReferenceEntity interactor;
    private Participant participantA;
    private Participant participantB;
    private Double score;
    private Set<Compartment> compartmentList;

    public Bridge(ReferenceEntity interactor, Participant participantA, Participant participantB) {
        this.interactor = interactor;
        this.participantA = participantA;
        this.participantB = participantB;
        this.score = participantA.getScore() * participantB.getScore();
        this.compartmentList = new HashSet<>();
    }

    public boolean addCompartment(Compartment compartment){
        return this.compartmentList.add(compartment);
    }

    public ReferenceEntity getInteractor() {
        return interactor;
    }

    public Participant getParticipantA() {
        return participantA;
    }

    public Participant getParticipantB() {
        return participantB;
    }

    public Double getScore() {
        return score;
    }

    public Set<Compartment> getCompartmentList() {
        return compartmentList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bridge bridge = (Bridge) o;
        return Objects.equals(interactor, bridge.interactor) &&
                Objects.equals(participantA, bridge.participantA) &&
                Objects.equals(participantB, bridge.participantB);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interactor, participantA, participantB);
    }

    @Override
    public String toString() {
        return "Bridge{" +
                "interactor=" + interactor.getDisplayName() +
                ", participantA=" + participantA.getReferenceEntity().getDisplayName() +
                ", participantB=" + participantB.getReferenceEntity().getDisplayName() +
                ", score=" + score +
                '}';
    }

    @Override
    public int compareTo(Bridge o) {
        return Double.compare(score, o.score);
    }
}
