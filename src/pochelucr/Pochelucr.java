package pochelucr;

import robocode.*;
import robocode.util.Utils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Pochelucr extends AdvancedRobot {

    //DEBUG
    private double x0,y0,x1,y1,a0,a1;

    public void onPaint(Graphics2D g)
    {
        g.setColor(Color.green);

        g.drawLine((int)x0,(int)y0,(int)x1,(int)y1);

        g.setColor(Color.BLUE);

        g.drawLine((int)x0,(int)y0,(int)(1000*Math.sin(a0)),(int)(1000*Math.cos(a0)));

        g.setColor(Color.RED);

        g.drawLine((int)x0,(int)y0,(int)(1000*Math.sin(a1+getGunHeadingRadians()-getGunTurnRemainingRadians())),(int)(1000*Math.cos(a1-getGunTurnRemainingRadians())));

        System.out.println("+++DEBUG+++");
        System.out.println(movementMode);
        System.out.println(roboMode);
        System.out.println(targetingMode);

    }


    private static int round = 0;
    private final static int decisionRound = 0;

    private Random r = new Random();

    private final double aimingAngleThreshold = 0.1;
    private final double movementThreshold = 0.05;
    private final double enemyChoiseThreshold = 100.0;
    private final double gunHeatAimingThreshold = 0.1;
    private double bulletPower = 1;

    private static int bulletHit[] = new int[TargetingMode.values().length];
    private static ArrayList<ArrayList<Integer>> bulletShoot = new ArrayList<ArrayList<Integer>>();
//    private static int bulletShoot[] = new int[TargetingMode.values().length];
    private final int targetingRecalculationTime = 50;
    private final int movementRecalculationTime = 250;

    private ArrayList<EnemyInfo> enemies = new ArrayList<EnemyInfo>();
    private EnemyInfo chosenEnemy = null;

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

    private enum TargetingMode {
        HEAD_ON,
        LINEAR,
        LINEAR_HEAD_FAKE,
        CIRCULAR;

        private static TargetingMode[] _values = TargetingMode.values();
        public static TargetingMode fromInteger(int x) {
            if(x < _values.length)
            {
                return _values[x];
            }
            return null;
        }
    }

    private TargetingMode targetingMode = TargetingMode.CIRCULAR;

    private enum MovementMode {
        PENDULUM,
        RAMMING,
        AVOID;

        private static MovementMode[] _values = MovementMode.values();
        public static MovementMode fromInteger(int x) {
            if(x < _values.length)
            {
                return _values[x];
            }
            return null;
        }
    }

    private MovementMode movementMode = MovementMode.PENDULUM;

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
//                    bulletPower = 3*chosenEnemy.lastDistance/getWidth()/2+getEnergy()/200+1;
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
//                setTurnGunRight(10);
                execute();
            }
        } catch (RuntimeException re) {
            System.out.println(re);
        }
    }

    private void chooseMovementMode()
    {
        movementMode = MovementMode.fromInteger(r.nextInt(MovementMode.values().length-1));
//        System.out.println(movementMode);
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

            targetingMode = TargetingMode.fromInteger(maxI);

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

        System.out.println("");
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

//        System.out.println("targetingMode="+targetingMode);

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
//                    bulletIds.get(targetingMode).add(setFireBullet(bulletPower).);
                    prepareFire();
                    roboMode = RoboMode.SCANNING;
                }
                break;
            case CHASE_WHEN_READY:
                if(canShoot()) {
                    if(chosenEnemy == null)
                        return;
                    chasingTime -= getTime() - lastChasingTime;
                    lastChasingTime = getTime();
                    double minimumTime = chosenEnemy.lastDistance/Rules.getBulletSpeed(1.0);
//                    System.out.println("Before Chase: "+chasingTime+"; Min: "+minimumTime);
                    if(chasingTime < minimumTime)
                    {
                        prepareFire();
                        chasingTime = chosenEnemy.lastDistance/Rules.getBulletSpeed(bulletPower);
//                        System.out.println("First: "+bulletPower);
                    }
                    else
                    {
                        double _bulletPower = (20 - chosenEnemy.lastDistance/chasingTime)/3;
                        prepareFire(_bulletPower);
//                        System.out.println("Following: "+_bulletPower);
                    }
//                    System.out.println("After Chase: "+chasingTime+"; Min: "+minimumTime);
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
            bulletShoot.get(targetingMode.ordinal()).add(setFireBullet(_bulletPower).hashCode());
        }
        catch(IndexOutOfBoundsException e)
        {
            System.out.println(e.getMessage());
        }
    }

    private void targetStateMachine(){
        if(chosenEnemy == null || getGunHeat() > gunHeatAimingThreshold)
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
            case CIRCULAR:
                double dT = 0.0;
                double predictedX = chosenEnemy.getRelativeX(), predictedY = chosenEnemy.getRelativeY();
                while((++dT*Rules.getBulletSpeed(bulletPower)+getGunHeat()/getGunCoolingRate()+Math.abs(turnAngle)/Rules.GUN_TURN_RATE_RADIANS) < Point2D.Double.distance(0,0,predictedX,predictedY)){
                    predictedX += chosenEnemy.lastVelocity*Math.sin(chosenEnemy.lastHeading + chosenEnemy.lastTurnRate*dT);
                    predictedY += chosenEnemy.lastVelocity*Math.cos(chosenEnemy.lastHeading + chosenEnemy.lastTurnRate*dT);
                    turnAngle = Math.atan2(predictedX,predictedY) - getGunHeadingRadians();
                }


                x0 = getX();
                x1 = predictedX+getX();
                y0 = getY();
                y1 = predictedY+getY();
                a0 = Math.atan2(predictedX,predictedY);
                a1 = turnAngle;

//                System.out.println("rX = "+predictedX+"; rY = "+predictedY+"; a = "+(int)Math.toDegrees(turnAngle)+"; a0 = "+(int)Math.toDegrees(Math.atan2(predictedY,predictedX)));
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

    private boolean inAvoidance = false;

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
            case AVOID:
                if(!inAvoidance)
                {
                    setAhead(r.nextDouble()*2*movementAbs-movementAbs);
                    double _angle = Math.PI*(r.nextDouble()-0.5);
                    if(_angle < 0)
                        turnLeftRadians(-_angle);
                    else
                        turnRightRadians(_angle);
                    inAvoidance = true;
                }
                else if(Math.abs(getDistanceRemaining()) <= movementThreshold) {
                    chooseMovementMode();
                    inAvoidance = false;
                }

        }
    }

    private boolean canShoot(){
//        System.out.println("Turn remaining = "+getGunTurnRemainingRadians());
//        System.out.println("Gun heat = "+getGunHeat());
        return (getGunHeat() == 0.0 && Math.abs(getGunTurnRemainingRadians()) <= aimingAngleThreshold);
    }

    private double angleToTurn(double angle){
        return (angle > Math.PI ? angle - Math.PI * 2 : angle);
    }

    //Initialization process
    //If you have data structures or preprocessing before the match
    private void initComponents() {
        if(round == 0) {
            Arrays.fill(bulletHit, 1);
            for (int i = 0; i < TargetingMode.values().length; i++)
            {
                bulletShoot.add(new ArrayList<Integer>());
                bulletShoot.get(i).add(0);
            }
//            Arrays.fill(bulletShoot, 1);
        }
        round++;
    }

    //Fancy colours for your bot
    private void initColors() {
        setColors(new Color(77, 0, 50), new Color(64, 32, 53), new Color(225, 227,136));
    }

    //When you scan an opponent do something
    public void onScannedRobot(ScannedRobotEvent e) {
        try {
            setTurnRadarLeftRadians(getRadarTurnRemainingRadians());

//            if(roboMode != RoboMode.SCANNING)
//                return;

            EnemyInfo scannedEnemy = getEnemyByName(e.getName());
            if(scannedEnemy == null)
            {
                scannedEnemy = new EnemyInfo(e.getName());
                enemies.add(scannedEnemy);
            }
            scannedEnemy.setNewData(getHeadingRadians()+e.getBearingRadians(),e.getVelocity(),e.getDistance(),e.getHeadingRadians(),getTime());

            double energyDrop = scannedEnemy.lastEnergy - e.getEnergy();
            if(energyDrop <= 3.0 && energyDrop >= 1.0) {
                System.out.println("Energy drop: " + e.getName() + " " + (scannedEnemy.lastEnergy - e.getEnergy()) + " at " + getTime());
                movementMode = MovementMode.AVOID;
//                System.out.println(movementMode);
            }

            scannedEnemy.lastEnergy = e.getEnergy();


            if(chosenEnemy == null || chosenEnemy.isDead || scannedEnemy.lastDistance < chosenEnemy.lastDistance - enemyChoiseThreshold)
                chosenEnemy = scannedEnemy;

            if(roboMode == RoboMode.SCANNING){
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
            getEnemyByName(e.getName()).isDead = true;
        } catch (RuntimeException re) {
            System.out.println(re);
        }
    }


    //You got hit by a bullet.. FIGHT OR FLIGHT
    public void onHitByBullet(HitByBulletEvent e) {
        try {
            EnemyInfo enemy = getEnemyByName(e.getName());
            if(enemy != null)
            {
                enemy.lastEnergy += Rules.getBulletDamage(e.getBullet().getPower());
            }
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

            EnemyInfo enemy = getEnemyByName(e.getName());
            if(enemy != null)
            {
                enemy.lastEnergy -= Rules.getBulletDamage(e.getBullet().getPower());
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
        movementMode = MovementMode.AVOID;
//        System.out.println(movementMode);
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
