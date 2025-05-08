package edu.olexandergalaktionov.physiocare.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.olexandergalaktionov.physiocare.R
import edu.olexandergalaktionov.physiocare.model.Record

class RecordAdapter(private val records: List<Record>) : RecyclerView.Adapter<RecordAdapter.RecordViewHolder>() {

    inner class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val patientName: TextView = itemView.findViewById(R.id.tvPatientName)
        val medicalRecord: TextView = itemView.findViewById(R.id.tvMedicalRecord)
        val appointmentCount: TextView = itemView.findViewById(R.id.tvAppointmentCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = records[position]

        // Llenar los campos con la información del Record
        holder.patientName.text = "Paciente: ${record.patient.name}" // Asumiendo que 'name' es un campo de 'patient'
        holder.medicalRecord.text = "Historial: ${record.medicalRecord}"
        holder.appointmentCount.text = "Citas: ${record.appointments?.size ?: 0}" // Si no hay citas, pone 0
    }

    override fun getItemCount(): Int {
        return records.size
    }
}


