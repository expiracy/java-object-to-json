import javax.management.ObjectName;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;
import java.util.Collection;

class Test {
    public int x = 5;
    public Integer y = 5;
    private String s = "a";
    private String[] l = {"a", "b"};
    private Test2 nested = new Test2();
}

class Test2 {
    public int a = 6;
}

public class JsonGenerator {
    public static void main(String[] args) {
        try {
            JsonGenerator.generateJson(new Test(), "./generated_jsons/");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void generateJsons(Object[] objects, String directory) throws IOException {
        for (Object object : objects) {
            JsonGenerator.generateJson(object, directory);
        }
    }

    public static String createJsonFile(String fileName) throws IOException {
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

        return (number > 1)
                ? fileName + (number - 1) + ".json"
                : fileName + ".json";
    }

    public static void generateJson(Object object, String directory) throws IOException {
        Class objectClass = object.getClass();

        String fullFileName = JsonGenerator.createJsonFile(directory + objectClass.getName());

        BufferedWriter writer = new BufferedWriter(new FileWriter(fullFileName));

        writer.write("{ ");

        String fieldAndValue = null;

        for (Field field : objectClass.getDeclaredFields()) {
            if (fieldAndValue != null) writer.write(", ");

            JsonGenerator.makeAccessible(field);

            fieldAndValue = JsonGenerator.getFieldAndValue(field, object);

            writer.write(fieldAndValue);
        }

        writer.write(" }");

        writer.close();
    }

    /**
     * Makes a private or protected field accessible
     *
     * @param field the field to make accessible
     */
    public static void makeAccessible(Field field) {
        if (!Modifier.isPublic(field.getModifiers())) {
            field.setAccessible(true);
        }
    }

    public static String getFieldAndValue(Field field, Object object) {
        try {
            Object value = field.get(object);

            String fieldName = StringUtils.wrapInQuotes(field.getName()) + ": ";

            if (field.getType().isArray()) {
                return fieldName + StringUtils.stringArray((Object[]) value);
            } else {
                return fieldName + StringUtils.wrapInQuotes(value.toString());
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace(); // Illegal state
            return null;
        }
    }
}

class StringUtils {
    public static String stringArray(Object[] array) {
        String stringArray = "[";

        for (int i = 0; i < array.length; i++) {
            if (i > 0) stringArray += ", ";
            stringArray += StringUtils.wrapInQuotes(array[i]);
        }

        return stringArray + "]";
    }

    public static String wrapInQuotes(Object object) {
        return "\"" + object + "\"";
    }
}
