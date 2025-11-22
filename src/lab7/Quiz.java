
package lab7;

import java.util.List;

public class Quiz {
    private String quizId;
    private String lessonId;
    private List<Question> questions;

    public Quiz() {}

    public Quiz(String quizId, String lessonId, List<Question> questions) {
        this.quizId = quizId;
        this.lessonId = lessonId;
        this.questions = questions;
    }

    public String getQuizId() { return quizId; }
    public void setQuizId(String quizId) { this.quizId = quizId; }
    public String getLessonId() { return lessonId; }
    public void setLessonId(String lessonId) { this.lessonId = lessonId; }
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
}

