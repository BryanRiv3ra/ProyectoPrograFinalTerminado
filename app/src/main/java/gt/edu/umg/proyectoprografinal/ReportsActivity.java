package gt.edu.umg.proyectoprografinal;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import gt.edu.umg.proyectoprografinal.BaseDeDatos.DatabaseHelper;

public class ReportsActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private IncidentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadIncidents();

        RecyclerView.ItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(300);
        animator.setRemoveDuration(300);
        recyclerView.setItemAnimator(animator);



        loadIncidents();
    }

    private void loadIncidents() {
        List<Incident> incidents = new ArrayList<>();
        Cursor cursor = dbHelper.getAllIncidents();

        while (cursor.moveToNext()) {
            Incident incident = new Incident(
                    cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("type")),
                    cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    cursor.getString(cursor.getColumnIndexOrThrow("severity")),
                    cursor.getString(cursor.getColumnIndexOrThrow("status")),
                    cursor.getString(cursor.getColumnIndexOrThrow("image_path")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")),
                    cursor.getString(cursor.getColumnIndexOrThrow("address")),
                    cursor.getString(cursor.getColumnIndexOrThrow("timestamp"))
            );
            incidents.add(incident);
        }
        cursor.close();

        adapter = new IncidentAdapter(incidents);
        recyclerView.setAdapter(adapter);
    }

    private static class Incident {
        long id;
        String type;
        String description;
        String severity;
        String status;
        String imagePath;
        double latitude;
        double longitude;
        String address;
        String timestamp;

        Incident(long id, String type, String description, String severity,
                 String status, String imagePath, double latitude, double longitude,
                 String address, String timestamp) {
            this.id = id;
            this.type = type;
            this.description = description;
            this.severity = severity;
            this.status = status;
            this.imagePath = imagePath;
            this.latitude = latitude;
            this.longitude = longitude;
            this.address = address;
            this.timestamp = timestamp;
        }
    }

    private class IncidentAdapter extends RecyclerView.Adapter<IncidentAdapter.ViewHolder> {
        private List<Incident> incidents;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        IncidentAdapter(List<Incident> incidents) {
            this.incidents = incidents;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_incident, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Incident incident = incidents.get(position);

            holder.typeText.setText(incident.type);
            holder.descriptionText.setText(incident.description);
            holder.severityText.setText("Severidad: " + incident.severity);
            holder.statusText.setText("Estado: " + incident.status);
            holder.addressText.setText(incident.address);
            holder.timestampText.setText(incident.timestamp);

            // Actualizar el color del estado según su valor
            holder.statusText.setText("Estado: " + incident.status);
            int colorResId;
            switch (incident.status.toUpperCase()) {
                case "EN PROCESO":
                    colorResId = R.color.status_en_proceso;
                    break;
                case "RESUELTO":
                    colorResId = R.color.status_resuelto;
                    break;
                case "CANCELADO":
                    colorResId = R.color.status_cancelado;
                    break;
                default: // PENDIENTE
                    colorResId = R.color.status_pendiente;
                    break;
            }
            holder.statusText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), colorResId));

            // Cargar imagen usando Glide
            Glide.with(holder.itemView.getContext())
                    .load(incident.imagePath)
                    .centerCrop()
                    .into(holder.imageView);

            // Configurar el clic largo para actualizar el estado
            holder.itemView.setOnLongClickListener(v -> {
                showOptionsDialog(holder.itemView.getContext(), incident);
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return incidents.size();
        }

        // Método para mostrar el diálogo de actualización de estado
        private void showStatusUpdateDialog(Context context, Incident incident) {
            String[] estados = {"EN PROCESO", "RESUELTO", "CANCELADO"};

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Actualizar Estado")
                    .setItems(estados, (dialog, which) -> {
                        String nuevoEstado = estados[which];
                        updateIncidentStatus(context, incident.id, nuevoEstado);
                    })
                    .show();
        }

        // Método para actualizar el estado en la base de datos
        private void updateIncidentStatus(Context context, long incidentId, String newStatus) {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            boolean updated = dbHelper.updateIncidentStatus(incidentId, newStatus);

            if (updated) {
                Toast.makeText(context, "Estado actualizado exitosamente", Toast.LENGTH_SHORT).show();
                // Recargar la lista de incidentes
                if (context instanceof ReportsActivity) {
                    ((ReportsActivity) context).loadIncidents();
                }
            } else {
                Toast.makeText(context, "Error al actualizar el estado", Toast.LENGTH_SHORT).show();
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView typeText;
            TextView descriptionText;
            TextView severityText;
            TextView statusText;
            TextView addressText;
            TextView timestampText;

            ViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.incidentImage);
                typeText = itemView.findViewById(R.id.typeText);
                descriptionText = itemView.findViewById(R.id.descriptionText);
                severityText = itemView.findViewById(R.id.severityText);
                statusText = itemView.findViewById(R.id.statusText);
                addressText = itemView.findViewById(R.id.addressText);
                timestampText = itemView.findViewById(R.id.timestampText);
            }
        }
        private void deleteIncident(Context context, Incident incident) {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            boolean deleted = dbHelper.deleteIncident(incident.id);

            if (deleted) {
                // Eliminar el item de la lista
                int position = incidents.indexOf(incident);
                if (position != -1) {
                    incidents.remove(position);
                    notifyItemRemoved(position);
                }

                Toast.makeText(context, "Reporte eliminado exitosamente",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Error al eliminar el reporte",
                        Toast.LENGTH_SHORT).show();
            }
        }
        private void showDeleteConfirmationDialog(Context context, Incident incident) {
            new AlertDialog.Builder(context)
                    .setTitle("Confirmar Eliminación")
                    .setMessage("¿Estás seguro de que quieres eliminar este reporte?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        deleteIncident(context, incident);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        }

        private void showOptionsDialog(Context context, Incident incident) {
            String[] options = {"Cambiar Estado", "Eliminar Reporte"};

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Opciones de Reporte")
                    .setItems(options, (dialog, which) -> {
                        switch (which) {
                            case 0: // Cambiar Estado
                                showStatusUpdateDialog(context, incident);
                                break;
                            case 1: // Eliminar
                                showDeleteConfirmationDialog(context, incident);
                                break;
                        }
                    })
                    .show();
        }
    }

    private void updateIncidentStatus(long incidentId, String newStatus) {
        boolean updated = dbHelper.updateIncidentStatus(incidentId, newStatus);
        if (updated) {
            Toast.makeText(this, "Estado actualizado exitosamente", Toast.LENGTH_SHORT).show();
            // Recargar la lista de incidentes
            loadIncidents();
        } else {
            Toast.makeText(this, "Error al actualizar el estado", Toast.LENGTH_SHORT).show();
        }
    }

















}