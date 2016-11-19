package pochelucr.movement;

import pochelucr.EnemyInfo;
import robocode.AdvancedRobot;
import robocode.util.Utils;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * Created by dabar347 on 19/11/2016.
 */
public class AntigravityMovementStrategy extends MovementStrategy{

    private final double wallBaseGravityK = 5;

    public void doMove(EnemyInfo chosenEnemy, EnemyInfo avoidedEnemy)
    {
        Point2D.Double forceVector = new Point2D.Double();

        int alives = 0;

        for (EnemyInfo v : enemies)
        {
            if (!v.isDead)
            {
                addToPoint(forceVector,calculateAntigravityForce(v));
                alives++;
            }
        }

        addToPoint(forceVector,calculateAntigravityForce(-alives*wallBaseGravityK,0,robot.getY()+10));//BOTTOM
        addToPoint(forceVector,calculateAntigravityForce(-alives*wallBaseGravityK,Math.PI,robot.getBattleFieldHeight()-robot.getY()+10));//TOP
        addToPoint(forceVector,calculateAntigravityForce(-alives*wallBaseGravityK,Math.PI/2,robot.getX()+10));
        addToPoint(forceVector,calculateAntigravityForce(-alives*wallBaseGravityK,Math.PI*3/2,robot.getBattleFieldWidth()-robot.getX()+10));

        double _angleGravity = Math.atan2(forceVector.x,forceVector.y);

        if(forceVector.distance(0,0) == 0) {

        } else if(Math.abs(_angleGravity-robot.getHeadingRadians())<Math.PI/2){
            robot.setTurnRightRadians(Utils.normalRelativeAngle(_angleGravity - robot.getHeadingRadians()));
            robot.setAhead(Double.POSITIVE_INFINITY);
        } else {
            robot.setTurnRightRadians(Utils.normalRelativeAngle(_angleGravity+Math.PI-robot.getHeadingRadians()));
            robot.setAhead(Double.NEGATIVE_INFINITY);
        }
    }

    private void addToPoint (Point2D.Double point1, Point2D.Double point2)
    {
        point1.x += point2.x;
        point1.y += point2.y;
    }

    private Point2D.Double calculateAntigravityForce(EnemyInfo enemy)
    {
        double absoluteBearing = enemy.getPredictedLastBearing(robot.getX(),robot.getY());
        double distance = enemy.getPredictedDistance(robot.getX(),robot.getY());
        return calculateAntigravityForce(enemy.getAntigravityConstant(),absoluteBearing,distance);
    }

    private Point2D.Double calculateAntigravityForce(double k, double absoluteBearing, double distance)
    {
        distance = Math.abs(distance);
        return new Point2D.Double(-k*Math.sin(absoluteBearing)/(distance*distance),
                                  -k*Math.cos(absoluteBearing)/(distance*distance));
    }

    public AntigravityMovementStrategy(AdvancedRobot robot, List<EnemyInfo> enemies){super(robot,enemies);}

    @Override
    public String toString()
    {
        return "Antigravity "+super.toString();
    }
}
