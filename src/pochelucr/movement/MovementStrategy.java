package pochelucr.movement;

import pochelucr.EnemyInfo;
import pochelucr.Strategy;
import robocode.AdvancedRobot;

import java.util.List;

/**
 * Created by dabar347 on 19/11/2016.
 */
public abstract class MovementStrategy extends Strategy{

    //CONSTS
    protected final int movementAbs = 150;
    protected final double movementThreshold = 0.05;

    private boolean isFinished = false;

    public boolean needsInteruption()
    {
        if(isFinished)
        {
            isFinished = false;
            return true;
        }
        return false;
    }

    protected void release()
    {
        isFinished = true;
    }

    @Override
    public double getAccuracy()
    {
        return 1.0;
    }

    public abstract void doMove(EnemyInfo chosenEnemy, EnemyInfo avoidedEnemy);

    protected List<EnemyInfo> enemies = null;
    public void setEnemyList(List<EnemyInfo> enemies)
    {
        if(this.enemies != null)
        {
            return;
        }
        this.enemies = enemies;
    }

    public MovementStrategy(AdvancedRobot robot, List<EnemyInfo> enemies)
    {
        super(robot);
        setEnemyList(enemies);
    }

    protected double angleToTurn(double angle){
        return (angle > Math.PI ? angle - Math.PI * 2 : angle);
    }

    @Override
    public String toString()
    {
        return "Movement " + super.toString();
    }
}
