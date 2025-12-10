# SiteMapCrawler - Architecture Documentation

## 1. Project Overview

**SiteMapCrawler** is a Java Desktop application designed to crawl websites, analyze their structure, and visualize the site map as an interactive graph. The application detects structural patterns to group similar pages and identifies external links.

### Key Features
* **Deep Crawling:** BFS-based traversal using a headless browser (Playwright).
* **Visual Graph:** Interactive node-link diagram representing the website structure.
* **Pattern Recognition:** Auto-grouping of repetitive structural items (e.g., product lists).
* **Modern UI:** JavaFX-based interface with Dark Mode and responsive layout.
* **Configurable:** JSON-based settings and Internationalization (i18n) support.

---

## 2. Architectural Pattern: MVVM

The project follows the **Model-View-ViewModel (MVVM)** architectural pattern to ensure separation of concerns, testability, and maintainability.

```mermaid
graph TD
    User((User))
    View[View (FXML + CSS)]
    ViewModel[ViewModel]
    Model[Model (Data)]
    Service[Service Layer]

    User -- Interacts --> View
    View -- Data Binding --> ViewModel
    ViewModel -- Commands --> Service
    Service -- Updates --> ViewModel
    Service -- Reads/Writes --> Model
    ViewModel -- Notifies --> View
```
### Layer Responsibilities

1.  **View (UI Layer)**
    * **Components:** FXML files, CSS stylesheets, JavaFX Controllers (Code-behind).
    * **Responsibility:** Defines the structure and look of the UI. Views bind to properties in the ViewModel.
    * **Constraint:** No business logic in Controllers.

2.  **ViewModel (Presentation Logic)**
    * **Components:** Java Classes implementing `Observable` properties.
    * **Responsibility:** Holds the state of the View, contains commands (methods) to handle user actions, and transforms Model data for presentation.
    * **Communication:** Uses JavaFX Properties/Bindings to update the View automatically.

3.  **Model (Data Layer)**
    * **Components:** POJOs (Plain Old Java Objects).
    * **Responsibility:** Represents the domain data (Nodes, Edges, Configuration).
    * **Constraint:** Pure data containers, no logic.

4.  **Service Layer (Business Logic)**
    * **Components:** Interfaces and Implementations.
    * **Responsibility:** Performs heavy lifting (crawling, graph algorithms, I/O).
    * **Constraint:** Stateless where possible, follows SOLID principles.

---

## 3. Technology Stack

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Language** | Java 17+ | Core language. |
| **Build Tool** | Maven | Dependency and build management. |
| **UI Framework** | JavaFX 20+ | Desktop GUI framework. |
| **Styling** | CSS3 | Custom themes (Dark/Light). |
| **Browser Engine** | Microsoft Playwright | High-performance headless browser automation. |
| **Graph Logic** | JGraphT | Mathematical graph theory algorithms. |
| **Visualization** | SmartGraph / FXGraph | Rendering the interactive graph. |
| **Configuration** | Jackson | JSON parsing for `config.json`. |
| **Logging** | SLF4J + Logback | Structured logging. |

---

## 4. Project Structure

The project follows a standard Maven directory layout with package separation by layer.

```text
src/main/java/com/yourname/sitemapcrawler/
├── App.java                        # Application Entry Point
├── constant/                       # System Constants
│   ├── AppConstants.java
│   └── ConfigKeys.java
├── model/                          # [Model] Data Structures
│   ├── SiteNode.java               # Vertex (URL, Title, Hash)
│   ├── SiteEdge.java               # Edge (Action, Selector)
│   ├── CrawlConfig.java            # Configuration POJO
│   └── enums/
│       └── NodeType.java           # INTERNAL, EXTERNAL, GROUPED
├── service/                        # [Service] Business Logic
│   ├── ICrawlerService.java        # Interface for Crawling
│   ├── impl/
│   │   ├── PlaywrightCrawlerService.java
│   │   ├── GraphManagerService.java
│   │   └── ConfigService.java
│   └── algorithm/
│       └── DomStructureAnalyzer.java # Pattern recognition logic
├── viewmodel/                      # [ViewModel] Presentation Logic
│   ├── MainViewModel.java
│   └── SettingsViewModel.java
├── view/                           # [View] UI Controllers
│   ├── MainView.java
│   └── components/
└── util/                           # Utilities (Logger, Alerts)
```
## 5. Core Logic & Algorithms

### 5.1. Crawling Strategy (BFS)
To avoid StackOverflow errors and ensure level-by-level discovery, the crawler uses **Breadth-First Search**:

1.  **Queue:** Stores `SiteNode` objects waiting to be visited.
2.  **Visited Set:** `Set<String>` storing hashes of visited URLs.
3.  **Process:**
    * Dequeue URL -> Render with Playwright -> Extract Links.
    * Filter links (ignore external/blacklisted).
    * Create Edges.
    * Enqueue new links.

### 5.2. Structural Pattern Recognition
To handle infinite lists (e.g., e-commerce products), the system identifies page types via **DOM Fingerprinting**:

1.  **Strip Content:** Remove text nodes and attributes, keep only Tag hierarchy.
2.  **Hashing:** Generate a hash of the simplified DOM tree.
3.  **Clustering:** If multiple sibling nodes share the same DOM Hash, group them into a single `GROUPED` node in the graph.

---

## 6. Coding Conventions

Strict adherence to the following conventions is required:

1.  **Documentation:**
    * **Javadoc:** Required for ALL Classes, Interfaces, and Public Methods.
    * **Format:** Must include `@author`, `@version`, `@param`, `@return`, and `@throws`.
    * **Language:** English.

2.  **Comments:**
    * **NO inline comments** (e.g., `// logic here`) are allowed inside methods. Code must be self-explanatory.
    * **NO trailing comments** at the end of lines.
    * Use Javadoc to explain *Why*, not *How*.

3.  **Formatting:**
    * Standard Java naming conventions (CamelCase for vars/methods, PascalCase for classes).
    * Braces on the same line (K&R style).

4.  **SOLID Principles:**
    * **SRP:** One class, one responsibility.
    * **DIP:** Depend on Abstractions (Interfaces), not Concretions.

---

## 7. Configuration & Data

### 7.1. Configuration (`config.json`)
Located in `src/main/resources/`.

```json
{
  "crawler": {
    "maxDepth": 3,
    "maxThreads": 4,
    "timeoutMs": 30000,
    "ignoredPatterns": ["logout", "cart", "mailto:"]
  },
  "ui": {
    "theme": "dark",
    "language": "en"
  }
}
```
### 7.2. Internationalization (i18n)
Located in `src/main/resources/i18n/`.
* `messages_en.properties` (Default)
* `messages_vi.properties`

---

## 8. Build & Run

### Prerequisites
* JDK 17 or higher.
* Maven 3.8+.

### Commands
```bash
# Clean and Build
mvn clean install

# Run Application
mvn javafx:run