package lab7;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Student extends User {
    private ArrayList<String> enrolledCourses;
    private Map<String, ArrayList<String>> progress;

    public Student() {
        super();
        this.role = "student";
        this.enrolledCourses = new ArrayList<>();
        this.progress = new HashMap<>();
    }

    public Student(String userId, String username, String email, String passwordHash, 
                   ArrayList<String> enrolledCourses, Map<String, ArrayList<String>> progress) {
        super(userId, username, email, passwordHash, "student");
        this.enrolledCourses = (enrolledCourses != null) ? enrolledCourses : new ArrayList<>();
        this.progress = (progress != null) ? progress : new HashMap<>();
    }

    public ArrayList<String> getEnrolledCourses() {
        return enrolledCourses;
    }

    public void setEnrolledCourses(ArrayList<String> enrolledCourses) {
        this.enrolledCourses = enrolledCourses;
    }

    public Map<String, ArrayList<String>> getProgress() {
        return progress;
    }

    public void setProgress(Map<String, ArrayList<String>> progress) {
        this.progress = progress;
    }

    public void enrollCourse(String courseId) {
        if (!enrolledCourses.contains(courseId)) {
            enrolledCourses.add(courseId);
            progress.putIfAbsent(courseId, new ArrayList<>());
        }
    }

    public void unenrollCourse(String courseId) {
        enrolledCourses.remove(courseId);
        progress.remove(courseId);
    }

    public void markLessonCompleted(String courseId, String lessonId) {
        progress.putIfAbsent(courseId, new ArrayList<>());
        ArrayList<String> completed = progress.get(courseId);
        if (!completed.contains(lessonId)) {
            completed.add(lessonId);
        }
    }

    public boolean isLessonCompleted(String courseId, String lessonId) {
        ArrayList<String> completed = progress.get(courseId);
        return completed != null && completed.contains(lessonId);
    }
}