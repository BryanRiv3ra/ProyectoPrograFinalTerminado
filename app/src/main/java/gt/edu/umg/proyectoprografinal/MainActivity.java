package gt.edu.umg.proyectoprografinal;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import gt.edu.umg.proyectoprografinal.BaseDeDatos.DatabaseHelper;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    private Uri photoUri;
    private DatabaseHelper dbHelper;
    private FusedLocationProviderClient fusedLocationClient;
    private EditText descriptionEditText;
    private Spinner typeSpinner, severitySpinner;
    private double currentLatitude, currentLongitude;
    private String currentAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initializeViews();
        setupSpinners();
        checkPermissions();
    }

    private void initializeViews() {
        descriptionEditText = findViewById(R.id.descriptionEditText);
        typeSpinner = findViewById(R.id.typeSpinner);
        severitySpinner = findViewById(R.id.severitySpinner);

        Button captureButton = findViewById(R.id.captureButton);
        captureButton.setOnClickListener(v -> capturePhoto());

        Button submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(v -> submitIncident());

        Button viewReportsButton = findViewById(R.id.viewReportsButton);
        viewReportsButton.setOnClickListener(v -> startActivity(new Intent(this, ReportsActivity.class)));

        Button testButton = findViewById(R.id.testLocationButton);
        testButton.setOnClickListener(v -> verificarUbicacion());
    }

    private void setupSpinners() {
        // Tipos de incidentes
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.incident_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        // Niveles de severidad
        ArrayAdapter<CharSequence> severityAdapter = ArrayAdapter.createFromResource(this,
                R.array.severity_levels, android.R.layout.simple_spinner_item);
        severityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        severitySpinner.setAdapter(severityAdapter);
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
                updateAddressFromLocation();
            }
        });
    }

    private void updateAddressFromLocation() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1);
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                currentAddress = address.getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void capturePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this,
                        "gt.edu.umg.proyectoprografinal.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "INCIDENT_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        try {
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void clearForm() {
        descriptionEditText.setText("");
        typeSpinner.setSelection(0);
        severitySpinner.setSelection(0);
        photoUri = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Toast.makeText(this, "Foto capturada exitosamente", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }

    private void verificarUbicacion() {
        // Verificar último registro
        Location lastLocation = dbHelper.getLastSavedLocation();
        if (lastLocation != null) {
            String mensaje = String.format(Locale.getDefault(),
                    "Última ubicación guardada:\nLatitud: %.6f\nLongitud: %.6f",
                    lastLocation.getLatitude(),
                    lastLocation.getLongitude());
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        }

        // Verificar todos los registros
        List<Map<String, Object>> incidents = dbHelper.getAllIncidentsWithLocation();
        Log.d("UbicacionVerificacion", "Total de incidentes con ubicación: " + incidents.size());

        for (Map<String, Object> incident : incidents) {
            Log.d("UbicacionVerificacion", String.format(Locale.getDefault(),
                    "ID: %d, Tipo: %s, Lat: %.6f, Lon: %.6f, Fecha: %s",
                    (Long) incident.get("id"),
                    (String) incident.get("type"),
                    (Double) incident.get("latitude"),
                    (Double) incident.get("longitude"),
                    (String) incident.get("timestamp")));
        }
    }

    private void submitIncident() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                String type = typeSpinner.getSelectedItem().toString();
                String description = descriptionEditText.getText().toString();
                String severity = severitySpinner.getSelectedItem().toString();

                if (description.isEmpty() || photoUri == null) {
                    Toast.makeText(this, "Por favor complete todos los campos y tome una foto",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Guardar con ubicación
                long result = dbHelper.insertIncident(
                        type,
                        description,
                        severity,
                        photoUri.toString(),
                        location.getLatitude(),
                        location.getLongitude(),
                        currentAddress
                );

                if (result != -1) {
                    String mensaje = String.format(Locale.getDefault(),
                            "Incidente reportado exitosamente\nLatitud: %.6f\nLongitud: %.6f",
                            location.getLatitude(),
                            location.getLongitude());
                    Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();

                    // Verificar el guardado
                    verificarUbicacion();

                    clearForm();
                } else {
                    Toast.makeText(this, "Error al guardar el incidente",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación actual",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }










}