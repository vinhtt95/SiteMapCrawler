package com.vinhtt.sitemapcrawler.viewmodel;

import com.vinhtt.sitemapcrawler.model.SiteNode;
import com.vinhtt.sitemapcrawler.service.ICrawlerService;
import com.vinhtt.sitemapcrawler.service.impl.PlaywrightCrawlerService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * ViewModel for the MainView. Manages UI state and delegates logic to services.
 *
 * @author vinhtt
 * @version 1.0
 */
public class MainViewModel {

    private final ICrawlerService crawlerService;
    private final StringProperty urlInput = new SimpleStringProperty("https://example.com");
    private final BooleanProperty isCrawling = new SimpleBooleanProperty(false);
    private final ObservableList<String> logs = FXCollections.observableArrayList();
    private final StringProperty statusMessage = new SimpleStringProperty("Ready");

    /**
     * Initializes the MainViewModel.
     */
    public MainViewModel() {
        this.crawlerService = new PlaywrightCrawlerService();
    }

    /**
     * Command to start the crawling process.
     */
    public void startCrawl() {
        if (isCrawling.get()) return;

        isCrawling.set(true);
        logs.clear();
        statusMessage.set("Crawling started...");

        crawlerService.startCrawling(
                urlInput.get(),
                2, // Default depth
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
        logs.add("Found Node: " + node.getTitle() + " [" + node.getType() + "]");
    }

    private void handleEdgeAdded(String edgeInfo) {
        // Logic to update graph model would go here
    }

    // Getters for Properties
    public StringProperty urlInputProperty() { return urlInput; }
    public BooleanProperty isCrawlingProperty() { return isCrawling; }
    public ObservableList<String> getLogs() { return logs; }
    public StringProperty statusMessageProperty() { return statusMessage; }
}