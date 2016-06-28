package shnulaa.fx.controller;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import cn.shnulaa.listener.ChangedListener;
import cn.shnulaa.listener.ProcessChangedListener;
import cn.shnulaa.main.ForkJoinDownload;
import cn.shnulaa.manager.Manager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;

/**
 * MainLayoutController
 * 
 * @author liuyq
 *
 */
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
	private ProgressBar process;

	@FXML
	private Label speedLab;

	@FXML
	private Label percentLab;

	/** array */
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
		localAddress.setText("e:\\");

		this.array = new Rectangle[WIDTH][HEIGHT];
		for (int j = 0; j < WIDTH; j++) {
			for (int i = 0; i < HEIGHT; i++) {
				final Rectangle r = new Rectangle();
				r.setX(i * 3);
				r.setY(j * 2);
				r.setWidth(3);
				r.setHeight(2);
				r.setFill(Color.WHITE);
				array[j][i] = r;
				processPane.getChildren().add(r);
			}
		}

		RectangleManager rm = RectangleManager.newInstance(array);

		m.addListener(new ChangedListener() {
			@Override
			public void change(final long current, final Thread t) {
				rm.changeColor(current, m.getSize());
			}
		});

		m.addProcessListener(new ProcessChangedListener() {
			@Override
			public void change(double rate, long speed, Thread t) {
				Platform.runLater(() -> {
					process.progressProperty().set(rate);
					percentLab.setText((int) (rate * 100) + "%");
					speedLab.setText(String.valueOf(speed) + "KB/S");
				});
			}
		});
	}

	/**
	 * the action for handle the button of Download
	 */
	@FXML
	private void handleDownload() {
		String addressTxt = address.getText();
		if (StringUtils.isEmpty(addressTxt)) {
			showAlert("Download Tools", "address URL must be specified..", Alert.AlertType.ERROR);
			return;
		}

		String localAddressTxt = localAddress.getText();
		if (StringUtils.isEmpty(localAddressTxt)) {
			showAlert("File Download Tools", "Local saved Address must be specified..", Alert.AlertType.ERROR);
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
			pauseOrResume.setText("暂停");
		} else {
			m.setPause(true);
			pauseOrResume.setText("继续");
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
					r.setFill(Color.WHITE);
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
	private void showAlert(String title, String message, Alert.AlertType type) {
		Alert a = new Alert(type);
		a.setTitle(title);
		a.setHeaderText(type.name());
		a.setResizable(false);
		a.setContentText(message);
		a.showAndWait();
	}
}
