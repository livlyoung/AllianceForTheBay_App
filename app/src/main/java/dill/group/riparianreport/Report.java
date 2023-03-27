package dill.group.riparianreport;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import java.util.ArrayList;

public class Report extends AppCompatActivity implements RecyclerViewInterface {


    String[][] choices = {    //Type of question followed by choices if it is a multiple choice question
            {"TEXT"},
            {"DATE"},
            {"TEXT"},
            {"TEXT"},
            {"MULTIPLE_CHOICE", "Yes, mowed and herbicide rings around shelters", "Somewhat, it was mowed", "Somewhat, there were herbicide rings", "No, it did not look maintained"},
            {"TEXT"},
            {"TEXT"},
            {"TEXT"},
            {"MULTIPLE_CHOICE", "Manure", "Livestock hoof prints", "Livestock actively in the buffer", "None"},
            {"MULTIPLE_CHOICE_OTHER", "Deer", "Voles", "None"},
            {"MULTIPLE_CHOICE_OTHER", "No", "Yes, missing stakes", "Yes, missing tubes", "Yes, fencing problems"},
            {"TEXT"},
            {"PHOTO"},
    };

    ArrayList<ReportModel> reportModels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);


        RecyclerView recyclerView = findViewById(R.id.recyclerview);

        setUpReportModels();

        ReportRecyclerViewAdapter adapter = new ReportRecyclerViewAdapter(this, reportModels, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void updateRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        ReportRecyclerViewAdapter adapter = new ReportRecyclerViewAdapter(this, reportModels, this);
        recyclerView.setAdapter(adapter);
    }



    private void setUpReportModels() {
        String[] reportQuestions = getResources().getStringArray(R.array.questions);

        for(int i = 0; i < reportQuestions.length; i++) { // The length of reportQuestions is the number of guestions on the form
            String type = choices[i][0];
            reportModels.add(new ReportModel(type, reportQuestions[i], choices[i]));
        }

    }


    @Override
    public void onItemClick(int pos) {

        String type = reportModels.get(pos).getType();
        if (type.equals("TEXT")) {
            makeTextDialog(pos, reportModels.get(pos).getQuestion());
        } else if (type.equals("MULTIPLE_CHOICE") || type.equals("MULTIPLE_CHOICE_OTHER")) {
            makeMultipleChoiceDialog(pos, type);
            Log.d("Pressed", "MULTIPLE_OTHER");
        }

    }

    public void makeMultipleChoiceDialog(int pos, String type) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(reportModels.get(pos).getQuestion());
        alert.setItems(reportModels.get(pos).getChoices(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // The 'which' argument contains the index position
                // of the selected item
                String answer = reportModels.get(pos).getChoices()[which];
                reportModels.get(pos).setAnswer(answer);
                updateRecyclerView();
            }
        });

        if (type.equals("MULTIPLE_CHOICE_OTHER")) {
            alert.setNegativeButton("OTHER", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    makeTextDialog(pos, reportModels.get(pos).getQuestion());
                    dialogInterface.cancel();
                }
            });
        }
        alert.create();
        alert.show();
    }


    public void makeTextDialog(int pos, String question) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText et = new EditText(this);
        alert.setTitle(question);
        alert.setView(et);
        alert.setPositiveButton("ENTER", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String answer = et.getText().toString();
                reportModels.get(pos).setAnswer(answer);
                updateRecyclerView();
                dialog.cancel();
            }
        });
        alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });
        alert.show();
    }



}