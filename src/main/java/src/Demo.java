package src;

import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.Dictionary;
import java.util.Hashtable;

public class Demo extends Application {
    private enum BtnType { Settings, BackToRadar, Table, Auto, KP, SC, RU }
    private enum WindowType { Settings, Radar, Table }

    private final String SRC_CONTROLLER =   "images/controller.png";
    private final String SRC_WHEEL =        "images/wheel.png";

    private final int SCENE_WIDTH = 1230;
    private final int SCENE_HEIGHT = 840;

    private final Insets PADDING_DEFAULT = new Insets(0);
    private final int SPACING_DEFAULT = 0;

    private final AngleDisplay m_angleDisplay =         new AngleDisplay();
    private final Locator m_locatorPanel =              new Locator(this::scDisableHook, (angle) -> m_angleDisplay.rotateArrow(angle));
    private final Controller m_locatorController =      new Controller((angle) -> m_locatorPanel.setAngle(angle), SRC_CONTROLLER);
    private final Controller m_brightnessController =   new Controller((Double angle) -> {}, SRC_WHEEL);
    private final Controller m_focusController =        new Controller((Double angle) -> {}, SRC_WHEEL);
    private final BottomTable m_bottomTable =           new BottomTable(m_locatorPanel);
    private final Settings m_settings =                 new Settings();

    private String IMAGE_AUTO =         "auto";
    private String IMAGE_BACK =         "back";
    private String IMAGE_BRIGHTNESS =   "brightness";
    private String IMAGE_FILE =         "file";
    private String IMAGE_FOCUS =        "focus";
    private String IMAGE_KP =           "kp";
    private String IMAGE_KP_MANAGE =    "kp manage";
    private String IMAGE_RU =           "ru";
    private String IMAGE_SETTINGS =     "settings";
    private String IMAGE_SC =           "sc";

    private Dictionary<String, ImageView> m_images = new Hashtable<>();

    private StackPane m_panesRoot;
    private SplitPane m_radarPane;
    private BorderPane m_settingsPane;

    private boolean m_isTableShown = false;

    private Dictionary<String, Boolean> m_bools = new Hashtable<>();

    public Demo() throws Exception {}

    // ******************** Initialization ************************************
    @Override
    public void init() {
        m_bools.put("kp", false);
        m_bools.put("auto", false);

        initPrefSizes();
        initButtons();
        initPanes();

        m_bottomTable.setVisible(false);
    }

    private void initPrefSizes() {
        setPrefSize(m_bottomTable,          new Pair<>(500, 150));
        setPrefSize(m_brightnessController, new Pair<>(50, 50));
        setPrefSize(m_focusController,      new Pair<>(50, 50));
        setPrefSize(m_locatorController,    new Pair<>(150, 150));
        setPrefSize(m_locatorPanel,         new Pair<>(500, 500));
    }

    private void setPrefSize(Region obj, Pair<Integer, Integer> size) { obj.setPrefSize(size.getKey(), size.getValue()); }

    private void initButtons() {
        putImage(IMAGE_AUTO,        "images/buttons/auto.png");
        putImage(IMAGE_BACK,        "images/buttons/settings.png");
        putImage(IMAGE_BRIGHTNESS,  "images/labels/brightness.png");
        putImage(IMAGE_FILE,        "images/buttons/file.png");
        putImage(IMAGE_FOCUS,       "images/labels/focus.png");
        putImage(IMAGE_KP,          "images/buttons/kp.png");
        putImage(IMAGE_KP_MANAGE,   "images/buttons/kp_manage.png");
        putImage(IMAGE_RU,          "images/buttons/ru.png");
        putImage(IMAGE_SC,          "images/buttons/scale.png");
        putImage(IMAGE_SETTINGS,    "images/buttons/settings.png");

        initBtnHandlers();
    }

    private void putImage(String name, String src) { m_images.put(name, new ImageView(new Image(getClass().getResourceAsStream(src)))); }

    private void initBtnHandlers() {
        initHandlerByBtnTypeFor(m_images.get(IMAGE_AUTO),       BtnType.Auto);
        initHandlerByBtnTypeFor(m_images.get(IMAGE_BACK),       BtnType.BackToRadar);
        initHandlerByBtnTypeFor(m_images.get(IMAGE_FILE),       BtnType.Table);
        initHandlerByBtnTypeFor(m_images.get(IMAGE_KP),         BtnType.KP);
        initHandlerByBtnTypeFor(m_images.get(IMAGE_RU),         BtnType.RU);
        initHandlerByBtnTypeFor(m_images.get(IMAGE_SC),         BtnType.SC);
        initHandlerByBtnTypeFor(m_images.get(IMAGE_SETTINGS),   BtnType.Settings);

        addPressedEffect(m_images.get(IMAGE_AUTO));
        addPressedEffect(m_images.get(IMAGE_BACK));
        addPressedEffect(m_images.get(IMAGE_FILE));
        addPressedEffect(m_images.get(IMAGE_KP));
        addPressedEffect(m_images.get(IMAGE_RU));
        addPressedEffect(m_images.get(IMAGE_SC));
        addPressedEffect(m_images.get(IMAGE_SETTINGS));

        addEnabledEffect(m_images.get(IMAGE_KP), "kp");
        addEnabledEffect(m_images.get(IMAGE_AUTO), "auto");
    }

