package com.vinhtt.sitemapcrawler.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinhtt.sitemapcrawler.model.NodeType;
import com.vinhtt.sitemapcrawler.model.SiteNode;
import com.vinhtt.sitemapcrawler.viewmodel.MainViewModel;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for the Main View.
 * Handles interaction with WebView (Vis.js) via JavaScript calls and manages the property sidebar.
 *
 * @author vinhtt
 * @version 1.5
 */
public class MainView {

    @FXML private TextField txtUrl;
    @FXML private Button btnStart;
    @FXML private Button btnStop;
    @FXML private ListView<String> listLogs;
    @FXML private Label lblStatus;

    @FXML private WebView graphWebView;

    @FXML private VBox propertiesPane;
    @FXML private Label lblNodeTitle;
    @FXML private TextField tfNodeUrl;
    @FXML private Label lblNodeType;
    @FXML private Button btnScanNode;

    private MainViewModel viewModel;
    private WebEngine webEngine;
    private boolean isJsReady = false;
    private final ObjectMapper jsonMapper = new ObjectMapper();

    /**
     * Bridge class to allow JavaScript to call Java methods.
     * This class MUST be public to be accessible by the WebEngine.
     */
    public class JavaConnector {
        /**
         * Called when a node is clicked in the graph.
         *
         * @param url The ID (URL) of the selected node.
         */
        public void onNodeSelected(String url) {
            Platform.runLater(() -> viewModel.selectNodeByUrl(url));
        }
    }

    /**
     * Initializes the controller after root element has been processed.
     */
    @FXML
    public void initialize() {
        this.viewModel = new MainViewModel();

        setupWebView();
        bindViewModel();
    }

    private void setupWebView() {
        webEngine = graphWebView.getEngine();
        webEngine.setJavaScriptEnabled(true);

        URL url = getClass().getResource("/graph_view.html");
        if (url != null) {
            webEngine.load(url.toExternalForm());
        }

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                isJsReady = true;
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaConnector", new JavaConnector());
            }
        });
    }

    private void bindViewModel() {
        txtUrl.textProperty().bindBidirectional(viewModel.urlInputProperty());
        listLogs.setItems(viewModel.getLogs());
        lblStatus.textProperty().bind(viewModel.statusMessageProperty());

        btnStart.disableProperty().bind(viewModel.isCrawlingProperty());
        btnStop.disableProperty().bind(viewModel.isCrawlingProperty().not());
        txtUrl.disableProperty().bind(viewModel.isCrawlingProperty());

        btnScanNode.disableProperty().bind(
                viewModel.isCrawlingProperty().or(viewModel.selectedNodeProperty().isNull())
        );

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

        viewModel.selectedNodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                propertiesPane.setVisible(true);
                propertiesPane.setManaged(true);
                lblNodeTitle.setText(newNode.getTitle());
                tfNodeUrl.setText(newNode.getUrl());
                lblNodeType.setText(newNode.getType().toString());
            } else {
                propertiesPane.setVisible(false);
                propertiesPane.setManaged(false);
            }
        });
    }

    private void injectNode(SiteNode node) {
        try {
            Map<String, Object> jsNode = new HashMap<>();
            jsNode.put("id", node.getUrl());
            jsNode.put("label", node.getTitle().length() > 20 ? node.getTitle().substring(0, 20) + "..." : node.getTitle());
            jsNode.put("group", node.getType().toString());
            jsNode.put("title", node.getUrl());

            String json = jsonMapper.writeValueAsString(jsNode);
            String safeJson = json.replace("'", "\\'");
            Platform.runLater(() ->
                    webEngine.executeScript("updateGraph('" + safeJson + "', null)")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void injectEdge(String edgeInfo) {
        try {
            String[] parts = edgeInfo.split(" -> ");
            if (parts.length < 2) return;

            Map<String, Object> jsEdge = new HashMap<>();
            jsEdge.put("from", parts[0]);
            jsEdge.put("to", parts[1]);

            String json = jsonMapper.writeValueAsString(jsEdge);
            String safeJson = json.replace("'", "\\'");
            Platform.runLater(() ->
                    webEngine.executeScript("updateGraph(null, '" + safeJson + "')")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the Start button click event.
     */
    @FXML
    private void onStartClick() {
        if (isJsReady) {
            webEngine.executeScript("clearGraph()");
        }
        viewModel.selectedNodeProperty().set(null);
        viewModel.startCrawl();
    }

    /**
     * Handles the Stop button click event.
     */
    @FXML
    private void onStopClick() {
        viewModel.stopCrawl();
    }

    /**
     * Handles the Scan This Node button click event.
     */
    @FXML
    private void onScanNodeClick() {
        SiteNode selected = viewModel.selectedNodeProperty().get();
        if (selected != null) {
            if (selected.getType() == NodeType.EXTERNAL) {
                return;
            }
            viewModel.scanNode(selected.getUrl());
        }
    }

    /**
     * Handles the Open Browser button click event.
     */
    @FXML
    private void onOpenBrowserClick() {
        String url = tfNodeUrl.getText();
        if (url != null && !url.isEmpty()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Cannot open browser: " + e.getMessage());
                alert.show();
            }
        }
    }
}