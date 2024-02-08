public class Scope {
    int c = 4;
    
    public static void main (String [] args) {
        int a = 1;
        System.out.println("a: " +a);
        {
            int b = 2;
            System.out.println("b: " + b);

        }

        int b =3;
        System.out.println("b: " + b);

        Scope scope = new Scope();
        System.out.println("c: " + scope.c);

    }
}