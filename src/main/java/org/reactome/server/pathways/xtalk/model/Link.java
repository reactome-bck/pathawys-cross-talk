package org.reactome.server.pathways.xtalk.model;

import org.reactome.server.graph.domain.model.Compartment;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.domain.model.PhysicalEntity;
import org.reactome.server.graph.domain.model.ReferenceEntity;

import java.util.*;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
public class Link implements Comparable<Link> {

    private Pathway pathwayA;
    private Pathway pathwayB;
    private List<Bridge> bridgeList;
    private Double score;
    private Set<Compartment> compartments;
    private Collection<PhysicalEntity> inCommon;

    public Link(Pathway pathwayA, Pathway pathwayB) {
        this.pathwayA = pathwayA;
        this.pathwayB = pathwayB;
        this.score = 0d;
        this.bridgeList = new ArrayList<>();
        this.compartments = new HashSet<>();
        this.inCommon = new HashSet<>();
    }

    public boolean addBridge(Bridge bridge) {
        if (this.bridgeList.contains(bridge)) return false;
        this.compartments.addAll(bridge.getCompartmentList());
        this.score = Math.max(this.score, bridge.getScore());
        return this.bridgeList.add(bridge);
    }

    public Pathway getPathwayA() {
        return pathwayA;
    }

    public Pathway getPathwayB() {
        return pathwayB;
    }

    public Double getScore() {
        return score;
    }

    public List<Bridge> getBridgeList() {
        return bridgeList;
    }

    public Map<ReferenceEntity, List<Bridge>> getBridgeMap() {
        Map<ReferenceEntity, List<Bridge>> rtn = new HashMap<>();
        for (Bridge bridge : getBridgeList()) {
            rtn.computeIfAbsent(bridge.getInteractor(), b -> new ArrayList<>()).add(bridge);
        }
        return rtn;
    }

    public Collection<PhysicalEntity> getInCommon() {
        return inCommon;
    }

    public void setInCommon(Collection<PhysicalEntity> inCommon) {
        this.inCommon = inCommon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Link link = (Link) o;
        return Objects.equals(pathwayA, link.pathwayA) &&
                Objects.equals(pathwayB, link.pathwayB);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathwayA, pathwayB);
    }

    @Override
    public String toString() {
        return "Link{" +
                "pathwayA=" + pathwayA.getStId() +
                ", pathwayB=" + pathwayB.getStId() +
                ", score=" + score +
                ", inCommon=" + inCommon.size() +
                '}';
    }

    @Override
    public int compareTo(Link o) {
        int c = Integer.compare(o.inCommon.size(), inCommon.size());
        if (c == 0) c = Double.compare(score, o.score);
        if (c == 0) c = Integer.compare(bridgeList.size(), o.bridgeList.size());
        if (c == 0) c = Integer.compare(compartments.size(), o.compartments.size());
        if (c == 0) c = pathwayA.compareTo(o.pathwayA);
        return c;
    }
}
