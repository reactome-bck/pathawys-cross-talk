package org.reactome.server.pathways.xtalk.util;

import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.domain.model.PhysicalEntity;
import org.reactome.server.graph.domain.model.ReferenceEntity;
import org.reactome.server.pathways.xtalk.model.Bridge;
import org.reactome.server.pathways.xtalk.model.Link;
import org.reactome.server.pathways.xtalk.model.Participant;

import java.util.*;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
public class LinkReportFactory {

    private static final String SEPARATOR = ",";
    private final StringBuilder output = new StringBuilder();

    private LinkReportFactory() {
    }

    public static LinkReportFactory get() {
        return new LinkReportFactory();
    }


    public String report(Link link) {
        addPathway(link.getPathwayA());
        addPathway(link.getPathwayB());
        addScore(link.getScore());
        addCommon(link.getInCommon());
        addBridges(link.getBridgeMap());
        output.append("\n");
        return output.toString();
    }

    private void addPathway(Pathway p) {
        output.append(p.getStId()).append(SEPARATOR).append("\"").append(p.getDisplayName()).append("\"\n");
    }

    private void addScore(double score) {
        output.append(String.format("Score: %.5f", score));
    }

    private void addCommon(Collection<PhysicalEntity> common) {
        output.append(SEPARATOR).append(SEPARATOR).append(SEPARATOR).append(SEPARATOR).append(SEPARATOR).append(SEPARATOR)
                .append("Common physical entities: ").append(common.size());
        for (PhysicalEntity physicalEntity : common) {
            output.append(SEPARATOR).append("\"").append(physicalEntity.getDisplayName()).append("\"");
        }
        output.append("\n");
    }

    private void addBridges(Map<ReferenceEntity, List<Bridge>> bridgeMap) {
        List<InteractorBridges> interactorBridges = new ArrayList<>();
        for (ReferenceEntity re : bridgeMap.keySet()) {
            interactorBridges.add(new InteractorBridges(re, bridgeMap.get(re)));
        }
        interactorBridges.sort(Collections.reverseOrder());
        for (InteractorBridges ib : interactorBridges) {
            ReferenceEntity re = ib.getInteractor();
            output.append(SEPARATOR).append(SEPARATOR).append(re.getDisplayName()).append("\n");
            for (Bridge bridge : ib.getBridgeList()) {
                addParticipant(bridge.getParticipantA(), re.getDisplayName(), bridge.getParticipantB(), bridge.getScore());
                addPhysicalEntities(bridge);
            }
        }
    }

    private void addParticipant(Participant partA, String interactor, Participant partB, double score) {
        output.append(SEPARATOR).append(SEPARATOR).append(SEPARATOR)
                .append(String.format("\"%s\", <-[%.3f]-  \"%s\"  -[%.3f]-> , \"%s\", Score: %.5f",
                        partA.getReferenceEntity().getDisplayName(),
                        partA.getScore(),
                        interactor,
                        partB.getScore(),
                        partB.getReferenceEntity().getDisplayName(),
                        score))
                .append("\n");
    }

    private void addPhysicalEntities(Bridge bridge) {
        Pathway pa = bridge.getParticipantA().getPathway();
        Pathway pb = bridge.getParticipantB().getPathway();
        for (PhysicalEntity peA : bridge.getParticipantA().getPhysicalEntities()) {
            for (PhysicalEntity peB : bridge.getParticipantB().getPhysicalEntities()) {
                output.append(SEPARATOR).append(SEPARATOR).append(SEPARATOR)
                        .append(getPathwayBrowserUrl(pa, peA))
                        .append(SEPARATOR).append(SEPARATOR)
                        .append(getPathwayBrowserUrl(pb, peB))
                        .append("\n");
            }
        }
    }

    private String getPathwayBrowserUrl(Pathway p, PhysicalEntity pe) {
        String url = "https://reactome.org/PathwayBrowser/#/" + p.getStId() + "&SEL=" + pe.getStId();
        return String.format("\"=HYPERLINK(\"\"%s\"\", \"\"%s\"\")\"", url, pe.getDisplayName());
    }
}
