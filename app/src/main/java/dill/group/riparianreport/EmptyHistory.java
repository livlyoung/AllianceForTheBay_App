package dill.group.riparianreport;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class EmptyHistory extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty_history);
    }

    @Override
    public void onBackPressed() {
        Main.readFromDatabase();
        super.onBackPressed();
    }
}