package com.vinhtt.sitemapcrawler.service;

import com.vinhtt.sitemapcrawler.model.SiteNode;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.function.Consumer;

/**
 * Interface defining the contract for the web crawling engine.
 *
 * @author vinhtt
 * @version 1.0
 */
public interface ICrawlerService {

    /**
     * Starts the crawling process asynchronously.
     *
     * @param startUrl     The seed URL to begin crawling.
     * @param maxDepth     The maximum depth for BFS traversal.
     * @param onNodeAdded  Callback function triggered when a new node is added.
     * @param onEdgeAdded  Callback function triggered when a new link is found.
     * @param onFinished   Callback function triggered when crawling completes.
     */
    void startCrawling(String startUrl, int maxDepth,
                       Consumer<SiteNode> onNodeAdded,
                       Consumer<String> onEdgeAdded,
                       Runnable onFinished);

    /**
     * Stops the current crawling process.
     */
    void stop();
}