package pochelucr.movement;

import pochelucr.EnemyInfo;
import robocode.AdvancedRobot;
import robocode.util.Utils;

import java.util.List;
import java.util.Random;

/**
 * Created by dabar347 on 19/11/2016.
 */
public class AvoidMovementStrategy extends MovementStrategy {


    private Random r = new Random();
    private boolean inAvoidance = false;

    public void setReset(){
        inAvoidance = false;
    }

    public void doMove(EnemyInfo chosenEnemy, EnemyInfo avoidedEnemy)
    {
        if(!inAvoidance && avoidedEnemy != null)
        {
            if(r.nextDouble() < 0.7)
            {
                release();
            }

            double _dist = r.nextGaussian()*movementAbs;
            robot.setAhead(_dist);
            double _angle = angleToTurn(Utils.normalAbsoluteAngle(r.nextGaussian()*Math.toRadians(30)+Math.PI/2+avoidedEnemy.lastBearing-robot.getHeadingRadians()));
            if(_angle < 0)
                robot.turnLeftRadians(-_angle);
            else
                robot.turnRightRadians(_angle);
            inAvoidance = true;
        }
        else if(Math.abs(robot.getDistanceRemaining()) <= movementThreshold || avoidedEnemy == null) {
            release();
            inAvoidance = false;
        }
    }

    public AvoidMovementStrategy(AdvancedRobot robot, List<EnemyInfo> enemies){super(robot,enemies);}

    @Override
    public String toString()
    {
        return "Avoid "+super.toString();
    }
}
