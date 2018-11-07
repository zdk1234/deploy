package csc.rm.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


public final class PropertiesUtil {

    private static Properties properties = new Properties();

    static {
        try (FileInputStream fis = new FileInputStream("conf/config.properties")) {
            properties.load(new InputStreamReader(fis, StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, String> getHashMap() {
        Set<Map.Entry<Object, Object>> entries = properties.entrySet();
        final Map<String, String> tMap = new HashMap<>();
        entries.forEach(entry -> tMap.put(entry.getKey().toString(), entry.getValue().toString()));
        return tMap;
    }

    public static String getValue(String key) {
        Object o = properties.get(key);
        return o == null ? null : o.toString();
    }
}