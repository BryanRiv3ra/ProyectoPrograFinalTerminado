package gt.edu.umg.proyectoprografinal;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import gt.edu.umg.proyectoprografinal.BaseDeDatos.DataBaseHelper;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri imageUri;
    private DataBaseHelper dbHelper;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa el helper de la base de datos
        dbHelper = new DataBaseHelper(this);

        // Inicializa el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Solicitar permisos de cámara y ubicación
        solicitarPermisos();
    }
    public String getRealPathFromURI(Context context, Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        return null;
    }
    private void solicitarPermisos() {
        // Verifica permisos de cámara
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 2000);
        }

        // Verifica permisos de ubicación
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Si ya tienes permiso, puedes obtener la ubicación
            obtenerUbicacion();
        }

        // Configura el botón de la cámara después de los permisos
        Button ButtonCamara = findViewById(R.id.buttonCamara);
        ButtonCamara.setOnClickListener(v -> abrirCamara());
    }

    private void abrirCamara() {
        // Crear un ContentValues para almacenar los detalles de la imagen
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Nueva Imagen");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Desde la Cámara");

        // Insertar la información y obtener la URI para guardar la imagen
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        // Crear el Intent de la cámara
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);  // Enviar la URI para guardar la imagen
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);  // Iniciar la cámara
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // La imagen ha sido capturada y guardada en la URI que proporcionaste
            Toast.makeText(this, "Imagen capturada y guardada en: " + imageUri.toString(), Toast.LENGTH_LONG).show();

            // Obtiene la ubicación GPS y guarda los datos
            getLastLocationAndSavePhoto(imageUri.toString());

        } else if (resultCode == RESULT_CANCELED) {
            // El usuario canceló la captura de la imagen
            Toast.makeText(this, "Captura de imagen cancelada", Toast.LENGTH_SHORT).show();
        }
    }

    private void getLastLocationAndSavePhoto(String imagePath) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Solicita los permisos si no están concedidos
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                // Guarda la ruta de la imagen y la ubicación en la base de datos
                dbHelper.insertPhotoData(imagePath, latitude, longitude);

                Toast.makeText(this, "Datos guardados en la base de datos", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 2000: // Permiso de cámara
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    abrirCamara(); // Abre la cámara si se concede el permiso
                } else {
                    Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
                }
                break;
            case LOCATION_PERMISSION_REQUEST_CODE: // Permiso de ubicación
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    obtenerUbicacion(); // Obtén ubicación si se concede el permiso
                } else {
                    Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void obtenerUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        // Verifica si la ubicación es null
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            // Guarda la ubicación en la base de datos
                            guardarUbicacion(latitude, longitude);
                        }
                    });
        }
    }

    private void guardarUbicacion(double latitude, double longitude) {
        // Crear un objeto ContentValues para almacenar los datos
        ContentValues values = new ContentValues();
        values.put("latitude", latitude);
        values.put("longitude", longitude);

        // Inserta los datos en la base de datos
        long newRowId = dbHelper.insertLocation(values);
        if (newRowId != -1) {
            Toast.makeText(this, "Ubicación guardada: " + latitude + ", " + longitude, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error al guardar la ubicación", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();  // Cierra la base de datos aquí
    }
}
