package com.cinechain.ui;

import com.cinechain.model.FilmRecommendation;
import com.cinechain.model.FilmRecommendation.ConsensusStatus;
import com.cinechain.service.LedgerDataService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.function.Consumer;

public final class FilmCardFactory {

    private FilmCardFactory() {}

    public static VBox createCard(
            FilmRecommendation film,
            Consumer<FilmRecommendation> onInspect,
            ToastManager toastManager
    ) {
        VBox card = new VBox();
        card.getStyleClass().add("movie-card");
        card.setPrefWidth(276);
        card.setMinWidth(250);
        card.setMaxWidth(320);
        card.setCursor(Cursor.HAND);

        // Clip outer container to 6px radius
        Rectangle cardClip = new Rectangle();
        cardClip.setArcWidth(12);
        cardClip.setArcHeight(12);
        cardClip.widthProperty().bind(card.widthProperty());
        cardClip.heightProperty().bind(card.heightProperty());
        card.setClip(cardClip);

        // 16:9 Header Poster Area
        StackPane posterArea = new StackPane();
        posterArea.setPrefHeight(162);
        posterArea.setMinHeight(162);
        posterArea.setMaxHeight(162);
        posterArea.getStyleClass().add("movie-poster-area");

        Canvas posterCanvas = buildPosterArtwork(film, 320, 162);
        posterArea.getChildren().add(posterCanvas);

        if (film.getPosterImageUrl() != null && !film.getPosterImageUrl().isBlank()) {
            ImageView imageView = new ImageView(new Image(film.getPosterImageUrl(), true));
            imageView.setFitWidth(320);
            imageView.setFitHeight(162);
            imageView.setPreserveRatio(false);
            posterArea.getChildren().add(imageView);
        }

        // Top overlay for consensus badge & match score
        HBox overlayTop = new HBox(8);
        overlayTop.setAlignment(Pos.TOP_LEFT);
        overlayTop.setPadding(new Insets(12));

        Label statusBadge = new Label();
        updateStatusBadge(statusBadge, film.getConsensusStatus());
        film.consensusStatusProperty().addListener((obs, oldVal, newVal) ->
                updateStatusBadge(statusBadge, newVal));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label matchChip = new Label(film.getMatchScore() + "% MATCH");
        matchChip.getStyleClass().add("match-score-chip");
        film.matchScoreProperty().addListener((obs, oldVal, newVal) ->
                matchChip.setText(newVal + "% MATCH"));

        overlayTop.getChildren().addAll(statusBadge, spacer, matchChip);
        StackPane.setAlignment(overlayTop, Pos.TOP_CENTER);
        posterArea.getChildren().add(overlayTop);

        // Card Body
        VBox body = new VBox(8);
        body.setPadding(new Insets(16));
        body.getStyleClass().add("movie-card-body");

        Label titleLabel = new Label();
        titleLabel.textProperty().bind(film.titleProperty());
        titleLabel.getStyleClass().add("title-md");
        titleLabel.setWrapText(false);

        Label metaLabel = new Label(film.getMetadataLine());
        metaLabel.getStyleClass().add("body-sm-muted");

        Label directorLabel = new Label("Dir. " + film.getDirector() + "  •  " + film.getValidatorCount() + " Validators");
        directorLabel.getStyleClass().add("body-sm-dim");
        film.validatorCountProperty().addListener((obs, oldVal, newVal) ->
                directorLabel.setText("Dir. " + film.getDirector() + "  •  " + newVal + " Validators"));

        // Blockchain Hash Bar
        HBox hashBar = new HBox(8);
        hashBar.setAlignment(Pos.CENTER_LEFT);
        hashBar.getStyleClass().add("hash-bar");
        hashBar.setPadding(new Insets(6, 10, 6, 10));

        Circle validationDot = new Circle(4);
        updateDotColor(validationDot, film.getConsensusStatus());
        film.consensusStatusProperty().addListener((obs, oldVal, newVal) ->
                updateDotColor(validationDot, newVal));

        Label hashLabel = new Label(LedgerDataService.abbreviateHash(film.getTxHash()));
        hashLabel.getStyleClass().add("code-sm");
        HBox.setHgrow(hashLabel, Priority.ALWAYS);

        Button copyBtn = new Button("COPY");
        copyBtn.getStyleClass().add("copy-hash-btn");
        copyBtn.setOnAction(e -> {
            e.consume();
            ClipboardContent content = new ClipboardContent();
            content.putString(film.getTxHash());
            Clipboard.getSystemClipboard().setContent(content);
            toastManager.show(
                    "Hash Copied",
                    LedgerDataService.abbreviateHash(film.getTxHash()) + " copied to clipboard.",
                    ToastManager.ToastType.INFO
            );
        });

        hashBar.getChildren().addAll(validationDot, hashLabel, copyBtn);

        body.getChildren().addAll(titleLabel, metaLabel, directorLabel, hashBar);
        card.getChildren().addAll(posterArea, body);

        card.setOnMouseClicked(e -> {
            if (onInspect != null) {
                onInspect.accept(film);
            }
        });

        return card;
    }

    private static void updateStatusBadge(Label badge, ConsensusStatus status) {
        badge.getStyleClass().removeAll("badge-active", "badge-inactive");
        if (status == ConsensusStatus.ACTIVE) {
            badge.setText("ACTIVE");
            badge.getStyleClass().add("badge-active");
        } else {
            badge.setText("INACTIVE");
            badge.getStyleClass().add("badge-inactive");
        }
    }

    private static void updateDotColor(Circle dot, ConsensusStatus status) {
        dot.setFill(status == ConsensusStatus.ACTIVE
                ? Color.web("#00C853")
                : Color.web("#FF5252"));
    }

    public static Canvas buildPosterArtwork(FilmRecommendation film, double width, double height) {
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Color accent = Color.web(film.getPosterAccentHex());
        Color secondary = Color.web(film.getPosterSecondaryHex());

        LinearGradient bg = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, secondary),
                new Stop(0.65, Color.web("#161616")),
                new Stop(1, Color.web("#121212"))
        );
        gc.setFill(bg);
        gc.fillRect(0, 0, width, height);

        // Subtle cinematic anamorphic lens lines & sprocket geometry
        gc.setStroke(accent.deriveColor(0, 1, 1, 0.25));
        gc.setLineWidth(1.2);
        gc.strokeOval(width * 0.56, -height * 0.2, height * 1.15, height * 1.15);
        gc.setStroke(accent.deriveColor(0, 1, 1, 0.14));
        gc.strokeOval(width * 0.62, -height * 0.05, height * 0.85, height * 0.85);

        // Film frame grid accents
        gc.setStroke(Color.web("#FFFFFF", 0.06));
        gc.setLineWidth(1.0);
        for (int x = 20; x < width; x += 48) {
            gc.strokeLine(x, 0, x, height);
        }
        gc.strokeLine(0, height * 0.52, width, height * 0.52);

        // Bottom dark scrim for card transition
        LinearGradient scrim = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT),
                new Stop(1, Color.web("#121212", 0.92))
        );
        gc.setFill(scrim);
        gc.fillRect(0, height * 0.45, width, height * 0.55);

        // Cryptographic block watermark at bottom-left of poster
        gc.setFill(Color.web("#E0E0E0", 0.72));
        gc.setFont(javafx.scene.text.Font.font("Monospaced", 11));
        gc.fillText("BLK #" + film.getBlockHeight(), 14, height - 14);

        return canvas;
    }
}
