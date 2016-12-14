package pochelucr.movement;

import pochelucr.EnemyInfo;
import pochelucr.Strategy;
import robocode.AdvancedRobot;
import robocode.util.Utils;

import java.util.List;

/**
 * Created by dabar347 on 19/11/2016.
 */
public abstract class MovementStrategy extends Strategy{

    //CONSTS
    protected final int movementAbs = 150;
    protected final double movementThreshold = 0.05;

    private int wonRounds = 0   ;
    private int nRounds = 0;

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
        return (nRounds == 0 ? 1 : (wonRounds == 0 ? 1.0/nRounds : (double)wonRounds/nRounds));
    }

    public void increaseAccuracy()
    {
        wonRounds++;
        nRounds++;
    }

    public void decreaseAccuracy()
    {
        nRounds++;
    }

    public abstract void doMove(EnemyInfo chosenEnemy, EnemyInfo avoidedEnemy);

    protected void moveToBearingDistane(double bearing, double distance)
    {
        if(Math.abs(bearing-robot.getHeadingRadians())<Math.PI/2){
            robot.setTurnRightRadians(Utils.normalRelativeAngle(bearing - robot.getHeadingRadians()));
            robot.setAhead(distance);
        } else {
            robot.setTurnRightRadians(Utils.normalRelativeAngle(bearing+Math.PI-robot.getHeadingRadians()));
            robot.setBack(distance);
        }
    }

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
