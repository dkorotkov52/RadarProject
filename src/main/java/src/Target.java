package src;

import javafx.scene.shape.Rectangle;

import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Target {
    public double angle = 0;
    public double x = 0;
    public double y = 0;
    private double sin = 0;
    private double cos = 0;
    private int signX = 1;
    private int signY = 1;
    private Rectangle target;
    private double radius;
    private double size;
    public double movingMode = 0;
    double xStart;
    double yStart;

    private boolean m_isStopped = false;

    private Runnable m_removeTargetFunc;

    public Target(int angle, Rectangle target, double center, double distance, double size, Runnable removeTargetFunc) {
        System.out.println("Alpha: " + angle + "; Beta: " + (-90 + angle));
        angle = -90 + angle;
        this.angle = angle;
        this.target = target;
        this.radius = center;
        this.size = size * 0.95;
        sin = Math.sin(AngleUtils.gradusToRodian(angle));
        cos = Math.cos(AngleUtils.gradusToRodian(angle));
        xStart = radius + distance * cos;
        yStart = radius + distance * sin;
        //movingMode = new Random().nextInt(2);
        movingMode = 0;
        m_removeTargetFunc = removeTargetFunc;
        redrawTarget();
//        startTarget();
    }

    public void startTarget() {
        Executors.newFixedThreadPool(1).submit(() -> {
            try {
                Thread.sleep(1000);

                for (int i = 0; i < 100; i++) {
                    if (m_isStopped) {return;}
                    if (angle >= -90 && angle < 0) {
                        signX = -1;
                        signY = -1;
                    }
                    else if (angle >= 0 && angle < 90) {
                        signX = -1;
                        signY = +1;
                    }
                    else if (angle >= 90 && angle < 180) {
                        signX = +1;
                        signY = +1;
                    }
                    else if (angle >= 180 && angle < 270) {
                        signX = +1;
                        signY = -1;
                    }
                    redrawTarget();
                    Thread.sleep(1000);
                    if((x>radius-radius*0.1 && x<radius+radius*0.1 && y<radius+radius*0.1 && y>radius-radius*0.1)
                    || AngleUtils.getDistance(x, y, radius) >= radius*0.8) {
                        m_removeTargetFunc.run();
                        return;
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void redrawTarget() {

        target.setVisible(true);

        if (x == 0) { x = xStart; }
        if (y == 0) { y = yStart; }

        System.out.println("1:::::" + x + "     "+ y);

        if (movingMode == 0) { // перемещаемся к центру
            double oldX = x;
            if (angle == 270) { y += 5; } else
                if (angle == 90) { y -= 5; } else
                    { x += 5 * signX; y = (((x - radius) * (y - radius)) / (oldX - radius)) + radius; }
        } else { // перемещаемся вдоль
            if ( angle <= -0 && angle >= -90 ) { y += 5; }
            if ( angle < -90 && angle >= -180 ) { x += 5; }
            if ( angle < -180 && angle >= -270 ) { y -= 5; }
            if ( angle < -270 && angle > -360 ) { x -= 5; }
        }


        target.relocate(x, y);

        target.setWidth(size * 0.03);
        target.setHeight(size * 0.03);
    }

    public void onRemove() {
        target.setVisible(false);
        m_isStopped = true;
    }

}
