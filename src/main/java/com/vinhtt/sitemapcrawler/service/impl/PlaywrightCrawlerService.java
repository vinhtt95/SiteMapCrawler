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
 * Modified for manual scanning and proper resource cleanup.
 *
 * @author vinhtt
 * @version 1.6
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
    public void crawlSinglePage(String url,
                                Consumer<SiteNode> onNodeAdded,
                                Consumer<String> onEdgeAdded,
                                Runnable onFinished) {

        if (playwright == null) {
            try {
                playwright = Playwright.create();
                browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                        .setChannel("chrome")
                        .setHeadless(false));
                context = browser.newContext();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        isRunning = true;

        CompletableFuture.runAsync(() -> {
            Page page = null;
            try {
                page = context.newPage();
                page.navigate(url);

                String title = page.title();
                if (title == null || title.isEmpty()) {
                    title = url;
                }

                SiteNode currentNode = new SiteNode(url, title, NodeType.INTERNAL);
                Platform.runLater(() -> onNodeAdded.accept(currentNode));

                String rootDomain = getDomainName(url);
                List<ElementHandle> links = page.querySelectorAll("a[href]");

                for (ElementHandle link : links) {
                    if (!isRunning) break;

                    String href = link.getAttribute("href");
                    String linkText = link.innerText().trim();
                    if (linkText.isEmpty()) linkText = href;

                    if (href == null || href.isEmpty() || href.startsWith("#") || href.startsWith("javascript")) continue;

                    String absoluteUrl = resolveUrl(url, href);
                    if (absoluteUrl == null) continue;
                    if (absoluteUrl.equals(url)) continue;

                    if (absoluteUrl.contains(rootDomain)) {
                        String groupUrl = tryGetGroupUrl(absoluteUrl);
                        if (groupUrl != null) {
                            SiteNode groupNode = new SiteNode(groupUrl, "[Group] " + getPathOnly(groupUrl), NodeType.GROUPED);
                            Platform.runLater(() -> {
                                onNodeAdded.accept(groupNode);
                                onEdgeAdded.accept(url + " -> " + groupUrl);
                            });
                        } else {
                            SiteNode childNode = new SiteNode(absoluteUrl, linkText, NodeType.PENDING);
                            Platform.runLater(() -> {
                                onNodeAdded.accept(childNode);
                                onEdgeAdded.accept(url + " -> " + absoluteUrl);
                            });
                        }
                    } else {
                        String domain = getDomainName(absoluteUrl);
                        String extId = "ext://" + domain;
                        SiteNode extNode = new SiteNode(extId, domain, NodeType.EXTERNAL);
                        Platform.runLater(() -> {
                            onNodeAdded.accept(extNode);
                            onEdgeAdded.accept(url + " -> " + extId);
                        });
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (page != null) {
                    page.close();
                }
                Platform.runLater(onFinished);
            }
        });
    }

    @Override
    public void stop() {
        isRunning = false;
        cleanup();
    }

    @Override
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
}