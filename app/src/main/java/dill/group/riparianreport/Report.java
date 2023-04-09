package dill.group.riparianreport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

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
            {"MULTIPLE_CHOICE", "Understood"},
    };

    ArrayList<ReportModel> reportModels = new ArrayList<>();
    ReportAdapter adapter;

    boolean free; // Limits user to answering one question at a time

    private DatabaseReference mDatabase; //Used to reference an instance of the database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);


        RecyclerView recyclerView = findViewById(R.id.recyclerview);

        setUpReportModels();

        adapter = new ReportAdapter(this, reportModels, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        free = true;

        Button submitButton = findViewById(R.id.submit_btn);
        submitButton.setOnClickListener(view -> submitReport());
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
        if (free) {
            String type = reportModels.get(pos).getType();
            if (type.equals("TEXT")) {
                makeTextDialog(pos);
            } else if (type.equals("MULTIPLE_CHOICE") || type.equals("MULTIPLE_CHOICE_OTHER")) {
                makeMultipleChoiceDialog(pos, type);
                Log.d("Pressed", "MULTIPLE_OTHER");
            } else if (type.equals("DATE")) {
                makeDateDialog(pos);
            }
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
                adapter.notifyItemChanged(pos);
                //free = true;
            }
        });

        if (type.equals("MULTIPLE_CHOICE_OTHER")) {
            alert.setNegativeButton("OTHER", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    makeTextDialog(pos);
                    dialogInterface.cancel();
                }
            });
        }
        alert.create();
        alert.show();
    }


    public void makeTextDialog(int pos) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText et = new EditText(this);
        alert.setTitle(reportModels.get(pos).getQuestion());
        alert.setView(et);
        alert.setPositiveButton("ENTER", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String answer = et.getText().toString();
                if (answer.length() > 0) {
                    reportModels.get(pos).setAnswer(answer);
                    adapter.notifyItemChanged(pos);
                }
                dialog.cancel();
                //free = true;
            }
        });
        alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
                //free = true;
            }
        });
        alert.show();
    }

    public void makeDateDialog(int pos) {
        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                String answer = String.valueOf(i1 + 1) + "/" + String.valueOf(i2) + "/" + String.valueOf(i);
                reportModels.get(pos).setAnswer(answer);
                Log.d("Date", String.valueOf(pos));
                adapter.notifyItemChanged(pos);
                free = true;
            }
        }, mYear, mMonth, mDay);

        dialog.show();
    }

    public boolean formIsComplete() {
        boolean complete = true;
        for(int i = 0; i < reportModels.size(); i++) {
            if (!(reportModels.get(i).isAnswered())) {
                complete = false;
            }
        }
        return complete;
    }

    public void submitReport() {
        for(int i = 0; i < reportModels.size(); i++) {
            System.out.println(String.valueOf(reportModels.get(i).isAnswered()) + " " + String.valueOf(i) + " " + reportModels.get(i).getAnswer());
        }
        if (formIsComplete()) {
            addToDatabase();
            Intent i = new Intent(this, Main.class);
            startActivity(i);
        } else {
            Toast.makeText(this, "please answer all fields", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    addToDatabase() has no parameters. It asynchronously accesses an instance of the database
    to add information to it. For each reportModel in the ArrayList, the question is added as
    a column attribute and the answer is added as an entry.
     */
    private void addToDatabase(){
        FirebaseApp.initializeApp(getApplicationContext());
        FirebaseDatabase data = FirebaseDatabase.getInstance();
        mDatabase = data.getReference("users/" + LoginActivity.Globalemail);

        //Generates a random key for each form submission for each user
        String questionSetId = mDatabase.child("questionSet").push().getKey();

        for(int i = 0; i < reportModels.size(); i++){
            final String attribute = reportModels.get(i).getQuestion();
            String answer = reportModels.get(i).getAnswer();
            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(attribute.contains(".")){
                        mDatabase.child(questionSetId).child(attribute.replace(".", ",")).setValue(answer);
                    }
                    else{
                        mDatabase.child(questionSetId).child(attribute).setValue(answer);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getApplicationContext(), "Error: Answers not saved", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}