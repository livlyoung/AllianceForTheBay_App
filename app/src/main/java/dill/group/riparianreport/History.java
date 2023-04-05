package dill.group.riparianreport;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;

public class History extends AppCompatActivity implements RecyclerViewInterface {

    String[] dates =  {
            "3/1/2023",
            "3/3/2023",
            "3/3/2023",
            "4/1/2023",
            "4/2/2023",
            "4/3/2023",
            "5/1/2023",
            "5/2/2023",
            "5/3/2023",
    };

    String[] answers = {    //Type of question followed by choices if it is a multiple choice question
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
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        initRecyclerView();
    }


    public void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.date_recycler_view);

        ArrayList<String> datesArray = new ArrayList<>(Arrays.asList(dates));
        HistoryAdapter adapter = new HistoryAdapter(this, datesArray, this );
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onItemClick(int pos) {
        String[] reportQuestions = getResources().getStringArray(R.array.questions);
        Intent i = new Intent(this, HistoryInner.class);
        i.putExtra("questions", reportQuestions);
        i.putExtra("answers", answers);
        startActivity(i);
    }
}