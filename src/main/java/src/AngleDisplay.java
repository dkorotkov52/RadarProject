package src;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

public class AngleDisplay extends Region {

    private ImageView m_arrowImageView;
    private ImageView m_counterImageView;

    public AngleDisplay() {
        init();
        getChildren().addAll(m_counterImageView, m_arrowImageView);
    }

    private void init() {
        final String SRC_ARROW =    "images/arrow.png";
        final String SRC_COUNTER =  "images/counter.png";

        Image arrowImage =      new Image(getClass().getResourceAsStream(SRC_ARROW));
        Image counterImage =    new Image(getClass().getResourceAsStream(SRC_COUNTER));

        m_arrowImageView =      new ImageView(arrowImage);
        m_arrowImageView.relocate(10, 2);

        m_counterImageView =    new ImageView(counterImage);
    }

    public void rotateArrow(double angle) {
        m_arrowImageView.setRotate(angle);
    }
}
