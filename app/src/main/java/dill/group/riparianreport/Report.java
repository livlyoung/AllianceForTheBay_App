package dill.group.riparianreport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.util.Value;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;

public class Report extends AppCompatActivity implements RecyclerViewInterface {


    String[][] choices = {    //Type of question followed by choices if it is a multiple choice question
            {"TEXT"},
            {"DATE"},
            {"TEXT"},
            {"TEXT"},
            {"MULTIPLE_CHOICE", "Yes, mowed and herbicide rings around shelters_Somewhat, it was mowed_Somewhat, there were herbicide rings_No, it did not look maintained"},
            {"TEXT"},
            {"TEXT"},
            {"TEXT"},
            {"MULTIPLE_CHOICE", "Manure_Livestock hoof prints_Livestock actively in the buffer_None"},
            {"MULTIPLE_CHOICE_OTHER", "Deer_Voles_None"},
            {"MULTIPLE_CHOICE_OTHER", "No_Yes, missing stakes_Yes, missing tubes_Yes, fencing problems"},
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

        reportModels = setUpReportModels();

        adapter = new ReportAdapter(this, reportModels, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        free = true;



        Button submitButton = findViewById(R.id.submit_btn);
        submitButton.setOnClickListener(view -> {
            try {
                submitReport();
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }



    public ArrayList<ReportModel> setUpReportModels() {
        ArrayList<ReportModel> array = new ArrayList<>();
        for(int i = 0; i < Main.questionsForForm.size(); i++) { // The length of reportQuestions is the number of guestions on the form
            String type = Main.choices.get(i)[0];
            if (type.equals("MULTIPLE_CHOICE") || type.equals("MULTIPLE_CHOICE_OTHER")) {
                array.add(new ReportModel(type, Main.questionsForForm.get(i), Main.choices.get(i)[1]));
            } else {
                array.add(new ReportModel(type, Main.questionsForForm.get(i)));
            }
        }
        return array;
    }

    public interface DataStatus {
        void DataIsLoaded(ArrayList<ReportModel> array);
    }

    public void setUpReportModelsFromDatabase() {
        ArrayList<ReportModel> array = new ArrayList<>();
        FirebaseApp.initializeApp(getApplicationContext());
        FirebaseDatabase data = FirebaseDatabase.getInstance();
        mDatabase = data.getReference("Questions");

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                for(DataSnapshot keyNode : snapshot.getChildren()) {
                    count += 1;
                }
                ArrayList<ReportModel> array = new ArrayList<>(count);
                for(DataSnapshot keyNode : snapshot.getChildren()) {
                    int idx = Integer.valueOf(keyNode.getKey()) - 1;
                    ReportModel reportModel = keyNode.getValue(ReportModel.class);
                    reportModels.add(idx, reportModel);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

    public void submitReport() throws GeneralSecurityException, IOException {

        for(int i = 0; i < reportModels.size(); i++) {
            System.out.println(String.valueOf(reportModels.get(i).isAnswered()) + " " + String.valueOf(i) + " " + reportModels.get(i).getAnswer());
        }
        if (formIsComplete()) {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    // explain here some stuff here add comments (my note to self -liv)....
                    //String spreadsheetID = "1w0wuukNIylydlSAfEktT-bKT-Fwq8J7bNeAL3qWII4Q"; //the client
                    //String subSheet = "questionsv1";  //the client
                    String subSheet = "Sheet2"; //our test sheet
                    String spreadsheetID = "1viMvjqx8xCIUtwZ1Isy1hlIkWYH7fna0o3ea1Rr8JWo"; //our test sheet
                    AssetManager assetManager = getApplicationContext().getAssets();
                    InputStream inputStream;
                    GoogleCredentials credentials;
                    try {
                        inputStream = assetManager.open("allianceforthebayapp-7ef0da005605.json");
                        credentials  = GoogleCredentials.fromStream(inputStream);
                        GoogleSheetsAPI.appendReports(spreadsheetID, reportModels, credentials, subSheet);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            addToDatabase();
            Intent i = new Intent(this, Main.class);
            startActivity(i);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    addToDatabase();
                }
            });


            ImageView check = findViewById(R.id.checkView);

            //Animation a = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.upscale);
            check.setVisibility(View.VISIBLE);
            Animation b = AnimationUtils.loadAnimation(this, R.anim.upscale);
            //check.setAnimation(a);
            check.setAnimation(b);
            check.animate();
            check.setVisibility(View.INVISIBLE);


            Handler handler = new Handler();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(getApplicationContext(), Main.class);
                    startActivity(i);
                }
            }, 2000);

            //Intent i = new Intent(this, Main.class);
            //startActivity(i);
        } else {
            Toast.makeText(this, "please answer all fields", Toast.LENGTH_SHORT).show();
        }
    }

    /**
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