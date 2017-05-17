package tk.geniusman.manager;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * UIManager
 * 
 * @author liuyq
 *
 */
// @SuppressWarnings("restriction")
public class UIManager {

    private static final int WIDTH = 100;
    private static final int HEIGHT = 100;
    private static final int PIXELS = WIDTH * HEIGHT;
    private Rectangle[][] array;
    private ProgressBar process;
    private Label speedLab;
    private Label percentLab;
    private Pane processPane;

    /**
     * UIManager
     * 
     * @param array
     */
    private UIManager(final Rectangle[][] array, final ProgressBar process, final Label speedLab,
            final Label percentLab, Pane processPane) {
        this.array = array;
        this.process = process;
        this.speedLab = speedLab;
        this.percentLab = percentLab;
        this.processPane = processPane;
    }

    /**
     * changePercent
     * 
     * @param rate
     * @param speed
     */
    public void changePercent(double rate, long speed) {
        process.progressProperty().set(rate);
        percentLab.setText((int) (rate * 100) + "%");
        speedLab.setText(String.valueOf(speed) + "KB/S");
    }

    /**
     * init the array to processPane
     * 
     * @param array
     * @param processPane
     */
    public void init() {
        for (int j = 0; j < WIDTH; j++) {
            for (int i = 0; i < HEIGHT; i++) {
                final Rectangle r = new Rectangle();
                r.setX(i * 4);
                r.setY(j * 2);
                r.setWidth(4);
                r.setHeight(2);
                r.setFill(Color.CORNSILK);
                array[j][i] = r;
                processPane.getChildren().add(r);
            }
        }
    }

    public void recovery() {

    }

    /**
     * changeColor
     * 
     * @param current
     * @param totol
     */
    public void changeColor(final long current, final long total) {
        Platform.runLater(() -> {

            System.out.println("current:" + current);
            if (total <= 0) {
                System.out.println("total is negative..");
                return;
            }

            int percent = (int) (current * PIXELS / total);

            int x = (int) percent / WIDTH;
            int y = (int) percent % HEIGHT;
            if (x >= 100 || y >= 100) {
                return;
            }

            System.out.println(String.format("PIXELS:%s, total:%s..", PIXELS, total));

            System.out.println(String.format("percent:%s, x:%s, y:%s", percent, x, y));
            final Rectangle r = array[x][y];
            if (r == null) {
                return;
            }
            synchronized (r) {
                if (r.getFill() != Color.BLUE) {
                    array[x][y].setFill(Color.BLUE);
                }
            }
        });
    }

    /**
     * newInstance
     * 
     * @param array
     * @return new instance of RectangleManager
     */
    public static UIManager newInstance(final Rectangle[][] array, final ProgressBar process, final Label speedLab,
            final Label percentLab, final Pane processPane) {
        return new UIManager(array, process, speedLab, percentLab, processPane);
    }

}
