import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test {
    public static void main(String[] args) {
        try {
            String directory = "./generated_jsons/"; // Directory to save the jsons to

            // Generating one file at a time
            JsonGenerator.generateJsonFile(new A(), directory);
            JsonGenerator.generateJsonFile(new C(), directory);

            // Generating multiple files at once
            Object[] objects = {new A(), new B(), new C()};
            JsonGenerator.generateJsonFiles(objects, directory);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

class A {
    public int a = 1;
    protected Integer b = 100;
    public BigDecimal c = BigDecimal.valueOf(3.14159265);

    private final int[] intArray = {1, 2, 3, 4};
    private final List<Float> floatList = new ArrayList<>();
    private final Map<Integer, String> hashMap = new HashMap<>();

    public B nested = new B();

    public A() {
        this.floatList.add(1.0f);
        this.floatList.add(2.0f);

        this.hashMap.put(1, "one");
    }
}

class B {
    public int a = 6;
    protected C nested = new C();
}

class C {
    public String a = "This is C";
}
