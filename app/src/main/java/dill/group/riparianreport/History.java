package dill.group.riparianreport;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class History extends AppCompatActivity implements RecyclerViewInterface {
    private HashMap<String, HashMap<String, String>> dictionary;

    String[] ques; //Questions from the form
    String[] dates; //Dates of all previously submitted forms
    private ArrayList<String[]> answers;   //ArrayList of answers for one form



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        dictionary = Main.dictionary;
        dates = Main.dates;
        ques = getQuestions();
        answers = getAnswers();
        initRecyclerView();
    }


    public void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.date_recycler_view);
        ArrayList<String> datesArray = new ArrayList<>(Arrays.asList(dates));
        HistoryAdapter adapter = new HistoryAdapter(this, datesArray, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(int pos) {
        Intent i = new Intent(this, HistoryInner.class);
        String[] ans = answers.get(pos);
        i.putExtra("questions", ques);
        i.putExtra("answers", ans);
        startActivity(i);
    }



    private String[] getQuestions(){
        String[] dates = dictionary.keySet().toArray(new String[0]);
        dates = sortDates(dates);
        String curKey = dates[0];
        HashMap<String, String> h = dictionary.get(curKey);
        String[] questions = h.keySet().toArray(new String[0]);
        return questions;
    }


    private ArrayList<String[]> getAnswers(){
        ArrayList<String[]> dateAnswers = new ArrayList<String[]>();
        String[] dates = dictionary.keySet().toArray(new String[0]);
        dates = sortDates(dates);
        for(int i = 0; i < dates.length; i++) {
            String curKey = dates[i];
            HashMap<String, String> h = dictionary.get(curKey);
            String[] answers = h.values().toArray(new String[0]);
            dateAnswers.add(answers);
        }
        return dateAnswers;
    }

    public static String[] sortDates(String[] dates){
        String[] sortedDates = Arrays.copyOf(dates, dates.length);

        for (int i = 0; i < sortedDates.length - 1; i++) {
            for (int j = i + 1; j < sortedDates.length; j++) {
                if (compareDates(sortedDates[i], sortedDates[j]) > 0) {
                    String temp = sortedDates[i];
                    sortedDates[i] = sortedDates[j];
                    sortedDates[j] = temp;
                }
            }
        }

        return sortedDates;
    }

    public static int compareDates(String date1, String date2) {
        String[] parts1 = date1.split("/");
        String[] parts2 = date2.split("/");
        parts1[2] = (parts1[2].replace("||", "")).split("-")[0];
        parts2[2] = (parts2[2].replace("||", "")).split("-")[0];
        int month1 = Integer.parseInt(parts1[0]);
        int day1 = Integer.parseInt(parts1[1]);
        int year1 = Integer.parseInt(parts1[2]);
        int month2 = Integer.parseInt(parts2[0]);
        int day2 = Integer.parseInt(parts2[1]);
        int year2 = Integer.parseInt(parts2[2]);
        if (year1 < year2) {
            return -1;
        } else if (year1 > year2) {
            return 1;
        } else if (month1 < month2) {
            return -1;
        } else if (month1 > month2) {
            return 1;
        } else if (day1 < day2) {
            return -1;
        } else if (day1 > day2) {
            return 1;
        } else {
            return 0;
        }
    }


}