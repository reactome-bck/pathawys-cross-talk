package org.reactome.server.pathways.xtalk.factory;

import org.reactome.server.graph.domain.model.Compartment;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.domain.model.PhysicalEntity;
import org.reactome.server.graph.domain.model.ReferenceEntity;
import org.reactome.server.graph.exception.CustomQueryException;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.reactome.server.pathways.xtalk.factory.result.BridgeRaw;
import org.reactome.server.pathways.xtalk.factory.result.ParticipantRaw;
import org.reactome.server.pathways.xtalk.factory.result.PathwayLinked;
import org.reactome.server.pathways.xtalk.model.Bridge;
import org.reactome.server.pathways.xtalk.model.Link;
import org.reactome.server.pathways.xtalk.model.Participant;

import java.util.*;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
public class LinkFactory {

    private static final AdvancedDatabaseObjectService ados = ReactomeGraphCore.getService(AdvancedDatabaseObjectService.class);

    private static final Map<String, Link> linkMap = new HashMap<>();

    private static Map<String, Bridge> bridgeMap;

    final static private String QUERY = "" +
            "MATCH (ir:ReferenceEntity) " +
            "WHERE NOT (:PhysicalEntity)-[:referenceEntity]->(ir) " +

            "MATCH (ir)<-[:interactor]-(it:Interaction)-[:interactor]->(ref:ReferenceEntity) " +
            "WHERE NOT ir = ref AND it.score >= {score} " +
            "WITH ir, COLLECT(ref) AS refs " +

            "UNWIND refs AS ref " +
            "MATCH (ref)<-[:referenceEntity]-(pe:PhysicalEntity)<-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator*]-(rle:ReactionLikeEvent{isInDisease:False, speciesName:\"Homo sapiens\"}) " +
            "WHERE NOT (pe)-[:hasModifiedResidue]->() " +
            "MATCH (pe)-[:compartment]->(c:Compartment) " +

            "MATCH path=(p:Pathway{hasDiagram:True, isInDisease:False})-[:hasEvent*]->(rle) " +
            "WHERE SINGLE(x IN NODES(path) WHERE (x:Pathway) AND x.hasDiagram) " +

            "MATCH (ir)<-[:interactor]-(it:Interaction)-[:interactor]->(ref) " +

            "OPTIONAL MATCH ancestors=(tlp:TopLevelPathway)-[:hasEvent*]->(p) " +

            "WITH DISTINCT ir, c, p, NODES(ancestors) AS ancestors, ref, it, COLLECT(DISTINCT pe) AS pes " +
//            "WITH DISTINCT ir, c, p, NODES(ancestors) AS ancestors, COLLECT(DISTINCT {physicalEntity: pe, referenceEntity: ref, score: it.score}) AS aux " +
            "WITH DISTINCT ir, c, p, ancestors, COLLECT(DISTINCT {referenceEntity: ref, physicalEntities: pes, score: it.score}) AS participants " +
            "WITH DISTINCT ir, c, COLLECT(DISTINCT {pathway: p, participants: participants, ancestors: ancestors}) AS pathways " +

            "WHERE SIZE(pathways) > 1 " +
            "RETURN ir AS interactor, c AS compartment, pathways " +
            "ORDER BY size(pathways) DESC";

    private LinkFactory() {}

