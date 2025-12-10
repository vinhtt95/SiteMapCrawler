package com.vinhtt.sitemapcrawler.viewmodel;

import com.vinhtt.sitemapcrawler.model.SiteNode;
import com.vinhtt.sitemapcrawler.service.ICrawlerService;
import com.vinhtt.sitemapcrawler.service.impl.PlaywrightCrawlerService;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.ArrayList; // Thêm import này

/**
 * ViewModel for the MainView. Manages UI state and delegates logic to services.
 *
 * @author vinhtt
 * @version 1.1
 */
public class MainViewModel {

    private final ICrawlerService crawlerService;
    private final StringProperty urlInput = new SimpleStringProperty("https://example.com");
    private final BooleanProperty isCrawling = new SimpleBooleanProperty(false);
    private final ObservableList<String> logs = FXCollections.observableArrayList();
    private final StringProperty statusMessage = new SimpleStringProperty("Ready");

    private final Graph<SiteNode, DefaultEdge> siteGraph;
    private final ObjectProperty<SiteNode> latestNode = new SimpleObjectProperty<>();
    private final ObjectProperty<String> latestEdge = new SimpleObjectProperty<>();

    /**
     * Initializes the MainViewModel.
     */
    public MainViewModel() {
        this.crawlerService = new PlaywrightCrawlerService();
        this.siteGraph = new SimpleDirectedGraph<>(DefaultEdge.class);
    }

    /**
     * Command to start the crawling process.
     */
    public void startCrawl() {
        if (isCrawling.get()) return;

        isCrawling.set(true);
        logs.clear();
        statusMessage.set("Crawling started...");

        synchronized (siteGraph) {
            // [FIXED] Tạo bản sao danh sách vertices trước khi xóa để tránh ConcurrentModificationException
            siteGraph.removeAllVertices(new ArrayList<>(siteGraph.vertexSet()));
        }

        crawlerService.startCrawling(
                urlInput.get(),
                2,
                this::handleNodeAdded,
                this::handleEdgeAdded,
                () -> {
                    isCrawling.set(false);
                    statusMessage.set("Crawling finished.");
                }
        );
    }

    /**
     * Command to stop the crawling process.
     */
    public void stopCrawl() {
        crawlerService.stop();
        isCrawling.set(false);
        statusMessage.set("Stopped by user.");
    }

    private void handleNodeAdded(SiteNode node) {
        synchronized (siteGraph) {
            if (!siteGraph.containsVertex(node)) {
                siteGraph.addVertex(node);
            }
        }

        Platform.runLater(() -> {
            logs.add("Found Node: " + node.getTitle() + " [" + node.getType() + "]");
            latestNode.set(node);
        });
    }

    private void handleEdgeAdded(String edgeInfo) {
        Platform.runLater(() -> latestEdge.set(edgeInfo));
    }

    public StringProperty urlInputProperty() { return urlInput; }
    public BooleanProperty isCrawlingProperty() { return isCrawling; }
    public ObservableList<String> getLogs() { return logs; }
    public StringProperty statusMessageProperty() { return statusMessage; }
    public ObjectProperty<SiteNode> latestNodeProperty() { return latestNode; }
    public ObjectProperty<String> latestEdgeProperty() { return latestEdge; }
}