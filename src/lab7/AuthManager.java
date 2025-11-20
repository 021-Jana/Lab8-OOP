package lab7;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuthManager {
    private final String USERS_FILE = "data/users.json";
    private List<User> users = new ArrayList<>();
    private User loggedUser = null;
    private Gson gson;

    public AuthManager() {
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(User.class, new JsonDatabaseManager.UserTypeAdapter())
            .create();
        loadUsers();
        ensureDefaultAdminExists();
    }

    public User getLoggedUser() {
        return loggedUser;
    }

    private void loadUsers() {
        try {
            File file = new File(USERS_FILE);
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            
            if (!file.exists()) {
                System.out.println("Users file doesn't exist, creating new one...");
                saveUsers();
                return;
            }
            
            FileReader reader = new FileReader(file);
            users = gson.fromJson(reader, new TypeToken<List<User>>(){}.getType());
            if (users == null) {
                users = new ArrayList<>();
            }
            reader.close();
        } catch (Exception e) {
            System.out.println("Error loading users: " + e.getMessage());
            users = new ArrayList<>();
        }
    }

    private void saveUsers() {
        try {
            File file = new File(USERS_FILE);
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            
            FileWriter writer = new FileWriter(file);
            gson.toJson(users, writer);
            writer.close();
        } catch (Exception e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    private void ensureDefaultAdminExists() {
        boolean adminExists = users.stream()
                .anyMatch(u -> u != null && "admin".equals(u.getRole()));
        
        if (!adminExists) {
            String adminId = UUID.randomUUID().toString();
            String hashedPassword = hashPassword("admin123");
            Admin defaultAdmin = new Admin(adminId, "admin", "admin@skillforge.com", hashedPassword, new ArrayList<>());
            users.add(defaultAdmin);
            saveUsers();
            System.out.println("Default admin account created: admin / admin123");
        }
    }


    private String validateUserInput(String username, String email, String password, String role) {
        if (username == null || username.trim().isEmpty() || 
            email == null || email.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            return "All fields are required!";
        }

        if (username.matches("^\\d+$")) {
            return "Username cannot be only numbers!";
        }

        if (username.length() < 3) {
            return "Username must be at least 3 characters long!";
        }

        if (!isValidEmail(email)) {
            return "Please enter a valid email address!";
        }

        if (password.length() < 6) {
            return "Password must be at least 6 characters long!";
        }

        if ("admin".equals(role)) {
            return "Admin accounts cannot be created through signup!";
        }

        if (!"student".equals(role) && !"instructor".equals(role)) {
            return "Invalid role selected!";
        }

        return null; 
    }

    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".") && email.length() > 5;
    }

    public String signup(String username, String email, String password, String role) {
        String validationError = validateUserInput(username, email, password, role);
        if (validationError != null) {
            return validationError;
        }

        
        for (User u : users) {
            if (u != null && u.getUsername() != null && u.getUsername().equalsIgnoreCase(username)) {
                return "Username already exists!";
            }
            if (u != null && u.getEmail() != null && u.getEmail().equalsIgnoreCase(email)) {
                return "Email already registered!";
            }
        }

        String hashed = hashPassword(password);
        String id = UUID.randomUUID().toString();

        User newUser;
        if (role.equals("student")) {
            newUser = new Student(id, username, email, hashed, new ArrayList<>(), new java.util.HashMap<>());
        } else {
            newUser = new Instructor(id, username, email, hashed, new ArrayList<>());
        }

        users.add(newUser);
        saveUsers();
        return "SUCCESS";
    }

    public String login(String username, String password) {
          System.out.println("Login attempt for: " + username);
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return "Please enter both username and password!";
        }

        String hashed = hashPassword(password);
        for (User u : users) {
        System.out.println("Checking user: " + u.getUsername() + ", role: " + u.getRole());
        if (u != null && u.getUsername() != null && u.getUsername().equalsIgnoreCase(username)) {
            System.out.println("User found, checking password...");
            if (u.getPasswordHash() != null && u.getPasswordHash().equals(hashed)) {
                loggedUser = u;
                System.out.println("Login SUCCESS for: " + username + ", role: " + u.getRole());
                return "SUCCESS";
            }
        }
    }
    return "Invalid username or password!"; 
    }

    public void logout() {
        loggedUser = null;
    }

    private String hashPassword(String pass) {
        if (pass == null) return null;
        
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pass.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            System.out.println("Error hashing password: " + e.getMessage());
            return null;
        }
    }
}