package com.cinechain.model;

import javafx.beans.property.*;

public class FilmRecommendation {

    public enum ConsensusStatus {
        ACTIVE,
        INACTIVE
    }

    private final String id;
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty director = new SimpleStringProperty();
    private final IntegerProperty releaseYear = new SimpleIntegerProperty();
    private final StringProperty runtime = new SimpleStringProperty();
    private final StringProperty genre = new SimpleStringProperty();
    private final StringProperty synopsis = new SimpleStringProperty();
    private final IntegerProperty matchScore = new SimpleIntegerProperty();
    private final ObjectProperty<ConsensusStatus> consensusStatus = new SimpleObjectProperty<>();
    private final StringProperty txHash = new SimpleStringProperty();
    private final LongProperty blockHeight = new SimpleLongProperty();
    private final IntegerProperty validatorCount = new SimpleIntegerProperty();
    private final StringProperty posterAccentHex = new SimpleStringProperty();
    private final StringProperty posterSecondaryHex = new SimpleStringProperty();
    private final StringProperty posterImageUrl = new SimpleStringProperty();

    public FilmRecommendation(
            String id,
            String title,
            String director,
            int releaseYear,
            String runtime,
            String genre,
            String synopsis,
            int matchScore,
            ConsensusStatus consensusStatus,
            String txHash,
            long blockHeight,
            int validatorCount,
            String posterAccentHex,
            String posterSecondaryHex,
            String posterImageUrl
    ) {
        this.id = id;
        this.title.set(title);
        this.director.set(director);
        this.releaseYear.set(releaseYear);
        this.runtime.set(runtime);
        this.genre.set(genre);
        this.synopsis.set(synopsis);
        this.matchScore.set(matchScore);
        this.consensusStatus.set(consensusStatus);
        this.txHash.set(txHash);
        this.blockHeight.set(blockHeight);
        this.validatorCount.set(validatorCount);
        this.posterAccentHex.set(posterAccentHex);
        this.posterSecondaryHex.set(posterSecondaryHex);
        this.posterImageUrl.set(posterImageUrl);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public String getDirector() {
        return director.get();
    }

    public StringProperty directorProperty() {
        return director;
    }

    public int getReleaseYear() {
        return releaseYear.get();
    }

    public IntegerProperty releaseYearProperty() {
        return releaseYear;
    }

    public String getRuntime() {
        return runtime.get();
    }

    public StringProperty runtimeProperty() {
        return runtime;
    }

    public String getGenre() {
        return genre.get();
    }

    public StringProperty genreProperty() {
        return genre;
    }

    public String getSynopsis() {
        return synopsis.get();
    }

    public StringProperty synopsisProperty() {
        return synopsis;
    }

    public int getMatchScore() {
        return matchScore.get();
    }

    public IntegerProperty matchScoreProperty() {
        return matchScore;
    }

    public ConsensusStatus getConsensusStatus() {
        return consensusStatus.get();
    }

    public void setConsensusStatus(ConsensusStatus status) {
        this.consensusStatus.set(status);
    }

    public ObjectProperty<ConsensusStatus> consensusStatusProperty() {
        return consensusStatus;
    }

    public String getTxHash() {
        return txHash.get();
    }

    public StringProperty txHashProperty() {
        return txHash;
    }

    public long getBlockHeight() {
        return blockHeight.get();
    }

    public void setBlockHeight(long height) {
        this.blockHeight.set(height);
    }

    public LongProperty blockHeightProperty() {
        return blockHeight;
    }

    public int getValidatorCount() {
        return validatorCount.get();
    }

    public void setValidatorCount(int count) {
        this.validatorCount.set(count);
    }

    public IntegerProperty validatorCountProperty() {
        return validatorCount;
    }

    public String getPosterAccentHex() {
        return posterAccentHex.get();
    }

    public String getPosterSecondaryHex() {
        return posterSecondaryHex.get();
    }

    public String getPosterImageUrl() {
        return posterImageUrl.get();
    }

    public String getMetadataLine() {
        return getReleaseYear() + "  •  " + getRuntime() + "  •  " + getGenre();
    }
}
