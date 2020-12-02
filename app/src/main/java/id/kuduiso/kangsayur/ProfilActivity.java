package id.kuduiso.kangsayur;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import id.kuduiso.kangsayur.helper.HelperDaftar;
import id.kuduiso.kangsayur.helper.HelperPenjual;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageView;

public class ProfilActivity extends AppCompatActivity {

    private ImageView imgProfil;
    private Button btnFotoProfil;
    private Button btnVerifEmail;
    private Button btnPinLokasi;
    private Button btnToko;
    private Button btnUbah;
    private Button btnSimpan;
    private Button btnKeluar;
    private TextInputLayout inpLayNama;
    private TextInputLayout inpLayPassword;
    private TextInputLayout inpLayEmail;
    private TextInputLayout inpLayNoHP;
    private TextInputLayout inpLayPeran;
    private TextInputLayout inpLayAlamat;
    private TextInputLayout inpLayStaToko;
//---- FIREBASE VARIABEL
    private FirebaseDatabase firDa;
    private DatabaseReference daRef;
    private StorageReference stoRef;
    private FirebaseAuth mAuth;
//---- STATE-STATE
    private static final int PICK_IMAGE_REQUEST = 1;
//---- VARIABEL-VARIABEL
    private Uri imageUri;
    private String email;
    private String passwd;
    private String noHP;
//---- Nama Constant Intent Data
    private static final String DATA_DAFTAR = "dataDaftar";
    private static final String ID_DAFTAR = "idDaftar";
    private static final String PERAN = "peran";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);
        getSupportActionBar().hide();

        // INISIASI VARIABEL
        imgProfil = findViewById(R.id.imgProfil);
        btnFotoProfil = findViewById(R.id.upFoPro);
        btnVerifEmail = findViewById(R.id.btnEmail);
        btnPinLokasi = findViewById(R.id.btnPinLokasi);
        btnToko = findViewById(R.id.btnToko);
        btnUbah = findViewById(R.id.btnUbah);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnKeluar = findViewById(R.id.btnKeluar);
        inpLayNama = findViewById(R.id.etNama);
        inpLayEmail = findViewById(R.id.etEmail);
        inpLayPassword = findViewById(R.id.etPassword);
        inpLayPassword.setErrorIconDrawable(0);
        inpLayNoHP = findViewById(R.id.etNoHP);
        inpLayPeran = findViewById(R.id.etPeran);
        inpLayAlamat = findViewById(R.id.etAlamat);
        inpLayStaToko = findViewById(R.id.etStatusToko);
        mAuth = FirebaseAuth.getInstance();

        //--- FUNGSI-FUNGSI AWAL
        cekEmailVerif();
        ambilData();

        // AKSI BUTTON
        btnFotoProfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bukaFileGambar();
            }
        });

        btnVerifEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailV = inpLayEmail.getEditText().getText().toString().trim();
                String passwdV = inpLayPassword.getEditText().getText().toString().trim();
                sendVerificationLink(emailV, passwdV);
            }
        });

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // DAPATKAN SEMUA DATA DAN SIMPAN DI FIREBASE
            }
        });

        btnUbah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // UBAH DATA
                updateUI(3);
            }
        });
    }

// ------ PROSES PILIH GAMBAR PROFIL
    private void bukaFileGambar() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
            && data != null && data.getData() != null) {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            Glide.with(this).load(imageUri).into(imgProfil);
        }

    }
// ------ CEK EMAIL APAKAH SUDAH TERVERIFIKASI
    private void cekEmailVerif() {
        if (mAuth.getCurrentUser().isEmailVerified()) {
            updateUI(1);
        } else {
            updateUI(2);
        }
    }

// --------------VERIFIKASI EMAIL
private void sendVerificationLink(String email, String passwd) {
    mAuth.createUserWithEmailAndPassword(email, passwd)
            .addOnCompleteListener(ProfilActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        mAuth.getCurrentUser().sendEmailVerification()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(ProfilActivity.this, "Silahkan cek e-mail", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(ProfilActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(ProfilActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
}

// ------- AMBIL DATA DARI INTENT PHONE-VERIF-ACTIVITY
    private void ambilData() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getStringExtra(PERAN).equals("penjual")) {
                HelperPenjual helperPenjual = intent.getParcelableExtra(DATA_DAFTAR);
                email = helperPenjual.getEmail();
                passwd = helperPenjual.getPassword();
                noHP = helperPenjual.getNoHP();
                String nama = helperPenjual.getNama();
                String peran = helperPenjual.getPeran();
                String status = helperPenjual.getStatusToko();
                //--MENAMPILKAN DATA DAFTAR-PENJUAL
                inpLayNama.getEditText().setText(nama);
                inpLayEmail.getEditText().setText(email);
                inpLayPassword.getEditText().setText(passwd);
                inpLayNoHP.getEditText().setText(noHP);
                inpLayPeran.getEditText().setText(peran);
                inpLayStaToko.getEditText().setText(status);
            } else {
                HelperDaftar helperDaftar = intent.getParcelableExtra(DATA_DAFTAR);
                email = helperDaftar.getEmail();
                passwd = helperDaftar.getPassword();
                noHP = helperDaftar.getNoHP();
                String nama = helperDaftar.getNama();
                String peran = helperDaftar.getPeran();
                //--MENAMPILKAN DATA DAFTAR
                inpLayNama.getEditText().setText(nama);
                inpLayEmail.getEditText().setText(email);
                inpLayPassword.getEditText().setText(passwd);
                inpLayNoHP.getEditText().setText(noHP);
                inpLayPeran.getEditText().setText(peran);
            }
        }
    }

// ------- MENYIMPAN DATA BARU KE FIREBASE
    private void simpanBaru() {
        String nwEmail = inpLayEmail.getEditText().getText().toString().trim();
        String nwPasswd = inpLayPassword.getEditText().getText().toString().trim();
        String nwNoHP = inpLayNoHP.getEditText().getText().toString().trim();
        String nama = inpLayNama.getEditText().getText().toString().trim();
        String peran = inpLayPeran.getEditText().getText().toString().trim();
        String status = inpLayStaToko.getEditText().getText().toString().trim();
        String alamat = inpLayAlamat.getEditText().getText().toString().trim();
        if (peran.equals("Penjual")){
            // SIMPAN DATA PENJUAL
            firDa = FirebaseDatabase.getInstance();
            daRef = firDa.getReference().child("penjual");
            // AKSI UBAH PENJUAL
            if (nwNoHP.equals(noHP)) {
                // HelperPenjual helperPenjual = new HelperPenjual(nama, nwEmail, nwPasswd, peran, nwNoHP, status, alamat, );
            } else {
                // VERIFIKASI NO-HP DULU

            }
        } else {
            // SIMPAN DATA PEMBELI
        }
    }

// ------ UPDATE UI
    private void updateUI(int kode) {
        switch (kode) {
            case 1:
                btnVerifEmail.setVisibility(View.GONE);
                break;
            case 2:
                btnVerifEmail.setVisibility(View.VISIBLE);
                break;
            case 3:
                // -- STATE UBAH
                inpLayNama.setEnabled(true);
                inpLayEmail.setEnabled(true);
                inpLayPassword.setEnabled(true);
                inpLayNoHP.setEnabled(true);
                inpLayAlamat.setEnabled(true);
                break;
        }
    }

}