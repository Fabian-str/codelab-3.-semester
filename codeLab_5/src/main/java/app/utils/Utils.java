package app.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Utils {

    public static String getPropertyValue(
            String propName,
            String resourceName
    ) {
        try (InputStream is = Utils.class.getClassLoader().getResourceAsStream(resourceName)) {

            if (is == null) {
                throw new IllegalStateException("Resource not found: " + resourceName);
            }

            Properties properties = new Properties();
            properties.load(is);

            String value = properties.getProperty(propName);

            if (value == null || value.isBlank()) {
                throw new IllegalStateException("Property " + propName + " missing or empty in " + resourceName);
            }

            return value.trim();

        } catch (IOException ex) {
            throw new IllegalStateException("Could not read resource: " + resourceName, ex);
        }
    }
}