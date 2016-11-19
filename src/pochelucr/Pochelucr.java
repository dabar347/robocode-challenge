package pochelucr;

import pochelucr.movement.AntigravityMovementStrategy;
import pochelucr.movement.AvoidMovementStrategy;
import pochelucr.movement.MovementStrategy;
import pochelucr.movement.RammingMovementStrategy;
import pochelucr.targeting.CircularTargetingStrategy;
import pochelucr.targeting.HeadOnTargetingStrategy;
import pochelucr.targeting.TargetingStrategy;
import robocode.*;
import robocode.util.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

public class Pochelucr extends AdvancedRobot {

    //DEBUG
    private static Logger LOGGER = Logger.getLogger(Pochelucr.class.getName());

    private static ArrayList<EnemyInfo> enemies = new ArrayList<EnemyInfo>();
    private EnemyInfo chosenEnemy = null;
    private EnemyInfo avoidedEnemy = null;

    private static ArrayList<TargetingStrategy> targetingStrategies = new ArrayList<TargetingStrategy>();
    private static TargetingStrategy targetingStrategy = null;

    private static ArrayList<MovementStrategy> movementStrategies = new ArrayList<MovementStrategy>();
    private AvoidMovementStrategy avoidanceStrategy = new AvoidMovementStrategy(this,enemies);
    private MovementStrategy movementStrategy = null;

    public void onPaint(Graphics2D g)
    {
//        g.setColor(Color.green);
//
//        g.drawLine((int)x0,(int)y0,(int)x1,(int)y1);
//
//        g.setColor(Color.BLUE);

//        g.drawLine((int)x0,(int)y0,(int)(1000*Math.sin(a0)),(int)(1000*Math.cos(a0)));
//
//        g.setColor(Color.RED);
//
//        g.drawLine((int)x0,(int)y0,(int)(1000*Math.sin(a1+getGunHeadingRadians()-getGunTurnRemainingRadians())),(int)(1000*Math.cos(a1-getGunTurnRemainingRadians())));

//        System.out.println(targetingStrategy);
        LOGGER.info("+++DEBUG+++");
        LOGGER.info(movementStrategy.toString());
        LOGGER.info(targetingStrategy.toString());
//        LOGGER.info(targetingMode);

    }


    private static int round = 0;
    private final static int decisionRound = 0;

    private Random r = new Random();

    private final double aimingAngleThreshold = 0.1;
    private final double enemyChoiseThreshold = 50.0;
    private final double gunHeatAimingThreshold = 0.1;


    private double bulletPower = 1;

    private final int targetingRecalculationTime = 50;
    private final int movementRecalculationTime = 250;

    private enum GunMode {
        OFF,
        WHEN_READY,
        CHASE_WHEN_READY
    }

    private GunMode gunMode = GunMode.WHEN_READY;

    private enum RoboMode {
        SCANNING,
        TO_AIM,
        TO_FIRE
    }

    private RoboMode roboMode = RoboMode.SCANNING;

    private long lastTargetingRecalculationTime = -targetingRecalculationTime;
    private long lastMovementRecalculationTime = -movementRecalculationTime;

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
                    bulletPower = 2*((1-chosenEnemy.lastDistance/getBattleFieldWidth())*3/4+(getEnergy()/100)/4)+1;

                if (getTime() - lastTargetingRecalculationTime >= targetingRecalculationTime)
                {
                    lastTargetingRecalculationTime = getTime();
                    chooseTargetingMode();
                }

                if (getTime() - lastMovementRecalculationTime >= movementRecalculationTime)
                {
                    lastMovementRecalculationTime = getTime();
                    chooseMovementMode();
                }

