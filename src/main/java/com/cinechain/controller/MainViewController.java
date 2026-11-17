package com.cinechain.controller;

import com.cinechain.model.FilmRecommendation;
import com.cinechain.model.FilmRecommendation.ConsensusStatus;
import com.cinechain.model.LedgerTransaction;
import com.cinechain.model.LedgerTransaction.TxStatus;
import com.cinechain.model.ValidatorNode;
import com.cinechain.model.ValidatorNode.NodeState;
import com.cinechain.service.LedgerDataService;
import com.cinechain.ui.FilmCardFactory;
import com.cinechain.ui.ToastManager;

import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.text.NumberFormat;
import java.util.Locale;

public class MainViewController {

    private enum Screen {
        DISCOVERY,
        CONSENSUS_LOG,
        VALIDATORS,
        SUBMIT
    }

    private static final PseudoClass FRESH_ROW_PSEUDO = PseudoClass.getPseudoClass("fresh-row");

    private final LedgerDataService dataService;
    private final ToastManager toastManager;
    private final StackPane modalHost;
    private final BorderPane shellLayout;

    private VBox utilityRail;
    private boolean railCollapsed = false;
    private Label brandWordmark;
    private Label brandSubtitle;
    private Label railBlockLabel;
    private Button navDiscoveryBtn;
    private Button navConsensusBtn;
    private Button navValidatorsBtn;
    private Button navSubmitBtn;
    private Button collapseBtn;

    
    private final StackPane contentStage = new StackPane();
    

    // Cached screen roots
    private Node discoveryView;
    private Node consensusLogView;
    private Node validatorsView;
    private Node submitView;

    // Discovery filter state
    private final FilteredList<FilmRecommendation> filteredFilms;
    private FlowPane movieGrid;
    private String activeStatusFilter = "ALL";
    private String searchQuery = "";

    public MainViewController(LedgerDataService dataService, ToastManager toastManager, StackPane modalHost) {
        this.dataService = dataService;
        this.toastManager = toastManager;
        this.modalHost = modalHost;
        this.filteredFilms = new FilteredList<>(dataService.getFilms(), f -> true);

        this.shellLayout = new BorderPane();
        this.shellLayout.getStyleClass().add("root-canvas");

        buildUtilityRail();
        buildAllScreens();

        shellLayout.setLeft(utilityRail);
        shellLayout.setCenter(contentStage);

        switchScreen(Screen.DISCOVERY);
        bindLiveConsensusNotifications();
    }

    public BorderPane getRoot() {
        return shellLayout;
    }

