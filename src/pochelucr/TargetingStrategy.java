package pochelucr;

import robocode.AdvancedRobot;
import robocode.Bullet;

import java.util.ArrayList;

/**
 * Created by dabar347 on 12/11/2016.
 */
public abstract class TargetingStrategy {

    protected AdvancedRobot robot;

    protected ArrayList<Bullet> shotBullets = new ArrayList<Bullet>();
    protected int hitCount = 1, n = 1;


    public void addBullet(Bullet bullet)
    {
        shotBullets.add(bullet);
        n++;
    }

    public boolean hitBulletCheck(Bullet bullet)
    {
        if(shotBullets.contains(bullet))
        {
            shotBullets.remove(bullet);
            hitCount++;
            return true;
        }
        else
        {
            return false;
        }
    }

    public double getAccuracy()
    {
        return (double)hitCount/(double)n;
    }

    public void setRobot(AdvancedRobot robot)
    {
        if(this.robot != null)
            return;
        this.robot = robot;
    }

    public TargetingStrategy(AdvancedRobot robot)
    {
        setRobot(robot);
    }

    @Override
    public String toString()
    {
        return "Targeting " + getAccuracy();
    }

    public abstract double target(EnemyInfo enemy, double bulletPower);
}
