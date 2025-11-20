package lab7;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class JsonDatabaseManager {
    private final File coursesFile;
    private final File usersFile;
    private final Gson gson;
    private HashMap<String, Course> courses;
    private ArrayList<User> users;

    public JsonDatabaseManager(String coursesFilePath, String usersFilePath) {
        this.coursesFile = new File(coursesFilePath);
        this.usersFile = new File(usersFilePath);
        
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(User.class, new UserTypeAdapter())
            .create();
            
        this.courses = new HashMap<>();
        this.users = new ArrayList<>();
        ensureFilesExist();
        loadAllData();
    }

    public JsonDatabaseManager(String coursesFilePath) {
        this(coursesFilePath, "data/users.json");
    }

    public static class UserTypeAdapter implements JsonSerializer<User>, JsonDeserializer<User> {
        private static final String TYPE_FIELD = "type";
        
        @Override
        public JsonElement serialize(User user, Type typeOfSrc, JsonSerializationContext context) {
            JsonElement jsonElement = context.serialize(user);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            
            if (user instanceof Student) {
                jsonObject.add(TYPE_FIELD, new JsonPrimitive("student"));
            } else if (user instanceof Instructor) {
                jsonObject.add(TYPE_FIELD, new JsonPrimitive("instructor"));
            } else if (user instanceof Admin) {
                jsonObject.add(TYPE_FIELD, new JsonPrimitive("admin"));
            } else {
                jsonObject.add(TYPE_FIELD, new JsonPrimitive("user"));
            }
            
            return jsonObject;
        }

        @Override
        public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            
            String type = "user";
            if (jsonObject.has(TYPE_FIELD)) {
                type = jsonObject.get(TYPE_FIELD).getAsString();
            } else {
                if (jsonObject.has("role")) {
                    String role = jsonObject.get("role").getAsString();
                    if ("student".equals(role)) {
                        type = "student";
                    } else if ("instructor".equals(role)) {
                        type = "instructor";
                    } else if ("admin".equals(role)) {
                        type = "admin";
                    }
                }
            }
            
            try {
                switch (type) {
                    case "student":
                        return context.deserialize(json, Student.class);
                    case "instructor":
                        return context.deserialize(json, Instructor.class);
                    case "admin":
                        return context.deserialize(json, Admin.class);
                    case "user":
                    default:
                        return context.deserialize(json, User.class);
                }
            } catch (Exception e) {
                System.err.println("Failed to deserialize user of type: " + type + ". Error: " + e.getMessage());
                try {
                    return context.deserialize(json, User.class);
                } catch (Exception ex) {
                    throw new JsonParseException("Failed to deserialize user", ex);
                }
            }
        }
    }

    private void ensureFilesExist() {
        try {
            if (coursesFile.getParentFile() != null && !coursesFile.getParentFile().exists()) {
                coursesFile.getParentFile().mkdirs();
            }
            if (usersFile.getParentFile() != null && !usersFile.getParentFile().exists()) {
                usersFile.getParentFile().mkdirs();
            }
            
            if (!coursesFile.exists()) {
                try (Writer w = new FileWriter(coursesFile)) {
                    w.write("{}");
                }
                System.out.println("Created new courses file: " + coursesFile.getAbsolutePath());
            }
            if (!usersFile.exists()) {
                try (Writer w = new FileWriter(usersFile)) {
                    w.write("[]");
                }
                System.out.println("Created new users file: " + usersFile.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error creating files: " + e.getMessage());
        }
    }

    private void loadAllData() {
        loadCourses();
        loadUsers();
    }

    public void loadCourses() {
        try (Reader r = new FileReader(coursesFile)) {
            Type type = new TypeToken<HashMap<String, Course>>(){}.getType();
            HashMap<String, Course> loadedCourses = gson.fromJson(r, type);
            if (loadedCourses != null) {
                courses = loadedCourses;
                System.out.println("Loaded " + courses.size() + " courses");
            } else {
                courses = new HashMap<>();
                System.out.println("No courses found, initialized empty courses map");
            }
        } catch (IOException e) {
            System.err.println("Error loading courses: " + e.getMessage());
            courses = new HashMap<>();
        }
    }

    public synchronized void saveCourses() {
        try (Writer w = new FileWriter(coursesFile)) {
            gson.toJson(courses, w);
            System.out.println("Saved " + courses.size() + " courses");
        } catch (IOException e) {
            System.err.println("Error saving courses: " + e.getMessage());
        }
    }

    public Course getCourse(String courseId) {
        return courses.get(courseId);
    }

    public void putCourse(Course course) {
        if (course != null && course.isValidCourse()) {
            courses.put(course.getCourseId(), course);
            saveCourses();
        } else {
            System.err.println("Invalid course data - not saving");
        }
    }

    public void removeCourse(String courseId) {
        if (courseId != null && !courseId.trim().isEmpty()) {
            courses.remove(courseId);
            saveCourses();
        }
    }

    public HashMap<String, Course> getCourses() {
        return courses;
    }

    public HashMap<String, Course> getApprovedCourses() {
        HashMap<String, Course> approvedCourses = new HashMap<>();
        for (Course course : courses.values()) {
            if (course.isApproved() && course.isValidCourse()) {
                approvedCourses.put(course.getCourseId(), course);
            }
        }
        return approvedCourses;
    }

    public HashMap<String, Course> getPendingCourses() {
        HashMap<String, Course> pendingCourses = new HashMap<>();
        for (Course course : courses.values()) {
            if (course.isPending() && course.isValidCourse()) {
                pendingCourses.put(course.getCourseId(), course);
            }
        }
        return pendingCourses;
    }

    public boolean approveCourse(String courseId) {
        if (courseId == null || courseId.trim().isEmpty()) return false;
        
        Course course = getCourse(courseId);
        if (course != null && course.isValidCourse()) {
            course.setApprovalStatus("APPROVED");
            course.setRejectionReason("");
            putCourse(course);
            System.out.println("Course approved: " + courseId);
            return true;
        }
        return false;
    }

    public boolean rejectCourse(String courseId, String reason) {
        if (courseId == null || courseId.trim().isEmpty()) return false;
        
        Course course = getCourse(courseId);
        if (course != null) {
            course.setApprovalStatus("REJECTED");
            course.setRejectionReason(reason != null ? reason : "");
            putCourse(course);
            System.out.println("Course rejected: " + courseId + " - Reason: " + reason);
            return true;
        }
        return false;
    }

    public void loadUsers() {
        try (Reader r = new FileReader(usersFile)) {
            Type listType = new TypeToken<ArrayList<User>>(){}.getType();
            ArrayList<User> loadedUsers = gson.fromJson(r, listType);
            if (loadedUsers != null) {
                users = loadedUsers;
                System.out.println("Loaded " + users.size() + " users");
            } else {
                users = new ArrayList<>();
                System.out.println("No users found, initialized empty users list");
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
            users = new ArrayList<>();
        } catch (JsonParseException e) {
            System.err.println("JSON parse error loading users: " + e.getMessage());
            users = new ArrayList<>();
            saveUsers();
        }
    }

    public synchronized void saveUsers() {
        try (Writer w = new FileWriter(usersFile)) {
            gson.toJson(users, w);
            System.out.println("Saved " + users.size() + " users");
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    public ArrayList<User> getUsers() {
        loadUsers();
        return users;
    }

    public List<User> getUsersByRole(String role) {
        loadUsers();
        return users.stream()
                .filter(u -> role != null && role.equals(u.getRole()))
                .collect(Collectors.toList());
    }

    public List<Instructor> getInstructors() {
        loadUsers();
        return users.stream()
                .filter(u -> u instanceof Instructor)
                .map(u -> (Instructor) u)
                .collect(Collectors.toList());
    }

    public User getUserById(String userId) {
        if (userId == null || userId.trim().isEmpty()) return null;
        
        loadUsers();
        return users.stream()
                .filter(u -> u.getUserId() != null && u.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    public User getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) return null;
        
        loadUsers();
        return users.stream()
                .filter(u -> u.getUsername() != null && u.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

    public void putUser(User user) {
        if (user == null || user.getUserId() == null || user.getUserId().trim().isEmpty()) {
            System.err.println("Invalid user data - not saving");
            return;
        }
        
        loadUsers();
        boolean found = false;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId().equals(user.getUserId())) {
                users.set(i, user);
                found = true;
                break;
            }
        }
        if (!found) {
            users.add(user);
        }
        saveUsers();
    }

    public void saveAll() {
        saveCourses();
        saveUsers();
    }

    public boolean enrollStudentInCourse(String courseId, String studentId) {
        if (courseId == null || courseId.trim().isEmpty() || studentId == null || studentId.trim().isEmpty()) {
            return false;
        }

        Course course = getCourse(courseId);
        User user = getUserById(studentId);
        
        if (course == null) {
            System.err.println("Course not found: " + courseId);
            return false;
        }
        if (user == null) {
            System.err.println("User not found: " + studentId);
            return false;
        }
        if (!(user instanceof Student)) {
            System.err.println("User is not a student: " + studentId);
            return false;
        }

        if (!course.isApproved()) {
            System.err.println("Course is not approved for enrollment: " + courseId);
            return false;
        }

        Student student = (Student) user;
        
        if (!course.getStudents().contains(studentId)) {
            course.enrollStudent(studentId);
            System.out.println("Added student to course enrollment list");
        }

        if (!student.getEnrolledCourses().contains(courseId)) {
            student.enrollCourse(courseId);
            System.out.println("Added course to student's enrolled courses");
        }

        putCourse(course);
        putUser(student);
        
        System.out.println("Enrollment completed successfully");
        return true;
    }

    public ArrayList<Lesson> getLessonsByCourse(String courseId) {
        if (courseId == null || courseId.trim().isEmpty()) return new ArrayList<>();
        
        Course course = getCourse(courseId);
        return course != null ? course.getLessons() : new ArrayList<>();
    }

    public void editLessonInCourse(String courseId, Lesson updatedLesson) {
        if (courseId == null || courseId.trim().isEmpty() || updatedLesson == null) return;
        
        Course course = getCourse(courseId);
        if (course != null) {
            for (int i = 0; i < course.getLessons().size(); i++) {
                if (course.getLessons().get(i).getLessonId().equals(updatedLesson.getLessonId())) {
                    course.getLessons().set(i, updatedLesson);
                    putCourse(course);
                    break;
                }
            }
        }
    }

    public void deleteLessonFromCourse(String courseId, String lessonId) {
        if (courseId == null || courseId.trim().isEmpty() || lessonId == null || lessonId.trim().isEmpty()) return;
        
        Course course = getCourse(courseId);
        if (course != null) {
            course.getLessons().removeIf(lesson -> lesson.getLessonId().equals(lessonId));
            putCourse(course);
        }
    }

    public void markLessonCompletedInCourse(String courseId, String studentId, String lessonId) {
        if (courseId == null || courseId.trim().isEmpty() || 
            studentId == null || studentId.trim().isEmpty() || 
            lessonId == null || lessonId.trim().isEmpty()) {
            return;
        }
        
        Course course = getCourse(courseId);
        User user = getUserById(studentId);
        
        if (course != null) {
            course.markLessonCompleted(studentId, lessonId);
            putCourse(course);
        }
        
        if (user instanceof Student) {
            Student student = (Student) user;
            student.markLessonCompleted(courseId, lessonId);
            putUser(student);
        }
    }

    public boolean isLessonCompleted(String courseId, String studentId, String lessonId) {
        if (courseId == null || courseId.trim().isEmpty() || 
            studentId == null || studentId.trim().isEmpty() || 
            lessonId == null || lessonId.trim().isEmpty()) {
            return false;
        }
        
        Course course = getCourse(courseId);
        if (course != null) {
            ArrayList<String> completedLessons = course.getProgress().get(studentId);
            return completedLessons != null && completedLessons.contains(lessonId);
        }
        return false;
    }
}