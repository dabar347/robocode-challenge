package pochelucr;

import robocode.*;
import robocode.util.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Pochelucr extends AdvancedRobot {

    private final double aimingAngleThreshold = 0.1;
    private final double movementThreshold = 0.1;
    private final double bulletPower = 1;

    private double bulletAvg[] = new double[TargetingMode.values().length];
    private ArrayList<ArrayList<Integer>> bulletIds = new ArrayList<ArrayList<Integer>>(TargetingMode.values().length);

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
        LINEAR
    }

    private TargetingMode targetingMode = TargetingMode.LINEAR;

    private enum MovementMode {
        PENDULUM
    }

    private MovementMode movementMode = MovementMode.PENDULUM;

    public void run() {
        try {
            initComponents();
            initColors();

            setAdjustGunForRobotTurn(true);
            setAdjustRadarForGunTurn(true);

            while (true) {
                //Add your execute methods here
                setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

                roboStateMachine();
                movementStateMachine();
//                setTurnGunRight(10);
                execute();
            }
        } catch (RuntimeException re) {
            System.out.println(re);
        }
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
                    setFire(bulletPower);
                }
                break;
        }
        roboMode = RoboMode.SCANNING;
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
        }

        setTurnGunRightRadians(Utils.normalAbsoluteAngle(turnAngle));

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
        }
    }

    private boolean canShoot(){
        return (getGunHeat() == 0.0 && Math.abs(getGunTurnRemainingRadians()) <= aimingAngleThreshold);
    }

    //Initialization process
    //If you have data structures or preprocessing before the match
    private void initComponents() {
        Arrays.fill(bulletAvg,0.5);
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
            e.getBullet();
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
