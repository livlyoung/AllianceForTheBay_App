package dill.group.riparianreport;

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

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Main extends AppCompatActivity {

    // Test to see if I can add to the code (Luke)

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        Button reportButton = findViewById(R.id.report_button);
        reportButton.setOnClickListener(view -> handleReportButton());

        Button historyButton = findViewById(R.id.view_previous_forms_button);
        historyButton.setOnClickListener(view -> handleHistoryButton());




    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Intent i = new Intent(this,LoginActivity.class);
            startActivity(i);
        }
        else{
            Log.d("User:", user.getDisplayName());
        }

    }

    public void handleReportButton() { // Makes a new "Report" (Form)
        Intent i = new Intent(this, Report.class);
        startActivity(i);
    }

    public void handleHistoryButton() { // Makes a new "Report" (Form)
        Intent i = new Intent(this, History.class);
        startActivity(i);
    }










}