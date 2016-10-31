package pochelucr;

import robocode.*;
import robocode.util.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Pochelucr extends AdvancedRobot {

    private static boolean isInit = false;

    private Random r = new Random();

    private final double aimingAngleThreshold = 0.1;
    private final double movementThreshold = 0.05;
    private double bulletPower = 1;

    private static int bulletHit[] = new int[TargetingMode.values().length];
    private static ArrayList<ArrayList<Integer>> bulletShoot = new ArrayList<ArrayList<Integer>>();
//    private static int bulletShoot[] = new int[TargetingMode.values().length];
    private final int targetingRecalculationTime = 50;

    private ArrayList<EnemyInfo> enemies = new ArrayList<EnemyInfo>();
    private EnemyInfo chosenEnemy = null;

    private enum GunMode {
        OFF,
        WHEN_READY
    }

    private GunMode gunMode = GunMode.WHEN_READY;

    private enum RoboMode {
        SCANNING,
        TO_AIM,
        TO_FIRE
    }

    private RoboMode roboMode = RoboMode.SCANNING;

    private enum TargetingMode {
        HEAD_ON,
        LINEAR,
        LINEAR_HEAD_FAKE;

        private static TargetingMode[] _values = TargetingMode.values();
        public static TargetingMode fromInteger(int x) {
            if(x < _values.length)
            {
                return _values[x];
            }
            return null;
        }
    }

    private TargetingMode targetingMode = TargetingMode.LINEAR;

    private enum MovementMode {
        PENDULUM,
        RAMMING
    }

    private MovementMode movementMode = MovementMode.RAMMING;

    private long lastTime = -targetingRecalculationTime;

    public void run() {
        try {
            initComponents();
            initColors();

            setAdjustGunForRobotTurn(true);
            setAdjustRadarForGunTurn(true);

            while (true) {
                //Add your execute methods here
                setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

                if(chosenEnemy != null)
//                    bulletPower = 3*chosenEnemy.lastDistance/getWidth()/2+getEnergy()/200+1;
                    bulletPower = 2*(1-chosenEnemy.lastDistance/getBattleFieldWidth())+1;

                if (getTime() - lastTime >= targetingRecalculationTime)
                {
                    lastTime = getTime();
                    chooseTargetingMode();
                }

                roboStateMachine();
                movementStateMachine();
//                setTurnGunRight(10);
                execute();
            }
        } catch (RuntimeException re) {
            System.out.println(re);
        }
    }

    private void chooseTargetingMode()
    {
        double bulletAvg[] = new double[bulletHit.length];

        for (int i = 0; i < bulletAvg.length; i++)
        {
            try {
                bulletAvg[i] = (double)bulletHit[i]/bulletShoot.get(i).size();
            }
            catch(IndexOutOfBoundsException e)
            {
                System.out.println(e.getMessage());
            }
        }

        double bulletHitSum = 0;

        for (double v : bulletAvg)
        {
            bulletHitSum += v;
        }

        for(int i = 0; i < bulletAvg.length; i++)
        {
            if (bulletHitSum == 0)
                bulletAvg[i] = 1.0/bulletAvg.length;
            else
                bulletAvg[i] = bulletAvg[i]/bulletHitSum;
        }

        double _r = r.nextDouble();
        double cumulativeProb = 0.0;

        System.out.println("_r="+_r);
        for (int i = 0; i < bulletAvg.length; i++)
        {
            System.out.println(TargetingMode.fromInteger(i)+"="+bulletAvg[i]+" "+bulletHit[i]+" "+bulletShoot.get(i).size());
        }

        for (int i = 0; i < bulletAvg.length; i++)
        {
            cumulativeProb += bulletAvg[i];
            if(_r <= cumulativeProb){
                targetingMode = TargetingMode.fromInteger(i);
                break;
            }
        }

        System.out.println("targetingMode="+targetingMode);

    }

    private void roboStateMachine() {
        switch (roboMode)
        {
            case TO_AIM:
                targetStateMachine();
                break;
            case TO_FIRE:
                gunStateMachine();
                break;
        }
    }

    private void gunStateMachine() {
        switch (gunMode)
        {
            case OFF:
                return;
            case WHEN_READY:
                if(canShoot()) {
//                    bulletIds.get(targetingMode).add(setFireBullet(bulletPower).);
                    prepareFire();
                }
                break;
        }
        roboMode = RoboMode.SCANNING;
    }

    private void prepareFire() {
//        setFire(bulletPower);
        try {
            bulletShoot.get(targetingMode.ordinal()).add(setFireBullet(bulletPower).hashCode());
        }
        catch(IndexOutOfBoundsException e)
        {
            System.out.println(e.getMessage());
        }
    }

    private void targetStateMachine(){
        if(chosenEnemy == null)
            return;

        double turnAngle = 0.0;

        switch (targetingMode)
        {
            case HEAD_ON:
                turnAngle = chosenEnemy.lastBearing-getGunHeadingRadians();
                break;
            case LINEAR:
                turnAngle = chosenEnemy.lastBearing-getGunHeadingRadians() + (chosenEnemy.lastVelocity * Math.sin(chosenEnemy.lastHeading - chosenEnemy.lastBearing) / Rules.getBulletSpeed(bulletPower));
                break;
            case LINEAR_HEAD_FAKE:
                turnAngle = chosenEnemy.lastBearing-getGunHeadingRadians() + (-chosenEnemy.lastVelocity * Math.sin(chosenEnemy.lastHeading - chosenEnemy.lastBearing) / Rules.getBulletSpeed(bulletPower));
                break;
        }

        turnAngle = angleToTurn(Utils.normalAbsoluteAngle(turnAngle));
        if(turnAngle < 0)
        {
            setTurnGunLeftRadians(-turnAngle);
        }
        else
        {
            setTurnGunRightRadians(turnAngle);
        }
        roboMode = RoboMode.TO_FIRE;
    }

    private double angleForLenearTargeting()
    {
        return 0.0;
    }

    private int direction = 1;
    private final int movementAbs = 150;

    private void movementStateMachine(){
        switch (movementMode)
        {
            case PENDULUM:
                if(Math.abs(getDistanceRemaining()) <= movementThreshold) {
                    setAhead(movementAbs * direction);
                    direction *= -1;
                }
                break;
            case RAMMING:
                if(chosenEnemy == null)
                    return;
                double toTurn = angleToTurn(Utils.normalAbsoluteAngle(chosenEnemy.lastBearing - getHeadingRadians()));
                if(toTurn < 0)
                {
                    setTurnLeftRadians(-toTurn);
                }
                else
                {
                    setTurnRightRadians(toTurn);
                }
                setAhead(chosenEnemy.lastDistance*(Math.PI-Math.abs(toTurn))/Math.PI);
                break;
        }
    }

    private boolean canShoot(){
        return (getGunHeat() == 0.0 && Math.abs(getGunTurnRemainingRadians()) <= aimingAngleThreshold);
    }

    private double angleToTurn(double angle){
        return (angle > Math.PI ? angle - Math.PI * 2 : angle);
    }

    //Initialization process
    //If you have data structures or preprocessing before the match
    private void initComponents() {
        if(!isInit) {
            Arrays.fill(bulletHit, 1);
            for (int i = 0; i < TargetingMode.values().length; i++)
            {
                bulletShoot.add(new ArrayList<Integer>());
                bulletShoot.get(i).add(0);
            }
//            Arrays.fill(bulletShoot, 1);
            isInit = true;
        }
    }

    //Fancy colours for your bot
    private void initColors() {
        Color thgColor = new Color(142, 255, 242);
        setColors(Color.black, Color.green, thgColor);
    }

    //When you scan an opponent do something
    public void onScannedRobot(ScannedRobotEvent e) {
        try {
            setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
            if(roboMode == RoboMode.SCANNING){
                EnemyInfo scannedEnemy = getEnemyByName(e.getName());
                if(scannedEnemy == null)
                {
                    scannedEnemy = new EnemyInfo(e.getName());
                    enemies.add(scannedEnemy);
                }
                scannedEnemy.lastBearing = getHeadingRadians()+e.getBearingRadians();
                scannedEnemy.lastHeading = e.getHeadingRadians();
                scannedEnemy.lastVelocity = e.getVelocity();
                scannedEnemy.lastDistance = e.getDistance();

                chosenEnemy = scannedEnemy;
                roboMode = RoboMode.TO_AIM;
            }

        } catch (RuntimeException re) {
            System.out.println(re);
        }
    }

    public static double rollingAvg(double value, double newEntry, double n, double weighting ) {
        return (value * n + newEntry * weighting)/(n + weighting);
    }

    public EnemyInfo getEnemyByName(String name)
    {
        for(EnemyInfo v : enemies)
        {
            if (v.getName().equals(name))
                return v;
        }
        return null;
    }


    //Somebody died, maybe record some information about it?
    public void onRobotDeath(RobotDeathEvent e) {
        try {

        } catch (RuntimeException re) {
            System.out.println(re);
        }
    }


    //You got hit by a bullet.. FIGHT OR FLIGHT
    public void onHitByBullet(HitByBulletEvent e) {
        try {

        } catch (RuntimeException re) {
            System.out.println(re);
        }
    }


    //You hit someone with your gun, make sure to use that to your advantage
    public void onBulletHit(BulletHitEvent e) {
        try {
            for (int i = 0; i < bulletShoot.size(); i++)
            {
                if(bulletShoot.get(i).contains(e.getBullet().hashCode()))
                {
                    bulletHit[i]++;
                    break;
                }
            }
//            bulletHit[targetingMode.ordinal()]++;
        } catch (RuntimeException re) {
            System.out.println(re);
        }
    }


    //Bullets can disable other bullets, there is also an event for this
    public void onBulletHitBullet(BulletHitBulletEvent e) {
        try {

        } catch (RuntimeException re) {
            System.out.println(re);
        }
    }

    //Trivial
    public void onHitWall(HitWallEvent e) {
        direction *= -1;
    }


    //Congrats
    public void onWin(WinEvent e) {
        try {

        } catch (RuntimeException re) {
            System.out.println(re);
        }
    }


    //GG, code some more to kill them all!
    public void onDeath(DeathEvent e) {
        try {

        } catch (RuntimeException re) {
            System.out.println(re);
        }
    }


    //If your robot gets too big it might not have enough time to complete a turn
    //There is an event for skipped turns, use this to debug
    public void onSkippedTurn(SkippedTurnEvent e) {
        System.out.println("TURN SKIP : " + e.getTime());
    }



}
