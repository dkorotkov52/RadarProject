package src;

import javafx.beans.DefaultProperty;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Rotate;

import java.util.function.Consumer;

@DefaultProperty("children")
public class Controller extends Region {
    private double size;
    private double width;
    private double height;
    private Circle foreground = new Circle();
    private Pane pane;
    private Rotate rotate = new Rotate();
    private double m_angle = 250;
    private Paint m_foregroundPaint;
    private EventHandler<MouseEvent> mouseFilter;
    private Consumer<Double> setAngle;

    private Image m_image;
    private ImageView m_imageView;

    // ******************** Constructors **************************************
    public Controller(Consumer<Double> setAngle, String imageSrc) {
        getStylesheets().add(Locator.class.getResource("radar.css").toExternalForm());
        this.setAngle = setAngle;
        m_foregroundPaint = Color.rgb(0, 0, 0, 0);

        m_image = new Image(getClass().getResourceAsStream(imageSrc));

        mouseFilter = evt -> {
            EventType<? extends MouseEvent> type = evt.getEventType();
            if (type.equals(MouseEvent.MOUSE_DRAGGED)) {
                double tmpAngle = AngleUtils.getAngleFromXY(evt.getX() + size * 0.5, evt.getY() + size * 0.5, size * 0.5, size * 0.5, 0);
                setAngle(tmpAngle);
            }
        };

        initGraphics();
        registerListeners();
    }

    // ******************** Initialization ************************************
    private void initGraphics() {
        getStyleClass().add("angle-picker");

        rotate.setAngle(m_angle);
        setAngle.accept(m_angle);

        foreground.setFill(m_foregroundPaint);

        m_imageView = new ImageView(m_image);

        pane = new Pane(m_imageView, foreground);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
        foreground.addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseFilter);
        foreground.addEventFilter(MouseEvent.MOUSE_CLICKED, mouseFilter);
    }

    public void setAngle(final double angle) {
        m_angle = angle % 360.0;
        rotate.setAngle(m_angle);
        m_imageView.setRotate(m_angle);
        setAngle.accept(m_angle);
    }


    // ******************** Resizing ******************************************
    private void resize() {
        width  = m_imageView.getLayoutBounds().getWidth();
        height = m_imageView.getLayoutBounds().getHeight();
        size   = width < height ? width : height;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.setPrefSize(size, size);
            pane.relocate((getWidth() - size) * 0.5, (getHeight() - size) * 0.5);

            foreground.setRadius(size * 0.4787234);
            foreground.relocate(size * 0.0212766, size * 0.0212766);

            m_imageView.relocate(size * 0, size * 0);

            rotate.setPivotX(-size * 0.37);
            rotate.setPivotY(0);

            redraw();
        }
    }

    private void redraw() {
        foreground.setFill(m_foregroundPaint);
    }
}

