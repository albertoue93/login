package com.example.registration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    TextView tieneCuenta;
    Button btnRegistrar;
    EditText txtInputUsername, txtInputEmail, txtInputPassword, txtInputConfirmPassword;
    private ProgressDialog mProgressBar;
    FirebaseAuth mAuth;
    FirebaseFirestore mStore;
    CheckBox isBarber, isClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        txtInputUsername = findViewById(R.id.inputUsername);
        txtInputEmail = findViewById(R.id.inputEmail);
        txtInputPassword = findViewById(R.id.inputPassword);
        txtInputConfirmPassword = findViewById(R.id.inputConfirmPassword);

        isBarber = findViewById(R.id.checkBox2);
        isClient = findViewById(R.id.checkBox);

        isBarber.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (compoundButton.isChecked()){
                isClient.setChecked(false);
            }
        });
        isClient.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (compoundButton.isChecked()){
                isBarber.setChecked(false);
            }
        });

        btnRegistrar = findViewById(R.id.btnRegister);
        tieneCuenta =findViewById(R.id.alreadyHaveAccount);
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verificarCredenciales();
            }
        });

        tieneCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,loginActivity.class));
            }
        });

        mProgressBar = new ProgressDialog(RegisterActivity.this);

    }

    public void verificarCredenciales(){
        final String username = txtInputUsername.getText().toString();
        final String email = txtInputEmail.getText().toString();
        String password = txtInputPassword.getText().toString();
        String confirmPass = txtInputConfirmPassword.getText().toString();

        if(username.isEmpty() || username.length() < 5){
            showError(txtInputUsername,"Username no valido");
        }else if (email.isEmpty() || !email.contains("@")){
            showError(txtInputEmail,"Email no valido");
        }else if (password.isEmpty() || password.length() < 7){
            showError(txtInputPassword,"Clave no valida minimo 7 caracteres");
        }else if (confirmPass.isEmpty() || !confirmPass.equals(password)){
            showError(txtInputConfirmPassword,"Clave no valida, no coincide.");
        }else{
            //Mostrar ProgressBar
            mProgressBar.setTitle("Proceso de Registro");
            mProgressBar.setMessage("Registrando usuario, espere un momento");
            mProgressBar.setCanceledOnTouchOutside(false);
            mProgressBar.show();
            //Registrar usuario
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();
                        String userID = user.getUid();
                        DocumentReference df = mStore.collection("Users").document(userID);
                        Map<String, Object> datauser = new HashMap<>();

                        datauser.put("username",username);
                        datauser.put("email",email);

                        datauser.put("isAdmin",1);

                        /*if (isBarber.isChecked()){
                            datauser.put("isAdmin",1);
                        }
                        if (isClient.isChecked()){
                            datauser.put("isUser",1);
                        }*/

                        df.set(datauser).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("Registro","Data de user creado");

                                //Exitoso -> Mostrar toast
                                mProgressBar.dismiss();
                                //redireccionar - intent a login

                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                RegisterActivity.this.finish();
                                Toast.makeText(getApplicationContext(),"Registrado Correctamente", Toast.LENGTH_LONG).show();


                                //ocultar progressBar
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("Registro","Error al crear documento");
                            }
                        });

                    }else{
                        Toast.makeText(getApplicationContext(),"Nose pudo registrar", Toast.LENGTH_LONG).show();
                    }
                }
            });

        }

    }

    private void showError(EditText input, String s){
        input.setError(s);
        input.requestFocus();
    }
}
