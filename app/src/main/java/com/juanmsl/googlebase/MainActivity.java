package com.juanmsl.googlebase;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.juanmsl.googlebase.logic.FBAuth;
import com.juanmsl.googlebase.logic.FieldValidator;

public class MainActivity extends AppCompatActivity {

    private FBAuth firebaseAuthentication;
    private Intent signupIntent;
    private Intent homeIntent;

    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private Button btnLogin;
    private Button btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuthentication = new FBAuth(this) {
            @Override
            public void onSuccess() {
                startActivity(homeIntent);
            }
        };
        signupIntent = new Intent(this, SignupActivity.class);
        homeIntent = new Intent(this, HomeActivity.class);

        emailLayout =  (TextInputLayout) findViewById(R.id.login_email_layout);
        passwordLayout =  (TextInputLayout) findViewById(R.id.login_password_layout);
        emailInput = (TextInputEditText) findViewById(R.id.login_email);
        passwordInput = (TextInputEditText) findViewById(R.id.login_password);
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnSignup = (Button) findViewById(R.id.btn_signup_activity);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginAction(v);
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signupAction(v);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuthentication.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuthentication.stop();
    }

    protected void loginAction(View v) {
        boolean validFields = true;
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        emailLayout.setErrorEnabled(false);
        passwordLayout.setErrorEnabled(false);

        if(!FieldValidator.validateEmail(email)) {
            emailLayout.setError("Ingrese un correo valido");
            emailLayout.setErrorEnabled(true);
            validFields = false;
        }

        if(!FieldValidator.validatePassword(password)) {
            passwordLayout.setError("La contraseña debe ser de minimo 8 caracteres y maximo 20");
            passwordLayout.setErrorEnabled(true);
            validFields = false;
        }

        if(validFields) {
            firebaseAuthentication.signInWithEmailAndPassword(email, password);
        }
    }

    protected void signupAction(View v) {
        startActivity(signupIntent);
    }
}