    private void initHandlerByBtnTypeFor(ImageView imageView, BtnType type) {imageView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> { onBtnClicked(type);}); }

    private void addPressedEffect(ImageView imageView) {
        imageView.addEventHandler(MouseEvent.MOUSE_PRESSED, (event) -> {
            ColorAdjust blackout = new ColorAdjust();
            blackout.setBrightness(-0.5);
            imageView.setEffect(blackout);
        });

        imageView.addEventHandler(MouseEvent.MOUSE_RELEASED, (event) -> {
            ColorAdjust blackout = new ColorAdjust();
            blackout.setBrightness(0);
            imageView.setEffect(blackout);
        });
    }

    private void addEnabledEffect(ImageView imageView, String boolName) {
        imageView.addEventHandler(MouseEvent.MOUSE_RELEASED, (event) -> {
            ColorAdjust blackout = new ColorAdjust();
            boolean state = m_bools.get(boolName);
            if (!state) {
                blackout.setBrightness(0.5);
                blackout.setContrast(0.5);
            }
            else {
                blackout.setBrightness(0);
            }
            m_bools.put(boolName, !state);
            imageView.setEffect(blackout);
        });
    }

    private void disableEnabledEffectFor(ImageView imageView) {
        ColorAdjust blackout = new ColorAdjust();
        blackout.setBrightness(0);
        imageView.setEffect(blackout);
    }

    private void initPanes() {
        m_radarPane = getRadarPane();
        m_settingsPane = new BorderPane();

        final Color COLOR_BG = Color.web("#E6DBC5");
        m_settingsPane.setBackground(new Background((new BackgroundFill(COLOR_BG, CornerRadii.EMPTY, Insets.EMPTY))));
        m_settingsPane.setCenter(m_settings);
        m_settings.addBackButton(m_images.get(IMAGE_BACK));

        m_panesRoot = new StackPane();
        m_panesRoot.getChildren().add(m_radarPane);
    }

    private void onBtnClicked(BtnType type) {
        if ((type != BtnType.Settings && type != BtnType.BackToRadar) && !m_settings.isDriveUnitEnabled()) { return; }

        switch (type) {
            case Settings:      switchWindowTo(WindowType.Settings); break;
            case BackToRadar:   switchWindowTo(WindowType.Radar); break;
            //case Table:         switchWindowTo(WindowType.Table); break;
            case Table:         m_bottomTable.setVisible(m_isTableShown = !m_isTableShown); break;

            case Auto:  m_locatorPanel.onAutoClicked(true); break;
            case KP:
                boolean value = m_locatorPanel.onKpClicked();
                m_bools.put("kp", value);
                if (!value) { disableEnabledEffectFor(m_images.get(IMAGE_KP)); }
                break;
            case SC:    m_locatorPanel.onScClicked(); break;
            case RU:    m_locatorPanel.onRuClicked(); break;
        }
    }

    private void switchWindowTo(WindowType type) {
        DoubleProperty translate;

        Node paneToAdd = null;
        Node paneToRemove = null;

        switch (type) {
            case Settings:
                paneToAdd = m_settingsPane;
                paneToRemove = m_radarPane;

                translate = paneToAdd.translateXProperty();
                translate.set(-1 * SCENE_WIDTH);
                break;
            case Radar:
                paneToAdd = m_radarPane;
                paneToRemove = m_settingsPane;

                translate = paneToAdd.translateXProperty();
                translate.set(SCENE_WIDTH);
                break;

            case Table:
                if (!m_isTableShown) {
                    paneToAdd = m_bottomTable;

                    paneToAdd.translateYProperty().set(-1 * m_bottomTable.getHeight());
                    translate = paneToAdd.translateYProperty();
                    //translate.set(-1 * m_bottomTable.getHeight());
                }
                else {
                    paneToRemove = m_bottomTable;

                    translate = paneToRemove.translateYProperty();
                    translate.set(m_bottomTable.getHeight());
                }
                m_isTableShown = !m_isTableShown;
                break;
            default:
                return;
        }

        //paneToAdd.translateXProperty().set(translateX);
        m_panesRoot.getChildren().add(paneToAdd);

        playSwitchTransition(translate, paneToRemove);
    }

    private void playSwitchTransition(DoubleProperty translate, Node toRemove) {
        final double TRANSITION_DURATION_SECONDS = 1.5d;

        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(translate, 0, Interpolator.EASE_IN);
        KeyFrame kf = new KeyFrame(Duration.seconds(TRANSITION_DURATION_SECONDS), kv);
        timeline.getKeyFrames().add(kf);
        timeline.setOnFinished(event -> {
            m_panesRoot.getChildren().remove(toRemove);
        });
        timeline.play();
    }

    private SplitPane getRadarPane() {
        final float SPLIT_PANE_DIVIDER_SHIFT = 0.9f;    // from 0 to 1
        final Color COLOR_BG = Color.web("#E6DBC5");
        final SplitPane splitPane = new SplitPane();

        splitPane.setDividerPosition(0, SPLIT_PANE_DIVIDER_SHIFT);
        splitPane.lookupAll(".split-pane-divider").stream().forEach(div ->  div.setMouseTransparent(true) );
        splitPane.setBackground(new Background((new BackgroundFill(COLOR_BG, CornerRadii.EMPTY, Insets.EMPTY))));

        splitPane.getItems().addAll(getMainPanel(), getControlPanel());

        return splitPane;
    }

    private VBox getMainPanel() {
        final int SPACING_BUTTON_LABEL =        15;
        final int SPACING_FOCUS_BRIGHTNESS =    550;

        final Insets PADDING_BOTTOM_TABLE =     new Insets(10, 0, 0, -100);
        final Insets PADDING_LEFT_PANE =        new Insets(0, 50, 50, 130);
        final Insets PADDING_LOCATOR_PANEL =    new Insets(20, 0, 0, 80);
        final Insets PADDING_SETTINGS =         new Insets(20,0,0,-110);
        final Insets PADDING_TABLE_BTN =        new Insets(40,0,0,-110);

        return createVerticalContainer(PADDING_LEFT_PANE, SPACING_DEFAULT,
                    createHorizontalContainer(PADDING_SETTINGS, SPACING_DEFAULT, m_images.get(IMAGE_SETTINGS)),
                    createHorizontalContainer(PADDING_DEFAULT, SPACING_FOCUS_BRIGHTNESS,
                            createVerticalContainer(PADDING_DEFAULT, SPACING_BUTTON_LABEL, m_focusController, m_images.get(IMAGE_FOCUS)),
                            createVerticalContainer(PADDING_DEFAULT, SPACING_BUTTON_LABEL, m_brightnessController, m_images.get(IMAGE_BRIGHTNESS))
                    ),
                    createHorizontalContainer(PADDING_LOCATOR_PANEL, SPACING_DEFAULT, m_locatorPanel),
                    createHorizontalContainer(PADDING_BOTTOM_TABLE, SPACING_DEFAULT, m_images.get(IMAGE_FILE), m_bottomTable)
                );
    }

    private VBox getControlPanel() {
        final int SPACING_BUTTON_LABEL =        10;
        final int SPACING_KP_AUTO_BUTTONS =     2;
        final int SPACING_SCALE_RU_BUTTONS =    100;

        final Insets PADDING_BUTTON_SCALE =         new Insets(35,5,0,0);
        final Insets PADDING_BUTTONS_KP_AUTO =      new Insets(0,0,0,10);
        final Insets PADDING_BUTTONS_SCALE_RU =     new Insets(135, 30, 0, 20);
        final Insets PADDING_CONTROLLER_LOCATOR =   new Insets(15,0,0,85);
        final Insets PADDING_COUNTER =              new Insets(25,25,0,38);

        return createVerticalContainer(PADDING_DEFAULT, SPACING_BUTTON_LABEL,
                    createHorizontalContainer(PADDING_COUNTER, SPACING_DEFAULT, m_angleDisplay),
                    createHorizontalContainer(PADDING_BUTTONS_KP_AUTO, SPACING_KP_AUTO_BUTTONS,
                            m_images.get(IMAGE_KP_MANAGE), m_images.get(IMAGE_KP), m_images.get(IMAGE_AUTO)
                            //createVerticalContainer(PADDING_DEFAULT, SPACING_BUTTON_LABEL, m_images.get(IMAGE_KP)),
                            //createVerticalContainer(PADDING_DEFAULT, SPACING_BUTTON_LABEL, m_images.get(IMAGE_AUTO))
                    ),
                    createHorizontalContainer(PADDING_CONTROLLER_LOCATOR, SPACING_DEFAULT, m_locatorController),
                    createHorizontalContainer(PADDING_BUTTONS_SCALE_RU, SPACING_SCALE_RU_BUTTONS,
                            createVerticalContainer(PADDING_BUTTON_SCALE, SPACING_DEFAULT, m_images.get(IMAGE_SC)),
                            createVerticalContainer(PADDING_DEFAULT, SPACING_BUTTON_LABEL, m_images.get(IMAGE_RU))
                    )
                );
    }

    public static VBox createVerticalContainer(Insets padding, int spacing, Node... elements) {
        VBox container = new VBox();
        container.setPadding(padding);
        container.setSpacing(spacing);
        container.getChildren().addAll(elements);
        return container;
    }

    public static HBox createHorizontalContainer(Insets padding, int spacing, Node... elements) {
        HBox container = new HBox();
        container.setPadding(padding);
        container.setSpacing(spacing);
        container.getChildren().addAll(elements);
        return container;
    }

    private void scDisableHook(Boolean isDisable) {
//        m_scBtn.setDisable(isDisable);
    }

    @Override
    public void start(Stage stage) {
        //Parent root = getRadarPane();

        Scene scene = new Scene(m_panesRoot, SCENE_WIDTH, SCENE_HEIGHT);

        //scene.getStylesheets().add(getClass().getResource("btn.css").toExternalForm());
        //TODO check on computers in university and thinking about correct way
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
