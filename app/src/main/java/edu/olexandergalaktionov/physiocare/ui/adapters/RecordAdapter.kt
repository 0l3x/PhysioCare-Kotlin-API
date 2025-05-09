package edu.olexandergalaktionov.physiocare.ui.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.olexandergalaktionov.physiocare.R
import edu.olexandergalaktionov.physiocare.model.Record
import edu.olexandergalaktionov.physiocare.ui.RecordDetailActivity

class RecordAdapter(
        private val records: List<Record>,
        private val onItemClick: (Record) -> Unit
    ) : RecyclerView.Adapter<RecordAdapter.RecordViewHolder>() {

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

        // Llena los campos con la información del Record
        holder.patientName.text = "Paciente: ${record.patient.name}"
        holder.medicalRecord.text = "Historial: ${record.medicalRecord}"
        holder.appointmentCount.text = "Citas: ${record.appointments?.size ?: 0}"

        holder.itemView.setOnClickListener {
            val context = it.context
            val intent = Intent(context, RecordDetailActivity::class.java)
            intent.putExtra("patientId", record.patient.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return records.size
    }
}


