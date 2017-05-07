import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created on 06/05/17.
 */
public class Flat2Hierarchical {

    private static Map<String, Object> valueMap = new HashMap<>();

    public static void main(final String[] args){

        String filePath = "application.properties";
        try {
            Files.lines(Paths.get(new Flat2Hierarchical()
                    .readFile(filePath)))
                    .forEach((line) -> {
                        Character firstChar = null;
                        if (line.length() > 0) firstChar = line.charAt(0);
                        if (firstChar != null && firstChar != '#' && firstChar != '\n') appendToMap(line);
                    });
            System.out.print(new Gson().toJson(valueMap));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String readFile(final String filePath){
        return getClass().getClassLoader().getResource(filePath).getFile();
    }

    private static void appendToMap(final String line){

        int indexOfEqual = line.indexOf('=');
        if (indexOfEqual == -1) throw new IllegalStateException("There is line that do not consist \"=\"");
        final String fullKey = line.substring(0, indexOfEqual);

        String value = null;
        if (indexOfEqual + 1 <= line.length() - 1){
            value = line.substring(indexOfEqual + 1, line.length());
        }

        final String[] keys = fullKey.split("\\.");
        appendToMap(keys, 0, valueMap, value, -1);
    }

    private static void appendToMap(final String[] keys, final int deepness, final Object currentNode, final String value, final int prevIndex){
        String key = keys[deepness];
        int arrayIndexPos = openBracketPosition(key);
        int index = -1;
        if (arrayIndexPos != -1){
            index = Integer.parseInt(key.substring(arrayIndexPos + 1, key.length() - 1));
            key = key.substring(0, arrayIndexPos);
        }

        if (deepness == keys.length - 1){
            if (arrayIndexPos > -1){
                if (currentNode instanceof  Map){
                    Object[] objects = (Object[]) ((Map<String, Object>) currentNode).getOrDefault(key, new Object[100]);
                    objects[index] = value;
                } else {
                    Map<String, Object> newMap = (Map<String, Object>) ((Object[])currentNode)[prevIndex];
                    if (newMap == null) newMap = new HashMap<>();
                    final Object[] list = (Object[]) newMap.getOrDefault(key, new Object[100]);
                    list[index] = value;
                    newMap.put(key, list);
                }
            } else {
                if (currentNode instanceof Map)
                    ((Map<String, Object>) currentNode).put(key, value);
                else {
                    Map<String, Object> newMap = (Map<String, Object>) ((Object[])currentNode)[prevIndex];
                    if (newMap == null) newMap = new HashMap<>();
                    newMap.put(key, value);
                    ((Object[])currentNode)[prevIndex] = newMap;
                }
            }
        } else {
            if (arrayIndexPos > -1){
                if (currentNode instanceof Map) {
                    ((Map<String, Object>) currentNode).putIfAbsent(key, new Object[100]);
                    appendToMap(keys, deepness + 1, ((Map<String, Object>) currentNode).get(key), value, index);
                }
                else {
                    final Map<String, Object> newMap = new HashMap<>();
                    newMap.putIfAbsent(key, new Object[100]);
                    ((Object[]) currentNode)[index] = newMap;
                    appendToMap(keys, deepness + 1, newMap, value, -1);
                }
            } else {
                ((Map<String, Object>) currentNode).putIfAbsent(key, new HashMap<String, Object>());
                appendToMap(keys, deepness + 1, ((Map<String, Object>) currentNode).get(key), value, index);
            }
        }

    }

    private static int openBracketPosition(final String key){

        if (key.charAt(key.length() - 1) != ']') return -1;

        char[] chars = key.toCharArray();
        int openBracketPosition = -1;
        for (int i = 0; i < chars.length; i ++){
            if (chars[i] == '['){
                openBracketPosition = i;
                break;
            }
        }

        if (openBracketPosition == -1 || openBracketPosition > key.length() - 2)
            return -1;

        return openBracketPosition;
    }

    private static Object[] cleanNull(final Object[] items){
        final List<Object> objects = new ArrayList<>(Arrays.asList(items));
        objects.removeAll(Collections.singleton(null));
        return objects.toArray();
    }
}
