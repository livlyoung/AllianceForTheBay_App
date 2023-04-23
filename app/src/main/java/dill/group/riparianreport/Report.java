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

    ArrayList<ReportModel> reportModels = new ArrayList<>();
    ReportAdapter adapter;

    boolean free; // Limits user to answering one question at a time

    private DatabaseReference mDatabase; //Used to reference an instance of the database

    public static String locName; //Name of the location of the current form being submitted

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

        testLocations();
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
                //Log.d("Pressed", "MULTIPLE_OTHER");
            } else if (type.equals("DATE")) {
                makeDateDialog(pos);
            } else if (type.equals("LOCATION")) {
                makeLocationDialog(pos);
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

    public void makeLocationDialog(int pos) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        String[] nameArray = Main.names.toArray(new String[Main.names.size()]);

        alert.setTitle(reportModels.get(pos).getQuestion());
        alert.setItems(nameArray, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // The 'which' argument contains the index position
                // of the selected item
                String answer = nameArray[which];
                reportModels.get(pos).setAnswer(answer);
                adapter.notifyItemChanged(pos);
                //free = true;
            }
        });
        alert.show();
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
                    /**
                     *  This section sets the spreadsheet ID, subSheet name,
                     *  and Google Credentials required to append report models
                     *  to a Google Sheet using the Google Sheets API. The
                     *  credentials are obtained from the provided JSON file.
                     *
                     *  @param spreadsheetID The ID of the Google Sheet to append the report models to.
                     *  @param reportModels The report models to append to the Google Sheet.
                     *  @param subSheet The name of the subSheet in the Google Sheet to append the report models to.
                     *  @throws RuntimeException If there is an IOException when attempting to open the JSON file.
                     *  @throws Exception If there is an error obtaining the Google Credentials from the JSON file.
                     */
                    //String spreadsheetID = "1viMvjqx8xCIUtwZ1Isy1hlIkWYH7fna0o3ea1Rr8JWo"; //our test sheet
                    String spreadsheetID = "1w0wuukNIylydlSAfEktT-bKT-Fwq8J7bNeAL3qWII4Q"; //the client
                    String subSheet = Main.questionVersion;
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

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    addToDatabase();
                }
            });


            ImageView check = findViewById(R.id.checkView);
            check.setVisibility(View.VISIBLE);
            Animation b = AnimationUtils.loadAnimation(this, R.anim.upscale);
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
        DatabaseReference rDatabase = data.getReference("Locations");

        //Generates a random key for each form submission for each user
        String questionSetId = mDatabase.child("questionSet").push().getKey();
        HashMap<String, String> ans = new HashMap<String, String>();


        for(int i = 0; i < reportModels.size(); i++){
            final String attribute = reportModels.get(i).getQuestion();
            String answer = reportModels.get(i).getAnswer();
            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(attribute.contains(".")){
                        mDatabase.child(questionSetId).child(attribute.replace(".", ",")).setValue(answer);
                        ans.put(attribute.replace(".", ","), answer);
                    }
                    else{
                        mDatabase.child(questionSetId).child(attribute).setValue(answer);
                        ans.put(attribute, answer);
                        if(attribute.equals("Site name or location")){
                            //Assuming the answer is the NAME of the location, not the address
                            rDatabase.child(answer).child("formID").setValue(questionSetId);
                            rDatabase.child(answer).child("gmail").setValue(LoginActivity.Globalemail);
                            locName = answer;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getApplicationContext(), "Error: Answers not saved", Toast.LENGTH_LONG).show();
                }
            });
        }
        fillMapReport(ans);
    }

    public static void fillMapReport(HashMap<String, String> ans){
        int idx = -1;
        for(int i = 0; i<Main.names.size(); i++){
            if(locName.equals(Main.names.get(i))){
                Main.reports.set(idx, ans);
                break;
            }
        }
    }

    public static void testLocations() {
        for(int i = 0; i < Main.names.size(); i++) {
            Log.d("Locations", Main.names.get(i) + " " + Main.locations.get(i));
            //System.out.println(Main.names.get(i) + " " + Main.locations.get(i));
        }
    }
}