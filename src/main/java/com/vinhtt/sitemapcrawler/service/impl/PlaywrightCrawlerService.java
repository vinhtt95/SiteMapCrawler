package com.vinhtt.sitemapcrawler.service.impl;

import com.microsoft.playwright.*;
import com.vinhtt.sitemapcrawler.model.NodeType;
import com.vinhtt.sitemapcrawler.model.SiteNode;
import com.vinhtt.sitemapcrawler.service.ICrawlerService;
import javafx.application.Platform;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Implementation of ICrawlerService using Microsoft Playwright.
 * Uses a BFS algorithm to traverse the website.
 *
 * @author vinhtt
 * @version 1.0
 */
public class PlaywrightCrawlerService implements ICrawlerService {

    private volatile boolean isRunning;
    private final Set<String> visitedUrls = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void startCrawling(String startUrl, int maxDepth,
                              Consumer<SiteNode> onNodeAdded,
                              Consumer<String> onEdgeAdded,
                              Runnable onFinished) {
        isRunning = true;
        visitedUrls.clear();

        CompletableFuture.runAsync(() -> {
            try (Playwright playwright = Playwright.create();
                 Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false))) {

                BrowserContext context = browser.newContext();
                Page page = context.newPage();

                Queue<CrawlTask> queue = new LinkedList<>();
                queue.add(new CrawlTask(startUrl, 0));

                while (isRunning && !queue.isEmpty()) {
                    CrawlTask current = queue.poll();
                    String currentUrl = current.url;
                    int depth = current.depth;

                    if (visitedUrls.contains(currentUrl)) continue;
                    visitedUrls.add(currentUrl);

                    try {
                        page.navigate(currentUrl);
                        String title = page.title();
                        SiteNode node = new SiteNode(currentUrl, title, NodeType.INTERNAL);

                        Platform.runLater(() -> onNodeAdded.accept(node));

                        if (depth < maxDepth) {
                            List<ElementHandle> links = page.querySelectorAll("a[href]");
                            for (ElementHandle link : links) {
                                String href = link.getAttribute("href");
                                if (href != null && href.startsWith("http")) {
                                    // Basic logic: only internal links for BFS, others are external nodes
                                    if (href.startsWith(startUrl)) {
                                        queue.add(new CrawlTask(href, depth + 1));
                                    } else {
                                        SiteNode extNode = new SiteNode(href, "External", NodeType.EXTERNAL);
                                        Platform.runLater(() -> onNodeAdded.accept(extNode));
                                    }
                                    // Visualize connection (Mockup logic for Edge)
                                    String edgeInfo = currentUrl + " -> " + href;
                                    Platform.runLater(() -> onEdgeAdded.accept(edgeInfo));
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Platform.runLater(onFinished);
            }
        });
    }

    @Override
    public void stop() {
        isRunning = false;
    }

    private record CrawlTask(String url, int depth) {}
}