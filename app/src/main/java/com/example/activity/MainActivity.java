package com.example.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    // Deklaracija UI elemenata
    private TextView statusSkeniranja;
    private Button btnUcitajBazu;
    private Button btnUnistavanje;
    private Button btnZavrsi;
    private Button btnKreirajIzvestaj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // Ovo je tvoj XML fajl

        // Inicijalizacija UI elemenata
        statusSkeniranja = findViewById(R.id.status_skeniranja);
        btnUcitajBazu = findViewById(R.id.btn_ucitaj_bazu);
        btnUnistavanje =findViewById(R.id.btn_unistavanje);
        btnZavrsi = findViewById(R.id.btn_zavrsi);
        btnKreirajIzvestaj = findViewById(R.id.btn_kreiraj_izvestaj);

        // Postavljanje početnog statusa
        statusSkeniranja.setText("Status: Nema očitavanja");

        // Klik događaj za "Učitaj Bazu"
        btnUcitajBazu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ovdje možeš dodati logiku za učitavanje baze
                statusSkeniranja.setText("Status: Učitavanje baze...");
                Toast.makeText(MainActivity.this, "Baza učitana", Toast.LENGTH_SHORT).show();
            }
        });

        btnUnistavanje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ovdje možeš dodati logiku za uništavanje
                statusSkeniranja.setText("Status: Uništavanje u toku...");
                Toast.makeText(MainActivity.this, "Proces uništavanja pokrenut", Toast.LENGTH_SHORT).show();
            }
        });

        btnZavrsi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ovdje možeš dodati logiku za završavanje procesa
                statusSkeniranja.setText("Status: Proces završen");
                Toast.makeText(MainActivity.this, "Proces završen", Toast.LENGTH_SHORT).show();
            }
        });
        btnKreirajIzvestaj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusSkeniranja.setText("Status: Kreiranje izveštaja...");
                Toast.makeText(MainActivity.this, "Izveštaj kreiran", Toast.LENGTH_SHORT).show();
            }
        });
    }
}