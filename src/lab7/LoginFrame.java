package lab7;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JsonDatabaseManager db;

    public LoginFrame(JsonDatabaseManager db) {
        this.db = db;
        initializeUI();
    }

    public LoginFrame() {
        this.db = new JsonDatabaseManager("data/courses.json", "data/users.json");
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Skill Forge - Login");
        setSize(400, 350);
        setLayout(null);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Title
        JLabel titleLabel = new JLabel("Skill Forge");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBounds(0, 30, 400, 30);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel);

        JLabel subtitleLabel = new JLabel("Online Learning Platform");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setBounds(0, 60, 400, 20);
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel.setForeground(Color.GRAY);
        add(subtitleLabel);

        // Username
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(50, 110, 100, 25);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setBounds(50, 135, 300, 35);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        add(usernameField);

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(50, 180, 100, 25);
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(50, 205, 300, 35);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        add(passwordField);

        // Login Button
        JButton loginBtn = new JButton("Login");
        loginBtn.setBounds(50, 260, 140, 40);
        loginBtn.setBackground(new Color(70, 130, 180));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Arial", Font.BOLD, 14));
        loginBtn.setFocusPainted(false);
        add(loginBtn);

        // Signup Button
        JButton signupBtn = new JButton("Create Account");
        signupBtn.setBounds(210, 260, 140, 40);
        signupBtn.setBackground(new Color(60, 179, 113));
        signupBtn.setForeground(Color.WHITE);
        signupBtn.setFont(new Font("Arial", Font.BOLD, 14));
        signupBtn.setFocusPainted(false);
        add(signupBtn);

        loginBtn.addActionListener(this::handleLogin);
        signupBtn.addActionListener(this::handleSignup);

        getRootPane().setDefaultButton(loginBtn);
        setVisible(true);
    }

    private void handleLogin(ActionEvent event) {
        login();
    }

    private void handleSignup(ActionEvent event) {
        openSignup();
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password!", 
                "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (username.matches("^\\d+$")) {
            JOptionPane.showMessageDialog(this, "Username cannot be only numbers!", 
                "Invalid Username", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AuthManager auth = new AuthManager();
        String result = auth.login(username, password);

        if (!result.equals("SUCCESS")) {
            JOptionPane.showMessageDialog(this, result, "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User loggedUser = auth.getLoggedUser();
        
        if (loggedUser == null) {
            JOptionPane.showMessageDialog(this, 
                "Error: Could not retrieve user data after login.", 
                "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (loggedUser.getRole().equals("student")) {
                if (loggedUser instanceof Student) {
                    Student student = (Student) loggedUser;
                    new StudentDashboardFrame(student, db).setVisible(true);
                } else {
                    Student student = createStudentFromUser(loggedUser);
                    new StudentDashboardFrame(student, db).setVisible(true);
                }
            } else if (loggedUser.getRole().equals("instructor")) {
                if (loggedUser instanceof Instructor) {
                    Instructor instructor = (Instructor) loggedUser;
                    new InstructorDashboardFrame(instructor, db).setVisible(true);
                } else {
                    Instructor instructor = createInstructorFromUser(loggedUser);
                    new InstructorDashboardFrame(instructor, db).setVisible(true);
                }
            } else if (loggedUser.getRole().equals("admin")) {
                if (loggedUser instanceof Admin) {
                    Admin admin = (Admin) loggedUser;
                    new AdminDashboardFrame(admin, db).setVisible(true);
                } else {
                    Admin admin = createAdminFromUser(loggedUser);
                    new AdminDashboardFrame(admin, db).setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Error: Unknown user role.", 
                    "Login Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            this.dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error during login: " + e.getMessage(), 
                "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Student createStudentFromUser(User user) {
        Student student = new Student();
        student.setUserId(user.getUserId());
        student.setUsername(user.getUsername());
        student.setEmail(user.getEmail());
        student.setPasswordHash(user.getPasswordHash());
        student.setRole(user.getRole());
        student.setEnrolledCourses(new java.util.ArrayList<>());
        student.setProgress(new java.util.HashMap<>());
        return student;
    }

    private Instructor createInstructorFromUser(User user) {
        Instructor instructor = new Instructor();
        instructor.setUserId(user.getUserId());
        instructor.setUsername(user.getUsername());
        instructor.setEmail(user.getEmail());
        instructor.setPasswordHash(user.getPasswordHash());
        instructor.setRole(user.getRole());
        instructor.setCreatedCourses(new java.util.ArrayList<>());
        return instructor;
    }

    private Admin createAdminFromUser(User user) {
        Admin admin = new Admin();
        admin.setUserId(user.getUserId());
        admin.setUsername(user.getUsername());
        admin.setEmail(user.getEmail());
        admin.setPasswordHash(user.getPasswordHash());
        admin.setRole(user.getRole());
        admin.setManagedCourses(new java.util.ArrayList<>());
        return admin;
    }

    private void openSignup() {
        new SignupFrame(db).setVisible(true);
        this.dispose();
    }
}