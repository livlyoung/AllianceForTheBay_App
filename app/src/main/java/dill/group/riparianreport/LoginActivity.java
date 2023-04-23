package dill.group.riparianreport;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * LoginActivity class is responsible for handling user login.
 * It checks the user credentials using FirebaseAuth API,
 * and provides an interface to log in to the application.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Overrides onBackPressed() method to finish all the activities in the back stack.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    // Initialize UI elements and Firebase instance variables
    private EditText mEmailField;
    private EditText mPasswordField;

    private FirebaseAuth mAuth;

    public static String Globalemail;

    /**
     * Sets up the LoginActivity UI and initializes Firebase instance variables.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Set up UI elements
        mEmailField = findViewById(R.id.editTextEmail);
        mPasswordField = findViewById(R.id.editTextPassword);

        FirebaseUser user = mAuth.getCurrentUser();

        // If a user is already logged in, fill in the email field with their email
        if (user != null) {
            //The database doesn't allow the 'period' character so we
            // need to change the dot in the email to a comma.
            // It doesn't have to be a comma that's just what we chose
            mEmailField.setText(user.getEmail().replace(".", ","));
        }

        Button loginButton = findViewById(R.id.buttonLogin);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(mEmailField.getText().toString(), mPasswordField.getText().toString().toLowerCase());
            }
        });

        // Set up create account button with onClick listener to launch the CreateAccount activity
        TextView createAccountButton = findViewById(R.id.textViewSignUp);
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), CreateAccount.class);
                startActivity(i);
                finish();
                LoginActivity.Globalemail = CreateAccount.Globalemail;

            }
        });
    }

    /**
     * Method to sign in the user using their email and password.
     * It validates the email and password fields before signing in.
     *
     * @param email The email entered by the user.
     * @param password The password entered by the user.
     */
    private void signIn(String email, String password) {
        if (!validateForm()) {
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Source: Chat GPT
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(LoginActivity.this, "Authentication succeeded.", Toast.LENGTH_SHORT).show();
                            Globalemail = email.replace(".", ",").toLowerCase();
                            Intent intent = new Intent(LoginActivity.this, Main.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * This method is used to validate the form data entered by the user in the LoginActivity.
     * It checks whether the email and password fields are empty.
     * If they are empty, it sets an error message on the corresponding
     * EditText view and sets the valid flag to false.
     * If the fields are not empty, it clears the error message and leaves the
     * valid flag unchanged. It returns the valid flag as a boolean
     * value indicating whether the form data is valid or not.
     *
     * @return True if email and password fields are valid, false otherwise.
     */
    private boolean validateForm() {
        //Source: Chat GPT
        boolean valid = true;

        String email = mEmailField.getText().toString().toLowerCase();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

}
