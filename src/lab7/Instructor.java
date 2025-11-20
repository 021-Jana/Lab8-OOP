package lab7;

import java.util.ArrayList;
import java.util.List;

public class Instructor extends User {
    private List<String> createdCourses;

    public Instructor() {
        super();
        this.role = "instructor";
        this.createdCourses = new ArrayList<>();
    }

    public Instructor(String userId, String username, String email, String passwordHash, List<String> createdCourses) {
        super(userId, username, email, passwordHash, "instructor");
        this.createdCourses = (createdCourses != null) ? createdCourses : new ArrayList<>();
    }

    public List<String> getCreatedCourses() {
        return createdCourses;
    }

    public void setCreatedCourses(List<String> createdCourses) {
        this.createdCourses = createdCourses;
    }

    public void addCreatedCourse(String courseId) {
        if (!createdCourses.contains(courseId)) {
            createdCourses.add(courseId);
        }
    }

    public void removeCreatedCourse(String courseId) {
        createdCourses.remove(courseId);
    }
}