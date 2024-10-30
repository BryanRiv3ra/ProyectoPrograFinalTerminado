package gt.edu.umg.proyectoprografinal.BaseDeDatos;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "incidentes.db";
    private static final int DATABASE_VERSION = 1;

    // Tabla de incidentes
    private static final String TABLE_INCIDENTS = "incidents";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_SEVERITY = "severity";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_IMAGE_PATH = "image_path";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_INCIDENTS_TABLE = "CREATE TABLE " + TABLE_INCIDENTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TYPE + " TEXT NOT NULL, "
                + COLUMN_DESCRIPTION + " TEXT, "
                + COLUMN_SEVERITY + " TEXT NOT NULL, "
                + COLUMN_STATUS + " TEXT NOT NULL, "
                + COLUMN_IMAGE_PATH + " TEXT, "
                + COLUMN_LATITUDE + " REAL, "
                + COLUMN_LONGITUDE + " REAL, "
                + COLUMN_ADDRESS + " TEXT, "
                + COLUMN_TIMESTAMP + " TEXT"  // Almacenará la fecha y hora en la zona horaria local
                + ")";

        db.execSQL(CREATE_INCIDENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INCIDENTS);
        onCreate(db);
    }

    public long insertIncident(String type, String description, String severity,
                               String imagePath, double latitude, double longitude, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_SEVERITY, severity);
        values.put(COLUMN_STATUS, "PENDIENTE");
        values.put(COLUMN_IMAGE_PATH, imagePath);
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);
        values.put(COLUMN_ADDRESS, address);

        // Obtén la fecha y hora en la zona horaria local
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getDefault());
        String localDateTime = dateFormat.format(new Date());

        values.put(COLUMN_TIMESTAMP, localDateTime);

        return db.insert(TABLE_INCIDENTS, null, values);
    }

    public Cursor getAllIncidents() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_INCIDENTS, null, null, null, null, null,
                COLUMN_TIMESTAMP + " DESC");
    }

    public boolean updateIncidentStatus(long id, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, newStatus);

        return db.update(TABLE_INCIDENTS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}) > 0;
    }

    // Método para obtener la última ubicación guardada
    @SuppressLint("Range")
    public Location getLastSavedLocation() {
        SQLiteDatabase db = this.getReadableDatabase();
        Location location = null;

        String query = "SELECT " + COLUMN_LATITUDE + ", " + COLUMN_LONGITUDE +
                " FROM " + TABLE_INCIDENTS +
                " ORDER BY " + COLUMN_TIMESTAMP + " DESC LIMIT 1";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            location = new Location("");
            location.setLatitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE)));
            location.setLongitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE)));
        }

        cursor.close();
        return location;
    }

    // Método para obtener todos los incidentes con ubicación
    @SuppressLint("Range")
    public List<Map<String, Object>> getAllIncidentsWithLocation() {
        List<Map<String, Object>> incidents = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_INCIDENTS +
                " WHERE " + COLUMN_LATITUDE + " IS NOT NULL" +
                " AND " + COLUMN_LONGITUDE + " IS NOT NULL" +
                " ORDER BY " + COLUMN_TIMESTAMP + " DESC";

        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            Map<String, Object> incident = new HashMap<>();
            incident.put("id", cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
            incident.put("type", cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)));
            incident.put("latitude", cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE)));
            incident.put("longitude", cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE)));
            incident.put("timestamp", cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP)));
            incidents.add(incident);
        }

        cursor.close();
        return incidents;
    }

    public boolean deleteIncident(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_INCIDENTS,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        return rowsAffected > 0;
    }
}