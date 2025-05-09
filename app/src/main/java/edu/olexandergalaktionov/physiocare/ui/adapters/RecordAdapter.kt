package edu.olexandergalaktionov.physiocare.ui.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.olexandergalaktionov.physiocare.R
import edu.olexandergalaktionov.physiocare.databinding.ItemRecordBinding
import edu.olexandergalaktionov.physiocare.model.Record
import edu.olexandergalaktionov.physiocare.ui.record.RecordDetailActivity

/**
 * Adapter para mostrar una lista de registros médicos.
 *
 * @param records Lista de registros médicos a mostrar.
 */
class RecordAdapter(
    private val records: List<Record>
) : RecyclerView.Adapter<RecordAdapter.RecordViewHolder>() {

    inner class RecordViewHolder(
        binding: ItemRecordBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        val patientName: TextView = itemView.findViewById(R.id.tvPatientName)
        val medicalRecord: TextView = itemView.findViewById(R.id.tvMedicalRecord)
        val appointmentCount: TextView = itemView.findViewById(R.id.tvAppointmentCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val binding = ItemRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecordViewHolder(binding)
    }

    /**
     * Asocia los datos del registro a la vista.
     *
     * @param holder El ViewHolder que contiene las vistas.
     * @param position La posición del elemento en la lista.
     */
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = records[position]

        // Mapea los campos con la información del Record
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

    /**
     * Devuelve el número de elementos en la lista de registros.
     */
    override fun getItemCount(): Int {
        return records.size
    }
}


