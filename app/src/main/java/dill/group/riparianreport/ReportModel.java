package dill.group.riparianreport;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class ReportModel { // Class that holds information regarding each question on the Report

    String question;
    String[] choices;
    String type;

    String answer;

    int order;

    boolean answered;


    public ReportModel (String type, String question, String[] choices) {
        this.question = question;
        this.type = type;

        answered = false;

        if (choices.length > 1) {
            this.choices = Arrays.copyOfRange(choices, 1, choices.length);
        }  else {
            this.choices = choices;
        }
    }


    public String[] getChoices() {return choices;}
    public String getQuestion() {return question;}

    public String getType() {return type;}

    public String getAnswer() {return answer;}

    public boolean isAnswered() {return answered;}



    public void setAnswer(String answer) {
        this.answer = answer;
        answered = true;
    }
}
