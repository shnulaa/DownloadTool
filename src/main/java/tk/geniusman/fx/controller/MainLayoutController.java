package tk.geniusman.fx.controller;

import java.io.File;
import java.util.Optional;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.StageStyle;
import tk.geniusman.main.ForkJoinDownload;
import tk.geniusman.manager.Manager;
import tk.geniusman.manager.UIManager;

/**
 * MainLayoutController
 * 
 * @author liuyq
 *
 */
// @SuppressWarnings("restriction")
public class MainLayoutController {

    @FXML
    private TextField address; // download URL address

    @FXML
    private TextField localAddress; // local IP address

    @FXML
    private Button open;

    @FXML
    private Button download;

    @FXML
    private Button pauseOrResume;

    @FXML
    private Pane processPane;

    @FXML
    private Pane root;

    @FXML
    private ProgressBar process;

    @FXML
    private Label speedLab;

    @FXML
    private Label percentLab;

    @FXML
    private Button terminate;

    @FXML
    private AnchorPane taskListPane;

    /** Rectangle object array */
    private Rectangle[][] array; // save the Rectangle object to array
    private static final int WIDTH = 100;
    private static final int HEIGHT = 100;
    // private static final int PIXELS = WIDTH * HEIGHT;
    private final Manager m = Manager.getInstance();

    /**
     * constructor
     */
    public MainLayoutController() {
    }

    @FXML
    private void initialize() {
        address.setText("http://down.360safe.com/cse/360cse_8.5.0.126.exe");
        localAddress.setText("d:\\tools\\");
        pauseOrResume.setDisable(true);

        this.array = new Rectangle[WIDTH][HEIGHT];
        UIManager uiManager = UIManager.newInstance(array, process, speedLab, percentLab, processPane);
        uiManager.init();

        // add change Color listener
        m.addListener((current, t) -> Platform.runLater(() -> uiManager.changeColor(current, m.getSize())));

        // add change Percent listener
        m.addProcessListener((rate, speed, t) -> Platform.runLater(() -> uiManager.changePercent(rate, speed)));

        // add finish listener
        m.addFinishListener((hasError, message) -> {
            Platform.runLater(() -> {
                download.setDisable(false);
                pauseOrResume.setText("Pause");
                pauseOrResume.setDisable(true);
                showAlert("File Download Tools", message,
                        hasError ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
            });
        });
    }

    /**
     * the action for handle the button of Download
     */
    @FXML
    private void handleDownload() {
        String addressTxt = address.getText();
        if (addressTxt == null || addressTxt.isEmpty()) {
            showAlert("File Download Tools", "address URL must be specified..", Alert.AlertType.ERROR);
            return;
        }

        String localAddressTxt = localAddress.getText();
        if (localAddressTxt == null || localAddressTxt.isEmpty()) {
            showAlert("File Download Tools", "Local saved Path must be specified..", Alert.AlertType.ERROR);
            return;
        }

        clearColor();
        final String[] args = { addressTxt, "15", localAddressTxt, null };
        new Thread(() -> {
            try {
                ForkJoinDownload.main(args);
                System.out.println("Clear..");
                m.clear();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }).start();

        download.setDisable(true);
        pauseOrResume.setDisable(false);
    }

    /**
     * The action for handle the button of Open File
     */
    @FXML
    private void handleOpen() {
        final DirectoryChooser dChooser = new DirectoryChooser();
        dChooser.setTitle("Choose the Saved Path");

        File defaultDirectory = new File("d:\\");
        dChooser.setInitialDirectory(defaultDirectory);
        File file = dChooser.showDialog(open.getScene().getWindow());
        if (file != null) {
            localAddress.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handlePauseOrResume() {
        if (m.isPause()) {
            m.resume();
            pauseOrResume.setText("Pause");
        } else {
            m.setPause(true);
            pauseOrResume.setText("Continue");
        }
    }

    @FXML
    private void handleTerminate() {
        Optional<ButtonType> ret = showAlert("File Download Tools", "Confirm to terminate..",
                Alert.AlertType.CONFIRMATION);
        if (ret.get() == ButtonType.OK) {
            Manager.getInstance().terminate();
        }
    }

    /**
     * clearColor
     */
    private void clearColor() {
        if (array == null) {
            return;
        }
        for (int j = 0; j < WIDTH; j++) {
            for (int i = 0; i < HEIGHT; i++) {
                final Rectangle r = array[j][i];
                if (r != null) {
                    r.setFill(Color.CORNSILK);
                }
            }
        }
    }

    /**
     * showAlert
     * 
     * @param title
     * @param message
     * @param type
     */
    private Optional<ButtonType> showAlert(String title, String message, Alert.AlertType type) {
        final Alert a = new Alert(type);
        a.initStyle(StageStyle.UNDECORATED);
        a.setTitle(title);
        a.setHeaderText(type.name());
        a.setResizable(false);
        a.setContentText(message);
        return a.showAndWait();
    }
}
