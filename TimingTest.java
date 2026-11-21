/** Exercise 6.51: measures how long it takes to count from 1 to 100 in a loop. */
public class TimingTest
{
    /** @return the time in nanoseconds needed to count from 1 to 100. */
    public static long timeCounting()
    {
        long start = System.nanoTime();
        int count = 0;
        for(int i = 1; i <= 100; i++) {
            count = i;
        }
        long end = System.nanoTime();
        return end - start;
    }

    public static void main(String[] args)
    {
        System.out.println("Counting 1..100 took " + timeCounting() + " ns");
        long start = System.currentTimeMillis();
        for(int i = 1; i <= 100; i++) { }
        System.out.println("currentTimeMillis difference: " + (System.currentTimeMillis() - start) + " ms");
    }
}
