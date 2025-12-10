# User Requirements Document (URD)

**Project Name:** SiteMapCrawler  
**Version:** 1.0  
**Status:** Draft  
**Last Updated:** 2025-12-10

---

## 1. Introduction

### 1.1 Purpose
The purpose of the **SiteMapCrawler** is to provide a desktop application that automates the process of discovering and mapping the structure of a website. By simulating user interactions via a browser engine, the application generates a visual site map in the form of a directed graph, helping users understand website hierarchy, navigation flows, and structural patterns.

### 1.2 Scope
* **Input:** A starting URL provided by the user.
* **Process:** Automated crawling using a headless browser (Playwright) with Breadth-First Search (BFS) logic.
* **Output:** An interactive graph visualization and exportable data (JSON/Image).
* **Platform:** Desktop (Java/JavaFX).

---

## 2. User Personas

* **Developer/Architect:** Uses the tool to reverse-engineer existing web apps or audit site structure.
* **QA Engineer:** Uses the tool to verify navigation paths and find broken links.
* **SEO Specialist:** Uses the tool to visualize internal linking structures.

---

## 3. Functional Requirements (FR)

### 3.1. Configuration & Input
| ID | Feature | Description | Priority |
| :--- | :--- | :--- | :--- |
| **FR-01** | **Target Entry** | The user must be able to input a valid URL (http/https) to start the crawling process. | High |
| **FR-02** | **Crawl Constraints** | The user must be able to configure: <br> - `Max Depth` (How deep the crawler goes). <br> - `Max Threads` (Concurrent browser contexts). <br> - `Timeout` (Max time per page). | Medium |
| **FR-03** | **Ignored Patterns** | The user can define a list of regex patterns (e.g., `logout`, `mailto:`, `#`) to be ignored during crawling. | Medium |

### 3.2. Crawling Engine
| ID | Feature | Description | Priority |
| :--- | :--- | :--- | :--- |
| **FR-04** | **Browser Automation** | The system must use a browser engine (Playwright) to render JavaScript, simulating a real user environment. | High |
| **FR-05** | **Link Discovery** | The system must extract hyperlinks (`<a href>`) and interactive elements (`button` with navigation events) from the DOM. | High |
| **FR-06** | **External Link Handling** | If a link leads to a different domain (External URL): <br> - The system must record it as an "External Node". <br> - The system **must stop** traversing that branch further. | High |
| **FR-07** | **Pattern Recognition** | The system must identify structurally similar pages (e.g., product details, news items) based on HTML DOM structure (excluding text content) and group them into a single logic node to prevent graph explosion. | High |
| **FR-08** | **Traversal Algorithm** | The system must use BFS (Breadth-First Search) or a robust pathfinding algorithm to ensure the shortest path to nodes is discovered first. | High |

### 3.3. Visualization & UI
| ID | Feature | Description | Priority |
| :--- | :--- | :--- | :--- |
| **FR-09** | **Real-time Graph** | The application must render the site map as a network graph (Nodes and Edges) in real-time as the crawler progresses. | High |
| **FR-10** | **Graph Interaction** | Users must be able to: <br> - Zoom in/out. <br> - Pan the canvas. <br> - Drag nodes to rearrange the layout. | Medium |
| **FR-11** | **Node Details** | Clicking on a node should display detailed information: <br> - Full URL. <br> - Page Title. <br> - Response Time. <br> - Screenshot (Thumbnail). | Medium |
| **FR-12** | **Crawl Status** | The UI must display a status log (e.g., "Crawling: /about", "Found: 5 links", "Skipped: External"). | Medium |

### 3.4. Export & Persistence
| ID | Feature | Description | Priority |
| :--- | :--- | :--- | :--- |
| **FR-13** | **Export Graph** | Users can export the visual graph as an image (PNG/JPG). | Low |
| **FR-14** | **Export Data** | Users can export the crawl result as a structured file (JSON/XML). | Low |

---

## 4. Non-Functional Requirements (NFR)

### 4.1. User Interface (UI/UX)
* **NFR-UI-01:** The application must support a **Modern Dark Mode** theme by default.
* **NFR-UI-02:** The application must support **Internationalization (i18n)**, specifically English (en) and Vietnamese (vi).

### 4.2. Performance
* **NFR-PERF-01:** The graph visualization must handle at least **500 nodes** without significant UI lag.
* **NFR-PERF-02:** The crawler must handle concurrent processing (multi-threaded) to speed up data collection.

### 4.3. Reliability
* **NFR-REL-01:** The application must not crash if a target website has invalid HTML or connection timeouts.
* **NFR-REL-02:** The system must detect infinite loops (cyclic links) and handle them gracefully.

---

## 5. Technical Constraints

The development must strictly adhere to the following technical standards:

* **Language:** Java 17 or higher.
* **Framework:** JavaFX (Modular).
* **Architecture:** MVVM (Model-View-ViewModel).
* **Design Principles:** SOLID Principles.
* **Documentation:** * Strict Javadoc for all classes and public methods.
    * **NO** inline comments explaining logic; code must be self-documenting.
* **Configuration:** JSON-based configuration files.
* **Dependencies:**
    * `Maven` for build management.
    * `Playwright` for browser automation.
    * `JGraphT` for graph algorithms.