
package lab7;

import java.util.List;

public class Question {
    private String questionText;
    private List<String> choices;
    private int correctIdx;

    public Question() {}

    public Question(String questionText, List<String> choices, int correctIdx) {
        this.questionText = questionText;
        this.choices = choices;
        this.correctIdx = correctIdx;
    }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public List<String> getChoices() { return choices; }
    public void setChoices(List<String> choices) { this.choices = choices; }
    public int getCorrectIdx() { return correctIdx; }
    public void setCorrectIdx(int index) { this.correctIdx = index; }
}
