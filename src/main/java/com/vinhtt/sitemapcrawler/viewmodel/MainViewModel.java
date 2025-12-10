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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * ViewModel for the MainView. Manages UI state and delegates logic to services.
 *
 * @author vinhtt
 * @version 1.5
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

    private final Map<String, SiteNode> nodeCache = new HashMap<>();
    private final ObjectProperty<SiteNode> selectedNode = new SimpleObjectProperty<>();

    /**
     * Initializes the MainViewModel.
     */
    public MainViewModel() {
        this.crawlerService = new PlaywrightCrawlerService();
        this.siteGraph = new SimpleDirectedGraph<>(DefaultEdge.class);
    }

    /**
     * Start crawling from the Input URL (Root).
     */
    public void startCrawl() {
        if (isCrawling.get()) return;
        scanNode(urlInput.get());
    }

    /**
     * Scans a specific node (URL).
     *
     * @param url The URL to scan.
     */
    public void scanNode(String url) {
        if (isCrawling.get()) return;

        isCrawling.set(true);
        statusMessage.set("Scanning: " + url + "...");

        synchronized (siteGraph) {
            if (siteGraph.vertexSet().isEmpty()) {
                nodeCache.clear();
            }
        }

        crawlerService.crawlSinglePage(
                url,
                this::handleNodeAdded,
                this::handleEdgeAdded,
                () -> {
                    isCrawling.set(false);
                    statusMessage.set("Scan finished for: " + url);
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

    /**
     * Finds and selects a node from the cache based on its URL.
     *
     * @param url The URL of the node to select.
     */
    public void selectNodeByUrl(String url) {
        if (nodeCache.containsKey(url)) {
            selectedNode.set(nodeCache.get(url));
        }
    }

    private void handleNodeAdded(SiteNode node) {
        synchronized (siteGraph) {
            if (!siteGraph.containsVertex(node)) {
                siteGraph.addVertex(node);
            }
        }
        nodeCache.put(node.getUrl(), node);

        Platform.runLater(() -> {
            logs.add("Found: " + node.getTitle());
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
    public ObjectProperty<SiteNode> selectedNodeProperty() { return selectedNode; }
}