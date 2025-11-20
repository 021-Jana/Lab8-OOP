package lab7;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.UUID;

public class InstructorDashboardFrame extends JFrame {

    private Instructor instructor;
    private JsonDatabaseManager db;

    private DefaultListModel<Course> courseListModel = new DefaultListModel<>();
    private JList<Course> courseList;

    private DefaultListModel<Lesson> lessonListModel = new DefaultListModel<>();
    private JList<Lesson> lessonList;

    private DefaultListModel<String> studentListModel = new DefaultListModel<>();
    private JList<String> studentList;

    private JTextArea lessonContentArea;
    private JComboBox<Course> lessonCourseComboBox;
    private JComboBox<Course> studentCourseComboBox;

    public InstructorDashboardFrame(Instructor instructor, JsonDatabaseManager db) {
        this.instructor = instructor;
        this.db = db;

        setTitle("Instructor Dashboard – " + instructor.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        loadInstructorCourses();

        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(40, 40, 45));
        panel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("Instructor Dashboard - Welcome, " + instructor.getUsername());
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 20));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(new Color(220, 80, 60));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame(db).setVisible(true);
            }
        });

        panel.add(title, BorderLayout.WEST);
        panel.add(logoutBtn, BorderLayout.EAST);

        return panel;
    }

    private JTabbedPane createMainPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));

        tabbedPane.addTab("Course Management", createCoursesPanel());
        tabbedPane.addTab("Lesson Management", createLessonsPanel());
        tabbedPane.addTab("Student Management", createStudentsPanel());

        return tabbedPane;
    }

    private JPanel createCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lbl = new JLabel("Your Courses");
        lbl.setFont(new Font("Arial", Font.BOLD, 18));
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        courseList = new JList<>(courseListModel);
        courseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseList.setFont(new Font("Arial", Font.PLAIN, 14));
        courseList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadCourseLessons();
                loadCourseStudents();
            }
        });

        JScrollPane scroll = new JScrollPane(courseList);
        scroll.setBorder(BorderFactory.createTitledBorder("Courses"));

        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton addBtn = new JButton("Create Course");
        JButton editBtn = new JButton("Edit Course");
        JButton deleteBtn = new JButton("Delete Course");

        addBtn.setBackground(new Color(70, 130, 180));
        editBtn.setBackground(new Color(60, 179, 113));
        deleteBtn.setBackground(new Color(220, 80, 60));
        
        addBtn.setForeground(Color.WHITE);
        editBtn.setForeground(Color.WHITE);
        deleteBtn.setForeground(Color.WHITE);

        addBtn.addActionListener(e -> createCourseDialog());
        editBtn.addActionListener(e -> editCourseDialog());
        deleteBtn.addActionListener(e -> deleteCourse());

        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLessonsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lbl = new JLabel("Course Lessons");
        lbl.setFont(new Font("Arial", Font.BOLD, 18));
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        
        JPanel courseSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        courseSelectionPanel.add(new JLabel("Select Course:"));
        
        lessonCourseComboBox = new JComboBox<>();
        lessonCourseComboBox.setPreferredSize(new Dimension(300, 25));
        lessonCourseComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Course selectedCourse = (Course) lessonCourseComboBox.getSelectedItem();
                if (selectedCourse != null) {
                    loadLessonsForCourse(selectedCourse);
                }
            }
        });
        
        courseSelectionPanel.add(lessonCourseComboBox);

        
        lessonList = new JList<>(lessonListModel);
        lessonList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lessonList.setFont(new Font("Arial", Font.PLAIN, 14));
        lessonList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadLessonContent();
            }
        });

        JScrollPane scroll = new JScrollPane(lessonList);
        scroll.setBorder(BorderFactory.createTitledBorder("Lessons"));

        
        lessonContentArea = new JTextArea();
        lessonContentArea.setEditable(false);
        lessonContentArea.setFont(new Font("Arial", Font.PLAIN, 14));
        lessonContentArea.setBorder(BorderFactory.createTitledBorder("Lesson Content"));
        lessonContentArea.setLineWrap(true);
        lessonContentArea.setWrapStyleWord(true);

        JScrollPane contentScroll = new JScrollPane(lessonContentArea);
        contentScroll.setPreferredSize(new Dimension(300, 200));

        // Buttons panel
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton addBtn = new JButton("Add Lesson");
        JButton editBtn = new JButton("Edit Lesson");
        JButton deleteBtn = new JButton("Delete Lesson");

        addBtn.setBackground(new Color(70, 130, 180));
        editBtn.setBackground(new Color(60, 179, 113));
        deleteBtn.setBackground(new Color(220, 80, 60));
        
        addBtn.setForeground(Color.WHITE);
        editBtn.setForeground(Color.WHITE);
        deleteBtn.setForeground(Color.WHITE);

        addBtn.addActionListener(e -> {
            Course selectedCourse = (Course) lessonCourseComboBox.getSelectedItem();
            if (selectedCourse != null) {
                createLessonDialog(selectedCourse);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a course first.", "No Course Selected", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        editBtn.addActionListener(e -> {
            Course selectedCourse = (Course) lessonCourseComboBox.getSelectedItem();
            Lesson selectedLesson = lessonList.getSelectedValue();
            if (selectedCourse != null && selectedLesson != null) {
                editLessonDialog(selectedCourse, selectedLesson);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a course and a lesson first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        deleteBtn.addActionListener(e -> {
            Course selectedCourse = (Course) lessonCourseComboBox.getSelectedItem();
            Lesson selectedLesson = lessonList.getSelectedValue();
            if (selectedCourse != null && selectedLesson != null) {
                deleteLesson(selectedCourse, selectedLesson);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a course and a lesson first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);

        // Right panel with content and buttons
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(contentScroll, BorderLayout.CENTER);
        rightPanel.add(btnPanel, BorderLayout.SOUTH);

        // Main layout
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(courseSelectionPanel, BorderLayout.CENTER);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scroll, BorderLayout.CENTER);
        centerPanel.add(rightPanel, BorderLayout.EAST);
        
        panel.add(centerPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lbl = new JLabel("Enrolled Students");
        lbl.setFont(new Font("Arial", Font.BOLD, 18));
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Course selection for students
        JPanel courseSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        courseSelectionPanel.add(new JLabel("Select Course:"));
        
        studentCourseComboBox = new JComboBox<>();
        studentCourseComboBox.setPreferredSize(new Dimension(300, 25));
        studentCourseComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Course selectedCourse = (Course) studentCourseComboBox.getSelectedItem();
                if (selectedCourse != null) {
                    loadStudentsForCourse(selectedCourse);
                }
            }
        });
        
        courseSelectionPanel.add(studentCourseComboBox);

        // Students list
        studentList = new JList<>(studentListModel);
        studentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentList.setFont(new Font("Arial", Font.PLAIN, 14));

        JScrollPane scroll = new JScrollPane(studentList);
        scroll.setBorder(BorderFactory.createTitledBorder("Students in Selected Course"));

        JLabel infoLabel = new JLabel("Select a course to view enrolled students");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(courseSelectionPanel, BorderLayout.CENTER);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(infoLabel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadInstructorCourses() {
        courseListModel.clear();
        System.out.println("Loading courses for instructor: " + instructor.getUsername());
        
        // Refresh instructor data from database
        User refreshedInstructor = db.getUserById(instructor.getUserId());
        if (refreshedInstructor instanceof Instructor) {
            this.instructor = (Instructor) refreshedInstructor;
        }
        
        System.out.println("Instructor created courses: " + instructor.getCreatedCourses());
        
        for (String courseId : instructor.getCreatedCourses()) {
            Course c = db.getCourse(courseId);
            if (c != null && c.isValidCourse()) {
                courseListModel.addElement(c);
                // Also add to combo boxes
                if (lessonCourseComboBox != null) lessonCourseComboBox.addItem(c);
                if (studentCourseComboBox != null) studentCourseComboBox.addItem(c);
                System.out.println("Loaded course: " + c.getTitle());
            } else {
                System.out.println("Course not found or invalid: " + courseId);
            }
        }
        System.out.println("Total courses loaded: " + courseListModel.size());
    }

    private void loadCourseLessons() {
        lessonListModel.clear();
        if (lessonContentArea != null) {
            lessonContentArea.setText("");
        }
        
        Course c = courseList.getSelectedValue();
        if (c == null) return;

        for (Lesson l : c.getLessons()) {
            lessonListModel.addElement(l);
        }
    }

    private void loadCourseStudents() {
        studentListModel.clear();
        
        Course c = courseList.getSelectedValue();
        if (c == null) return;

        for (String studentId : c.getStudents()) {
            User student = db.getUserById(studentId);
            if (student != null) {
                studentListModel.addElement(student.getUsername() + " (" + student.getEmail() + ")");
            }
        }
    }

    private void loadLessonsForCourse(Course course) {
        lessonListModel.clear();
        if (lessonContentArea != null) {
            lessonContentArea.setText("");
        }
        
        if (course == null) return;

        for (Lesson l : course.getLessons()) {
            lessonListModel.addElement(l);
        }
        
        System.out.println("Loaded " + lessonListModel.size() + " lessons for course: " + course.getTitle());
    }

    private void loadStudentsForCourse(Course course) {
        studentListModel.clear();
        
        if (course == null) return;

        for (String studentId : course.getStudents()) {
            User student = db.getUserById(studentId);
            if (student != null) {
                studentListModel.addElement(student.getUsername() + " (" + student.getEmail() + ")");
            }
        }
        
        System.out.println("Loaded " + studentListModel.size() + " students for course: " + course.getTitle());
    }

    private void loadLessonContent() {
        if (lessonContentArea == null) return;
        
        Lesson l = lessonList.getSelectedValue();
        if (l == null) {
            lessonContentArea.setText("");
            return;
        }
        lessonContentArea.setText("Title: " + l.getTitle() + "\n\nContent:\n" + l.getContent());
    }

    // Enhanced validation for course creation
    private boolean isValidCourseTitle(String title) {
        return title != null && !title.trim().isEmpty() && !title.matches("^\\d+$");
    }

    private boolean isValidLessonTitle(String title) {
        return title != null && !title.trim().isEmpty() && !title.matches("^\\d+$");
    }

    private void createCourseDialog() {
        JTextField titleField = new JTextField(20);
        JTextArea descArea = new JTextArea(4, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(new JLabel("Course Title:"), BorderLayout.NORTH);
        panel.add(titleField, BorderLayout.CENTER);
        panel.add(new JLabel("Description:"), BorderLayout.SOUTH);
        panel.add(new JScrollPane(descArea), BorderLayout.AFTER_LAST_LINE);

        int option = JOptionPane.showConfirmDialog(this, panel, 
                "Create New Course", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            String description = descArea.getText().trim();

            if (!isValidCourseTitle(title)) {
                JOptionPane.showMessageDialog(this, 
                    "Course title cannot be empty or only numbers!", 
                    "Invalid Title", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (description.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Course description cannot be empty!", 
                    "Invalid Description", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String id = "C" + UUID.randomUUID().toString().substring(0, 8);
            Course c = new Course(id, title, description, instructor.getUserId());

            instructor.addCreatedCourse(id);
            db.putUser(instructor);
            db.putCourse(c);
            db.saveAll();

            courseListModel.addElement(c);
            if (lessonCourseComboBox != null) lessonCourseComboBox.addItem(c);
            if (studentCourseComboBox != null) studentCourseComboBox.addItem(c);
            
            JOptionPane.showMessageDialog(this, 
                "Course created successfully! It is now pending admin approval.", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void editCourseDialog() {
        Course c = courseList.getSelectedValue();
        if (c == null) {
            JOptionPane.showMessageDialog(this, "Please select a course first.", "No Course Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField titleField = new JTextField(c.getTitle(), 20);
        JTextArea descArea = new JTextArea(c.getDescription(), 4, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(new JLabel("Course Title:"), BorderLayout.NORTH);
        panel.add(titleField, BorderLayout.CENTER);
        panel.add(new JLabel("Description:"), BorderLayout.SOUTH);
        panel.add(new JScrollPane(descArea), BorderLayout.AFTER_LAST_LINE);

        int option = JOptionPane.showConfirmDialog(this, panel, 
                "Edit Course", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            String description = descArea.getText().trim();

            if (!isValidCourseTitle(title)) {
                JOptionPane.showMessageDialog(this, 
                    "Course title cannot be empty or only numbers!", 
                    "Invalid Title", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (description.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Course description cannot be empty!", 
                    "Invalid Description", JOptionPane.ERROR_MESSAGE);
                return;
            }

            c.setTitle(title);
            c.setDescription(description);
            db.putCourse(c);
            db.saveAll();

            courseList.repaint();
            JOptionPane.showMessageDialog(this, "Course updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteCourse() {
        Course c = courseList.getSelectedValue();
        if (c == null) {
            JOptionPane.showMessageDialog(this, "Please select a course first.", "No Course Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the course: " + c.getTitle() + "?\nThis action cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            instructor.removeCreatedCourse(c.getCourseId());
            db.removeCourse(c.getCourseId());
            db.putUser(instructor);
            db.saveAll();

            courseListModel.removeElement(c);
            if (lessonCourseComboBox != null) lessonCourseComboBox.removeItem(c);
            if (studentCourseComboBox != null) studentCourseComboBox.removeItem(c);
            lessonListModel.clear();
            studentListModel.clear();
            if (lessonContentArea != null) {
                lessonContentArea.setText("");
            }
            
            JOptionPane.showMessageDialog(this, "Course deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void createLessonDialog(Course course) {
        JTextField titleField = new JTextField(20);
        JTextArea contentArea = new JTextArea(6, 20);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(new JLabel("Lesson Title:"), BorderLayout.NORTH);
        panel.add(titleField, BorderLayout.CENTER);
        panel.add(new JLabel("Content:"), BorderLayout.SOUTH);
        panel.add(new JScrollPane(contentArea), BorderLayout.AFTER_LAST_LINE);

        int option = JOptionPane.showConfirmDialog(this, panel, 
                "Create New Lesson for: " + course.getTitle(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();

            if (!isValidLessonTitle(title)) {
                JOptionPane.showMessageDialog(this, 
                    "Lesson title cannot be empty or only numbers!", 
                    "Invalid Title", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (content.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Lesson content cannot be empty!", 
                    "Invalid Content", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Lesson l = new Lesson("L" + UUID.randomUUID().toString().substring(0, 8),
                    title, content);

            course.addLesson(l);
            db.putCourse(course);
            db.saveAll();

            lessonListModel.addElement(l);
            JOptionPane.showMessageDialog(this, "Lesson created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void editLessonDialog(Course course, Lesson lesson) {
        JTextField titleField = new JTextField(lesson.getTitle(), 20);
        JTextArea contentArea = new JTextArea(lesson.getContent(), 6, 20);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(new JLabel("Lesson Title:"), BorderLayout.NORTH);
        panel.add(titleField, BorderLayout.CENTER);
        panel.add(new JLabel("Content:"), BorderLayout.SOUTH);
        panel.add(new JScrollPane(contentArea), BorderLayout.AFTER_LAST_LINE);

        int option = JOptionPane.showConfirmDialog(this, panel, 
                "Edit Lesson", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();

            if (!isValidLessonTitle(title)) {
                JOptionPane.showMessageDialog(this, 
                    "Lesson title cannot be empty or only numbers!", 
                    "Invalid Title", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (content.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Lesson content cannot be empty!", 
                    "Invalid Content", JOptionPane.ERROR_MESSAGE);
                return;
            }

            lesson.setTitle(title);
            lesson.setContent(content);
            db.putCourse(course);
            db.saveAll();

            lessonList.repaint();
            loadLessonContent();
            JOptionPane.showMessageDialog(this, "Lesson updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteLesson(Course course, Lesson lesson) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the lesson: " + lesson.getTitle() + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            course.removeLessonById(lesson.getLessonId());
            db.putCourse(course);
            db.saveAll();

            lessonListModel.removeElement(lesson);
            if (lessonContentArea != null) {
                lessonContentArea.setText("");
            }
            JOptionPane.showMessageDialog(this, "Lesson deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}