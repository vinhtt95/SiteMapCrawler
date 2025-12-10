package com.vinhtt.sitemapcrawler.service.impl;

import com.microsoft.playwright.*;
import com.vinhtt.sitemapcrawler.model.NodeType;
import com.vinhtt.sitemapcrawler.model.SiteNode;
import com.vinhtt.sitemapcrawler.service.ICrawlerService;
import javafx.application.Platform;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of ICrawlerService using Microsoft Playwright.
 * Includes intelligent grouping for list items and better naming for external nodes.
 *
 * @author vinhtt
 * @version 1.2
 */
public class PlaywrightCrawlerService implements ICrawlerService {

    private volatile boolean isRunning;
    private final Set<String> visitedUrls = Collections.synchronizedSet(new HashSet<>());

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;

    private static final Pattern LIST_ITEM_PATTERN = Pattern.compile("(.*/)([^/]+/?)$");
    private final Map<String, Integer> patternCounter = new HashMap<>();
    private static final int GROUPING_THRESHOLD = 3;

    @Override
    public void startCrawling(String startUrl, int maxDepth,
                              Consumer<SiteNode> onNodeAdded,
                              Consumer<String> onEdgeAdded,
                              Runnable onFinished) {

        cleanup();

        isRunning = true;
        visitedUrls.clear();
        patternCounter.clear();

        CompletableFuture.runAsync(() -> {
            try {
                playwright = Playwright.create();
                browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                        .setChannel("chrome")
                        .setHeadless(false));
                context = browser.newContext();
                Page page = context.newPage();

                Queue<CrawlTask> queue = new LinkedList<>();
                String rootDomain = getDomainName(startUrl);
                queue.add(new CrawlTask(startUrl, 0));

                while (isRunning && !queue.isEmpty()) {
                    CrawlTask current = queue.poll();

                    if (visitedUrls.contains(current.url)) continue;
                    visitedUrls.add(current.url);

                    try {
                        page.navigate(current.url);
                        String title = page.title();
                        if (title == null || title.isEmpty()) {
                            title = current.url;
                        }

                        SiteNode node = new SiteNode(current.url, title, NodeType.INTERNAL);
                        Platform.runLater(() -> onNodeAdded.accept(node));

                        if (current.depth < maxDepth) {
                            List<ElementHandle> links = page.querySelectorAll("a[href]");
                            for (ElementHandle link : links) {
                                String href = link.getAttribute("href");
                                if (href == null || href.isEmpty() || href.startsWith("#") || href.startsWith("javascript")) continue;

                                String absoluteUrl = resolveUrl(current.url, href);
                                if (absoluteUrl == null) continue;

                                if (absoluteUrl.startsWith(startUrl) || absoluteUrl.contains(rootDomain)) {
                                    String groupUrl = tryGetGroupUrl(absoluteUrl);
                                    if (groupUrl != null) {
                                        SiteNode groupNode = new SiteNode(groupUrl, "List: " + getPathOnly(groupUrl) + "*", NodeType.GROUPED);
                                        Platform.runLater(() -> {
                                            onNodeAdded.accept(groupNode);
                                            onEdgeAdded.accept(current.url + " -> " + groupUrl);
                                        });
                                    } else {
                                        queue.add(new CrawlTask(absoluteUrl, current.depth + 1));
                                        String finalUrl = absoluteUrl;
                                        Platform.runLater(() -> onEdgeAdded.accept(current.url + " -> " + finalUrl));
                                    }

                                } else {
                                    String domain = getDomainName(absoluteUrl);
                                    String externalNodeId = "ext://" + domain;

                                    SiteNode extNode = new SiteNode(externalNodeId, domain + " (Ext)", NodeType.EXTERNAL);

                                    Platform.runLater(() -> {
                                        onNodeAdded.accept(extNode);
                                        onEdgeAdded.accept(current.url + " -> " + externalNodeId);
                                    });
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
                cleanup();
                Platform.runLater(onFinished);
            }
        });
    }

    @Override
    public void stop() {
        isRunning = false;
    }

    /**
     * Cleans up Playwright resources.
     */
    public void cleanup() {
        try {
            if (context != null) { context.close(); context = null; }
            if (browser != null) { browser.close(); browser = null; }
            if (playwright != null) { playwright.close(); playwright = null; }
        } catch (Exception e) {
            System.err.println("Error cleaning up Playwright: " + e.getMessage());
        }
    }

    private String tryGetGroupUrl(String url) {
        Matcher matcher = LIST_ITEM_PATTERN.matcher(url);
        if (matcher.find()) {
            String basePath = matcher.group(1);
            String lastPart = matcher.group(2);

            if (lastPart.matches("\\d+/?")) {
                return basePath;
            }

            patternCounter.put(basePath, patternCounter.getOrDefault(basePath, 0) + 1);
            if (patternCounter.get(basePath) > GROUPING_THRESHOLD) {
                return basePath;
            }
        }
        return null;
    }

    private String getDomainName(String url) {
        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (URISyntaxException e) {
            return "External";
        }
    }

    private String getPathOnly(String url) {
        try {
            return new URI(url).getPath();
        } catch (Exception e) { return url; }
    }

    private String resolveUrl(String baseUrl, String href) {
        try {
            return new URI(baseUrl).resolve(href).toString();
        } catch (Exception e) { return null; }
    }

    private record CrawlTask(String url, int depth) {}
}