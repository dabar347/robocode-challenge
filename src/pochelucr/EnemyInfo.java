package pochelucr;

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

    public boolean isDead = false;

    public double lastEnergy = 0.0;

    public void setNewData(double bearing, double velocity, double distance, double heading, double time)
    {
        lastBearing = bearing;
        lastVelocity = velocity;
        lastDistance = distance;

        lastTurnRate = (heading - lastHeading)/(time-lastTime);
        lastHeading = heading;
        lastTime = time;
    }

    public double getRelativeX()
    {
        return lastDistance*Math.sin(lastBearing);
    }

    public double getRelativeY()
    {
        return lastDistance*Math.cos(lastBearing);
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