    public static Collection<Link> getLinks(double score){
        List<Link> links = new ArrayList<>();
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("score", score);
            for (BridgeRaw bridgeRaw : ados.getCustomQueryResults(BridgeRaw.class, QUERY, params)) {

                final ReferenceEntity interactor = bridgeRaw.getInteractor();
                final Compartment compartment = bridgeRaw.getCompartment();

                for (int i = 0; i < bridgeRaw.getPathways().size(); i++) {
                    final PathwayLinked pathwayA = bridgeRaw.getPathways().get(i);
                    for (int j = 0; j < bridgeRaw.getPathways().size(); j++) {
                        final PathwayLinked pathwayB = bridgeRaw.getPathways().get(j);

                        if (pathwayA.equals(pathwayB) || areRelatives(pathwayA, pathwayB)) continue;
                        LinkFactory.bridgeMap = new HashMap<>();

                        final Link link = getOrCreateLink(pathwayA.getPathway(), pathwayB.getPathway());

                        for (ParticipantRaw rawA : pathwayA.getParticipants()) {
                            Participant participantA = new Participant(pathwayA.getPathway(), rawA, compartment);
                            for (ParticipantRaw rawB : pathwayB.getParticipants()) {
                                Participant participantB = new Participant(pathwayB.getPathway(), rawB, compartment);
                                if(participantA.equals(participantB)) continue;

                                Bridge bridge = getOrCreateBridge(interactor, participantA, participantB);
                                bridge.addCompartment(compartment);
                                link.addBridge(bridge);
                            }
                        }
                        if (!link.getBridgeList().isEmpty() && !links.contains(link)) links.add(link);
                    }
                }
            }
        } catch (CustomQueryException e) {
            e.printStackTrace();
        }

        for (Link link : links) {
            link.setInCommon(getCommonPhysicalEntities(link.getPathwayA(), link.getPathwayB()));
        }

        links.sort(Collections.reverseOrder());
        return links;
    }

    private static boolean areRelatives(PathwayLinked a, PathwayLinked b){
        return a.isRelatedTo(b) || b.isRelatedTo(a);
    }

    private static Link getOrCreateLink(Pathway pathwayA, Pathway pathwayB){
        if(pathwayA.getDbId() < pathwayB.getDbId()){
            String key = pathwayA.getDbId() + "#" + pathwayB.getDbId();
            return linkMap.computeIfAbsent(key, k -> new Link(pathwayA, pathwayB));
        } else {
            String key = pathwayB.getDbId() + "#" + pathwayA.getDbId();
            return linkMap.computeIfAbsent(key, k -> new Link(pathwayB, pathwayA));
        }
    }

    private static Bridge getOrCreateBridge(ReferenceEntity interactor, Participant participantA, Participant participantB){
        if(participantA.getPathway().getDbId() < participantB.getPathway().getDbId()){
            String key = interactor.getDbId() + "#" + participantA.getReferenceEntity().getDbId() + "#" + participantB.getReferenceEntity().getDbId();
            return bridgeMap.computeIfAbsent(key, k -> new Bridge(interactor, participantA, participantB));
        } else {
            String key = interactor.getDbId() + "#" + participantB.getReferenceEntity().getDbId() + "#" + participantA.getReferenceEntity().getDbId();
            return bridgeMap.computeIfAbsent(key, k -> new Bridge(interactor, participantB, participantA));
        }
    }

    private static Collection<PhysicalEntity> getCommonPhysicalEntities(Pathway a, Pathway b){
        try {
            String query = "" +
                    "MATCH (:Pathway{stId:{a}})-[:hasEvent*]->(:ReactionLikeEvent)-[:input|output|catalystActivity|physicalEntity|entityFunctionalStatus|diseaseEntity|regulatedBy|regulator*]->(pe:PhysicalEntity) " +
                    "WITH DISTINCT pe " +
                    "WHERE NOT (pe)-[:referenceEntity]->(:ReferenceEntity{trivial:true}) " +
                    "MATCH (:Pathway{stId:{b}})-[:hasEvent*]->(:ReactionLikeEvent)-[:input|output|catalystActivity|physicalEntity|entityFunctionalStatus|diseaseEntity|regulatedBy|regulator*]->(pe)" +
                    "RETURN DISTINCT pe";
            Map<String, Object> params = new HashMap<>();
            params.put("a", a.getStId());
            params.put("b", b.getStId());
            return ados.getCustomQueryResults(PhysicalEntity.class, query, params);
        } catch (CustomQueryException e) {
            e.printStackTrace();
            return new LinkedList<>();
        }
    }
}
