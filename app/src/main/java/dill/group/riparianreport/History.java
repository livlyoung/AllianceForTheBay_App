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

    String[] ques;
    String[] dates; /*=  {
            "3/1/2023",
            "3/3/2023",
            "3/3/2023",
            "4/1/2023",
            "4/2/2023",
            "4/3/2023",
            "5/1/2023",
            "5/2/2023",
            "5/3/2023",
    };*/
    private ArrayList<String[]> answers;/* = {    //Type of question followed by choices if it is a multiple choice question
            "John Doe",
            "3/1/2023",
            "Lancaster",
            "3 hours",
            "Somewhat, it was mowed",
            "No invasive species",
            "30 Minuets",
            "5",
            "Manure",
            "Deer",
            "No",
            "No",
            "Understood",
    };*/


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
        String curKey = dates[0];
        HashMap<String, String> h = dictionary.get(curKey);
        String[] questions = h.keySet().toArray(new String[0]);
        return questions;
    }

    private boolean checkEmpty(){
        if(dates.length == 1){
            if(dates[0].equals("-")){
                return true;
            }
        }
        return false;
    }

    private ArrayList<String[]> getAnswers(){
        ArrayList<String[]> dateAnswers = new ArrayList<String[]>();
        String[] dates = dictionary.keySet().toArray(new String[0]);
        for(int i = 0; i < dates.length; i++) {
            String curKey = dates[i];
            HashMap<String, String> h = dictionary.get(curKey);
            String[] answers = h.values().toArray(new String[0]);
            dateAnswers.add(answers);
        }
        return dateAnswers;
    }


}