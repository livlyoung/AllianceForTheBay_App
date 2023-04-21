package dill.group.riparianreport;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends AppCompatActivity {

    // Test to see if I can add to the code (Luke)

    public static HashMap<String, HashMap<String, String>> dictionary;

    public static String[] dates;
    public static String[] questions;
    public static ArrayList<String[]> choices;
    public static ArrayList<String> questionsForForm;
    FirebaseAuth mAuth;
    static DatabaseReference databaseR;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        LoginActivity.Globalemail = user.getEmail().replace(".", ",");
        databaseR = FirebaseDatabase.getInstance().getReference();
        readFromDatabase();
        choices = new ArrayList<String[]>();
        questionsForForm = new ArrayList<String>();
        getQuestionsFromDatabase();



        Button reportButton = findViewById(R.id.report_button);
        reportButton.setOnClickListener(view -> handleReportButton());

        Button historyButton = findViewById(R.id.view_previous_forms_button);
        historyButton.setOnClickListener(view -> handleHistoryButton());

        Button mapButton = findViewById(R.id.view_map_button);
        mapButton.setOnClickListener(view -> handleMapButton());

    }


    public void handleReportButton() { // Makes a new "Report" (Form)
        Intent i = new Intent(this, Report.class);
        startActivity(i);
    }

    public void handleHistoryButton() { // View previous "Report" forms
        if(checkEmpty()){
            Intent j = new Intent(this, EmptyHistory.class);
            startActivity(j);
        }
        else {
            Intent i = new Intent(this, History.class);
            startActivity(i);
        }
    }

    public void handleMapButton() {
        Intent i = new Intent(this, Map.class);
        startActivity(i);
    }


    /*
    This function accesses the database and pulls a datasnapshot from it (an instance of the database).
     */
    public static void readFromDatabase(){
        String userId = LoginActivity.Globalemail;
        databaseR.child("users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    DataSnapshot dss = task.getResult();
                    dictionary = parseJSONString(dss);
                    dates = getDates();
                }
            }
        });
    }

    /*
    This function takes a datasnapshot (an instance of the database) and parses it into a hashmap.
    The key of the map is the date the form was submitted and the value is a dictionary containing
    the questions and answers of the form.
     */
    private static HashMap<String, HashMap<String, String>> parseJSONString(DataSnapshot dataSnapshot){
        HashMap<String, HashMap<String, String>> dictionary = new HashMap<>();
        String date;
        if(dataSnapshot.hasChildren()){
            Iterator<DataSnapshot> iter = dataSnapshot.getChildren().iterator();
            while (iter.hasNext()){
                DataSnapshot snap = iter.next();
                HashMap<String, String> answers = parseAnswers(snap);
                date = answers.get("Date of Check-up").toString();
                String entryID = snap.getKey();
                dictionary.put(date + "||" + entryID + "||", answers);
            }
        }
        else{
            HashMap<String, String> empty = new HashMap<>();
            empty.put("","");
            dictionary.put("-", empty);
        }
        return dictionary;
    }


    /*
   This function takes in a datasnapshot and returns a hashmap of questions and answers from a
   single form from a single user.
    */
    private static HashMap<String, String> parseAnswers(DataSnapshot dss){
        HashMap<String, String> answers = new HashMap<>();
        Iterator<DataSnapshot> iterator = dss.getChildren().iterator();
        while(iterator.hasNext()){
            DataSnapshot snap = iterator.next();
            String question = snap.getKey();
            String answer = (String) snap.getValue();
            answers.put(question, answer);
        }
        return answers;
    }


    private static String[] getDates(){
        String[] dates = dictionary.keySet().toArray(new String[0]);
        for(int i=0; i<dates.length; i++){
            dates[i] = dates[i].replace("||", "");
            String[] split = dates[i].split("-");
            if(split.length >= 1) {
                dates[i] = split[0];
            }
            else{
                dates[i] = "-";
            }
        }
        return dates;
    }

    /*
    Formats the dates correctly (A.K.A if there are no previously submitted forms, it adds a "-" to
    the list of dates).
     */
    private boolean checkEmpty(){
        if(dates.length == 1){
            if(dates[0].equals("-")){
                return true;
            }
        }
        return false;
    }

    /*
    Accesses the database and reads the questions from it. Uses parseQuestions to fill arrays for
    questions, choices, and choicetypes. Is mainly used to set up the report form on report.java.
     */
    private void getQuestionsFromDatabase(){
        DatabaseReference databaseR = FirebaseDatabase.getInstance().getReference();
        databaseR.child("Questions").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    DataSnapshot dss = task.getResult();
                    if(dss.hasChildren()){
                        Iterator<DataSnapshot> iter = dss.getChildren().iterator();
                        while (iter.hasNext()){
                            DataSnapshot snap = iter.next();
                            parseQuestions(snap);
                        }
                    }
                }
            }
        });
    }

    /*
    Reads the questions, choices, and choicetype from a datasnapshot of the database and parses
    each to arrays. Is mainly used to set up the report form on report.java.
     */
    private void parseQuestions(DataSnapshot dss){
        String[] choiceType;
        String type = (String) dss.child("type").getValue();
        String ch = (String) dss.child("choices").getValue();
        String question = (String) dss.child("question").getValue();
        if(!ch.contains("NONE")){
            choiceType = new String[]{type, ch};
        }
        else{
            choiceType = new String[]{type};
        }
        choices.add(choiceType);
        questionsForForm.add(question);
    }

    private void getLocations(){

    }




}