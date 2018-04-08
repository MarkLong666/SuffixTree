import java.util.HashSet;

public class Main {

    public static void main(String[] args) {
        HashSet<Integer> set = new HashSet<>();
        set.add(1);
        set.add(1);
        for (Integer i : set) {
            System.out.println(i);
        }
    }
}
