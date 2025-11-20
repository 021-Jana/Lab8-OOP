package lab7;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class StudentDashboardFrame extends JFrame {

    private JsonDatabaseManager db;
    private Student student;

    private DefaultListModel<Course> allCoursesModel = new DefaultListModel<>();
    private JList<Course> allCoursesList;

    private DefaultListModel<Course> enrolledCoursesModel = new DefaultListModel<>();
    private JList<Course> enrolledCoursesList;

    private DefaultListModel<Lesson> lessonListModel = new DefaultListModel<>();
    private JList<Lesson> lessonList;

    private JTextArea lessonContentArea;
    private JTextField searchField;

    public StudentDashboardFrame(Student student, JsonDatabaseManager db) {
        this.student = student;
        this.db = db;

        setTitle("Student Dashboard – " + student.getUsername());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        loadAllCourses();
        loadEnrolledCourses();

        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(40, 40, 45));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("Student Dashboard - Welcome, " + student.getUsername());
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

        tabbedPane.addTab("Browse Courses", createAllCoursesPanel());
        tabbedPane.addTab("My Courses", createEnrolledCoursesPanel());

        return tabbedPane;
    }

    private JPanel createAllCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JButton searchBtn = new JButton("Search");
        JButton refreshBtn = new JButton("Refresh");
        JButton enrollBtn = new JButton("Enroll in Selected Course");

        searchBtn.setBackground(new Color(70, 130, 180));
        refreshBtn.setBackground(new Color(100, 100, 100));
        enrollBtn.setBackground(new Color(60, 179, 113));
        
        searchBtn.setForeground(Color.WHITE);
        refreshBtn.setForeground(Color.WHITE);
        enrollBtn.setForeground(Color.WHITE);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(new JLabel("Search:"));
        buttonPanel.add(searchField);
        buttonPanel.add(searchBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(enrollBtn);

        searchPanel.add(buttonPanel, BorderLayout.NORTH);

        // Courses list
        allCoursesList = new JList<>(allCoursesModel);
        allCoursesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        allCoursesList.setFont(new Font("Arial", Font.PLAIN, 14));
        allCoursesList.setCellRenderer(new CourseListRenderer());

        JScrollPane scroll = new JScrollPane(allCoursesList);
        scroll.setBorder(BorderFactory.createTitledBorder("Available Courses"));

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        // Actions
        searchBtn.addActionListener(e -> filterCourses());
        refreshBtn.addActionListener(e -> loadAllCourses());
        enrollBtn.addActionListener(e -> enrollSelectedCourse());
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { filterCourses(); }
        });

        return panel;
    }

    private JPanel createEnrolledCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

       
        enrolledCoursesList = new JList<>(enrolledCoursesModel);
        enrolledCoursesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        enrolledCoursesList.setFont(new Font("Arial", Font.PLAIN, 14));
        enrolledCoursesList.setCellRenderer(new CourseListRenderer());
        enrolledCoursesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadLessons();
            }
        });

        JScrollPane coursesScroll = new JScrollPane(enrolledCoursesList);
        coursesScroll.setBorder(BorderFactory.createTitledBorder("My Enrolled Courses"));
        coursesScroll.setPreferredSize(new Dimension(300, 0));

       
        JPanel lessonsPanel = new JPanel(new BorderLayout(10, 10));

        lessonListModel = new DefaultListModel<>();
        lessonList = new JList<>(lessonListModel);
        lessonList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lessonList.setFont(new Font("Arial", Font.PLAIN, 14));
        lessonList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadLessonContent();
            }
        });

        JScrollPane lessonsScroll = new JScrollPane(lessonList);
        lessonsScroll.setBorder(BorderFactory.createTitledBorder("Lessons"));

        // Initialize lessonContentArea properly
        lessonContentArea = new JTextArea();
        lessonContentArea.setEditable(false);
        lessonContentArea.setFont(new Font("Arial", Font.PLAIN, 14));
        lessonContentArea.setBorder(BorderFactory.createTitledBorder("Lesson Content"));
        lessonContentArea.setLineWrap(true);
        lessonContentArea.setWrapStyleWord(true);

        JScrollPane contentScroll = new JScrollPane(lessonContentArea);

        JButton markCompleteBtn = new JButton("Mark as Completed");
        markCompleteBtn.setBackground(new Color(60, 179, 113));
        markCompleteBtn.setForeground(Color.WHITE);
        markCompleteBtn.addActionListener(e -> markLessonCompleted());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(markCompleteBtn);

        lessonsPanel.add(lessonsScroll, BorderLayout.CENTER);
        lessonsPanel.add(contentScroll, BorderLayout.SOUTH);
        lessonsPanel.add(buttonPanel, BorderLayout.NORTH);

        panel.add(coursesScroll, BorderLayout.WEST);
        panel.add(lessonsPanel, BorderLayout.CENTER);

        return panel;
    }

    private class CourseListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                                                     boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Course) {
                Course course = (Course) value;
                setText("<html><b>" + course.getTitle() + "</b><br>" + 
                       course.getDescription() + "<br>" +
                       "<small>Students: " + course.getStudents().size() + 
                       " | Lessons: " + course.getLessons().size() + "</small></html>");
            }
            return this;
        }
    }

    private void loadAllCourses() {
        allCoursesModel.clear();
        for (Course c : db.getApprovedCourses().values()) {
            if (c.isValidCourse()) {
                allCoursesModel.addElement(c);
            }
        }
    }

    private void filterCourses() {
        String query = searchField.getText().toLowerCase();
        allCoursesModel.clear();
        for (Course c : db.getApprovedCourses().values()) {
            if (c.isValidCourse() && 
                (c.getTitle().toLowerCase().contains(query) || 
                 c.getDescription().toLowerCase().contains(query))) {
                allCoursesModel.addElement(c);
            }
        }
    }

    private void enrollSelectedCourse() {
        Course selectedCourse = allCoursesList.getSelectedValue();
        if (selectedCourse == null) {
            JOptionPane.showMessageDialog(this, "Please select a course first!", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String courseId = selectedCourse.getCourseId();
        
        if (student.getEnrolledCourses().contains(courseId)) {
            JOptionPane.showMessageDialog(this, "You are already enrolled in this course!", 
                "Already Enrolled", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Enroll in course: " + selectedCourse.getTitle() + "?",
                "Confirm Enrollment", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = db.enrollStudentInCourse(courseId, student.getUserId());
            
            if (success) {
                User updatedUser = db.getUserById(student.getUserId());
                if (updatedUser instanceof Student) {
                    this.student = (Student) updatedUser;
                }
                
                JOptionPane.showMessageDialog(this, 
                    "Successfully enrolled in: " + selectedCourse.getTitle(), 
                    "Enrollment Successful", JOptionPane.INFORMATION_MESSAGE);
                
                loadEnrolledCourses();
                
                JTabbedPane tabbedPane = (JTabbedPane) getContentPane().getComponent(1);
                tabbedPane.setSelectedIndex(1);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to enroll in course. Please try again.", 
                    "Enrollment Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadEnrolledCourses() {
        enrolledCoursesModel.clear();
        
        // Refresh student data
        User freshStudent = db.getUserById(student.getUserId());
        if (freshStudent instanceof Student) {
            this.student = (Student) freshStudent;
        }
        
        for (String courseId : student.getEnrolledCourses()) {
            Course course = db.getCourse(courseId);
            if (course != null && course.isValidCourse()) {
                enrolledCoursesModel.addElement(course);
            }
        }
        
        lessonListModel.clear();
        if (lessonContentArea != null) {
            lessonContentArea.setText("");
        }
    }

    private void loadLessons() {
        lessonListModel.clear();
        if (lessonContentArea != null) {
            lessonContentArea.setText("");
        }
        
        Course c = enrolledCoursesList.getSelectedValue();
        if (c == null) return;

        for (Lesson l : c.getLessons()) {
            lessonListModel.addElement(l);
        }
    }

    private void loadLessonContent() {
        if (lessonContentArea == null) return;
        
        Lesson l = lessonList.getSelectedValue();
        if (l == null) { 
            lessonContentArea.setText(""); 
            return; 
        }
        
        Course c = enrolledCoursesList.getSelectedValue();
        boolean isCompleted = c != null && c.getProgress().get(student.getUserId()) != null && 
                             c.getProgress().get(student.getUserId()).contains(l.getLessonId());
        
        String status = isCompleted ? " (Completed)" : " (Not Started)";
        lessonContentArea.setText("Title: " + l.getTitle() + status + "\n\nContent:\n" + l.getContent());
    }

    private void markLessonCompleted() {
        Course c = enrolledCoursesList.getSelectedValue();
        Lesson l = lessonList.getSelectedValue();
        
        if (c == null || l == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a course and a lesson first!", 
                "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (c.getProgress().get(student.getUserId()) != null && 
            c.getProgress().get(student.getUserId()).contains(l.getLessonId())) {
            JOptionPane.showMessageDialog(this, 
                "This lesson is already marked as completed!", 
                "Already Completed", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        db.markLessonCompletedInCourse(c.getCourseId(), student.getUserId(), l.getLessonId());
        
        User updatedUser = db.getUserById(student.getUserId());
        if (updatedUser instanceof Student) {
            this.student = (Student) updatedUser;
        }

        loadLessonContent();
        JOptionPane.showMessageDialog(this, 
            "Lesson marked as completed!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}