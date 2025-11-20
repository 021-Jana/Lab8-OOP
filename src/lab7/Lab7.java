package lab7;

import javax.swing.SwingUtilities;

public class Lab7 {
    public static void main(String[] args) {
        JsonDatabaseManager db = new JsonDatabaseManager("data/courses.json", "data/users.json");
        
        SwingUtilities.invokeLater(() -> {
            new LoginFrame(db).setVisible(true);
        });
    }
}