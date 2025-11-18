package database;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class LoadEnv{
    private static Properties properties = new Properties();

    static {
        try {
            FileInputStream filepath = new FileInputStream("/home/muturiiii/Desktop/DevOps-Cloud/Cloud/Stay-in-cloud/bucketStorage/src/main/java/database/db.properties");
            if (filepath == null) {
                System.err.println("Error: app.properties file not found in package.");
                throw new RuntimeException("app.properties file not found");
            }
            properties.load(filepath);
        } catch (IOException e) {
            System.err.println("Error loading app.properties: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error loading app.properties", e);
        }
    }
    public static String getIP() {
        return properties.getProperty("sql-ip");
    }
    public static String getPort() {
        return properties.getProperty("sql-port");
    }
    public static String getUsername() {
        return properties.getProperty("sql-user");
    }
    public static String getPassword() {
        return properties.getProperty("sql-password");
    }
    public static String getDatabase(){
        return properties.getProperty("database");
    }
}