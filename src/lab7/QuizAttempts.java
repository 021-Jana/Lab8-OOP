
package lab7;

import java.util.Date;

public class QuizAttempts {
    private String quizId;
    private int score;
    private Date attemptDate;

    public QuizAttempts() {}

    public QuizAttempts(String quizId, int score, Date attemptDate) {
        this.quizId = quizId;
        this.score = score;
        this.attemptDate = attemptDate;
    }

    public String getQuizId() { return quizId; }
    public void setQuizId(String quizId) { this.quizId = quizId; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public Date getAttemptDate() { return attemptDate; }
    public void setAttemptDate(Date date) { this.attemptDate = date; }
}

