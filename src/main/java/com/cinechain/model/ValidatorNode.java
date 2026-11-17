package com.cinechain.model;

import javafx.beans.property.*;

public class ValidatorNode {

    public enum NodeState {
        ACTIVE,
        INACTIVE
    }

    private final StringProperty nodeId = new SimpleStringProperty();
    private final StringProperty region = new SimpleStringProperty();
    private final StringProperty walletAddress = new SimpleStringProperty();
    private final DoubleProperty stakedTokens = new SimpleDoubleProperty();
    private final DoubleProperty uptimePercent = new SimpleDoubleProperty();
    private final LongProperty lastValidatedBlock = new SimpleLongProperty();
    private final ObjectProperty<NodeState> state = new SimpleObjectProperty<>();

    public ValidatorNode(
            String nodeId,
            String region,
            String walletAddress,
            double stakedTokens,
            double uptimePercent,
            long lastValidatedBlock,
            NodeState state
    ) {
        this.nodeId.set(nodeId);
        this.region.set(region);
        this.walletAddress.set(walletAddress);
        this.stakedTokens.set(stakedTokens);
        this.uptimePercent.set(uptimePercent);
        this.lastValidatedBlock.set(lastValidatedBlock);
        this.state.set(state);
    }

    public String getNodeId() {
        return nodeId.get();
    }

    public StringProperty nodeIdProperty() {
        return nodeId;
    }

    public String getRegion() {
        return region.get();
    }

    public StringProperty regionProperty() {
        return region;
    }

    public String getWalletAddress() {
        return walletAddress.get();
    }

    public StringProperty walletAddressProperty() {
        return walletAddress;
    }

    public double getStakedTokens() {
        return stakedTokens.get();
    }

    public void setStakedTokens(double stake) {
        this.stakedTokens.set(stake);
    }

    public DoubleProperty stakedTokensProperty() {
        return stakedTokens;
    }

    public double getUptimePercent() {
        return uptimePercent.get();
    }

    public DoubleProperty uptimePercentProperty() {
        return uptimePercent;
    }

    public long getLastValidatedBlock() {
        return lastValidatedBlock.get();
    }

    public void setLastValidatedBlock(long block) {
        this.lastValidatedBlock.set(block);
    }

    public LongProperty lastValidatedBlockProperty() {
        return lastValidatedBlock;
    }

    public NodeState getState() {
        return state.get();
    }

    public void setState(NodeState state) {
        this.state.set(state);
    }

    public ObjectProperty<NodeState> stateProperty() {
        return state;
    }
}
