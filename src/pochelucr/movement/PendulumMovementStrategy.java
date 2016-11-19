package pochelucr.movement;

import pochelucr.EnemyInfo;
import robocode.AdvancedRobot;

import java.util.List;

/**
 * Created by dabar347 on 19/11/2016.
 */
public class PendulumMovementStrategy extends MovementStrategy {

    private int direction = 1;

    @Override
    public void doMove(EnemyInfo chosenEnemy, EnemyInfo avoidedEnemy)
    {
        if(Math.abs(robot.getDistanceRemaining()) <= movementThreshold) {
            robot.setAhead(movementAbs * direction);
            direction *= -1;
        }
    }

    public PendulumMovementStrategy(AdvancedRobot robot, List<EnemyInfo> enemies){super(robot,enemies);}

    @Override
    public String toString()
    {
        return "Pendulum "+super.toString();
    }
}
