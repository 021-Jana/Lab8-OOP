package lab7;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Course {
    private String courseId;
    private String title;
    private String description;
    private String instructorId;
    private ArrayList<Lesson> lessons;
    private ArrayList<String> students;
    private Map<String, ArrayList<String>> progress = new HashMap<>();
    private String approvalStatus; // PENDING, APPROVED, REJECTED
    private String rejectionReason;

    public Course() {
        lessons = new ArrayList<>();
        students = new ArrayList<>();
        approvalStatus = "PENDING";
        rejectionReason = "";
    }

    public Course(String courseId, String title, String desc, String instructorId) {
        this();
        this.courseId = courseId;
        this.title = title;
        this.description = desc;
        this.instructorId = instructorId;
    }

    // getters & setters
    public String getCourseId() { return courseId; }
    public void setCourseId(String id) { this.courseId = id; }
    public String getTitle() { return title; }
    public void setTitle(String t) { this.title = t; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public String getInstructorId() { return instructorId; }
    public void setInstructorId(String id) { this.instructorId = id; }
    public ArrayList<Lesson> getLessons() { return lessons; }
    public ArrayList<String> getStudents() { return students; }
    public void setLessons(ArrayList<Lesson> lessons) { this.lessons = lessons; }
    public void setStudents(ArrayList<String> students) { this.students = students; }
    public Map<String, ArrayList<String>> getProgress() { return progress; }
    public void setProgress(Map<String, ArrayList<String>> progress) { this.progress = progress; }
    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String status) { this.approvalStatus = status; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String reason) { this.rejectionReason = reason; }

    // Validation methods
    public boolean isValidTitle() {
        return title != null && !title.trim().isEmpty() && !title.matches("^\\d+$");
    }

    public boolean isValidDescription() {
        return description != null && !description.trim().isEmpty();
    }

    public boolean hasValidLessons() {
        if (lessons == null) return false;
        for (Lesson lesson : lessons) {
            if (lesson.getTitle() == null || lesson.getTitle().trim().isEmpty() || 
                lesson.getTitle().matches("^\\d+$")) {
                return false;
            }
        }
        return true;
    }

    public void addLesson(Lesson l) { 
        if (l != null && l.getTitle() != null && !l.getTitle().trim().isEmpty() && !l.getTitle().matches("^\\d+$")) {
            lessons.add(l);
        }
    }
    
    public boolean removeLessonById(String lessonId) {
        return lessons.removeIf(l -> l.getLessonId().equals(lessonId));
    }
    
    public Lesson getLessonById(String lessonId) {
        return lessons.stream().filter(l -> l.getLessonId().equals(lessonId)).findFirst().orElse(null);
    }
    
    public void enrollStudent(String studentId) {
        if (studentId != null && !studentId.trim().isEmpty() && !students.contains(studentId)) {
            students.add(studentId);
        }
    }
    
    public void unenrollStudent(String studentId) {
        students.remove(studentId);
    }
    
    public void markLessonCompleted(String studentId, String lessonId) {
        if (studentId != null && lessonId != null) {
            progress.putIfAbsent(studentId, new ArrayList<>());
            if (!progress.get(studentId).contains(lessonId)) {
                progress.get(studentId).add(lessonId);
            }
        }
    }

    public boolean isApproved() {
        return "APPROVED".equals(approvalStatus);
    }

    public boolean isPending() {
        return "PENDING".equals(approvalStatus);
    }

    public boolean isRejected() {
        return "REJECTED".equals(approvalStatus);
    }

    public boolean isValidCourse() {
        return isValidTitle() && isValidDescription() && hasValidLessons();
    }

    @Override
    public String toString() {
        String status = "";
        switch (approvalStatus) {
            case "APPROVED": status = "✓"; break;
            case "REJECTED": status = "✗"; break;
            case "PENDING": status = "⏳"; break;
        }
        return title + " " + status + " (" + lessons.size() + " lessons, " + students.size() + " students)";
    }
}