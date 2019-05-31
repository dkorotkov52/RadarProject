package src;

import javafx.beans.DefaultProperty;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;


@DefaultProperty("children")
public class Locator extends Region {
    private final String SRC_BG =           "images/radar/bg.png";
    private final String SRC_VIEW_ANGLE =   "images/radar/view_angle.png";

    private double size;
    private double radius;
    private double width;
    private double height;

    private List<Rectangle> targetsRect = new ArrayList<>();
    private List<Target> targets = new ArrayList<>();

    private Pane pane;

    private double _angle = 0.0;
    private double tempAngle = 0;

    private double speed = 0.15;

    private int currentTargetIdx = 0;

    private ImageView m_bgImageView;
    private ImageView m_viewAngleImageView;

    private Target targetForMoving;

    private Consumer<Boolean> scDisableHook;


    private int topAngle = 52;
    private int bottomAngle = 360 - topAngle;

    private boolean scTurnOn = false;
    private boolean isLocatorMoving;
    private Future locatorMovingFuture;
    private BottomTable bottomTable;

    private Consumer<Double> m_displayRadar;

    // ******************** Constructors **************************************
    public Locator(Consumer<Boolean> scDisableHook, Consumer<Double> displayRadar) throws Exception {
        this.scDisableHook = scDisableHook;
        m_displayRadar = displayRadar;
        getStylesheets().add(Locator.class.getResource("radar.css").toExternalForm());

        for (int i = 0; i < 6; i++) {
            targetsRect.add(new Rectangle());
        }

        initGraphics();
        registerListeners();
    }

    public void setBottomTable(BottomTable table) { this.bottomTable = table;  }


