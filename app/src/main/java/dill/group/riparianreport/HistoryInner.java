package dill.group.riparianreport;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;

public class HistoryInner extends AppCompatActivity implements RecyclerViewInterface {

    String[] questions;

    String[] answers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_inner);

        Intent intent = getIntent();
        questions = intent.getStringArrayExtra("questions");
        answers = intent.getStringArrayExtra("answers");

        initRecyclerView();
    }

    public void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.history_inner_recycler_view);
        ArrayList<String> questionsArray = new ArrayList<>(Arrays.asList(questions));
        ArrayList<String> answersArray = new ArrayList<>(Arrays.asList(answers));
        HistoryInnerAdapter adapter = new HistoryInnerAdapter(this, questionsArray, answersArray, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(int pos) {
        return;
    }
}