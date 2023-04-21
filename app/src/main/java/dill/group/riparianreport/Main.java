package dill.group.riparianreport;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
    public static ArrayList<String> names;
    public static ArrayList<String> locations;
    public static ArrayList<HashMap<String, String>> reports;
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

        //Initialize Firebase Capabilities
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        LoginActivity.Globalemail = user.getEmail().replace(".", ",");
        databaseR = FirebaseDatabase.getInstance().getReference();

        //Initialize and fill array of locations for Report.java
        locations = new ArrayList<String>();
        names = new ArrayList<String>();
        reports = new ArrayList<HashMap<String, String>>();
        getLocationsFromDatabase();

        //Read previous forms from the database for History.java
        readFromDatabase();

        //Initialize and fill arrays for questions for Report.java
        choices = new ArrayList<String[]>();
        questionsForForm = new ArrayList<String>();
        getQuestionsFromDatabase();

        Log.d("Main", "it came back!!!!!!!");





        Button reportButton = findViewById(R.id.report_button);
        reportButton.setOnClickListener(view -> handleReportButton());

        Button historyButton = findViewById(R.id.view_previous_forms_button);
        historyButton.setOnClickListener(view -> handleHistoryButton());

        Button mapButton = findViewById(R.id.view_map_button);
        mapButton.setOnClickListener(view -> handleMapButton());

        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(view -> handleLogoutButton());

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
        for (int i = 0; i < dates.length - 1; i++) {
            for (int j = i + 1; j < dates.length; j++) {
                if (History.compareDates(dates[i], dates[j]) > 0) {
                    String temp = dates[i];
                    dates[i] = dates[j];
                    dates[j] = temp;
                }
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

    /*
    This method pulls all of the locations listed within the database and stores them within
    a global HashMap for use within Report.java.
     */
    private void getLocationsFromDatabase(){
        databaseR.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting locations", task.getException());
                }
                else {
                    DataSnapshot dss = task.getResult();

                    //For the Locations
                    DataSnapshot dataSnapshot = dss.child("Locations");
                    Iterator<DataSnapshot> iter = dataSnapshot.getChildren().iterator();
                    while (iter.hasNext()){
                        DataSnapshot snap = iter.next();
                        String name = (String) snap.getKey();
                        names.add(name);
                        String loc = (String) snap.child("address").getValue();
                        locations.add(loc);
                        String gmail = (String) snap.child("gmail").getValue();
                        String form = (String) snap.child("formID").getValue();
                        HashMap<String, String> ans = parseAnswers(dss.child("users").child(gmail).child(form));
                        reports.add(ans);
                    }

                }
            }
        });
    }


    public void handleLogoutButton() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle("Logout")
                .setMessage("Are you sure you would like to logout?").setIcon(R.drawable.warning)
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Logout
                        mAuth.signOut();
                        Toast.makeText(getApplicationContext(), "Signed out successfully.", Toast.LENGTH_SHORT).show();
                        Intent logIn = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(logIn);
                        dialogInterface.cancel();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).show();
    }


}