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
            release();
            inAvoidance = false;
    }

    public AvoidMovementStrategy(AdvancedRobot robot, List<EnemyInfo> enemies){super(robot,enemies);}

    @Override
    public String toString()
    {
        return "Avoid "+super.toString();
    }
}
