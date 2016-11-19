package pochelucr;

import robocode.AdvancedRobot;

/**
 * Created by dabar347 on 19/11/2016.
 */
public abstract class Strategy {
    protected AdvancedRobot robot;

    public abstract double getAccuracy();

    public void setRobot(AdvancedRobot robot)
    {
        if(this.robot != null)
            return;
        this.robot = robot;
    }

    public Strategy(AdvancedRobot robot)
    {
        setRobot(robot);
    }

    @Override
    public String toString()
    {
        return "Strategy " + getAccuracy();
    }
}
