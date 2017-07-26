package io.github.diogenesc.partiuceunes;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "EmailPassword";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void Cadastro(View v) {
        TextView tEmail=(TextView) findViewById(R.id.tEmail);
        TextView tSenha=(TextView) findViewById(R.id.tSenha);

        Log.d(TAG, "createAccount:" + tEmail);

        SharedPreferences sp=getSharedPreferences("Login", 0);
        SharedPreferences.Editor Ed=sp.edit();
        Ed.putString("usr",tEmail.getText().toString());
        Ed.commit();

        if(!tEmail.getText().toString().isEmpty() && !tSenha.getText().toString().isEmpty()) {
            ProgressDialog.show(this, "Carregando", "Espere um momento...");
            mAuth.createUserWithEmailAndPassword(tEmail.getText().toString(), tSenha.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.

                            if (task.isSuccessful()) {
                                Log.w(TAG, "signInWithEmail:successs", task.getException());
                                //Toast.makeText(RegisterActivity.this, R.string.sucesso_cadastro,
                                //Toast.LENGTH_LONG).show();
                                enviaEmailConfirmacao();
                                alertaConfirmacao();
                            } else {
                                Log.w(TAG, "signInWithEmail:failed", task.getException());
                                Toast.makeText(RegisterActivity.this, R.string.falha_cadastro,
                                        Toast.LENGTH_LONG).show();
                            }
                            // ...
                        }
                    });
        }
        else{
            Toast.makeText(RegisterActivity.this, R.string.falha_cadastro,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,LoginActivity.class));
    }

    public void enviaEmailConfirmacao(){
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        if (task.isSuccessful()) {
                            Log.e(TAG, "sendEmailVerification:success", task.getException());
                        } else {
                            Log.e(TAG, "sendEmailVerification:failure", task.getException());
                        }
                    }
                });
    }

    public void alertaConfirmacao(){
        new AlertDialog.Builder(this).setTitle(R.string.titulo_alerta_cadastro).
                setMessage(R.string.sucesso_cadastro).setPositiveButton("Ok",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
                onBackPressed();
            }
        }).show();
    }
}
