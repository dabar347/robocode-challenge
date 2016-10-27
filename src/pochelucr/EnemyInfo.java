package pochelucr;

/**
 * Created by dabar347 on 27/10/2016.
 */
public class EnemyInfo {

    private String name;

    public double lastBearing = 0.0;

    public EnemyInfo(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
