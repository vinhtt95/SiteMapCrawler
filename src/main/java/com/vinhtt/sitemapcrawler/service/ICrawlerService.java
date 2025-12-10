package com.vinhtt.sitemapcrawler.service;

import com.vinhtt.sitemapcrawler.model.SiteNode;
import java.util.function.Consumer;

/**
 * Interface defining the contract for the web crawling engine.
 *
 * @author vinhtt
 * @version 1.5
 */
public interface ICrawlerService {

    /**
     * Crawls a SINGLE page (no recursion).
     *
     * @param url          The URL to crawl.
     * @param onNodeAdded  Callback when a node (link) is found.
     * @param onEdgeAdded  Callback when a connection is found.
     * @param onFinished   Callback when the single page crawl is done.
     */
    void crawlSinglePage(String url,
                         Consumer<SiteNode> onNodeAdded,
                         Consumer<String> onEdgeAdded,
                         Runnable onFinished);

    /**
     * Stops the current crawling process.
     */
    void stop();

    /**
     * Cleans up browser resources.
     */
    void cleanup();
}