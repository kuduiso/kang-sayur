package id.kuduiso.kangsayur;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import id.kuduiso.kangsayur.helper.HelperDaftar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MasukActivity extends AppCompatActivity {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=\\S+$)" +           //no white spaces
                    ".{8,}" +               //at least 8 characters
                    "$");

    private TextInputLayout inpLayEmail;
    private TextInputLayout inpLayPasswd;
    private Button btnMasuk;
    private LinearLayout lyVerif;
    private TextView tvLinkVerif;
//    FIREBASE LIBRARY
    private FirebaseDatabase firDa;
    private DatabaseReference daRef;
    private FirebaseAuth mAuth;
//    VARIABEL-VARIABEL GLOBAL
    private  String email;
    private String passwd;
//    KONSTANTA
    private static final String TAG = "MasukActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_masuk);
        getSupportActionBar().hide();

        // Variabel -variabel
        btnMasuk = findViewById(R.id.btnMasuk);
        inpLayEmail = findViewById(R.id.etEmail);
        inpLayPasswd = findViewById(R.id.etPassword);
        inpLayPasswd.setErrorIconDrawable(0);
        lyVerif = findViewById(R.id.lyVerif);
        tvLinkVerif = findViewById(R.id.tvLinkVerif);

        // INISIASI FIREBASE AUTH
        mAuth = FirebaseAuth.getInstance();

        // Button Masuk
        btnMasuk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmInput(v);
            }
        });

        // AKSI KIRIM LINK VERIFIKASI
        tvLinkVerif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // LAKUKAN AKSI
                sendVerificationLink(email, passwd);
            }
        });


    }

    private boolean validateEmail() {
        email = inpLayEmail.getEditText().getText().toString().trim();

        if (email.isEmpty()) {
            inpLayEmail.setError("Harus diisi");
            return false;
        } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inpLayEmail.setError("E-mail tidak valid");
            return false;
        } else {
            inpLayEmail.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        passwd = inpLayPasswd.getEditText().getText().toString().trim();

        if (passwd.length() < 8) {
            inpLayPasswd.setError("Password tidak valid");
            return false;
        } else if (!PASSWORD_PATTERN.matcher(passwd).matches()) {
            inpLayPasswd.setError("Gunakan huruf besar, kecil dan angka tanpa spasi!");
            return false;
        } else {
            inpLayPasswd.setError(null);
            return true;
        }
    }

    public void confirmInput(View v) {
        if (!validateEmail() | !validatePassword()) {
            return;
        } else {
            // Membuat ArrayList untuk menampung Email
            List<HelperDaftar> dtDaftar = new ArrayList<>();

            // CEK DATA BELUM TERDAFTAR
            firDa = FirebaseDatabase.getInstance();
            daRef = firDa.getReference().child("pengguna");
            daRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    // SIMPAN DATA EMAIL KE ARRAYLIST
                    for (DataSnapshot dataSnapshot : snapshot.child("penjual").getChildren()) {
                        String nwNama = dataSnapshot.child("nama").getValue().toString();
                        String nwEmail = dataSnapshot.child("email").getValue().toString();
                        String nwPasswd = dataSnapshot.child("password").getValue().toString();
                        String nwNoHP = dataSnapshot.child("noHP").getValue().toString();
                        String nwPeran = dataSnapshot.child("peran").getValue().toString();

                        HelperDaftar dataPenjual = new HelperDaftar(nwNama, nwEmail, nwPasswd, nwPeran, nwNoHP);

                        dtDaftar.add(dataPenjual);
                    }

                    for (DataSnapshot dataSnapshot : snapshot.child("pembeli").getChildren()) {
                        String nwNama = dataSnapshot.child("nama").getValue().toString();
                        String nwEmail = dataSnapshot.child("email").getValue().toString();
                        String nwPasswd = dataSnapshot.child("password").getValue().toString();
                        String nwNoHP = dataSnapshot.child("noHP").getValue().toString();
                        String nwPeran = dataSnapshot.child("peran").getValue().toString();

                        HelperDaftar dataPembeli = new HelperDaftar(nwNama, nwEmail, nwPasswd, nwPeran, nwNoHP);

                        dtDaftar.add(dataPembeli);
                    }

                    // CEK EMAIL DAN NOMOR HP
                    boolean cekEmail = false;
                    for (HelperDaftar nwDtDaftar: dtDaftar) {
                        if (nwDtDaftar.getEmail().equals(email)) {
                            cekEmail = true;
                            inpLayEmail.setError(null);
                            if (nwDtDaftar.getPassword().equals(passwd)) {
                                if (nwDtDaftar.getPeran().equals("Pembeli")) {
                                    inpLayPasswd.setError(null);
                                    //Toast.makeText(MasukActivity.this, "Silahkan Masuk Pembeli", Toast.LENGTH_SHORT).show();
                                    signIn(email, passwd);
                                } else {
                                    inpLayPasswd.setError(null);
                                    //Toast.makeText(MasukActivity.this, "Silahkan Masuk Penjual", Toast.LENGTH_SHORT).show();
                                    signIn(email, passwd);
                                }
                            } else {
                                inpLayPasswd.setError("Password salah");
                            }
                            break;
                            // ---------- Pindah Intent ---------------
                        } else {
                            cekEmail = false;
                        }
                    }
                    // JIKA EMAIL BELUM TERDAFTAR
                    if (!cekEmail) {
                        inpLayEmail.setError("Email belum terdaftar");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }


// ---- PEMROSESAN DATA EMAIL UNTUK VERIFIKASI
    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MasukActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                            // ...
                        }

                        // ...
                    }
                });
    }

    private void sendVerificationLink(String email, String passwd) {
        mAuth.createUserWithEmailAndPassword(email, passwd)
                .addOnCompleteListener(MasukActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mAuth.getCurrentUser().sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(MasukActivity.this, "Silahkan cek e-mail", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(MasukActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(MasukActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(MasukActivity.this, "Login Berhasil", Toast.LENGTH_SHORT).show();
            //Intent intent = new Intent(MasukActivity.this, ProfilActivity.class);
            //startActivity(intent);
        } else {
            //Toast.makeText(MasukActivity.this, "Login Gagal", Toast.LENGTH_SHORT).show();
            lyVerif.setVisibility(View.VISIBLE);
        }
    }
}