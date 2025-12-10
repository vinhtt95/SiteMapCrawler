package com.vinhtt.sitemapcrawler.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinhtt.sitemapcrawler.model.NodeType;
import com.vinhtt.sitemapcrawler.model.SiteNode;
import com.vinhtt.sitemapcrawler.viewmodel.MainViewModel;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject; // Requires javafx-web

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for the Main View.
 * Handles interaction with WebView (Vis.js) via JavaScript calls.
 *
 * @author vinhtt
 * @version 1.1
 */
public class MainView {

    @FXML private TextField txtUrl;
    @FXML private Button btnStart;
    @FXML private Button btnStop;
    @FXML private ListView<String> listLogs;
    @FXML private Label lblStatus;

    // [UPDATED] WebView instead of StackPane
    @FXML private WebView graphWebView;

    private MainViewModel viewModel;
    private WebEngine webEngine;
    private boolean isJsReady = false;
    private final ObjectMapper jsonMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        this.viewModel = new MainViewModel();

        // 1. Setup WebView
        webEngine = graphWebView.getEngine();
        webEngine.setJavaScriptEnabled(true);

        // Load the HTML template
        URL url = getClass().getResource("/graph_view.html");
        if (url != null) {
            webEngine.load(url.toExternalForm());
        }

        // Wait for page load
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                isJsReady = true;
                // Optional: Inject Java bridge if needed for callbacks
                // JSObject window = (JSObject) webEngine.executeScript("window");
                // window.setMember("javaApp", this);
            }
        });

        // 2. Data Binding
        txtUrl.textProperty().bindBidirectional(viewModel.urlInputProperty());
        listLogs.setItems(viewModel.getLogs());
        lblStatus.textProperty().bind(viewModel.statusMessageProperty());

        // State Binding
        btnStart.disableProperty().bind(viewModel.isCrawlingProperty());
        btnStop.disableProperty().bind(viewModel.isCrawlingProperty().not());
        txtUrl.disableProperty().bind(viewModel.isCrawlingProperty());

        // 3. Listen to Data Changes and Update Graph via JS
        viewModel.latestNodeProperty().addListener((obs, oldVal, newNode) -> {
            if (newNode != null && isJsReady) {
                injectNode(newNode);
            }
        });

        viewModel.latestEdgeProperty().addListener((obs, oldVal, newEdge) -> {
            if (newEdge != null && isJsReady) {
                injectEdge(newEdge);
            }
        });
    }

    /**
     * Converts Java Node to Vis.js Node format and sends to JS.
     */
    private void injectNode(SiteNode node) {
        try {
            Map<String, Object> jsNode = new HashMap<>();
            jsNode.put("id", node.getUrl());
            jsNode.put("label", node.getTitle());
            jsNode.put("group", node.getType().toString());

            // Adjust label for grouped nodes
            if (node.getType() == NodeType.GROUPED) {
                jsNode.put("label", "[GROUP] " + node.getTitle());
            }

            String json = jsonMapper.writeValueAsString(jsNode);
            // Execute JS function: updateGraph(nodeJson, null)
            Platform.runLater(() ->
                    webEngine.executeScript("updateGraph('" + json + "', null)")
            );
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Converts Java Edge info to Vis.js Edge format and sends to JS.
     */
    private void injectEdge(String edgeInfo) {
        try {
            String[] parts = edgeInfo.split(" -> ");
            if (parts.length < 2) return;

            Map<String, Object> jsEdge = new HashMap<>();
            jsEdge.put("from", parts[0]);
            jsEdge.put("to", parts[1]);

            String json = jsonMapper.writeValueAsString(jsEdge);
            // Execute JS function: updateGraph(null, edgeJson)
            Platform.runLater(() ->
                    webEngine.executeScript("updateGraph(null, '" + json + "')")
            );
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void onStartClick() {
        if (isJsReady) {
            webEngine.executeScript("clearGraph()");
        }
        viewModel.startCrawl();
    }

    @FXML
    private void onStopClick() {
        viewModel.stopCrawl();
    }
}