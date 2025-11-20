package lab7;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class SignupFrame extends JFrame {
    private JTextField usernameField, emailField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;
    private JsonDatabaseManager db;
    private AuthManager auth;

    public SignupFrame(JsonDatabaseManager db) {
        this.db = db;
        this.auth = new AuthManager();
        initializeUI();
    }

    public SignupFrame() {
        this.db = new JsonDatabaseManager("data/courses.json", "data/users.json");
        this.auth = new AuthManager();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Signup - Skill Forge");
        setSize(400, 400);
        setLayout(null);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        // Title
        JLabel titleLabel = new JLabel("Create New Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBounds(100, 20, 200, 30);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel);

        // Username
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(50, 70, 100, 25);
        add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setBounds(150, 70, 200, 30);
        add(usernameField);

        // Email
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(50, 120, 100, 25);
        add(emailLabel);

        emailField = new JTextField();
        emailField.setBounds(150, 120, 200, 30);
        add(emailField);

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(50, 170, 100, 25);
        add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(150, 170, 200, 30);
        add(passwordField);

        // Role
        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setBounds(50, 220, 100, 25);
        add(roleLabel);

        roleBox = new JComboBox<>(new String[]{"student", "instructor"});
        roleBox.setBounds(150, 220, 200, 30);
        add(roleBox);

        // Signup Button
        JButton signupBtn = new JButton("Create Account");
        signupBtn.setBounds(100, 280, 200, 40);
        signupBtn.setBackground(new Color(70, 130, 180));
        signupBtn.setForeground(Color.WHITE);
        signupBtn.setFont(new Font("Arial", Font.BOLD, 14));
        signupBtn.setFocusPainted(false);
        add(signupBtn);

        // Back to Login Button
        JButton backBtn = new JButton("Back to Login");
        backBtn.setBounds(100, 330, 200, 30);
        backBtn.setBackground(new Color(200, 200, 200));
        backBtn.setFocusPainted(false);
        add(backBtn);

        signupBtn.addActionListener(this::handleSignup);
        backBtn.addActionListener(this::handleBackToLogin);

        getRootPane().setDefaultButton(signupBtn);
        setVisible(true);
    }

    private void handleSignup(ActionEvent event) {
        signup();
    }

    private void handleBackToLogin(ActionEvent event) {
        goBackToLogin();
    }

    private void signup() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = roleBox.getSelectedItem().toString();

        String validationError = validateInputs(username, email, password);
        if (validationError != null) {
            showErrorDialog(validationError);
            return;
        }

        String result = auth.signup(username, email, password, role);

        if (result.equals("SUCCESS")) {
            showSuccessDialog();
            goBackToLogin();
        } else {
            showErrorDialog(result);
        }
    }

    private String validateInputs(String username, String email, String password) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            return "Please fill in all fields!";
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

        return null;
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") && email.length() > 5;
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Signup Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccessDialog() {
        JOptionPane.showMessageDialog(this,
            "Account created successfully!\nYou can now login with your credentials.",
            "Success",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void goBackToLogin() {
        new LoginFrame(db).setVisible(true);
        this.dispose();
    }
}