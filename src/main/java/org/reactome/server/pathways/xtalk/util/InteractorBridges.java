package org.reactome.server.pathways.xtalk.util;

import org.reactome.server.graph.domain.model.ReferenceEntity;
import org.reactome.server.pathways.xtalk.model.Bridge;

import java.util.Collections;
import java.util.List;

public class InteractorBridges implements Comparable<InteractorBridges> {

    private ReferenceEntity interactor;
    private List<Bridge> bridgeList;
    private Double score = 0d;

    public InteractorBridges(ReferenceEntity interactor, List<Bridge> bridgeList) {
        this.interactor = interactor;
        this.bridgeList = bridgeList;
        for (Bridge bridge : bridgeList) {
            this.score = Math.max(bridge.getScore(), this.score);
        }
        bridgeList.sort(Collections.reverseOrder());
    }

    public ReferenceEntity getInteractor() {
        return interactor;
    }

    public List<Bridge> getBridgeList() {
        return bridgeList;
    }

    public Double getScore() {
        return score;
    }

    @Override
    public int compareTo(InteractorBridges o) {
        return this.score.compareTo(o.score);
    }
}
