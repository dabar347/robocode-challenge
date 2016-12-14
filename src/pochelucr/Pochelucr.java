package pochelucr;

import pochelucr.movement.*;
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

    private static ArrayList<Strategy> targetingStrategies = new ArrayList<Strategy>();
    private static TargetingStrategy targetingStrategy = null;

    private static ArrayList<Strategy> movementStrategies = new ArrayList<Strategy>();
    private AvoidMovementStrategy avoidanceStrategy = new AvoidMovementStrategy(this,enemies);
    private MovementStrategy normalMovementStrategy = null;
    private MovementStrategy movementStrategy = null;

    public void onPaint(Graphics2D g)
    {

    }


    private static int round = 0;
    private final static int decisionRound = 0;

    private Random r = new Random();

    private final double aimingAngleThreshold = 0.1;
    private final double enemyChoiseThreshold = 50.0;
    private final double gunHeatAimingThreshold = 0.1;


    private double bulletPower = 1;

    private final int targetingRecalculationTime = 250;
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

                if(chosenEnemy != null) {
                    bulletPower = 2 * ((1 - chosenEnemy.lastDistance / getBattleFieldWidth()) * 3 / 4 + (getEnergy() / 100) / 4) + 1;
                    bulletPower = (Rules.getBulletDamage(bulletPower) > chosenEnemy.lastEnergy) ? chosenEnemy.lastEnergy/4 : bulletPower;
                }

                if (getTime() - lastTargetingRecalculationTime >= targetingRecalculationTime)
                {
                    lastTargetingRecalculationTime = getTime();
                    targetingStrategy = (TargetingStrategy)chooseMode(targetingStrategies);
                }

                if (getTime() - lastMovementRecalculationTime >= movementRecalculationTime)
                {
                    lastMovementRecalculationTime = getTime();
                    chooseMovementMode();
                }

                targetStateMachine();
                gunStateMachine();
                movementStateMachine();
                execute();
            }
        } catch (RuntimeException re) {
            LOGGER.severe(re.toString());
        }
    }

    private void chooseMovementMode()
    {
        movementStrategy = normalMovementStrategy;
    }

    private Strategy chooseMode(ArrayList<Strategy> strategies)
    {
        double avg[] = new double[strategies.size()];

        for (int i = 0; i < strategies.size(); i++)
        {
            try {
                avg[i] = strategies.get(i).getAccuracy();
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

            for (int i = 0; i < avg.length; i++)
            {
                if (maxV < avg[i])
                {
                    maxI = i;
                    maxV = avg[i];
                }
            }

            return strategies.get(maxI);
        }

        double sum = 0;

        for (double v : avg)
        {
            sum += v;
        }

        for(int i = 0; i < avg.length; i++)
        {
            if (sum == 0)
                avg[i] = 1.0/avg.length;
            else
                avg[i] = avg[i]/sum;
        }

        double _r = r.nextDouble();
        double cumulativeProb = 0.0;

        for (int i = 0; i < avg.length; i++)
        {
            cumulativeProb += avg[i];
            if(_r <= cumulativeProb){
                return strategies.get(i);
            }
        }

        return null;
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

            movementStrategies.add(new MinimumRiskMovementStrategy(this,enemies));
        }
        normalMovementStrategy = (MovementStrategy)chooseMode(movementStrategies);
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

    private EnemyInfo getEnemyByName(String name)
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
            getEnemyByName(e.getName()).decreaseDanger();
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
                if(((TargetingStrategy)(targetingStrategies.get(i))).hitBulletCheck(e.getBullet()))
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
            normalMovementStrategy.increaseAccuracy();
        } catch (RuntimeException re) {
            LOGGER.severe(re.toString());
        }
    }


    //GG, code some more to kill them all!
    public void onDeath(DeathEvent e) {
        try {
            normalMovementStrategy.decreaseAccuracy();
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
