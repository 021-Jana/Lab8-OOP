package lab7;

import java.util.ArrayList;
import java.util.List;

public class Admin extends User {
    private List<String> managedCourses;

    public Admin() {
        super();
        this.role = "admin";
        this.managedCourses = new ArrayList<>();
    }

    public Admin(String userId, String username, String email, String passwordHash, List<String> managedCourses) {
        super(userId, username, email, passwordHash, "admin");
        this.managedCourses = (managedCourses != null) ? managedCourses : new ArrayList<>();
    }

    public List<String> getManagedCourses() {
        return managedCourses;
    }

    public void setManagedCourses(List<String> managedCourses) {
        this.managedCourses = managedCourses;
    }

    public void addManagedCourse(String courseId) {
        if (!managedCourses.contains(courseId)) {
            managedCourses.add(courseId);
        }
    }

    public void removeManagedCourse(String courseId) {
        managedCourses.remove(courseId);
    }
}