package lab7;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class StudentView extends JFrame {
    private final JsonDatabaseManager db;
    private final String courseId;
    private final String studentId;
    private JPanel lessonsPanel;

    public StudentView(JsonDatabaseManager db, String courseId, String studentId) {
        this.db = db;
        this.courseId = courseId;
        this.studentId = studentId;

        setTitle("Student Dashboard - Lessons");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        lessonsPanel = new JPanel();
        lessonsPanel.setLayout(new BoxLayout(lessonsPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(lessonsPanel);
        add(scrollPane);

        loadLessons();
    }

    public void loadLessons() {
        lessonsPanel.removeAll();

        ArrayList<Lesson> lessons = db.getLessonsByCourse(courseId);

        for (Lesson lesson : lessons) {
            JPanel lessonPanel = new JPanel(new BorderLayout());
            lessonPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            lessonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

            JLabel titleLabel = new JLabel("Title: " + lesson.getTitle());
            titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
            lessonPanel.add(titleLabel, BorderLayout.NORTH);

            JTextArea contentArea = new JTextArea(lesson.getContent());
            contentArea.setLineWrap(true);
            contentArea.setWrapStyleWord(true);
            contentArea.setEditable(false);
            lessonPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);

            JButton completeBtn = new JButton("Mark as Completed");
            if (db.isLessonCompleted(courseId, studentId, lesson.getLessonId())) {
                completeBtn.setText("Completed");
                completeBtn.setEnabled(false);
            }

            completeBtn.addActionListener(e -> {
                db.markLessonCompletedInCourse(courseId, studentId, lesson.getLessonId());
                loadLessons(); // update
            });

            JPanel btnPanel = new JPanel();
            btnPanel.add(completeBtn);
            lessonPanel.add(btnPanel, BorderLayout.SOUTH);

            lessonsPanel.add(lessonPanel);
        }

        lessonsPanel.revalidate();
        lessonsPanel.repaint();
    }
}
