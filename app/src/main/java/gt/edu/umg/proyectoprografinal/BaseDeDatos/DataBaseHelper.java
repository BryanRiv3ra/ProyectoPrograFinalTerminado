package gt.edu.umg.proyectoprografinal.BaseDeDatos;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "photos.db";
    private static final int DATABASE_VERSION = 1;

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Registro de depuración
        Log.d("DataBaseHelper", "Creando la base de datos");

        String CREATE_TABLE_PHOTOS = "CREATE TABLE photos (id INTEGER PRIMARY KEY AUTOINCREMENT, image_path TEXT, latitude REAL, longitude REAL)";
        db.execSQL(CREATE_TABLE_PHOTOS);

        String CREATE_TABLE_LOCATIONS = "CREATE TABLE locations (id INTEGER PRIMARY KEY AUTOINCREMENT, latitude REAL, longitude REAL)";
        db.execSQL(CREATE_TABLE_LOCATIONS);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS photos");
        db.execSQL("DROP TABLE IF EXISTS locations");
        onCreate(db);
    }

    public long insertPhotoData(String imagePath, double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("image_path", imagePath);
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        long id = db.insert("photos", null, values);
        // db.close(); // Elimina esta línea
        return id;
    }

    public long insertLocation(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = db.insert("locations", null, values);
        // db.close(); // Elimina esta línea
        return id;
    }
}
