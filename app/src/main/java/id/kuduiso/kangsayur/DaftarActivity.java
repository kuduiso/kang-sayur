package id.kuduiso.kangsayur;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import id.kuduiso.kangsayur.helper.HelperDaftar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DaftarActivity extends AppCompatActivity {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=\\S+$)" +           //no white spaces
                    ".{8,}" +               //at least 8 characters
                    "$");

    private static final Pattern FULLNAME_PATTERN =
            Pattern.compile("^" +
                    "[A-Za-z.'\\s]+" +      //any letter
                    "$");

    private static final Pattern PHONENUMB_PATTERN =
            Pattern.compile("^" +
                    "([0-9]*)" +      //any letter
                    "$");
    private TextInputLayout inpLayEmail;
    private TextInputLayout inpLayPasswd;
    private TextInputLayout inpLayKonfPasswd;
    private TextInputLayout inpLayPhone;
    private TextInputLayout inpLayName;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private TextView tvValidRadio;
    private Button buttonDaftar;
    //    Firebase variabel
    private FirebaseDatabase firDa;
    private DatabaseReference daRef;
    //    Nama Constant Intent Data
    private static final String DATA_DAFTAR = "dataDaftar";
    private static final String ASAL = "asal";
    private static final String ACTIVITY_INI = "daftarActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar);
        // hide action bar
        getSupportActionBar().hide();

        // variabel-variabel
        inpLayEmail = findViewById(R.id.etEmail);
        inpLayPasswd = findViewById(R.id.etPassword);
        inpLayPasswd.setErrorIconDrawable(0);
        inpLayKonfPasswd = findViewById(R.id.etKonfPasswd);
        inpLayPhone = findViewById(R.id.noHP);
        inpLayName = findViewById(R.id.etNama);
        radioGroup = findViewById(R.id.rgPeran);
        tvValidRadio = findViewById(R.id.validRadio);
        buttonDaftar = findViewById(R.id.btnDaftar);

        buttonDaftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmInput(v);
            }
        });
    }

    private boolean validateEmail() {
        String email = inpLayEmail.getEditText().getText().toString().trim();

        if (email.isEmpty()) {
            inpLayEmail.setError("Harus diisi");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inpLayEmail.setError("E-mail tidak valid");
            return false;
        } else {
            inpLayEmail.setError(null);
            return true;
        }
    }

    private boolean validateName() {
        String nama = inpLayName.getEditText().getText().toString().trim();

        if (nama.isEmpty()) {
            inpLayName.setError("Harus diisi");
            return false;
        } else if (!FULLNAME_PATTERN.matcher(nama).matches()) {
            inpLayName.setError("Nama tidak valid");
            return false;
        } else {
            inpLayName.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String passwd = inpLayPasswd.getEditText().getText().toString().trim();

        if (passwd.length() < 8) {
            inpLayPasswd.setError("Minimal 8 karakter");
            return false;
        } else if (!PASSWORD_PATTERN.matcher(passwd).matches()) {
            inpLayPasswd.setError("Gunakan huruf besar, kecil dan angka tanpa spasi!");
            return false;
        } else {
            inpLayPasswd.setError(null);
            return true;
        }
    }

    private boolean validateKonfPasswd() {
        String passwd = inpLayPasswd.getEditText().getText().toString().trim();
        String konfPasswd = inpLayKonfPasswd.getEditText().getText().toString().trim();

        if (!konfPasswd.equals(passwd)) {
            inpLayKonfPasswd.setError("Password tidak sesuai");
            return false;
        } else {
            inpLayKonfPasswd.setError(null);
            return true;
        }
    }

    private boolean validatePhone() {
        String phoneNumb = inpLayPhone.getEditText().getText().toString().trim();
        if (phoneNumb.isEmpty()) {
            inpLayPhone.setError("Harus diisi");
            return false;
        } else if (!PHONENUMB_PATTERN.matcher(phoneNumb).matches()) {
            inpLayPhone.setError("Nomor HP tidak valid");
            return false;
        } else {
            inpLayPhone.setError(null);
            return true;
        }

    }

    private boolean validateRadio() {
        int radioID = radioGroup.getCheckedRadioButtonId();
        if (radioID == -1) {
            tvValidRadio.setText("Pilih salah satu");
            tvValidRadio.setVisibility(View.VISIBLE);
            radioGroup.setBackground(getDrawable(R.drawable.outline_box));
            return false;
        } else {
            tvValidRadio.setVisibility(View.GONE);
            radioGroup.setBackground(null);
            return true;
        }
    }

    //    -------------------------- BAGIAN PENDAFTARAN -------------
    public void confirmInput(View v) {
        if (!validateEmail() | !validatePassword() | !validateKonfPasswd() | !validatePhone() | !validateName() | !validateRadio()) {
            return;
        } else {
            // Ambil Semua Data Register
            int radioID = radioGroup.getCheckedRadioButtonId();
            radioButton = findViewById(radioID);
            String nama = inpLayName.getEditText().getText().toString().trim();
            String email = inpLayEmail.getEditText().getText().toString().trim();
            String passwd = inpLayPasswd.getEditText().getText().toString().trim();
            String phoneNumb = "+62" + inpLayPhone.getEditText().getText().toString().trim();
            String textRadio = radioButton.getText().toString();

            // Tampung di helperDaftar
            HelperDaftar helperDaftar = new HelperDaftar(nama, email, passwd, textRadio, phoneNumb);

            // Membuat ArrayList untuk menampung Email
            List<String> dtEmail = new ArrayList<>();

            // CEK DATA BELUM TERDAFTAR
            firDa = FirebaseDatabase.getInstance();
            daRef = firDa.getReference().child("pengguna");
            daRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    // SIMPAN DATA EMAIL KE ARRAYLIST
                    for (DataSnapshot dataSnapshot : snapshot.child("penjual").getChildren()) {
                        String nwEmail = dataSnapshot.child("email").getValue().toString();
                        dtEmail.add(nwEmail);
                    }

                    for (DataSnapshot dataSnapshot : snapshot.child("pembeli").getChildren()) {
                        String nwEmail = dataSnapshot.child("email").getValue().toString();
                        dtEmail.add(nwEmail);
                    }

                    // CEK EMAIL DAN NOMOR HP
                    if (dtEmail.contains(email) & (snapshot.child("penjual/" + phoneNumb).exists() | snapshot.child("pembeli/" + phoneNumb).exists())) {
                        inpLayEmail.setError("Email sudah terdaftar");
                        inpLayPhone.setError("Nomor HP sudah terdaftar");
                    } else if (dtEmail.contains(email)) {
                        inpLayPhone.setError(null);
                        inpLayEmail.setError("Email sudah terdaftar");
                    } else if (snapshot.child("penjual/" + phoneNumb).exists() | snapshot.child("pembeli/" + phoneNumb).exists()) {
                        inpLayEmail.setError(null);
                        inpLayPhone.setError("Nomor HP sudah terdaftar");
                    } else {
                        inpLayEmail.setError(null);
                        inpLayPhone.setError(null);
                        // ---------- Pindah Intent ---------------
                        Intent intent = new Intent(DaftarActivity.this, PhoneVerifActivity.class);
                        intent.putExtra(DATA_DAFTAR, helperDaftar);
                        intent.putExtra(ASAL, ACTIVITY_INI);
                        startActivity(intent);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}