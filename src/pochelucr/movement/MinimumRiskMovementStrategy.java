package pochelucr.movement;

import pochelucr.EnemyInfo;
import robocode.AdvancedRobot;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by dabar347 on 30/11/2016.
 */
public class MinimumRiskMovementStrategy extends MovementStrategy {

    private final int POINT_COUNT_TO_GEN = 10;
    private final int WAITING_TIME = 50;
    private final Random r = new Random();

    public Point2D.Double _point = null;
    public double _angle, _distance;

    private long lastTime = 0;

    @Override
    public void doMove(EnemyInfo chosenEnemy, EnemyInfo avoidedEnemy)
    {
        System.out.println("DM: "+(robot.getTime() - lastTime < WAITING_TIME)+", "+(Math.abs(robot.getDistanceRemaining()) >= 1));
        if(robot.getTime() - lastTime < WAITING_TIME && Math.abs(robot.getDistanceRemaining()) >= 1)//TIMEOUT
            return;

        lastTime = robot.getTime();

        List<Point2D.Double> points = generatePoints();

        double min = 0.0;
        Point2D.Double minPoint = null;

        System.out.println(points.size());

        for(Point2D.Double point : points)
        {
            double risk = getRisk(point);
            if(risk <= min || minPoint == null)
            {
                minPoint = point;
                min = risk;
            }
//            System.out.println(point+" "+risk);
        }

        System.out.println(minPoint+" "+min+" <---");

        _point = minPoint;
        _angle = Math.atan2(minPoint.x-robot.getX(),minPoint.y-robot.getY());
        _distance = minPoint.distance(robot.getX(),robot.getY());
        moveToBearingDistane(Math.atan2(minPoint.x-robot.getX(),minPoint.y-robot.getY()),minPoint.distance(robot.getX(),robot.getY()));

        //Move to point
    }

    private double getRisk(Point2D.Double point)
    {
        double ret = 0.0;
        for(EnemyInfo e : enemies)
        {
            ret += getRiskForEnemy(point,e);
        }
        return ret;
    }

    private double getRiskForEnemy(Point2D.Double point, EnemyInfo enemy)
    {
        return (enemy.lastEnergy/Math.pow(point.distance(enemy.lastAbsPosition),2));
    }

    private List<Point2D.Double> generatePoints()
    {
        ArrayList<Point2D.Double> list = new ArrayList<Point2D.Double>();

        for (int i = 0; i < POINT_COUNT_TO_GEN; i++)
        {
            list.add(new Point2D.Double(r.nextDouble()*robot.getBattleFieldWidth(),r.nextDouble()*robot.getBattleFieldHeight()));
        }
        return list;
//        return new LinkedList<Point2D>();
    }

    public MinimumRiskMovementStrategy(AdvancedRobot robot, List<EnemyInfo> enemies){
        super(robot,enemies);
        lastTime = robot.getTime() - WAITING_TIME;
    }

    @Override
    public String toString()
    {
        return "MinimumRisk "+super.toString();
    }
}
