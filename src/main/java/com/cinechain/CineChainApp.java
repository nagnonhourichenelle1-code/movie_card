package com.cinechain;

import com.cinechain.service.LedgerDataService;
import com.cinechain.controller.MainViewController;
import com.cinechain.ui.ToastManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Objects;

public class CineChainApp extends Application {

    private LedgerDataService dataService;

    @Override
    public void start(Stage primaryStage) {
        dataService = new LedgerDataService();

        StackPane rootStack = new StackPane();
        rootStack.getStyleClass().add("root-canvas");

        StackPane modalHost = new StackPane();
        modalHost.setPickOnBounds(false);

        StackPane toastHost = new StackPane();
        toastHost.setPickOnBounds(false);

        ToastManager toastManager = new ToastManager(toastHost);
        MainViewController mainController = new MainViewController(dataService, toastManager, modalHost);

        rootStack.getChildren().addAll(mainController.getRoot(), modalHost, toastHost);

        Scene scene = new Scene(rootStack, 1440, 900);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/com/cinechain/styles/cinechain.css")).toExternalForm()
        );

        primaryStage.setTitle("CineChain Cinematic Ledger");
        primaryStage.setMinWidth(1080);
        primaryStage.setMinHeight(720);
        primaryStage.setScene(scene);
        primaryStage.show();

        toastManager.show(
                "Mainnet Terminal Ready",
                "Synced to Block #" + dataService.currentBlockHeightProperty().get(),
                ToastManager.ToastType.INFO
        );
    }

    @Override
    public void stop() {
        if (dataService != null) {
            dataService.stopLiveConsensusFeed();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