    private void buildUtilityRail() {
        utilityRail = new VBox(16);
        utilityRail.getStyleClass().add("utility-rail");
        utilityRail.setPrefWidth(244);
        utilityRail.setMinWidth(244);
        utilityRail.setMaxWidth(244);
        utilityRail.setPadding(new Insets(24, 14, 24, 14));

        // Brand Header
        HBox brandRow = new HBox(10);
        brandRow.setAlignment(Pos.CENTER_LEFT);

        VBox brandText = new VBox(2);
        brandWordmark = new Label("CineChain");
        brandWordmark.getStyleClass().add("brand-wordmark");
        brandSubtitle = new Label("CINEMATIC LEDGER");
        brandSubtitle.getStyleClass().add("brand-subtitle");
        brandText.getChildren().addAll(brandWordmark, brandSubtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        collapseBtn = new Button("<<");
        collapseBtn.getStyleClass().add("rail-collapse-btn");
        collapseBtn.setOnAction(e -> toggleRail());

        brandRow.getChildren().addAll(brandText, spacer, collapseBtn);

        // Navigation Items
        VBox navList = new VBox(8);
        navList.setPadding(new Insets(16, 0, 0, 0));

        navDiscoveryBtn = createNavButton("Discovery & Curations", "D", Screen.DISCOVERY);
        navConsensusBtn = createNavButton("Consensus Log", "L", Screen.CONSENSUS_LOG);
        navValidatorsBtn = createNavButton("Validator Nodes", "V", Screen.VALIDATORS);
        navSubmitBtn = createNavButton("Submit Recommendation", "+", Screen.SUBMIT);

        navList.getChildren().addAll(navDiscoveryBtn, navConsensusBtn, navValidatorsBtn, navSubmitBtn);

        Region verticalSpacer = new Region();
        VBox.setVgrow(verticalSpacer, Priority.ALWAYS);

        // Live Block Height Telemetry Card inside rail footer
        VBox blockBox = new VBox(6);
        blockBox.getStyleClass().add("hash-bar");
        blockBox.setPadding(new Insets(12));

        HBox blockTop = new HBox(8);
        blockTop.setAlignment(Pos.CENTER_LEFT);
        Circle liveDot = new Circle(4, Color.web("#00C853"));
        Label netTitle = new Label("MAINNET SYNC");
        netTitle.getStyleClass().add("label-md");
        blockTop.getChildren().addAll(liveDot, netTitle);

        railBlockLabel = new Label();
        railBlockLabel.getStyleClass().add("code-accent");
        updateRailBlockText(dataService.currentBlockHeightProperty().get());
        dataService.currentBlockHeightProperty().addListener((obs, o, n) -> updateRailBlockText(n.longValue()));

        blockBox.getChildren().addAll(blockTop, railBlockLabel);

        utilityRail.getChildren().addAll(brandRow, navList, verticalSpacer, blockBox);
    }

    private void updateRailBlockText(long height) {
        if (railCollapsed) {
            railBlockLabel.setText("#" + (height % 10000));
        } else {
            railBlockLabel.setText("Block #" + NumberFormat.getNumberInstance(Locale.US).format(height));
        }
    }

    private Button createNavButton(String fullText, String compactGlyph, Screen target) {
        Button btn = new Button(fullText);
        btn.getStyleClass().add("nav-item");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setUserData(new String[]{fullText, compactGlyph});
        btn.setOnAction(e -> switchScreen(target));
        return btn;
    }

    private void toggleRail() {
        railCollapsed = !railCollapsed;
        double targetWidth = railCollapsed ? 76 : 244;
        utilityRail.setPrefWidth(targetWidth);
        utilityRail.setMinWidth(targetWidth);
        utilityRail.setMaxWidth(targetWidth);

        brandWordmark.setVisible(!railCollapsed);
        brandWordmark.setManaged(!railCollapsed);
        brandSubtitle.setVisible(!railCollapsed);
        brandSubtitle.setManaged(!railCollapsed);
        collapseBtn.setText(railCollapsed ? ">>" : "<<");

        for (Button btn : new Button[]{navDiscoveryBtn, navConsensusBtn, navValidatorsBtn, navSubmitBtn}) {
            String[] labels = (String[]) btn.getUserData();
            btn.setText(railCollapsed ? labels[1] : labels[0]);
            btn.setAlignment(railCollapsed ? Pos.CENTER : Pos.CENTER_LEFT);
        }
        updateRailBlockText(dataService.currentBlockHeightProperty().get());
    }

    private void switchScreen(Screen screen) {
        for (Button btn : new Button[]{navDiscoveryBtn, navConsensusBtn, navValidatorsBtn, navSubmitBtn}) {
            btn.getStyleClass().remove("nav-item-active");
        }
        Node targetNode = switch (screen) {
            case DISCOVERY -> {
                navDiscoveryBtn.getStyleClass().add("nav-item-active");
                yield discoveryView;
            }
            case CONSENSUS_LOG -> {
                navConsensusBtn.getStyleClass().add("nav-item-active");
                yield consensusLogView;
            }
            case VALIDATORS -> {
                navValidatorsBtn.getStyleClass().add("nav-item-active");
                yield validatorsView;
            }
            case SUBMIT -> {
                navSubmitBtn.getStyleClass().add("nav-item-active");
                yield submitView;
            }
        };
        contentStage.getChildren().setAll(targetNode);
    }

    private void buildAllScreens() {
        discoveryView = buildDiscoveryScreen();
        consensusLogView = buildConsensusLogScreen();
        validatorsView = buildValidatorsScreen();
        submitView = buildSubmitScreen();
    }

    // -------------------------------------------------------------------------
    // SCREEN 1: DISCOVERY & CURATIONS
    // -------------------------------------------------------------------------
    private Node buildDiscoveryScreen() {
        VBox page = new VBox(24);
        page.setPadding(new Insets(32));

        // Top Bar Header
        HBox headerRow = new HBox(16);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleCol = new VBox(4);
        Label pageTitle = new Label("Trending On-Chain Recommendations");
        pageTitle.getStyleClass().add("headline-lg");
        Label pageSub = new Label("Cryptographically verified film curations backed by decentralized validator consensus.");
        pageSub.getStyleClass().add("body-md-muted");
        titleCol.getChildren().addAll(pageTitle, pageSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button submitCta = new Button("+ Submit Curation");
        submitCta.getStyleClass().add("btn-primary");
        submitCta.setOnAction(e -> switchScreen(Screen.SUBMIT));

        headerRow.getChildren().addAll(titleCol, spacer, submitCta);

        // Featured Hero Showcase Card
        FilmRecommendation featured = dataService.getFilms().get(0);
        HBox heroCard = new HBox(24);
        heroCard.getStyleClass().add("hero-card");
        heroCard.setPadding(new Insets(24));
        heroCard.setAlignment(Pos.CENTER_LEFT);

        StackPane heroPoster = new StackPane(FilmCardFactory.buildPosterArtwork(featured, 360, 202));
        heroPoster.setMinSize(360, 202);
        heroPoster.setMaxSize(360, 202);

        VBox heroInfo = new VBox(12);
        HBox.setHgrow(heroInfo, Priority.ALWAYS);

        HBox heroBadges = new HBox(10);
        heroBadges.setAlignment(Pos.CENTER_LEFT);
        Label featuredTag = new Label("FEATURED CONSENSUS");
        featuredTag.getStyleClass().add("badge-active");
        Label heroHashChip = new Label("VERIFIED  " + LedgerDataService.abbreviateHash(featured.getTxHash()));
        heroHashChip.getStyleClass().addAll("hash-chip", "code-sm");
        heroBadges.getChildren().addAll(featuredTag, heroHashChip);

        Label heroTitle = new Label(featured.getTitle());
        heroTitle.getStyleClass().add("headline-xl");

        Label heroMeta = new Label(featured.getMetadataLine() + "  •  Dir. " + featured.getDirector() + "  •  " + featured.getMatchScore() + "% Match");
        heroMeta.getStyleClass().add("body-sm-muted");

        Label heroSynopsis = new Label(featured.getSynopsis());
        heroSynopsis.getStyleClass().add("body-md");
        heroSynopsis.setWrapText(true);
        heroSynopsis.setMaxWidth(640);

        HBox heroActions = new HBox(12);
        Button validateHeroBtn = new Button("Validate On-Chain");
        validateHeroBtn.getStyleClass().add("btn-primary");
        validateHeroBtn.setOnAction(e -> {
            LedgerTransaction tx = dataService.validateFilmOnChain(featured);
            toastManager.show(
                    "Block #" + tx.getBlockHeight() + " Validated",
                    featured.getTitle() + " consensus attested (" + featured.getValidatorCount() + " validators).",
                    ToastManager.ToastType.SUCCESS
            );
        });

        Button inspectHeroBtn = new Button("Inspect Contract");
        inspectHeroBtn.getStyleClass().add("btn-secondary");
        inspectHeroBtn.setOnAction(e -> openFilmModal(featured));

        heroActions.getChildren().addAll(validateHeroBtn, inspectHeroBtn);
        heroInfo.getChildren().addAll(heroBadges, heroTitle, heroMeta, heroSynopsis, heroActions);
        heroCard.getChildren().addAll(heroPoster, heroInfo);

        // Filter & Search Toolbar
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        Button allTab = new Button("All Films");
        Button activeTab = new Button("Active Consensus");
        Button inactiveTab = new Button("Inactive / Disputed");

        allTab.getStyleClass().addAll("filter-tab", "filter-tab-selected");
        activeTab.getStyleClass().add("filter-tab");
        inactiveTab.getStyleClass().add("filter-tab");

        allTab.setOnAction(e -> selectStatusFilter("ALL", allTab, activeTab, inactiveTab));
        activeTab.setOnAction(e -> selectStatusFilter("ACTIVE", activeTab, allTab, inactiveTab));
        inactiveTab.setOnAction(e -> selectStatusFilter("INACTIVE", inactiveTab, allTab, activeTab));

        Region filterSpacer = new Region();
        HBox.setHgrow(filterSpacer, Priority.ALWAYS);

        TextField searchInput = new TextField();
        searchInput.setPromptText("Search title, director, genre, or hash...");
        searchInput.getStyleClass().add("cine-input");
        searchInput.setPrefWidth(300);
        searchInput.textProperty().addListener((obs, o, n) -> {
            searchQuery = n == null ? "" : n.trim().toLowerCase(Locale.ROOT);
            applyFilmFilter();
        });

        filterBar.getChildren().addAll(allTab, activeTab, inactiveTab, filterSpacer, searchInput);

        // Movie Card Grid
        movieGrid = new FlowPane(24, 24);
        movieGrid.setPrefWrapLength(1100);
        rebuildMovieGrid();
        filteredFilms.addListener((ListChangeListener<FilmRecommendation>) c -> rebuildMovieGrid());

        page.getChildren().addAll(headerRow, heroCard, filterBar, movieGrid);

        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("cine-scroll");
        return scrollPane;
    }

    private void selectStatusFilter(String mode, Button selected, Button... others) {
        this.activeStatusFilter = mode;
        selected.getStyleClass().add("filter-tab-selected");
        for (Button b : others) {
            b.getStyleClass().remove("filter-tab-selected");
        }
        applyFilmFilter();
    }

    private void applyFilmFilter() {
        filteredFilms.setPredicate(film -> {
            boolean statusMatch = switch (activeStatusFilter) {
                case "ACTIVE" -> film.getConsensusStatus() == ConsensusStatus.ACTIVE;
                case "INACTIVE" -> film.getConsensusStatus() == ConsensusStatus.INACTIVE;
                default -> true;
            };
            if (!statusMatch) return false;
            if (searchQuery.isEmpty()) return true;

            return film.getTitle().toLowerCase(Locale.ROOT).contains(searchQuery)
                    || film.getDirector().toLowerCase(Locale.ROOT).contains(searchQuery)
                    || film.getGenre().toLowerCase(Locale.ROOT).contains(searchQuery)
                    || film.getTxHash().toLowerCase(Locale.ROOT).contains(searchQuery);
        });
    }

    private void rebuildMovieGrid() {
        movieGrid.getChildren().clear();
        for (FilmRecommendation film : filteredFilms) {
            movieGrid.getChildren().add(FilmCardFactory.createCard(film, this::openFilmModal, toastManager));
        }
        if (filteredFilms.isEmpty()) {
            Label empty = new Label("No on-chain curations match the active filter criteria.");
            empty.getStyleClass().add("body-md-muted");
            movieGrid.getChildren().add(empty);
        }
    }

    // -------------------------------------------------------------------------
    // SCREEN 2: CONSENSUS LOG (8-col TableView + 4-col Inspection Rail)
    // -------------------------------------------------------------------------
    private Node buildConsensusLogScreen() {
        VBox page = new VBox(24);
        page.setPadding(new Insets(32));

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleCol = new VBox(4);
        Label title = new Label("Consensus Log");
        title.getStyleClass().add("headline-lg");
        Label subtitle = new Label("Real-time cryptographic ledger stream and smart contract state transitions.");
        subtitle.getStyleClass().add("body-md-muted");
        titleCol.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button verifyBlockBtn = new Button("Trigger Checkpoint Audit");
        verifyBlockBtn.getStyleClass().add("btn-secondary");
        verifyBlockBtn.setOnAction(e -> toastManager.show(
                "Checkpoint Verified",
                "Merkle state root verified at Block #" + dataService.currentBlockHeightProperty().get(),
                ToastManager.ToastType.INFO
        ));
        header.getChildren().addAll(titleCol, spacer, verifyBlockBtn);

        // Split: 8-col Table + 4-col Inspector Rail
        HBox split = new HBox(24);
        VBox.setVgrow(split, Priority.ALWAYS);

        TableView<LedgerTransaction> table = new TableView<>(dataService.getTransactions());
        table.getStyleClass().add("ledger-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        HBox.setHgrow(table, Priority.ALWAYS);

        table.setRowFactory(tv -> {
            TableRow<LedgerTransaction> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem != null && newItem.isFreshlyValidated()) {
                    row.pseudoClassStateChanged(FRESH_ROW_PSEUDO, true);
                    PauseTransition flash = new PauseTransition(Duration.millis(400));
                    flash.setOnFinished(ev -> {
                        row.pseudoClassStateChanged(FRESH_ROW_PSEUDO, false);
                        newItem.setFreshlyValidated(false);
                    });
                    flash.play();
                } else {
                    row.pseudoClassStateChanged(FRESH_ROW_PSEUDO, false);
                }
            });
            return row;
        });

        TableColumn<LedgerTransaction, String> blockCol = new TableColumn<>("BLOCK");
        blockCol.setCellValueFactory(c -> new SimpleStringProperty("#" + c.getValue().getBlockHeight()));
        blockCol.setMinWidth(105);

        TableColumn<LedgerTransaction, String> hashCol = new TableColumn<>("TX HASH");
        hashCol.setCellValueFactory(c -> c.getValue().txHashProperty());
        hashCol.setMinWidth(160);

        TableColumn<LedgerTransaction, String> filmCol = new TableColumn<>("CURATION TITLE");
        filmCol.setCellValueFactory(c -> c.getValue().filmTitleProperty());
        filmCol.setMinWidth(160);

        TableColumn<LedgerTransaction, String> actionCol = new TableColumn<>("EVENT");
        actionCol.setCellValueFactory(c -> c.getValue().actionTypeProperty());
        actionCol.setMinWidth(145);

        TableColumn<LedgerTransaction, String> stakeCol = new TableColumn<>("STAKE");
        stakeCol.setCellValueFactory(c -> c.getValue().stakeAmountProperty());
        stakeCol.setMinWidth(105);

        TableColumn<LedgerTransaction, TxStatus> statusCol = new TableColumn<>("STATE");
        statusCol.setMinWidth(105);
        statusCol.setCellValueFactory(c -> c.getValue().statusProperty());
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(TxStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(item.name());
                badge.getStyleClass().add(switch (item) {
                    case VERIFIED -> "badge-active";
                    case PENDING -> "badge-pending";
                    case REJECTED -> "badge-inactive";
                });
                setGraphic(badge);
            }
        });

        TableColumn<LedgerTransaction, String> timeCol = new TableColumn<>("TIME");
        timeCol.setCellValueFactory(c -> c.getValue().timestampProperty());
        timeCol.setMinWidth(85);

        table.getColumns().addAll(blockCol, hashCol, filmCol, actionCol, stakeCol, statusCol, timeCol);

        // Contextual 4-column Inspector Rail
        VBox inspector = new VBox(16);
        inspector.getStyleClass().add("surface-card");
        inspector.setPadding(new Insets(20));
        inspector.setPrefWidth(340);
        inspector.setMinWidth(320);

        Label inspTitle = new Label("Transaction Inspector");
        inspTitle.getStyleClass().add("title-md");
        Label inspHint = new Label("Select any block row to audit cryptographic signature and contract state.");
        inspHint.getStyleClass().add("body-sm-muted");
        inspHint.setWrapText(true);

        VBox detailsBox = new VBox(12);
        Label dBlock = new Label("Block: —");
        dBlock.getStyleClass().add("code-accent");
        Label dFilm = new Label("Film: —");
        dFilm.getStyleClass().add("body-md");
        Label dValidator = new Label("Validator: —");
        dValidator.getStyleClass().add("body-sm-muted");
        Label dContract = new Label("Contract: —");
        dContract.getStyleClass().add("code-sm");
        Label dHash = new Label("Hash: —");
        dHash.getStyleClass().add("code-sm");
        dHash.setWrapText(true);

        Button copyInspectorHash = new Button("Copy Signature Hash");
        copyInspectorHash.getStyleClass().add("btn-secondary");
        copyInspectorHash.setMaxWidth(Double.MAX_VALUE);
        copyInspectorHash.setDisable(true);

        table.getSelectionModel().selectedItemProperty().addListener((obs, o, selected) -> {
            if (selected == null) return;
            dBlock.setText("Block #" + selected.getBlockHeight() + "  •  " + selected.getTimestamp());
            dFilm.setText(selected.getFilmTitle() + " (" + selected.getActionType() + ")");
            dValidator.setText("Validator Node: " + selected.getValidatorNode());
            dContract.setText("Contract: " + selected.getContractAddress());
            dHash.setText("Tx Hash: " + selected.getTxHash());
            copyInspectorHash.setDisable(false);
            copyInspectorHash.setOnAction(ev -> {
                ClipboardContent content = new ClipboardContent();
                content.putString(selected.getTxHash());
                Clipboard.getSystemClipboard().setContent(content);
                toastManager.show("Signature Copied", selected.getTxHash(), ToastManager.ToastType.INFO);
            });
        });

        if (!dataService.getTransactions().isEmpty()) {
            table.getSelectionModel().selectFirst();
        }

        detailsBox.getChildren().addAll(dBlock, dFilm, dValidator, dContract, dHash);
        inspector.getChildren().addAll(inspTitle, inspHint, new Separator(), detailsBox, copyInspectorHash);

        split.getChildren().addAll(table, inspector);
        page.getChildren().addAll(header, split);
        return page;
    }

    // -------------------------------------------------------------------------
    // SCREEN 3: VALIDATOR NODES
    // -------------------------------------------------------------------------
    private Node buildValidatorsScreen() {
        VBox page = new VBox(24);
        page.setPadding(new Insets(32));

        Label title = new Label("Consensus Validator Fleet");
        title.getStyleClass().add("headline-lg");
        Label subtitle = new Label("Monitor active quorum nodes, staked CINE collateral, and node consensus health.");
        subtitle.getStyleClass().add("body-md-muted");

        VBox headerBox = new VBox(4, title, subtitle);

        // Summary Metric Strip
        HBox statsStrip = new HBox(24);
        long activeCount = dataService.getValidators().stream().filter(v -> v.getState() == NodeState.ACTIVE).count();
        statsStrip.getChildren().addAll(
                createMetricCard("ACTIVE VALIDATORS", activeCount + " / " + dataService.getValidators().size(), "#00C853"),
                createMetricCard("TOTAL QUORUM STAKE", "594,050 CINE", "#2979FF"),
                createMetricCard("MEAN FINALITY TIME", "1.42 Seconds", "#E0E0E0")
        );

        TableView<ValidatorNode> nodeTable = new TableView<>(dataService.getValidators());
        nodeTable.getStyleClass().add("ledger-table");
        nodeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(nodeTable, Priority.ALWAYS);

        TableColumn<ValidatorNode, String> idCol = new TableColumn<>("NODE ID");
        idCol.setCellValueFactory(c -> c.getValue().nodeIdProperty());

        TableColumn<ValidatorNode, String> regionCol = new TableColumn<>("REGION");
        regionCol.setCellValueFactory(c -> c.getValue().regionProperty());

        TableColumn<ValidatorNode, String> walletCol = new TableColumn<>("WALLET");
        walletCol.setCellValueFactory(c -> c.getValue().walletAddressProperty());

        TableColumn<ValidatorNode, String> stakeCol = new TableColumn<>("STAKED CINE");
        stakeCol.setCellValueFactory(c -> new SimpleStringProperty(
                NumberFormat.getNumberInstance(Locale.US).format(c.getValue().getStakedTokens()) + " CINE"));

        TableColumn<ValidatorNode, String> uptimeCol = new TableColumn<>("UPTIME");
        uptimeCol.setCellValueFactory(c -> new SimpleStringProperty(
                String.format(Locale.US, "%.2f%%", c.getValue().getUptimePercent())));

        TableColumn<ValidatorNode, NodeState> stateCol = new TableColumn<>("STATUS");
        stateCol.setCellValueFactory(c -> c.getValue().stateProperty());
        stateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(NodeState item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(item.name());
                badge.getStyleClass().add(item == NodeState.ACTIVE ? "badge-active" : "badge-inactive");
                setGraphic(badge);
            }
        });

        TableColumn<ValidatorNode, Void> actionCol = new TableColumn<>("ACTION");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button toggleBtn = new Button();

            {
                toggleBtn.setOnAction(e -> {
                    ValidatorNode node = getTableView().getItems().get(getIndex());
                    if (node.getState() == NodeState.ACTIVE) {
                        node.setState(NodeState.INACTIVE);
                        toastManager.show("Node Slashed / Paused", node.getNodeId() + " set to INACTIVE.", ToastManager.ToastType.ERROR);
                    } else {
                        node.setState(NodeState.ACTIVE);
                        toastManager.show("Node Rejoined Quorum", node.getNodeId() + " is now ACTIVE.", ToastManager.ToastType.SUCCESS);
                    }
                    getTableView().refresh();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                ValidatorNode node = getTableView().getItems().get(getIndex());
                toggleBtn.getStyleClass().removeAll("btn-secondary", "btn-reject");
                if (node.getState() == NodeState.ACTIVE) {
                    toggleBtn.setText("Suspend Node");
                    toggleBtn.getStyleClass().add("btn-reject");
                } else {
                    toggleBtn.setText("Activate Node");
                    toggleBtn.getStyleClass().add("btn-secondary");
                }
                setGraphic(toggleBtn);
            }
        });

        nodeTable.getColumns().addAll(idCol, regionCol, walletCol, stakeCol, uptimeCol, stateCol, actionCol);

        page.getChildren().addAll(headerBox, statsStrip, nodeTable);
        return page;
    }

    private VBox createMetricCard(String label, String value, String valueColorHex) {
        VBox box = new VBox(6);
        box.getStyleClass().add("surface-card");
        box.setPadding(new Insets(16, 20, 16, 20));
        HBox.setHgrow(box, Priority.ALWAYS);

        Label k = new Label(label);
        k.getStyleClass().add("label-md");

        Label v = new Label(value);
        v.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + valueColorHex + ";");

        box.getChildren().addAll(k, v);
        return box;
    }

    // -------------------------------------------------------------------------
    // SCREEN 4: SUBMIT RECOMMENDATION
    // -------------------------------------------------------------------------
    private Node buildSubmitScreen() {
        VBox page = new VBox(24);
        page.setPadding(new Insets(32));

        Label title = new Label("Submit On-Chain Curation");
        title.getStyleClass().add("headline-lg");
        Label subtitle = new Label("Anchor a new film recommendation hash to the CineChain consensus ledger.");
        subtitle.getStyleClass().add("body-md-muted");

        VBox formCard = new VBox(16);
        formCard.getStyleClass().add("surface-card");
        formCard.setPadding(new Insets(24));
        formCard.setMaxWidth(760);

        TextField titleInput = new TextField();
        titleInput.setPromptText("Film Title (e.g., Cryptographic Horizon)");
        titleInput.getStyleClass().add("cine-input");

        HBox rowTwo = new HBox(16);
        TextField directorInput = new TextField();
        directorInput.setPromptText("Director");
        directorInput.getStyleClass().add("cine-input");
        HBox.setHgrow(directorInput, Priority.ALWAYS);

        TextField yearInput = new TextField("2026");
        yearInput.setPromptText("Year");
        yearInput.getStyleClass().add("cine-input");
        yearInput.setPrefWidth(110);

        TextField runtimeInput = new TextField("124 min");
        runtimeInput.setPromptText("Runtime");
        runtimeInput.getStyleClass().add("cine-input");
        runtimeInput.setPrefWidth(130);
        rowTwo.getChildren().addAll(directorInput, yearInput, runtimeInput);

        ComboBox<String> genreCombo = new ComboBox<>();
        genreCombo.getItems().addAll("Sci-Fi Thriller", "Cyber Mystery", "Neo-Noir Drama", "Documentary", "Experimental");
        genreCombo.setValue("Sci-Fi Thriller");
        genreCombo.getStyleClass().add("cine-combo");
        genreCombo.setMaxWidth(Double.MAX_VALUE);

        TextArea synopsisInput = new TextArea();
        synopsisInput.setPromptText("Synopsis & provenance notes...");
        synopsisInput.getStyleClass().add("cine-textarea");
        synopsisInput.setPrefRowCount(4);
        synopsisInput.setWrapText(true);

        // Radio Tier Selection
        ToggleGroup tierGroup = new ToggleGroup();
        RadioButton fastStakeRadio = new RadioButton("Stake 500 CINE for Immediate Quorum Validation (ACTIVE)");
        fastStakeRadio.setToggleGroup(tierGroup);
        fastStakeRadio.setSelected(true);
        fastStakeRadio.getStyleClass().add("cine-radio");

        RadioButton standardRadio = new RadioButton("Submit Unstaked Proposal (INACTIVE until 100 validator votes)");
        standardRadio.setToggleGroup(tierGroup);
        standardRadio.getStyleClass().add("cine-radio");

        CheckBox attestCheck = new CheckBox("I cryptographically attest that metadata and hash integrity are accurate.");
        attestCheck.setSelected(true);
        attestCheck.getStyleClass().add("cine-checkbox");

        HBox submitRow = new HBox(12);
        submitRow.setAlignment(Pos.CENTER_RIGHT);

        Button clearBtn = new Button("Reset");
        clearBtn.getStyleClass().add("btn-secondary");
        clearBtn.setOnAction(e -> {
            titleInput.clear();
            directorInput.clear();
            synopsisInput.clear();
        });

        Button submitBtn = new Button("Anchor Recommendation On-Chain");
        submitBtn.getStyleClass().add("btn-primary");
        submitBtn.setOnAction(e -> {
            String filmTitle = titleInput.getText().trim();
            String director = directorInput.getText().trim();
            if (filmTitle.isEmpty() || director.isEmpty()) {
                toastManager.show("Validation Error", "Film title and director are required.", ToastManager.ToastType.ERROR);
                return;
            }
            if (!attestCheck.isSelected()) {
                toastManager.show("Attestation Required", "Confirm cryptographic attestation checkbox.", ToastManager.ToastType.ERROR);
                return;
            }
            int parsedYear = 2026;
            try {
                parsedYear = Integer.parseInt(yearInput.getText().trim());
            } catch (NumberFormatException ignored) {}

            FilmRecommendation created = dataService.submitNewRecommendation(
                    filmTitle,
                    director,
                    parsedYear,
                    runtimeInput.getText().trim().isEmpty() ? "120 min" : runtimeInput.getText().trim(),
                    genreCombo.getValue(),
                    synopsisInput.getText().trim().isEmpty()
                            ? "Decentralized curation submitted via CineChain Desktop Terminal."
                            : synopsisInput.getText().trim(),
                    fastStakeRadio.isSelected()
            );

            toastManager.show(
                    "Curation Anchored",
                    created.getTitle() + " committed at Block #" + created.getBlockHeight(),
                    ToastManager.ToastType.SUCCESS
            );
            titleInput.clear();
            directorInput.clear();
            synopsisInput.clear();
            switchScreen(Screen.DISCOVERY);
        });

        submitRow.getChildren().addAll(clearBtn, submitBtn);

        formCard.getChildren().addAll(
                createFieldLabel("FILM TITLE"), titleInput,
                createFieldLabel("DIRECTOR, RELEASE YEAR & RUNTIME"), rowTwo,
                createFieldLabel("GENRE CLASSIFICATION"), genreCombo,
                createFieldLabel("SYNOPSIS"), synopsisInput,
                createFieldLabel("CONSENSUS TIER"), fastStakeRadio, standardRadio,
                attestCheck,
                submitRow
        );

        page.getChildren().addAll(new VBox(4, title, subtitle), formCard);

        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("cine-scroll");
        return scrollPane;
    }

    private Label createFieldLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("label-md");
        return l;
    }

    // -------------------------------------------------------------------------
    // LEVEL 2 OVERLAY: FILM INSPECTION MODAL
    // -------------------------------------------------------------------------
    private void openFilmModal(FilmRecommendation film) {
        StackPane backdrop = new StackPane();
        backdrop.getStyleClass().add("modal-backdrop");
        backdrop.setPadding(new Insets(32));

        VBox modal = new VBox(16);
        modal.getStyleClass().add("modal-panel");
        modal.setMaxWidth(620);
        modal.setMaxHeight(Region.USE_PREF_SIZE);
        modal.setPadding(new Insets(24));

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);
        Label badge = new Label(film.getConsensusStatus().name());
        badge.getStyleClass().add(film.getConsensusStatus() == ConsensusStatus.ACTIVE ? "badge-active" : "badge-inactive");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("rail-collapse-btn");
        closeBtn.setOnAction(e -> modalHost.getChildren().remove(backdrop));
        topRow.getChildren().addAll(badge, spacer, closeBtn);

        Label title = new Label(film.getTitle());
        title.getStyleClass().add("headline-lg");

        Label meta = new Label(film.getMetadataLine() + "  •  Dir. " + film.getDirector());
        meta.getStyleClass().add("body-sm-muted");

        Label synopsis = new Label(film.getSynopsis());
        synopsis.getStyleClass().add("body-md");
        synopsis.setWrapText(true);

        VBox hashBox = new VBox(6);
        hashBox.getStyleClass().add("hash-bar");
        hashBox.setPadding(new Insets(12));
        Label hashHeader = new Label("CRYPTOGRAPHIC LEDGER HASH");
        hashHeader.getStyleClass().add("label-md");
        Label fullHash = new Label(film.getTxHash());
        fullHash.getStyleClass().add("code-accent");
        fullHash.setWrapText(true);
        hashBox.getChildren().addAll(hashHeader, fullHash);

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button rejectBtn = new Button("Reject State");
        rejectBtn.getStyleClass().add("btn-reject");
        rejectBtn.setOnAction(e -> {
            LedgerTransaction tx = dataService.rejectFilmOnChain(film);
            toastManager.show(
                    "Consensus Rejected",
                    film.getTitle() + " marked INACTIVE at Block #" + tx.getBlockHeight(),
                    ToastManager.ToastType.ERROR
            );
            modalHost.getChildren().remove(backdrop);
        });

        Button validateBtn = new Button("Validate On-Chain");
        validateBtn.getStyleClass().add("btn-primary");
        validateBtn.setOnAction(e -> {
            LedgerTransaction tx = dataService.validateFilmOnChain(film);
            toastManager.show(
                    "Block #" + tx.getBlockHeight() + " Validated",
                    film.getTitle() + " attested on-chain.",
                    ToastManager.ToastType.SUCCESS
            );
            modalHost.getChildren().remove(backdrop);
        });

        actions.getChildren().addAll(rejectBtn, validateBtn);
        modal.getChildren().addAll(topRow, title, meta, synopsis, hashBox, actions);

        backdrop.getChildren().add(modal);
        backdrop.setOnMouseClicked(e -> {
            if (e.getTarget() == backdrop) {
                modalHost.getChildren().remove(backdrop);
            }
        });

        modalHost.getChildren().add(backdrop);
    }

    private void bindLiveConsensusNotifications() {
        dataService.startLiveConsensusFeed(tx -> toastManager.show(
                "New Block #" + tx.getBlockHeight(),
                tx.getFilmTitle() + " • " + tx.getActionType() + " by " + tx.getValidatorNode(),
                ToastManager.ToastType.SUCCESS
        ));
    }
}
