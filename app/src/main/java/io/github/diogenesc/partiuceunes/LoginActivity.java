package io.github.diogenesc.partiuceunes;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import org.w3c.dom.Text;

import static io.github.diogenesc.partiuceunes.R.id.Login;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "EmailPassword";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    if(user.isEmailVerified()) chamaMain();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        SharedPreferences sp1=this.getSharedPreferences("Login",0);

        String usr=sp1.getString("usr", null);
        TextView user=(TextView) findViewById(R.id.tEmail);
        user.setText(usr);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void Login(View v) {
        TextView tEmail=(TextView) findViewById(R.id.tEmail);
        TextView tSenha=(TextView) findViewById(R.id.tSenha);


        SharedPreferences sp=getSharedPreferences("Login", 0);
        SharedPreferences.Editor Ed=sp.edit();
        Ed.putString("usr",tEmail.getText().toString());
        Ed.commit();

        View view = this.getCurrentFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        Log.d(TAG, "createAccount:" + tEmail);

        if(!tEmail.getText().toString().trim().isEmpty() && !tSenha.getText().toString().trim().isEmpty()) {
            showLoading();
            mAuth.signInWithEmailAndPassword(tEmail.getText().toString().trim(), tSenha.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                            if (task.isSuccessful()) {
                                Log.w(TAG, "signInWithEmail:successs", task.getException());

                                if (user.isEmailVerified()) {
                                    Toast.makeText(LoginActivity.this, R.string.sucesso_login,
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    hideLoading();
                                    alertaNaoVerificado();
                                }
                            } else {
                                hideLoading();
                                Log.w(TAG, "signInWithEmail:failed", task.getException());
                                Toast.makeText(LoginActivity.this, R.string.falha_login,
                                        Toast.LENGTH_SHORT).show();
                            }
                            // ...
                        }
                    });
        }
        else{
            hideLoading();
            Toast.makeText(LoginActivity.this, R.string.falha_login,
                    Toast.LENGTH_SHORT).show();
        }

        tSenha.setText("");
    }

    public void showLoading(){
        loading=ProgressDialog.show(this, "Carregando", "Espere um momento...");
    }

    public void hideLoading(){
        loading.dismiss();
        loading.hide();
    }

    public void alertaNaoVerificado(){
        new AlertDialog.Builder(this).setTitle(R.string.titulo_alerta_cadastro).
                setMessage(R.string.naoverificado_login).setPositiveButton("Ok",null).show();
    }

    public void chamaMain(){
        startActivity(new Intent(this,MainActivity.class));
    }

    public void ChamaCadastro(View v){
        startActivity(new Intent(this,RegisterActivity.class));
    }

    public void Desloga(View v){
        mAuth.signOut();
    }
}
