/** Exercise 6.56: a class that counts how many instances of it have been created. */
public class InstanceCounter
{
    private static int instances = 0;   // class variable: shared by all instances

    public InstanceCounter()
    {
        instances++;
    }

    /** @return the number of instances created so far. */
    public static int numberOfInstances()
    {
        return instances;
    }
}
