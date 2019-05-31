package src;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

import java.util.function.Consumer;

public class Settings extends Region {
    private enum BtnType {Green, Red}

    private boolean m_isDriveUnitEnabled = false;

    private Image m_btnGreenOn;
    private Image m_btnGreenOff;
    private Image m_btnRedOn;
    private Image m_btnRedOff;

    private ImageView m_bg;
    private ImageView m_driveUnitOnImageView;
    private ImageView m_driveUnitOffImageView;

    public Settings() { init(); }

    private void OnDriveUnitStateChange(boolean newState) {
        if (m_isDriveUnitEnabled == newState) { return; }
        m_isDriveUnitEnabled = newState;
        if (!m_isDriveUnitEnabled) {
            m_driveUnitOnImageView.setImage(m_btnGreenOff);
        }
    }

    private void init() {
        final String SRC_BG = "images/settings_bg.jpg";
        m_bg = new ImageView(new Image(getClass().getResourceAsStream(SRC_BG)));
        getChildren().add(m_bg);

        initButtons();
    }

    private void initButtons() {
        final String SRC_BUTTON_GREEN_ON =  "images/buttons/settings/btn_on_green.png";
        final String SRC_BUTTON_GREEN_OFF = "images/buttons/settings/btn_off_green.png";
        final String SRC_BUTTON_RED_ON =    "images/buttons/settings/btn_on_red.png";
        final String SRC_BUTTON_RED_OFF =   "images/buttons/settings/btn_off_red.png";

        final Insets PADDING_DRIVE_UNIT = new Insets(708, 0, 0, 1110);
        final int SPACING_BUTTONS_ON_OFF = 3;

        m_btnGreenOn =  new Image(getClass().getResourceAsStream(SRC_BUTTON_GREEN_ON));
        m_btnGreenOff = new Image(getClass().getResourceAsStream(SRC_BUTTON_GREEN_OFF));
        m_btnRedOn =    new Image(getClass().getResourceAsStream(SRC_BUTTON_RED_ON));
        m_btnRedOff =   new Image(getClass().getResourceAsStream(SRC_BUTTON_RED_OFF));

        m_driveUnitOnImageView =    new ImageView(m_btnGreenOff);
        m_driveUnitOffImageView =   new ImageView(m_btnRedOff);

        getChildren().add(Demo.createVerticalContainer(PADDING_DRIVE_UNIT, SPACING_BUTTONS_ON_OFF, m_driveUnitOnImageView, m_driveUnitOffImageView));

        initHandlers();
    }

    private void initHandlers() {
        initHandlerForBtn(m_driveUnitOnImageView,   BtnType.Green,  this::OnDriveUnitStateChange);
        initHandlerForBtn(m_driveUnitOffImageView,  BtnType.Red,    this::OnDriveUnitStateChange);
    }

    private void initHandlerForBtn(ImageView btn, BtnType btnType, Consumer<Boolean> callback) {
        btn.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            btn.setImage(btnType == BtnType.Green ? m_btnGreenOn : m_btnRedOn);
        });
        btn.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            if (btnType == BtnType.Red) {
                btn.setImage(m_btnRedOff);
            }
            callback.accept(btnType == BtnType.Green);
        });
    }

    public void addBackButton(Node btn) {
        final Insets PADDING_DRIVE_UNIT = new Insets(20, 0, 0, 1130);
        getChildren().add(Demo.createVerticalContainer(PADDING_DRIVE_UNIT, 0, btn));
    }

    public boolean isDriveUnitEnabled() { return m_isDriveUnitEnabled; }
}
