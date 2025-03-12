package com.merlita.diariolab;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class AltaActivity extends AppCompatActivity {
    EditText etNombre, etDescripcion, etCuenta;
    Button bt;
    Intent upIntent;
    Spinner spCategoria, spIdioma, spFormato;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alta);

        etNombre = findViewById(R.id.etNombre);
        etNombre.setHint("Nombre");
        etDescripcion = findViewById(R.id.etDescripcion);
        etDescripcion.setHint("Descripcion");
        etCuenta = findViewById(R.id.etCuenta);
        etCuenta.setHint("Veces repetidas");
        bt = findViewById(R.id.btnGuardar);


    }



    public void clickVolver(View v){

        if(etCuenta.getText().toString()!="" &&
                etNombre.getText().toString()!="" &&
                etDescripcion.getText().toString()!="")
        {
            Intent i = new Intent();

            // Obtengo referencias a todos los campos
            EditText etTitulo = findViewById(R.id.etNombre);
            EditText etAutor = findViewById(R.id.etDescripcion);

            // Valido campos obligatorios (según esquema SQL)
            if (etTitulo.getText().toString().isEmpty() ||
                    etAutor.getText().toString().isEmpty()) {

                Toast.makeText(this, "Complete los campos obligatorios (*)", Toast.LENGTH_SHORT).show();
                /*setResult(RESULT_CANCELED);
                finish();
                return;*/
            }else{
                try {
                    // Convertir fechas a timestamp

                    // Preparar todos los datos para enviar
                    String nombre = etTitulo.getText().toString();
                    String desc = etAutor.getText().toString();
                    int cuenta=0;
                    try{
                        cuenta = Integer.parseInt(etCuenta.getText().toString());
                    }catch (NumberFormatException ignored){

                    }
                    i.putExtra("DESCRIPCION", desc);
                    i.putExtra("NOMBRE", nombre);
                    i.putExtra("CUENTA", cuenta);

                    setResult(RESULT_OK, i);
                } finally {
                    finish();
                }
            }

        }
    }


}
