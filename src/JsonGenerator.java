import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.*;

class A {
    public int a = 1;
    public Integer b = 100;
    public BigDecimal c = BigDecimal.valueOf(3.14159265);

    public int[] intArray = {1,2,3,4};
    public List<Float> floatList = new ArrayList<>();
    public Map<Integer, String> hashMap = new HashMap<>();

    public B nested = new B();

    public A() {
        this.floatList.add(1.0f);
        this.floatList.add(2.0f);

        this.hashMap.put(1, "one");
    }
}

class B {
    public int a = 6;
    public C nested = new C();
}

class C {
    public String a = "This is C";
}

public class JsonGenerator {
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

    /**
     * Create a json file for the state of each object
     *
     * @param objects the objects to store the states of
     * @param directory the directory store the json files in
     * @throws IOException occurs when there was an error creating the file due to an invalid directory
     */
    public static void generateJsonFiles(Object[] objects, String directory) throws IOException {
        for (Object object : objects) {
            JsonGenerator.generateJsonFile(object, directory);
        }
    }

    /**
     * Create a json file for the state of a specified object
     *
     * @param object the object to store the states of
     * @param directory the directory store the json files in
     * @throws IOException occurs when there was an error creating the file due to an invalid directory
     */
    public static void generateJsonFile(Object object, String directory) throws IOException {
        Class objectClass = object.getClass();

        // File name including the suffix (if applicable) and the extension
        String fullFileName = JsonGenerator.createJsonFile(directory + objectClass.getName());

        BufferedWriter writer = new BufferedWriter(new FileWriter(fullFileName));

        String jsonString = JsonGenerator.generateJsonString(object);

        writer.write(jsonString); // Write the json string to the file

        writer.close();
    }

    private static String createJsonFile(String fileName) throws IOException {
        File jsonFile = new File(fileName + ".json");

        // The initial file suffix
        int number = 1;

        // Find the next available file name
        while (true) {
            if (!jsonFile.exists()) {
                jsonFile.createNewFile();
                break;
            }
            jsonFile = new File(fileName + number++ + ".json");
        }

        // Do not include the file suffix if the file name was already unique
        return (number > 1)
                ? fileName + (number - 1) + ".json"
                : fileName + ".json";
    }

    /**
     * Generates a string that represents the object's attributes in a json format
     *
     * @param object the object to generate the string for
     * @return the json string for the object
     */
    private static String generateJsonString(Object object) {
        String jsonString = "{ ";

        Class objectClass = object.getClass();

        String fieldAndValue = null;

        for (Field field : objectClass.getDeclaredFields()) {
            if (fieldAndValue != null) jsonString += ", ";

            JsonGenerator.makeAccessible(field);

            fieldAndValue = JsonGenerator.getFieldAndValue(field, object);

            jsonString += fieldAndValue;
        }

        jsonString += " }";

        return jsonString;
    }

    /**
     * Makes a private or protected field accessible
     *
     * @param field the field to make accessible
     */
    private static void makeAccessible(Field field) {
        if (!Modifier.isPublic(field.getModifiers())) {
            field.setAccessible(true);
        }
    }

    /**
     * Extracts the attribute name and value from a field
     *
     * @param field the field
     * @param object the object which the field belongs to
     * @return a string representing the key value pair
     */
    private static String getFieldAndValue(Field field, Object object) {
        try {
            Object attribute = field.get(object);

            String fieldString = Formatter.wrapInQuotes(field.getName()) + ": ";
            String valueString = "";

            // Depending on the attribute's type, choose the appropriate serializer
            if (attribute instanceof String || attribute instanceof Number) {
                valueString = Formatter.wrapInQuotes(attribute.toString());

            } else if (field.getType().isArray()) {
                valueString = Formatter.stringArray(ArrayHelper.toObjectArray(attribute));

            } else if (attribute instanceof Collection<?>) {
                valueString = Formatter.stringArray(((Collection<?>) attribute).toArray());

            } else if (attribute instanceof Map<?,?>) {
                valueString = Formatter.stringArray(((Map<?, ?>) attribute).entrySet().toArray());

            } else {
                valueString = JsonGenerator.generateJsonString(attribute);
            }

            return fieldString + valueString;

        } catch (IllegalAccessException e) {
            e.printStackTrace(); // Illegal state
            return "";
        }
    }


}

/**
 * Contains some useful formatting methods
 */
class Formatter {
    public static String stringArray(Object[] array) {
        String stringArray = "[";

        for (int i = 0; i < array.length; i++) {
            if (i > 0) stringArray += ", ";
            stringArray += Formatter.wrapInQuotes(array[i]);
        }

        return stringArray + "]";
    }

    public static String wrapInQuotes(Object object) {
        return "\"" + object + "\"";
    }
}


class ArrayHelper {
    /**
     * Converts a primitive array to an Object array
     *
     * @param array the primitive array
     * @return the Object[] array
     */
    public static Object[] toObjectArray(Object array) {
        int length = Array.getLength(array);

        Object[] ret = new Object[length];

        for (int i = 0; i < length; i++) {
            ret[i] = Array.get(array, i);
        }

        return ret;
    }
}
