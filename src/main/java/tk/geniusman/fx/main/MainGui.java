package tk.geniusman.fx.main;

import java.awt.AWTException;
import java.awt.PopupMenu;
import java.awt.TrayIcon;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tk.geniusman.manager.Manager;

/**
 * MainGui
 * 
 * @author liuyq
 *
 */
public class MainGui extends Application {

    private Stage primaryStage;
    private Scene rootScene;
    private TrayIcon trayIcon;

    private volatile double xOffset = 0;
    private volatile double yOffset = 0;

    /**
     * the main entrance of program
     * 
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("restriction")
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.initStyle(StageStyle.UNDECORATED);
        Platform.setImplicitExit(false);

        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Java Download Tools");

        setUserAgentStylesheet(STYLESHEET_MODENA);
        System.setProperty("com.sun.javafx.highContrastTheme", "BLACKONWHITE");
        com.sun.javafx.application.PlatformImpl.setAccessibilityTheme("High Contrast #2");

        try {
            // Load the root layout from the FXML file
            FXMLLoader mainLayoutLoader = new FXMLLoader(MainGui.class.getResource("/ui/MainLayout2.fxml"));
            Pane rootLayout = mainLayoutLoader.load();
            setDragable(rootLayout);

            rootScene = new Scene(rootLayout);
            primaryStage.setScene(rootScene);
            primaryStage.setResizable(false);

            addToTray();

            primaryStage.getIcons().add(new Image(MainGui.class.getResource("/image/icon1.png").toString()));
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * set the root layout dragable
     * 
     * @param n
     *            the instance of Node
     */
    private void setDragable(Node n) {
        n.setOnMousePressed((event) -> {
            xOffset = primaryStage.getX() - event.getScreenX();
            yOffset = primaryStage.getY() - event.getScreenY();
        });
        n.setOnMouseDragged((event) -> {
            primaryStage.setX(event.getScreenX() + xOffset);
            primaryStage.setY(event.getScreenY() + yOffset);
        });
    }

    /**
     * set the root layout dragable
     * 
     * @param the
     *            instance of Pane
     */
    private void setDragable(Pane p) {
        setDragable((Node) p);
        for (Node n : p.getChildren()) {
            setDragable(n);
        }
    }

    private void addToTray() {
        // ensure awt is initialized
        java.awt.Toolkit.getDefaultToolkit();

        // make sure system tray is supported
        if (!java.awt.SystemTray.isSupported()) {
            System.out.println("No system tray support!");
            // log.warn("No system tray support!");
        }

        final java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
        try {

            java.awt.Image image = ImageIO.read(MainGui.class.getResource("/image/icon2.png"));
            trayIcon = new TrayIcon(image);
            trayIcon.addActionListener((e) -> Platform.runLater(() -> primaryStage.show()));

            java.awt.MenuItem openItem = new java.awt.MenuItem("Display");
            openItem.addActionListener((e) -> Platform.runLater(() -> show()));

            java.awt.MenuItem exitItem = new java.awt.MenuItem("Exit");
            exitItem.addActionListener((e) -> Manager.getInstance().terminate());

            PopupMenu popup = new PopupMenu();
            popup.add(openItem);
            popup.addSeparator();
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);
            trayIcon.setToolTip("Not Connected");
            tray.add(trayIcon);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public void show() {
        primaryStage.show();
    }

    public void hide() {
        primaryStage.hide();
    }

    public void setTooltip(String message) {
        if (trayIcon != null) {
            trayIcon.setToolTip(message);
        }
    }

    public void showNotification(String message) {
        trayIcon.displayMessage("File Download Tools", message, java.awt.TrayIcon.MessageType.INFO);
    }
}
