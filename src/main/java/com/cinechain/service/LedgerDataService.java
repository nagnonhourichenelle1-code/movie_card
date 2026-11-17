package com.cinechain.service;

import com.cinechain.model.FilmRecommendation;
import com.cinechain.model.FilmRecommendation.ConsensusStatus;
import com.cinechain.model.LedgerTransaction;
import com.cinechain.model.LedgerTransaction.TxStatus;
import com.cinechain.model.ValidatorNode;
import com.cinechain.model.ValidatorNode.NodeState;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class LedgerDataService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final ObservableList<FilmRecommendation> films = FXCollections.observableArrayList();
    private final ObservableList<LedgerTransaction> transactions = FXCollections.observableArrayList();
    private final ObservableList<ValidatorNode> validators = FXCollections.observableArrayList();
    private final LongProperty currentBlockHeight = new SimpleLongProperty(19_482_104L);
    private final Random random = new Random();
    private Timeline liveConsensusTimeline;

    public LedgerDataService() {
        seedInitialData();
    }

    private void seedInitialData() {
        films.addAll(
                new FilmRecommendation(
                        "film-01",
                        "Zero-Knowledge Noir",
                        "Sora Takahashi",
                        2026,
                        "134 min",
                        "Sci-Fi Thriller",
                        "An investigative archivist in Neo-Kyoto uncovers a tampered cinema archive whose frames cryptographically encode pre-collapse governance keys.",
                        98,
                        ConsensusStatus.ACTIVE,
                        "0x9f4a8c12e7d0b341908fa77c12e8b40f91a6d201",
                        19_482_104L,
                        412,
                        "#00C853",
                        "#103224",
                        null
                ),
                new FilmRecommendation(
                        "film-02",
                        "Meridian Protocol",
                        "Elena Vance",
                        2025,
                        "118 min",
                        "Cyber Mystery",
                        "Across three orbital relay stations, an audit crew traces an anomalous smart contract executing film distribution royalties in reverse.",
                        94,
                        ConsensusStatus.ACTIVE,
                        "0x3b71d4e90218a6c54ef9012b7c3e4581a098f32d",
                        19_482_099L,
                        389,
                        "#2979FF",
                        "#122447",
                        null
                ),
                new FilmRecommendation(
                        "film-03",
                        "Glass Celluloid",
                        "Mateo Lindqvist",
                        2024,
                        "142 min",
                        "Neo-Noir Drama",
                        "A restorationist reconstructs a banned 1990s master reel using fragmented zero-knowledge proofs buried inside subterranean vault nodes.",
                        91,
                        ConsensusStatus.ACTIVE,
                        "0x7e20b118d9f4c36a105e82d930c14f8a6e22b09c",
                        19_482_091L,
                        304,
                        "#00C853",
                        "#1C2B22",
                        null
                ),
                new FilmRecommendation(
                        "film-04",
                        "Byzantine Cut",
                        "Amara Okafor",
                        2026,
                        "109 min",
                        "Documentary",
                        "An observational chronicle of independent filmmakers using decentralized consensus networks to verify provenance against synthetic forgery.",
                        89,
                        ConsensusStatus.ACTIVE,
                        "0x5c89e401a3f72b19d80643ec2f1b908a4d77e11f",
                        19_482_084L,
                        276,
                        "#2979FF",
                        "#1B263B",
                        null
                ),
                new FilmRecommendation(
                        "film-05",
                        "Silent Quorum",
                        "Henrik Solberg",
                        2023,
                        "126 min",
                        "Psychological Sci-Fi",
                        "When an isolated Nordic telemetry outpost loses quorum sync, its projectionist must decide which memory reel represents ground truth.",
                        62,
                        ConsensusStatus.INACTIVE,
                        "0xd109f34a78b21c5e90a43b817c22d50f9e18a443",
                        19_482_060L,
                        88,
                        "#FF5252",
                        "#35181C",
                        null
                ),
                new FilmRecommendation(
                        "film-06",
                        "Obsidian Frame",
                        "Claire Moreau",
                        2025,
                        "115 min",
                        "Experimental",
                        "Shot entirely under sodium-vapor lamps, an architectural meditation on cinema vaults, magnetic tape decay, and immutable hash signatures.",
                        95,
                        ConsensusStatus.ACTIVE,
                        "0x8a42c7901f6e5d3b2081c4a97f32e10b6d95c812",
                        19_482_052L,
                        351,
                        "#00C853",
                        "#162C20",
                        null
                ),
                new FilmRecommendation(
                        "film-07",
                        "Epoch 404",
                        "Devon K. Patel",
                        2024,
                        "98 min",
                        "Tech Thriller",
                        "A rogue consensus validator triggers a fork during an international film festival premiere, forcing real-time cryptographic arbitration.",
                        54,
                        ConsensusStatus.INACTIVE,
                        "0x2f60e89b44d17a30c982f1b650a3e87d4c19b207",
                        19_482_038L,
                        41,
                        "#FF5252",
                        "#331D20",
                        null
                ),
                new FilmRecommendation(
                        "film-08",
                        "Solaris Ledger",
                        "Nadia Kuznetsova",
                        2026,
                        "151 min",
                        "Space Opera",
                        "Deep-space archival couriers guard an orbital vault containing humanity's verified cinema canon as solar flares corrupt legacy drives.",
                        96,
                        ConsensusStatus.ACTIVE,
                        "0x6b13d92f80e41c77a509b32d6e8f14a2c073e559",
                        19_482_021L,
                        468,
                        "#2979FF",
                        "#142038",
                        null
                )
        );

        validators.addAll(
                new ValidatorNode("VAL-EU-WEST-01", "Frankfurt, DE", "0xA14B...90F2", 142_500.0, 99.98, 19_482_104L, NodeState.ACTIVE),
                new ValidatorNode("VAL-AP-NORTHEAST-04", "Tokyo, JP", "0x7C9E...31D8", 118_200.0, 99.91, 19_482_104L, NodeState.ACTIVE),
                new ValidatorNode("VAL-US-EAST-02", "Virginia, US", "0x4F20...88A1", 96_800.0, 99.74, 19_482_103L, NodeState.ACTIVE),
                new ValidatorNode("VAL-EU-NORTH-07", "Stockholm, SE", "0x9D33...54C0", 84_150.0, 99.42, 19_482_102L, NodeState.ACTIVE),
                new ValidatorNode("VAL-SA-EAST-03", "Sao Paulo, BR", "0x2E81...19B7", 42_000.0, 81.20, 19_482_049L, NodeState.INACTIVE),
                new ValidatorNode("VAL-AP-SOUTH-09", "Singapore, SG", "0x6B54...E021", 110_400.0, 99.85, 19_482_104L, NodeState.ACTIVE)
        );

        transactions.addAll(
                new LedgerTransaction(19_482_104L, "0x9f4a8c12...91a6d201", "Zero-Knowledge Noir", "CONSENSUS_VALIDATE", "VAL-EU-WEST-01", "15:02:41", TxStatus.VERIFIED, "450.00 CINE", "0xCC01...7A90", false),
                new LedgerTransaction(19_482_103L, "0x3b71d4e9...a098f32d", "Meridian Protocol", "CURATION_ATTEST", "VAL-AP-NORTHEAST-04", "15:02:18", TxStatus.VERIFIED, "320.00 CINE", "0xCC01...7A90", false),
                new LedgerTransaction(19_482_102L, "0x7e20b118...6e22b09c", "Glass Celluloid", "HASH_ANCHOR", "VAL-US-EAST-02", "15:01:55", TxStatus.VERIFIED, "210.00 CINE", "0xCC02...19B4", false),
                new LedgerTransaction(19_482_101L, "0xd109f34a...9e18a443", "Silent Quorum", "QUORUM_REJECT", "VAL-SA-EAST-03", "15:01:22", TxStatus.REJECTED, "0.00 CINE", "0xCC01...7A90", false),
                new LedgerTransaction(19_482_100L, "0x5c89e401...4d77e11f", "Byzantine Cut", "STAKE_PROPOSAL", "VAL-EU-NORTH-07", "15:00:49", TxStatus.VERIFIED, "500.00 CINE", "0xCC04...88E2", false),
                new LedgerTransaction(19_482_099L, "0x2f60e89b...4c19b207", "Epoch 404", "FORK_CHALLENGE", "VAL-SA-EAST-03", "15:00:12", TxStatus.PENDING, "125.00 CINE", "0xCC02...19B4", false),
                new LedgerTransaction(19_482_098L, "0x8a42c790...6d95c812", "Obsidian Frame", "CONSENSUS_VALIDATE", "VAL-AP-SOUTH-09", "14:59:38", TxStatus.VERIFIED, "410.00 CINE", "0xCC01...7A90", false),
                new LedgerTransaction(19_482_097L, "0x6b13d92f...c073e559", "Solaris Ledger", "ROYALTY_SETTLE", "VAL-EU-WEST-01", "14:59:04", TxStatus.VERIFIED, "880.00 CINE", "0xCC03...41D0", false)
        );
    }

    public ObservableList<FilmRecommendation> getFilms() {
        return films;
    }

    public ObservableList<LedgerTransaction> getTransactions() {
        return transactions;
    }

    public ObservableList<ValidatorNode> getValidators() {
        return validators;
    }

    public LongProperty currentBlockHeightProperty() {
        return currentBlockHeight;
    }

    public void startLiveConsensusFeed(Consumer<LedgerTransaction> onNewBlock) {
        if (liveConsensusTimeline != null) {
            liveConsensusTimeline.stop();
        }
        liveConsensusTimeline = new Timeline(new KeyFrame(Duration.seconds(9), event -> {
            LedgerTransaction tx = appendSimulatedBlock();
            if (onNewBlock != null) {
                onNewBlock.accept(tx);
            }
        }));
        liveConsensusTimeline.setCycleCount(Timeline.INDEFINITE);
        liveConsensusTimeline.play();
    }

    public void stopLiveConsensusFeed() {
        if (liveConsensusTimeline != null) {
            liveConsensusTimeline.stop();
        }
    }

    private LedgerTransaction appendSimulatedBlock() {
        long nextBlock = currentBlockHeight.get() + 1;
        currentBlockHeight.set(nextBlock);

        FilmRecommendation film = films.get(random.nextInt(films.size()));
        List<ValidatorNode> activeNodes = validators.stream()
                .filter(v -> v.getState() == NodeState.ACTIVE)
                .toList();
        ValidatorNode node = activeNodes.isEmpty()
                ? validators.get(0)
                : activeNodes.get(random.nextInt(activeNodes.size()));

        node.setLastValidatedBlock(nextBlock);

        String[] actions = {"CONSENSUS_VALIDATE", "CURATION_ATTEST", "HASH_ANCHOR", "ROYALTY_SETTLE"};
        String action = actions[random.nextInt(actions.length)];
        String hash = generateShortHash(film.getTitle() + nextBlock + System.nanoTime());
        String stake = (150 + random.nextInt(600)) + ".00 CINE";

        LedgerTransaction tx = new LedgerTransaction(
                nextBlock,
                hash,
                film.getTitle(),
                action,
                node.getNodeId(),
                LocalTime.now().format(TIME_FMT),
                TxStatus.VERIFIED,
                stake,
                "0xCC01...7A90",
                true
        );

        transactions.add(0, tx);
        if (transactions.size() > 60) {
            transactions.remove(transactions.size() - 1);
        }
        return tx;
    }

    public LedgerTransaction validateFilmOnChain(FilmRecommendation film) {
        long nextBlock = currentBlockHeight.get() + 1;
        currentBlockHeight.set(nextBlock);

        film.setConsensusStatus(ConsensusStatus.ACTIVE);
        film.setValidatorCount(film.getValidatorCount() + 1);
        film.setBlockHeight(nextBlock);

        LedgerTransaction tx = new LedgerTransaction(
                nextBlock,
                generateShortHash(film.getTxHash() + nextBlock),
                film.getTitle(),
                "MANUAL_VALIDATION",
                "VAL-EU-WEST-01",
                LocalTime.now().format(TIME_FMT),
                TxStatus.VERIFIED,
                "250.00 CINE",
                "0xCC01...7A90",
                true
        );
        transactions.add(0, tx);
        return tx;
    }

    public LedgerTransaction rejectFilmOnChain(FilmRecommendation film) {
        long nextBlock = currentBlockHeight.get() + 1;
        currentBlockHeight.set(nextBlock);

        film.setConsensusStatus(ConsensusStatus.INACTIVE);
        film.setBlockHeight(nextBlock);

        LedgerTransaction tx = new LedgerTransaction(
                nextBlock,
                generateShortHash("REJECT-" + film.getTxHash() + nextBlock),
                film.getTitle(),
                "CONSENSUS_REJECT",
                "VAL-EU-WEST-01",
                LocalTime.now().format(TIME_FMT),
                TxStatus.REJECTED,
                "0.00 CINE",
                "0xCC01...7A90",
                true
        );
        transactions.add(0, tx);
        return tx;
    }

    public FilmRecommendation submitNewRecommendation(
            String title,
            String director,
            int year,
            String runtime,
            String genre,
            String synopsis,
            boolean immediateStake
    ) {
        long nextBlock = currentBlockHeight.get() + 1;
        currentBlockHeight.set(nextBlock);
        String fullHash = generateFullHash(title + director + nextBlock);

        FilmRecommendation created = new FilmRecommendation(
                "film-" + (films.size() + 1),
                title,
                director,
                year,
                runtime,
                genre,
                synopsis,
                immediateStake ? 92 : 78,
                immediateStake ? ConsensusStatus.ACTIVE : ConsensusStatus.INACTIVE,
                fullHash,
                nextBlock,
                immediateStake ? 18 : 1,
                immediateStake ? "#00C853" : "#2979FF",
                "#182621",
                null
        );
        films.add(0, created);

        LedgerTransaction tx = new LedgerTransaction(
                nextBlock,
                abbreviateHash(fullHash),
                title,
                immediateStake ? "CURATION_STAKE" : "CURATION_SUBMIT",
                "VAL-EU-WEST-01",
                LocalTime.now().format(TIME_FMT),
                immediateStake ? TxStatus.VERIFIED : TxStatus.PENDING,
                immediateStake ? "500.00 CINE" : "50.00 CINE",
                "0xCC04...88E2",
                true
        );
        transactions.add(0, tx);
        return created;
    }

    public static String abbreviateHash(String hash) {
        if (hash == null || hash.length() <= 20) {
            return hash == null ? "" : hash;
        }
        return hash.substring(0, 10) + "..." + hash.substring(hash.length() - 8);
    }

    private static String generateShortHash(String seed) {
        return abbreviateHash(generateFullHash(seed));
    }

    private static String generateFullHash(String seed) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(seed.getBytes());
            StringBuilder sb = new StringBuilder("0x");
            for (int i = 0; i < 20; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "0x" + Integer.toHexString(seed.hashCode()) + "9a4f8c2b10e7d631";
        }
    }
}
