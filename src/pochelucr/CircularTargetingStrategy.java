package pochelucr;

import robocode.AdvancedRobot;
import robocode.Rules;

import java.awt.geom.Point2D;

/**
 * Created by dabar347 on 12/11/2016.
 */
public class CircularTargetingStrategy extends TargetingStrategy {

    public CircularTargetingStrategy (AdvancedRobot robot) {super(robot);}

    @Override
    public double target(EnemyInfo enemy, double bulletPower)
    {
        double turnAngle = 0.0;
        double dT = 0.0;
        double predictedX = enemy.getRelativeX(robot.getX()), predictedY = enemy.getRelativeY(robot.getY());
        while((++dT* Rules.getBulletSpeed(bulletPower)+robot.getGunHeat()/robot.getGunCoolingRate()+Math.abs(turnAngle)/Rules.GUN_TURN_RATE_RADIANS) < Point2D.Double.distance(0,0,predictedX,predictedY)){
            predictedX += enemy.lastVelocity*Math.sin(enemy.lastHeading + enemy.lastTurnRate*dT);
            predictedY += enemy.lastVelocity*Math.cos(enemy.lastHeading + enemy.lastTurnRate*dT);
            turnAngle = Math.atan2(predictedX,predictedY) - robot.getGunHeadingRadians();
        }

        return turnAngle;
    }

    @Override
    public String toString()
    {
        return "Circular"+super.toString();
    }
}
