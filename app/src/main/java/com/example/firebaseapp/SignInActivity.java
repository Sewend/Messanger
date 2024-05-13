package com.example.firebaseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";
    private FirebaseAuth auth;

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText repeatPasswordEditText;

    private EditText nameEditText;
    private TextView toggleLoginSignUpTextView;
    private Button loginSignUpButton;

    private boolean loginModeActive;

    private FirebaseDatabase database;
    private DatabaseReference usersDatabaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        auth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();
        usersDatabaseReference = database.getReference().child("users");

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        repeatPasswordEditText = findViewById(R.id.repeatPasswordEditText);
        nameEditText = findViewById(R.id.nameEditText);
        toggleLoginSignUpTextView = findViewById(R.id.toggleLoginSignUpTextView);
        loginSignUpButton = findViewById(R.id.loginSignUpButton);

        loginSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginSignUpUser(emailEditText.getText().toString().trim(), passwordEditText.getText().toString().trim());
            }
        });

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(SignInActivity.this, UserListActivity.class));
        }
    }

    private void loginSignUpUser(String email, String password) {

        if (loginModeActive) {
            if (passwordEditText.getText().toString().trim().length() < 7) {
                Toast.makeText(this, "Пароль должен быть больше 7 символов", Toast.LENGTH_SHORT).show();
            } else if (emailEditText.getText().toString().trim().equals("")) {
                Toast.makeText(this, "Пожалуйста, введите почту!", Toast.LENGTH_SHORT).show();
            } else {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = auth.getCurrentUser();
                            Intent intent = new Intent(SignInActivity.this, UserListActivity.class);
                            intent.putExtra("userName", nameEditText.getText().toString().trim());
                            startActivity(intent);
                            //updateUI(null);
                        }else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
            }
        } else {
            if (!passwordEditText.getText().toString().trim().equals(
                    repeatPasswordEditText.getText().toString().trim()
            )) {
                Toast.makeText(this, "Пароль не совпадает!", Toast.LENGTH_SHORT).show();
            } else if (passwordEditText.getText().toString().trim().length() < 7) {
                Toast.makeText(this, "Пароль должен быть больше 7 символов", Toast.LENGTH_SHORT).show();
            } else if (emailEditText.getText().toString().trim().equals("")) {
                Toast.makeText(this, "Пожалуйста, введите почту!", Toast.LENGTH_SHORT).show();
            }
            else {
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = auth.getCurrentUser();
                                    createUser(user);
                                    //updateUI(null);
                                    Intent intent = new Intent(SignInActivity.this, UserListActivity.class);
                                    intent.putExtra("userName", nameEditText.getText().toString().trim());
                                    startActivity(intent);
                                } else {
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(SignInActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                    //updateUI(null);
                                }
                            }
                        });
            }
        }
    }

    private void createUser(FirebaseUser firebaseUser) {
        User user = new User();
        user.setId(firebaseUser.getUid());
        user.setEmail(firebaseUser.getEmail());
        user.setName(nameEditText.getText().toString().trim());
        usersDatabaseReference.push().setValue(user);
    }

    public void toggleLoginMode(View view) {

        if (loginModeActive) {
            loginModeActive = false;
            loginSignUpButton.setText("Зарегистрироваться");
            toggleLoginSignUpTextView.setText("Или войти в систему");
            repeatPasswordEditText.setVisibility(View.VISIBLE);
        } else {
            loginModeActive = true;
            loginSignUpButton.setText("Войти");
            toggleLoginSignUpTextView.setText("Или зарегистрироваться");
            repeatPasswordEditText.setVisibility(View.GONE);
        }
    }
}