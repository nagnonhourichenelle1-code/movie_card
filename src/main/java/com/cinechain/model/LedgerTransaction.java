package com.cinechain.model;

import javafx.beans.property.*;

public class LedgerTransaction {

    public enum TxStatus {
        VERIFIED,
        PENDING,
        REJECTED
    }

    private final LongProperty blockHeight = new SimpleLongProperty();
    private final StringProperty txHash = new SimpleStringProperty();
    private final StringProperty filmTitle = new SimpleStringProperty();
    private final StringProperty actionType = new SimpleStringProperty();
    private final StringProperty validatorNode = new SimpleStringProperty();
    private final StringProperty timestamp = new SimpleStringProperty();
    private final ObjectProperty<TxStatus> status = new SimpleObjectProperty<>();
    private final StringProperty stakeAmount = new SimpleStringProperty();
    private final StringProperty contractAddress = new SimpleStringProperty();
    private final BooleanProperty freshlyValidated = new SimpleBooleanProperty(false);

    public LedgerTransaction(
            long blockHeight,
            String txHash,
            String filmTitle,
            String actionType,
            String validatorNode,
            String timestamp,
            TxStatus status,
            String stakeAmount,
            String contractAddress,
            boolean freshlyValidated
    ) {
        this.blockHeight.set(blockHeight);
        this.txHash.set(txHash);
        this.filmTitle.set(filmTitle);
        this.actionType.set(actionType);
        this.validatorNode.set(validatorNode);
        this.timestamp.set(timestamp);
        this.status.set(status);
        this.stakeAmount.set(stakeAmount);
        this.contractAddress.set(contractAddress);
        this.freshlyValidated.set(freshlyValidated);
    }

    public long getBlockHeight() {
        return blockHeight.get();
    }

    public LongProperty blockHeightProperty() {
        return blockHeight;
    }

    public String getTxHash() {
        return txHash.get();
    }

    public StringProperty txHashProperty() {
        return txHash;
    }

    public String getFilmTitle() {
        return filmTitle.get();
    }

    public StringProperty filmTitleProperty() {
        return filmTitle;
    }

    public String getActionType() {
        return actionType.get();
    }

    public StringProperty actionTypeProperty() {
        return actionType;
    }

    public String getValidatorNode() {
        return validatorNode.get();
    }

    public StringProperty validatorNodeProperty() {
        return validatorNode;
    }

    public String getTimestamp() {
        return timestamp.get();
    }

    public StringProperty timestampProperty() {
        return timestamp;
    }

    public TxStatus getStatus() {
        return status.get();
    }

    public void setStatus(TxStatus newStatus) {
        this.status.set(newStatus);
    }

    public ObjectProperty<TxStatus> statusProperty() {
        return status;
    }

    public String getStakeAmount() {
        return stakeAmount.get();
    }

    public StringProperty stakeAmountProperty() {
        return stakeAmount;
    }

    public String getContractAddress() {
        return contractAddress.get();
    }

    public StringProperty contractAddressProperty() {
        return contractAddress;
    }

    public boolean isFreshlyValidated() {
        return freshlyValidated.get();
    }

    public void setFreshlyValidated(boolean fresh) {
        this.freshlyValidated.set(fresh);
    }

    public BooleanProperty freshlyValidatedProperty() {
        return freshlyValidated;
    }
}
