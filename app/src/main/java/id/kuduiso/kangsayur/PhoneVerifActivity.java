package id.kuduiso.kangsayur;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import id.kuduiso.kangsayur.helper.HelperDaftar;
import id.kuduiso.kangsayur.helper.HelperPenjual;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PhoneVerifActivity extends AppCompatActivity {
    private static final String TAG = "PhoneVerifActivity";
    private TextInputLayout inpLayKode;
    private Button btnKirimUlang;
    private Button btnVerifikasi;
    private LinearLayout linLayout;
    private HelperDaftar dataDaftar;
    private String phoneNumb;
//    Firebase variabel
    private FirebaseDatabase firDa;
    private DatabaseReference daRef;
//    Firebase Verification Phone
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
//    Nama Constant Intent Data
    private static final String DATA_DAFTAR = "dataDaftar";
    private static final String ID_DAFTAR = "idDaftar";
    private static final String ASAL = "asal";
    private static final String PERAN = "peran";
//    Variabel Timer
    private static final long START_TIME_IN_MILIS = 95000;
    private TextView tvCountDown;
    private long timerTersisa = START_TIME_IN_MILIS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verif);
        getSupportActionBar().hide();

        // Variabel-variabel
        inpLayKode = findViewById(R.id.kodeVerif);
        btnKirimUlang = findViewById(R.id.btnKirimUlang);
        btnVerifikasi = findViewById(R.id.btnVerif);
        linLayout = findViewById(R.id.phoneVerifLayout);
        tvCountDown = findViewById(R.id.timerCode);

        // Inisialisasi FIREBASE AUTH
        mAuth = FirebaseAuth.getInstance();
        // END Inisialisasi FIREBASE AUTH

        // Inisialisasi PHONE AUTH CALLBACK;
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted:" + phoneAuthCredential);
                mVerificationInProgress = false;
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                mVerificationInProgress = false;

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(PhoneVerifActivity.this, "Nomor HP Tidak Valid", Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Snackbar.make(linLayout, "Kuota Terlampaui.", Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId, token);
                Log.d(TAG, "onCodeSent:" + verificationId);

                //SIMPAN VERIFICATION ID DAN KIRIM ULANG TOKEN
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };
        // END Inisialisasi PHONE AUTH CALLBACK

        // Menjalankan Verifikasi Dengan Nomor HP
        jalankanVerifikasi();

        //AKSI BUTTON
        startTimer();
        btnKirimUlang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // DISABLE BUTTON SELAMA 1 MENIT
                resetTimer();
                startTimer();
                // KIRIM ULANG KODE
                resendVerificationCode(phoneNumb, mResendToken);
            }
        });

        btnVerifikasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // VERIFIKASI DI SINI
                if (inpLayKode.getEditText().getText().toString().isEmpty()) {
                    inpLayKode.setError("Harus diisi");
                } else {
                    String kode = inpLayKode.getEditText().getText().toString().trim();
                    verifyPhoneNumberWithCode(mVerificationId, kode);
                }
            }
        });
    }

// ----------   TIMER ---------------
    private void startTimer() {
        new CountDownTimer(timerTersisa, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerTersisa = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                btnKirimUlang.setEnabled(true);
            }
        }.start();

        btnKirimUlang.setEnabled(false);
    }

    private void resetTimer() {
        timerTersisa = START_TIME_IN_MILIS;
        updateCountDownText();
    }

    private void updateCountDownText() {
        int minutes = (int) (timerTersisa / 1000) / 60;
        int seconds = (int) (timerTersisa / 1000) % 60;

        String timerFormat = String.format(Locale.getDefault(),"%02d:%02d", minutes, seconds);
        tvCountDown.setText(timerFormat);
    }
// ----------   PEMROSESAN DATA DAN VERIFIKASI ---------------
    public void jalankanVerifikasi() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getStringExtra(ASAL).equals("daftarActivity")) {
                HelperDaftar helperDaftar = intent.getParcelableExtra(DATA_DAFTAR);

                // DATA DARI HELPER DAFTAR
                phoneNumb = helperDaftar.getNoHP();
                String nama = helperDaftar.getNama();
                String email = helperDaftar.getEmail();
                String passwd = helperDaftar.getPassword();
                String peran = helperDaftar.getPeran();

                // SIMPAN DATA KE DATA DAFTAR
                dataDaftar = new HelperDaftar(nama, email, passwd, peran, phoneNumb);

                // MULAI VERIFIKASI NOMOR HP
                startPhoneNumberVerification(phoneNumb);
            }
        }
    }

    public void simpanData(FirebaseUser user) {
        // Firebase Database
        firDa = FirebaseDatabase.getInstance();
        daRef = firDa.getReference("pengguna");
        if (user != null) {
            // ------------ SIMPAN DATA PENJUAL
            if (dataDaftar.getPeran().equals("Penjual")) {
                // ----- PINDAHKAN KE HELPER-DATA-PENJUAL
                String nwNama = dataDaftar.getNama();
                String nwEmail = dataDaftar.getEmail();
                String nwPasswd = dataDaftar.getPassword();
                String nwPeran = dataDaftar.getPeran();
                String nwNoHP = dataDaftar.getNoHP();
                String staTok = "buka";
                String alamat = "";
                String pinAlamat = "";
                String imgProfile = "";
                HelperPenjual helperPenjual = new HelperPenjual(nwNama, nwEmail, nwPasswd, nwPeran, nwNoHP, staTok, alamat, pinAlamat, imgProfile);
                daRef.child("penjual").child(phoneNumb).setValue(helperPenjual).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Berhasil Disimpan");
                        // PINDAH INTENT
                        Intent intent = new Intent(PhoneVerifActivity.this, ProfilActivity.class);
                        intent.putExtra(DATA_DAFTAR, helperPenjual);
                        intent.putExtra(PERAN, "penjual");
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PhoneVerifActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Gagal Disimpan", e);
                    }
                });
            } else {
                // ------------ SIMPAN DATA PEMBELI
                daRef.child("pembeli").child(phoneNumb).setValue(dataDaftar).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Berhasil Disimpan");
                        //PINDAH INTENT
                        Intent intent = new Intent(PhoneVerifActivity.this, ProfilActivity.class);
                        intent.putExtra(DATA_DAFTAR, dataDaftar);
                        intent.putExtra(PERAN, "pembeli");
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PhoneVerifActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Gagal Disimpan", e);
                    }
                });
            }
        } else {
            Log.d(TAG, "USER NULL");
        }
    }

// ----------- VERIFIKASI NOMOR HP --------------
    private void startPhoneNumberVerification(String phoneNumber) {
        //--- PhoneAuth
        PhoneAuthOptions options =
           PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallbacks)
            .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
        //--- END PhoneAuth

        mVerificationInProgress = true;
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // ------ verifikasi kode
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // ------ END verifikasi kode
        signInWithPhoneAuthCredential(credential);
    }

// ---- RESEND VERIFIKASI KODE
    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthOptions options =
          PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .setForceResendingToken(token)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

// ---- SIGN IN DENGAN NOMOR HP
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //  SIGN IN SUCCESS
                            Toast.makeText(PhoneVerifActivity.this, "Berhasil", Toast.LENGTH_SHORT).show();

                            FirebaseUser user = task.getResult().getUser();
                            // -- Tambahan
                            inpLayKode.setError(null);
                            simpanData(user);
                            // -- END Tambahan
                        } else {
                            //  SIGN IN FAILURE
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                inpLayKode.setError("Kode Invalid");
                            }
                        }
                    }
                });
    }
}