                roboStateMachine();
                movementStateMachine();
                execute();
            }
        } catch (RuntimeException re) {
            LOGGER.severe(re.toString());
        }
    }

    private void chooseMovementMode()
    {
        movementStrategy = movementStrategies.get(r.nextInt(movementStrategies.size()));
    }

    private void chooseTargetingMode()
    {
        double bulletAvg[] = new double[targetingStrategies.size()];

        for (int i = 0; i < targetingStrategies.size(); i++)
        {
            try {
                bulletAvg[i] = targetingStrategies.get(i).getAccuracy();
            }
            catch(IndexOutOfBoundsException e)
            {
                LOGGER.severe(e.toString());
            }
        }

        if(decisionRound != 0 && round >= decisionRound)
        {
            double maxV = 0.0;
            int maxI = 0;

            for (int i = 0; i < bulletAvg.length; i++)
            {
                if (maxV < bulletAvg[i])
                {
                    maxI = i;
                    maxV = bulletAvg[i];
                }
            }

//            targetingMode = TargetingMode.fromInteger(maxI);
            targetingStrategy = targetingStrategies.get(maxI);

            return;
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

//        LOGGER.info("");
//        LOGGER.info("_r="+_r);
//        for (int i = 0; i < bulletAvg.length; i++)
//        {
////            LOGGER.info(TargetingMode.fromInteger(i)+"="+bulletAvg[i]+" "+bulletHit[i]+" "+bulletShoot.get(i).size());
//            LOGGER.info(targetingStrategies.get(i).toString());
//        }

        for (int i = 0; i < bulletAvg.length; i++)
        {
            cumulativeProb += bulletAvg[i];
            if(_r <= cumulativeProb){
//                targetingMode = TargetingMode.fromInteger(i);
                targetingStrategy = targetingStrategies.get(i);
                break;
            }
        }

        if(targetingStrategy == null)
        {
            LOGGER.severe("_r = "+_r+", cP = "+cumulativeProb);
        }

//        LOGGER.info("targetingMode="+targetingMode);

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

    private double chasingTime = 0.0;
    private double lastChasingTime = 0.0;
    private void gunStateMachine() {
        switch (gunMode)
        {
            case OFF:
                break;
            case WHEN_READY:
                if(canShoot()) {
                    prepareFire();
                    roboMode = RoboMode.SCANNING;
                }
                else {
                    roboMode = RoboMode.TO_AIM;
                }
                break;
            case CHASE_WHEN_READY:
                if(canShoot()) {
                    if(chosenEnemy == null)
                        return;
                    chasingTime -= getTime() - lastChasingTime;
                    lastChasingTime = getTime();
                    double minimumTime = chosenEnemy.lastDistance/Rules.getBulletSpeed(1.0);
                    if(chasingTime < minimumTime)
                    {
                        prepareFire();
                        chasingTime = chosenEnemy.lastDistance/Rules.getBulletSpeed(bulletPower);
                    }
                    else
                    {
                        double _bulletPower = (20 - chosenEnemy.lastDistance/chasingTime)/3;
                        prepareFire(_bulletPower);
                    }
                    roboMode = RoboMode.TO_AIM;
                    return;
                }
        }
    }


    private void prepareFire() {
        prepareFire(bulletPower);
    }

    private void prepareFire(double _bulletPower) {
        try {
            if(targetingStrategy != null)
                targetingStrategy.addBullet(setFireBullet(_bulletPower));
        }
        catch(Exception e)
        {
            LOGGER.severe(e.toString());
        }
    }

    private void targetStateMachine(){
        if(chosenEnemy == null || getGunHeat() > gunHeatAimingThreshold)
            return;

        double turnAngle;

        if(targetingStrategy == null)
        {
            return;
        }
        turnAngle = targetingStrategy.target(chosenEnemy,bulletPower);

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

    private void movementStateMachine(){
        movementStrategy.doMove(chosenEnemy,avoidedEnemy);
        if(movementStrategy.needsInteruption())
        {
            chooseMovementMode();
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
        if(round == 0) {
            targetingStrategies.add(new CircularTargetingStrategy(this));
            targetingStrategies.add(new HeadOnTargetingStrategy(this));

            movementStrategies.add(new AntigravityMovementStrategy(this,enemies));
            movementStrategies.add(new RammingMovementStrategy(this,enemies));
        }
        round++;
    }

    //Fancy colours for your bot
    private void initColors() {
        setColors(new Color(77, 0, 50), new Color(64, 32, 53), new Color(225, 227, 136));
        setBulletColor(new Color(139, 204, 0));
    }

    //When you scan an opponent do something
    public void onScannedRobot(ScannedRobotEvent e) {
        try {
            setTurnRadarLeftRadians(getRadarTurnRemainingRadians());

            EnemyInfo scannedEnemy = getEnemyByName(e.getName());
            if(scannedEnemy == null)
            {
                scannedEnemy = new EnemyInfo(e.getName());
                enemies.add(scannedEnemy);
            }
            scannedEnemy.setNewData(getHeadingRadians()+e.getBearingRadians(),e.getVelocity(),e.getDistance(),e.getHeadingRadians(),getTime(),getX(),getY());

            double energyDrop = scannedEnemy.lastEnergy - e.getEnergy();
            if(energyDrop <= 3.0 && energyDrop >= 1.0 && scannedEnemy == chosenEnemy) {
                avoidedEnemy = scannedEnemy;
                movementStrategy = avoidanceStrategy;
                avoidanceStrategy.setReset();
            }

            scannedEnemy.lastEnergy = e.getEnergy();
            scannedEnemy.isDead = false;


            if(chosenEnemy == null || chosenEnemy.isDead || scannedEnemy.lastDistance < chosenEnemy.lastDistance - enemyChoiseThreshold)
                chosenEnemy = scannedEnemy;

            if(roboMode == RoboMode.SCANNING){
                roboMode = RoboMode.TO_AIM;
            }

        } catch (RuntimeException re) {
            LOGGER.severe(re.toString());
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
            getEnemyByName(e.getName()).isDead = true;
        } catch (RuntimeException re) {
            LOGGER.severe(re.toString());
        }
    }


    //You got hit by a bullet.. FIGHT OR FLIGHT
    public void onHitByBullet(HitByBulletEvent e) {
        try {
            EnemyInfo enemy = getEnemyByName(e.getName());
            if(enemy != null)
            {
                enemy.lastEnergy += Rules.getBulletDamage(e.getBullet().getPower());
                enemy.increaseDanger();
            }
        } catch (RuntimeException re) {
            LOGGER.severe(re.toString());
        }
    }


    //You hit someone with your gun, make sure to use that to your advantage
    public void onBulletHit(BulletHitEvent e) {
        try {
            for (int i = 0; i < targetingStrategies.size(); i++)
            {
                if(targetingStrategies.get(i).hitBulletCheck(e.getBullet()))
                    break;
            }

            EnemyInfo enemy = getEnemyByName(e.getName());
            if(enemy != null)
            {
                enemy.lastEnergy -= Rules.getBulletDamage(e.getBullet().getPower());
            }
        } catch (RuntimeException re) {
            LOGGER.severe(re.toString());
        }
    }


    //Bullets can disable other bullets, there is also an event for this
    public void onBulletHitBullet(BulletHitBulletEvent e) {
        try {

        } catch (RuntimeException re) {
            LOGGER.severe(re.toString());
        }
    }

    //Trivial
    public void onHitWall(HitWallEvent e) {
        movementStrategy = avoidanceStrategy;
        avoidanceStrategy.setReset();
    }


    //Congrats
    public void onWin(WinEvent e) {
        try {

        } catch (RuntimeException re) {
            LOGGER.severe(re.toString());
        }
    }


    //GG, code some more to kill them all!
    public void onDeath(DeathEvent e) {
        try {

        } catch (RuntimeException re) {
            LOGGER.severe(re.toString());
        }
    }


    //If your robot gets too big it might not have enough time to complete a turn
    //There is an event for skipped turns, use this to debug
    public void onSkippedTurn(SkippedTurnEvent e) {
        LOGGER.severe("TURN SKIP : " + e.getTime());
    }



}
