package database;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.springframework.boot.CommandLineRunner;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static java.lang.System.exit;

@RestController
public class DBConnect implements CommandLineRunner {
    static Connection conn;
    String ipAddress= LoadEnv.getIP();
    String port=LoadEnv.getPort();
    String db_user=LoadEnv.getUsername();
    String db_password=LoadEnv.getPassword();
    String database=LoadEnv.getDatabase();
    String db_url = "jdbc:mysql://" + ipAddress + ":" + port;

    public DBConnect() {
        String connectDB = connectToDatabase();
        System.out.println("Connection Status: " + connectDB);
    }

    public String connectToDatabase() {
        try {
            conn = DriverManager.getConnection(db_url, db_user, db_password);
            if (conn != null && conn.isValid(5)) {
                return "Connected successfully";
            } else {
                exit(1);
                return "Connection failed";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Unexpected error: " + e.getMessage();
        }
    }
    @GetMapping("/api/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> loginData){
        try {
            conn = DriverManager.getConnection(db_url, db_user, db_password);
            String username = loginData.get("username");
            String password = loginData.get("password");
            
            String retrievequery="SELECT * FROM "+database+".users WHERE Username=? AND Password=?";
            PreparedStatement stmt= conn.prepareStatement(retrievequery);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet resultSet= stmt.executeQuery();
            
            if (resultSet.next()){
                String accountName = resultSet.getString("AccountName");
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Login successful",
                    "accountName", accountName
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "Invalid username or password"));
            }

        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", "Database error: " + e.getMessage()));
        }
    }

    @PostMapping("/api/signup")
    public ResponseEntity<Map<String, String>> createUser(@RequestBody Map<String, String> userData){
        try {
            conn=DriverManager.getConnection(db_url, db_user, db_password);
            
            String accountName = userData.get("AccountName");
            String username = userData.get("Username");
            String password = userData.get("Password");
            
            // First check if username already exists
            String checkQuery = "SELECT * FROM "+database+".users WHERE Username=?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, username);
            ResultSet checkResult = checkStmt.executeQuery();
            
            if (checkResult.next()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("status", "error", "message", "Username already exists"));
            }
            
            // Create new user
            String newUser="INSERT INTO "+database+".users(AccountName, Username, Password) VALUES(?,?,?)";
            PreparedStatement createStatement=conn.prepareStatement(newUser);
            createStatement.setString(1, accountName);
            createStatement.setString(2, username);
            createStatement.setString(3, password);
            createStatement.executeUpdate();

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "User created successfully"
            ));

        }catch(Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", "Database error: " + e.getMessage()));
        }
    }

    @Override
    public void run(String... args) throws Exception {
        new DBConnect();
    }
}

