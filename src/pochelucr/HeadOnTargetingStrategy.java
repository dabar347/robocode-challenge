package pochelucr;

import robocode.AdvancedRobot;
import robocode.Rules;

import java.awt.geom.Point2D;

/**
 * Created by dabar347 on 12/11/2016.
 */
public class HeadOnTargetingStrategy extends TargetingStrategy{
    public HeadOnTargetingStrategy (AdvancedRobot robot) {super(robot);}

    @Override
    public double target(EnemyInfo enemy, double bulletPower)
    {
        return enemy.getPredictedLastBearing(robot.getX(),robot.getY())-robot.getGunHeadingRadians();
    }

    @Override
    public String toString()
    {
        return "HeadOn"+super.toString();
    }
}
