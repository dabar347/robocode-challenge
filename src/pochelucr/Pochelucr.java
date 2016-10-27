package pochelucr;

import robocode.AdvancedRobot;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.SkippedTurnEvent;
import robocode.WinEvent;
import robocode.util.Utils;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.ArrayList;
import java.util.Dictionary;

public class Pochelucr extends AdvancedRobot {

    private final double aimingAngleThreshold = 0.1;

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
        HEAD_ON
    }

    private TargetingMode targetingMode = TargetingMode.HEAD_ON;


    private int direction = 1;

    public void run() {
        try {
            initComponents();
            initColors();

            setAdjustGunForRobotTurn(true);
            setAdjustRadarForGunTurn(true);

            while (true) {
                //Add your execute methods here
                setTurnRadarRight(Double.POSITIVE_INFINITY);
                setTurnLeft(8*direction);
                setAhead(15*direction);

                roboStateMachine();
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
                    setFire(1);
                }
                break;
        }
        roboMode = RoboMode.SCANNING;
    }

    private void targetStateMachine(){
        if(chosenEnemy == null)
            return;

        switch (targetingMode)
        {
            case HEAD_ON:
                setTurnGunRight(chosenEnemy.lastBearing-getGunHeading());
                break;
        }
        roboMode = RoboMode.TO_FIRE;
    }

    private boolean canShoot(){
        return (getGunHeat() == 0.0 && Math.abs(getGunTurnRemaining()) <= aimingAngleThreshold);
    }

    //Initialization process
    //If you have data structures or preprocessing before the match
    private void initComponents() {
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
                scannedEnemy.lastBearing = getHeading()+e.getBearing();
                chosenEnemy = scannedEnemy;
                roboMode = RoboMode.TO_AIM;
            }

        } catch (RuntimeException re) {
            System.out.println(re);
        }
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
