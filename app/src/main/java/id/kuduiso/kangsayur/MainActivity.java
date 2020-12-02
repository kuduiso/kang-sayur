package id.kuduiso.kangsayur;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        // Variabel - variabel
        Button btnDaftar = findViewById(R.id.btnDaftar);
        Button btnMasuk = findViewById(R.id.btnMasuk);
        mAuth = FirebaseAuth.getInstance();

        //---- CEK SESSION LOGIN USER APAKAH MASIH ADA
        // mAuth.signOut();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Toast.makeText(MainActivity.this, "User: "+user, Toast.LENGTH_SHORT).show();
            //Langsung Masuk ke halaman Penjual atau Pembeli
            //Intent intent = new Intent(MainActivity.this, ProfilActivity.class);
            //startActivity(intent);
        }

        btnDaftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DaftarActivity.class);
                startActivity(intent);
            }
        });

        btnMasuk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MasukActivity.class);
                startActivity(intent);
            }
        });
    }
}