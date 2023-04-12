package dill.group.riparianreport;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReportModel { // Class that holds information regarding each question on the Report

    String question;
    String[] choices;

    String choicesString;
    String type;

    String answer;

    int order;

    boolean answered;


    public ReportModel (String type, String question, String sChoices) {
        this.question = question;
        this.type = type;
        this.choicesString = sChoices;
        answered = false;
        this.choices = sChoices.split("_");
    }

    public ReportModel (String type, String question) {
        this.question = question;
        this.type = type;
        answered = false;
    }

    public ReportModel() {

    }




    public String[] getChoices() {return choices;}

    public String getChoicesString() {return choicesString;}
    public String getQuestion() {return question;}

    public String getType() {return type;}

    public String getAnswer() {return answer;}

    public boolean isAnswered() {return answered;}



    public void setAnswer(String answer) {
        this.answer = answer;
        answered = true;
    }
}
