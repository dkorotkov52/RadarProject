package src;

public class AngleUtils {
    public static double gradusToRodian(double gradus) {
        return gradus * (3.14 / 180);
    }

    public static double getDistance(final double x, final double y, final double center) {
        double deltaX = x - center;
        double deltaY = y - center;
        return Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
    }

    public static double getAngleFromXY(final double x, final double y, final double centerX, final double centerY, final double angleOffset) {
        // For ANGLE_OFFSET =  0 -> Angle of 0 is at 3 o'clock
        // For ANGLE_OFFSET = 90  ->Angle of 0 is at 12 o'clock
        double deltaX = x - centerX;
        double deltaY = y - centerY;
        double radius = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
        double nx     = deltaX / radius;
        double ny     = deltaY / radius;
        double theta  = Math.atan2(ny, nx);
        theta         = Double.compare(theta, 0.0) >= 0 ? Math.toDegrees(theta) : Math.toDegrees((theta)) + 360.0;
        double angle  = (theta + angleOffset) % 360;
        return angle;
    }
}
