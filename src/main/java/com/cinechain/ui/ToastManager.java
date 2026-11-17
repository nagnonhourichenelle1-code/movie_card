package com.cinechain.ui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ToastManager {

    public enum ToastType {
        SUCCESS,
        INFO,
        ERROR
    }

    private final VBox toastContainer;

    public ToastManager(StackPane rootOverlayHost) {
        this.toastContainer = new VBox(10);
        this.toastContainer.setAlignment(Pos.BOTTOM_RIGHT);
        this.toastContainer.setPickOnBounds(false);
        this.toastContainer.setMaxWidth(380);
        this.toastContainer.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(toastContainer, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(toastContainer, new Insets(0, 24, 24, 0));
        rootOverlayHost.getChildren().add(toastContainer);
    }

    public void show(String title, String message, ToastType type) {
        HBox toast = new HBox(12);
        toast.getStyleClass().add("toast-card");
        toast.setMaxWidth(360);
        toast.setAlignment(Pos.CENTER_LEFT);

        Region accentStrip = new Region();
        accentStrip.setMinWidth(4);
        accentStrip.setPrefWidth(4);
        accentStrip.setMaxWidth(4);
        accentStrip.getStyleClass().add(switch (type) {
            case SUCCESS -> "toast-strip-success";
            case INFO -> "toast-strip-info";
            case ERROR -> "toast-strip-error";
        });

        VBox textCol = new VBox(3);
        HBox.setHgrow(textCol, Priority.ALWAYS);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("toast-title");
        titleLabel.setWrapText(true);

        Label detailLabel = new Label(message);
        detailLabel.getStyleClass().add("toast-detail");
        detailLabel.setWrapText(true);

        textCol.getChildren().addAll(titleLabel, detailLabel);
        toast.getChildren().addAll(accentStrip, textCol);

        toast.setOpacity(0);
        toast.setTranslateY(12);
        toastContainer.getChildren().add(toast);

        if (toastContainer.getChildren().size() > 4) {
            toastContainer.getChildren().remove(0);
        }

        FadeTransition fadeIn = new FadeTransition(Duration.millis(180), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(180), toast);
        slideIn.setFromY(12);
        slideIn.setToY(0);

        fadeIn.play();
        slideIn.play();

        PauseTransition hold = new PauseTransition(Duration.seconds(4));
        hold.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(220), toast);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(done -> toastContainer.getChildren().remove(toast));
            fadeOut.play();
        });
        hold.play();
    }
}
