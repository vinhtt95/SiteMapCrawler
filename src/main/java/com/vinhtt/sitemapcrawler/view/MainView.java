package com.vinhtt.sitemapcrawler.view;

import com.vinhtt.sitemapcrawler.viewmodel.MainViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controller for the Main View.
 * Binds UI components to the ViewModel.
 *
 * @author vinhtt
 * @version 1.0
 */
public class MainView {

    @FXML private TextField txtUrl;
    @FXML private Button btnStart;
    @FXML private Button btnStop;
    @FXML private ListView<String> listLogs;
    @FXML private Label lblStatus;

    private MainViewModel viewModel;

    /**
     * Initializes the controller after root element has been processed.
     */
    @FXML
    public void initialize() {
        this.viewModel = new MainViewModel();

        // Data Binding
        txtUrl.textProperty().bindBidirectional(viewModel.urlInputProperty());
        listLogs.setItems(viewModel.getLogs());
        lblStatus.textProperty().bind(viewModel.statusMessageProperty());

        // State Binding
        btnStart.disableProperty().bind(viewModel.isCrawlingProperty());
        btnStop.disableProperty().bind(viewModel.isCrawlingProperty().not());
        txtUrl.disableProperty().bind(viewModel.isCrawlingProperty());
    }

    /**
     * Handles the Start button click event.
     */
    @FXML
    private void onStartClick() {
        viewModel.startCrawl();
    }

    /**
     * Handles the Stop button click event.
     */
    @FXML
    private void onStopClick() {
        viewModel.stopCrawl();
    }
}