    // ******************** Initialization ************************************
    private void initGraphics() {
        final Image bgImage = new Image(getClass().getResourceAsStream(SRC_BG));
        final Image viewAngleImage = new Image(getClass().getResourceAsStream(SRC_VIEW_ANGLE));

        m_bgImageView = new ImageView(bgImage);
        m_viewAngleImageView = new ImageView(viewAngleImage);

        pane = new Pane(m_bgImageView, m_viewAngleImageView);
        pane.getChildren().addAll(targetsRect);
        getChildren().setAll(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
    }


    public void setAngle(final Double angle) {
        int sign = tempAngle <= angle ? 1 : -1;
        tempAngle = angle;
         _angle += sign * speed;
        m_viewAngleImageView.setRotate(_angle);
        m_displayRadar.accept(_angle);
    }

    public double getAngle() { return m_viewAngleImageView.getRotate(); }

    public boolean onKpClicked() {
//        int angle = new Random().nextInt(360);
//        Target newTarget = addNewTarget(angle, radius);
//        newTarget.startTarget();
        if (targetForMoving == null) {return false;}
        if (isLocatorMoving) { onRuClicked(); }
        targetForMoving.startTarget();
        onKpOnRightPanelClicked(targetForMoving);
        return true;
    }

    public Target addNewTarget(int angle, double distance, Runnable removeTargetFunc) {
        double halfSize = size * 0.5;
        distance = halfSize * distance * 0.01;
        Target newTarget = new Target(angle, targetsRect.get(currentTargetIdx++), radius, distance, size, removeTargetFunc);
        targets.add(newTarget);
        targetForMoving = newTarget;
        return newTarget;
    }

    public void removeTarget() {
        if (locatorMovingFuture != null) {
            locatorMovingFuture.cancel(true);
        }
        if (targets.size() != 0) {
            targets.get(0).onRemove();
            targets.remove(0);
        }
        targetForMoving = null;
        isLocatorMoving = false;
    }

    public void onAutoClicked(boolean isEnabled) {
        onRuClicked();

        if (isEnabled == isLocatorMoving) { return; }
        if (!isEnabled) {
            isLocatorMoving = false;
            return;
        }

        isLocatorMoving = true;
        final Target targetFinal = targetForMoving;
        int count = 20;

        locatorMovingFuture = Executors.newFixedThreadPool(1).submit(() -> {
            try {
                while (isLocatorMoving) {
                    Thread.sleep(100);
                    double absTargetAngle;
                    {
                        double angle;
                        {
                            double angleMode1 = AngleUtils.getAngleFromXY(targetFinal.x, targetFinal.y, radius, radius, 0) - 360;
                            double angleMode0 = targetFinal.angle;
                            angle = targetFinal.movingMode == 0 ? angleMode0 : angleMode1;
                            angle += 90;
                            System.out.println("Auto: " + angle);
                        }
                        absTargetAngle = Math.abs((360 + angle) % 360);
                    }
                    double absAngle = Math.abs((360 + _angle) % 360);
                    double currentDiff = Math.abs(absTargetAngle - absAngle);

                    if (absTargetAngle < absAngle - 30 || absTargetAngle > absAngle + 30) {
                        for (int j = 0; j < count; j++) {
                            Thread.sleep(100);
                            if (absAngle >= 270 && absAngle <= 360 && absTargetAngle >= 0 && absTargetAngle <= 90) {
                                currentDiff = absTargetAngle + 360 - absAngle;
                                //_angle -= currentDiff / count;// против часовой
                                _angle += currentDiff / count;// по часовой
                            } else if (absTargetAngle - absAngle > 0 && currentDiff <= 180) {
                                //System.out.println(_angle);
                                _angle += currentDiff / count;// по часовой
                            } else {
                                _angle -= currentDiff / count;// против часовой
                            }
                            setAngle(_angle);
                        }
                    }
                }

            } catch (InterruptedException e) {
                //e.printStackTrace();
                // it's ok for now
                System.out.println("locatorMovingFuture from Auto was interrupted.");
            }
        });
    }

    public void onKpOnRightPanelClicked(Target targetForMoving) {
        Target target = targets.get(0);
        isLocatorMoving = true;
        target = targetForMoving;

        double minAngle = 500;
        double diff = minAngle;

        for (Target el : targets) {
            double targetAngle = AngleUtils.getAngleFromXY(el.x, el.y, radius, radius, 0);
            diff = Math.abs(targetAngle - _angle);
            if (diff < minAngle) {
                target = el;
                minAngle = diff;
            }
        }

        final Target targetFinal = target;
        this.targetForMoving = targetFinal;

        locatorMovingFuture = Executors.newFixedThreadPool(1).submit(() -> {
            try {
                //for (int i = 0; i < 100; i++) {
                while (isLocatorMoving) {
                    Thread.sleep(100);
                    double absTargetAngle;
                    {
                        double angle;
                        {
                            double angleMode1 = AngleUtils.getAngleFromXY(targetFinal.x, targetFinal.y, radius, radius, 0) - 360;
                            double angleMode0 = targetFinal.angle;
                            angle = targetFinal.movingMode == 0 ? angleMode0 : angleMode1;
                            angle += 90;
                            System.out.println("Auto: " + angle);
                        }
                        absTargetAngle = Math.abs((360 + angle) % 360);
                    }
                    double absAngle = Math.abs((360 + _angle) % 360);
                    double currentDiff = Math.abs(absTargetAngle - absAngle);

                    double degreePerStep = currentDiff < 45 ? 1d : 3d;
                    double estimate = 0.3d;

                    if (Math.abs(degreePerStep - currentDiff) <= estimate) {
                        _angle += getRotationAngle(absTargetAngle, absAngle, currentDiff);
                        break;
                    }

                    _angle += getRotationAngle(absTargetAngle, absAngle, degreePerStep);
//                    if (absAngle >= 270 && absAngle <= 360 && absTargetAngle >= 0 && absTargetAngle <= 90) {
//                        currentDiff = absTargetAngle + 360 - absAngle;
//                        _angle += degreePerStep;// по часовой
//                    } else if (absTargetAngle - absAngle > 0 && currentDiff <= 180) {
//                        _angle += degreePerStep;// по часовой
//                    } else {
//                        _angle -= degreePerStep;// против часовой
//                    }

                    setAngle(_angle);

                    if (absTargetAngle > absAngle - topAngle && absTargetAngle < absAngle + topAngle) {
//                        scDisableHook.accept(false);
                        bottomTable.setDisableOnScBtn(targets.indexOf(targetForMoving), false);
                        scTurnOn = true;
                    }

                }

            } catch (InterruptedException e) {
                //e.printStackTrace();
                // it's ok for now
                System.out.println("locatorMovingFuture from KP was interrupted.");
            }
        });
    }

    private double getRotationAngle(double targetAngle, double currAngle, double dAngle) {
        return Math.signum(targetAngle - currAngle) * dAngle;
    }

    public void setLocatorMoving(boolean isLocatorMoving) {
        this.isLocatorMoving = isLocatorMoving;
    }

    public void onScClicked() {
        scTurnOn = true;
    }

    public void onRuClicked() {
        if (scTurnOn && locatorMovingFuture != null) {
            locatorMovingFuture.cancel(true);
            locatorMovingFuture = null;
        }
    }

    // ******************** Resizing ******************************************
    private void resize() {
        width = m_bgImageView.getLayoutBounds().getWidth();
        height = m_bgImageView.getLayoutBounds().getHeight();
        size = width < height ? width : height;
        radius = size * 0.5;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.setPrefSize(size, size);
            pane.relocate((getWidth() - size) * 0.5, (getHeight() - size) * 0.5);
        }
    }
}
