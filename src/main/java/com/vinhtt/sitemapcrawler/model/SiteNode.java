package com.vinhtt.sitemapcrawler.model;

import java.util.Objects;

/**
 * Represents a single web page (vertex) in the site map graph.
 *
 * @author vinhtt
 * @version 1.0
 */
public class SiteNode {

    private final String url;
    private final String title;
    private final NodeType type;

    /**
     * Constructs a new SiteNode.
     *
     * @param url   The absolute URL of the page.
     * @param title The title of the page.
     * @param type  The classification of the node.
     */
    public SiteNode(String url, String title, NodeType type) {
        this.url = url;
        this.title = title;
        this.type = type;
    }

    /**
     * Gets the URL of the node.
     *
     * @return The URL string.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets the title of the page.
     *
     * @return The page title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the type of the node.
     *
     * @return The NodeType.
     */
    public NodeType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SiteNode siteNode = (SiteNode) o;
        return Objects.equals(url, siteNode.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    public String toString() {
        return title + " (" + url + ")";
    }
}