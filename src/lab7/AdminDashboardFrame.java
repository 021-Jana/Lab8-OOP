package lab7;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;

public class AdminDashboardFrame extends JFrame {

    private Admin admin;
    private JsonDatabaseManager db;

    private DefaultListModel<Course> pendingCoursesModel = new DefaultListModel<>();
    private JList<Course> pendingCoursesList;

    private JTextArea courseDetailsArea;

    public AdminDashboardFrame(Admin admin, JsonDatabaseManager db) {
        this.admin = admin;
        this.db = db;

        setTitle("Admin Dashboard – " + admin.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        loadPendingCourses();

        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(40, 40, 45));
        panel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("Admin Dashboard - Welcome, " + admin.getUsername());
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

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lbl = new JLabel("Course Approval Management");
        lbl.setFont(new Font("Arial", Font.BOLD, 18));
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        
        pendingCoursesList = new JList<>(pendingCoursesModel);
        pendingCoursesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pendingCoursesList.setFont(new Font("Arial", Font.PLAIN, 14));
        pendingCoursesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadCourseDetails();
            }
        });

        JScrollPane scroll = new JScrollPane(pendingCoursesList);
        scroll.setBorder(BorderFactory.createTitledBorder("Pending Courses for Approval"));
        scroll.setPreferredSize(new Dimension(300, 0));

       
        JPanel detailsPanel = new JPanel(new BorderLayout(10, 10));
        
        courseDetailsArea = new JTextArea();
        courseDetailsArea.setEditable(false);
        courseDetailsArea.setFont(new Font("Arial", Font.PLAIN, 14));
        courseDetailsArea.setBorder(BorderFactory.createTitledBorder("Course Details"));
        courseDetailsArea.setLineWrap(true);
        courseDetailsArea.setWrapStyleWord(true);

        JScrollPane detailsScroll = new JScrollPane(courseDetailsArea);

        
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton approveBtn = new JButton("Approve Course");
        JButton rejectBtn = new JButton("Reject Course");
        JButton refreshBtn = new JButton("Refresh List");

        approveBtn.setBackground(new Color(60, 179, 113));
        rejectBtn.setBackground(new Color(220, 80, 60));
        refreshBtn.setBackground(new Color(70, 130, 180));
        
        approveBtn.setForeground(Color.WHITE);
        rejectBtn.setForeground(Color.WHITE);
        refreshBtn.setForeground(Color.WHITE);

        approveBtn.addActionListener(e -> approveSelectedCourse());
        rejectBtn.addActionListener(e -> rejectSelectedCourse());
        refreshBtn.addActionListener(e -> loadPendingCourses());

        btnPanel.add(approveBtn);
        btnPanel.add(rejectBtn);
        btnPanel.add(refreshBtn);

        detailsPanel.add(detailsScroll, BorderLayout.CENTER);
        detailsPanel.add(btnPanel, BorderLayout.SOUTH);

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.WEST);
        panel.add(detailsPanel, BorderLayout.CENTER);

        return panel;
    }

    private void loadPendingCourses() {
        pendingCoursesModel.clear();
        courseDetailsArea.setText("");
        
        Map<String, Course> pendingCourses = db.getPendingCourses();
        for (Course course : pendingCourses.values()) {
            if (course.isValidCourse()) {
                pendingCoursesModel.addElement(course);
            }
        }
        
        System.out.println("Loaded " + pendingCoursesModel.size() + " pending courses");
    }

    private void loadCourseDetails() {
        Course course = pendingCoursesList.getSelectedValue();
        if (course == null) {
            courseDetailsArea.setText("");
            return;
        }

        
        User instructor = db.getUserById(course.getInstructorId());
        String instructorName = (instructor != null) ? instructor.getUsername() : "Unknown Instructor";

        StringBuilder details = new StringBuilder();
        details.append("Course Title: ").append(course.getTitle()).append("\n\n");
        details.append("Description: ").append(course.getDescription()).append("\n\n");
        details.append("Instructor: ").append(instructorName).append("\n\n");
        details.append("Number of Lessons: ").append(course.getLessons().size()).append("\n\n");
        details.append("Current Status: ").append(course.getApprovalStatus()).append("\n\n");
        
        
        if (!course.isValidCourse()) {
            details.append("⚠️ VALIDATION ISSUES:\n");
            if (!course.isValidTitle()) {
                details.append("  - Invalid course title\n");
            }
            if (!course.isValidDescription()) {
                details.append("  - Invalid course description\n");
            }
            if (!course.hasValidLessons()) {
                details.append("  - Some lessons have invalid titles\n");
            }
            details.append("\n");
        }
        
        if (!course.getLessons().isEmpty()) {
            details.append("Lessons:\n");
            for (Lesson lesson : course.getLessons()) {
                details.append("  - ").append(lesson.getTitle()).append("\n");
            }
        }

        courseDetailsArea.setText(details.toString());
    }

    private void approveSelectedCourse() {
        Course course = pendingCoursesList.getSelectedValue();
        if (course == null) {
            JOptionPane.showMessageDialog(this, "Please select a course first.", "No Course Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

     
        if (!course.isValidCourse()) {
            JOptionPane.showMessageDialog(this, 
                "Cannot approve course with validation issues!\nPlease check the course details.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to APPROVE the course: " + course.getTitle() + "?",
                "Confirm Approval", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = db.approveCourse(course.getCourseId());
            if (success) {
                JOptionPane.showMessageDialog(this, 
                    "Course approved successfully! It is now visible to students.", 
                    "Approval Successful", JOptionPane.INFORMATION_MESSAGE);
                loadPendingCourses();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to approve course. Please try again.", 
                    "Approval Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void rejectSelectedCourse() {
        Course course = pendingCoursesList.getSelectedValue();
        if (course == null) {
            JOptionPane.showMessageDialog(this, "Please select a course first.", "No Course Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String reason = JOptionPane.showInputDialog(this,
                "Please provide a reason for rejecting this course:",
                "Rejection Reason", JOptionPane.QUESTION_MESSAGE);

        if (reason != null && !reason.trim().isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to REJECT the course: " + course.getTitle() + "?\nReason: " + reason,
                    "Confirm Rejection", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = db.rejectCourse(course.getCourseId(), reason.trim());
                if (success) {
                    JOptionPane.showMessageDialog(this, 
                        "Course rejected successfully!", 
                        "Rejection Successful", JOptionPane.INFORMATION_MESSAGE);
                    loadPendingCourses();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to reject course. Please try again.", 
                        "Rejection Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (reason != null) {
            JOptionPane.showMessageDialog(this, 
                "Please provide a reason for rejection.", 
                "Reason Required", JOptionPane.WARNING_MESSAGE);
        }
    }
}