package gt.edu.umg.proyectoprografinal.BaseDeDatos;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "photos.db";
    private static final int DATABASE_VERSION = 1;

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE photos (id INTEGER PRIMARY KEY, image_path TEXT, latitude REAL, longitude REAL)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void insertPhotoData(String imagePath, double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("image_path", imagePath);
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        db.insert("photos", null, values);
        db.close();

    }
    public long insertLocation(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase(); // Aquí es donde se utiliza getWritableDatabase
        return db.insert("locations", null, values);
    }
}
