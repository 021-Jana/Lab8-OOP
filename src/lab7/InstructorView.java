package lab7;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class InstructorView extends JFrame {
    private final JsonDatabaseManager db;
    private final String courseId;
    private JPanel lessonsPanel;

    public InstructorView(JsonDatabaseManager db, String courseId) {
        this.db = db;
        this.courseId = courseId;

        setTitle("Instructor Dashboard - Lessons");
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

            JPanel btnPanel = new JPanel();

            JButton editBtn = new JButton("Edit");
            editBtn.addActionListener(e -> {
                String newContent = JOptionPane.showInputDialog(this, "Edit content:", lesson.getContent());
                if (newContent != null && !newContent.isEmpty()) {
                    lesson.setContent(newContent);
                    db.editLessonInCourse(courseId, lesson);
                    loadLessons(); // update
                }
            });

            JButton deleteBtn = new JButton("Delete");
            deleteBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    db.deleteLessonFromCourse(courseId, lesson.getLessonId());
                    loadLessons(); // update
                }
            });

            btnPanel.add(editBtn);
            btnPanel.add(deleteBtn);
            lessonPanel.add(btnPanel, BorderLayout.SOUTH);

            lessonsPanel.add(lessonPanel);
        }

        lessonsPanel.revalidate();
        lessonsPanel.repaint();
    }
}
