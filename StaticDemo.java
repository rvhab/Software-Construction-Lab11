/** Exercise 6.55: demonstrates which calls between static and instance methods are legal. */
public class StaticDemo
{
    private int instanceField = 5;

    public static void staticA() { System.out.println("static A"); }
    public static void staticB() { staticA(); }              // static -> static: OK
    public void instanceA() { System.out.println("instance A"); }
    public void instanceB() { staticA(); }                    // instance -> static: OK
    public void instanceC() { instanceA(); }                  // instance -> instance: OK

    public static void staticC()
    {
        // instanceA();            // ERROR: non-static method cannot be referenced from a static context
        // System.out.println(instanceField);   // ERROR too
        new StaticDemo().instanceA();                         // OK: only through an object
    }
}
