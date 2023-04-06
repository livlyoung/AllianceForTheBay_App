package dill.group.riparianreport;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SplashScreen extends AppCompatActivity {

    SharedPreferences prefs = null;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the button bar
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        setContentView(R.layout.activity_splash_screen);
        mAuth = FirebaseAuth.getInstance();

        View view = findViewById(R.id.splash_screen);

        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);


        //hides action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user == null) {
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(i);
                        }
                    });
                }
                else {
                    //Toast.makeText(getApplicationContext(),user.getEmail(),Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), Main.class));
                }

            }
        }, 2000);

        prefs = getSharedPreferences("dill.group.riparianreport", MODE_PRIVATE);

    }
}