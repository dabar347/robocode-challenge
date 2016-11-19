package pochelucr.movement;

import pochelucr.EnemyInfo;
import robocode.AdvancedRobot;
import robocode.util.Utils;

import java.util.List;

/**
 * Created by dabar347 on 19/11/2016.
 */
public class RammingMovementStrategy extends MovementStrategy {

    public void doMove(EnemyInfo chosenEnemy, EnemyInfo avoidedEnemy)
    {
        if(chosenEnemy == null)
            return;
        double toTurn = angleToTurn(Utils.normalAbsoluteAngle(chosenEnemy.lastBearing - robot.getHeadingRadians()));
        if(toTurn < 0)
        {
            robot.setTurnLeftRadians(-toTurn);
        }
        else
        {
            robot.setTurnRightRadians(toTurn);
        }
        robot.setAhead(chosenEnemy.lastDistance*(Math.PI-Math.abs(toTurn))/Math.PI);
    }

    public RammingMovementStrategy(AdvancedRobot robot, List<EnemyInfo> enemies){super(robot,enemies);}

    @Override
    public String toString()
    {
        return "Ramming "+super.toString();
    }
}
