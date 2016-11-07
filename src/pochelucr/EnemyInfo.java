package pochelucr;

import java.awt.geom.Point2D;

/**
 * Created by dabar347 on 27/10/2016.
 */
public class EnemyInfo {

    private String name;

    public double lastBearing = 0.0;
    public double lastVelocity = 0.0;
    public double lastHeading = 0.0;
    public double lastDistance = 0.0;
    public double lastTurnRate = 0.0;
    public double lastTime = 0.0;

    public Point2D.Double lastAbsPosition;

    public boolean isDead = false;

    public double lastEnergy = 0.0;

    public void setNewData(double bearing, double velocity, double distance, double heading, double time, double myX, double myY)
    {
        lastBearing = bearing;
        lastVelocity = velocity;
        lastDistance = distance;

        lastAbsPosition = new Point2D.Double(myX + lastDistance*Math.sin(lastBearing), myY + lastDistance*Math.cos(lastBearing));

        lastTurnRate = (heading - lastHeading)/(time-lastTime);
        lastHeading = heading;
        lastTime = time;
    }

    public double getRelativeX(double x)
    {
        return lastAbsPosition.x - x;
//        return lastDistance*Math.sin(lastBearing);
    }

    public double getRelativeY(double y)
    {
        return lastAbsPosition.y - y;
//        return lastDistance*Math.cos(lastBearing);
    }

    public double getPredictedDistance(double x, double y)
    {
        return lastAbsPosition.distance(x,y);
    }

    public double getPredictedLastBearing(double x, double y)
    {
        return Math.atan2(lastAbsPosition.x - x,lastAbsPosition.y - y);
    }

    public EnemyInfo(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
