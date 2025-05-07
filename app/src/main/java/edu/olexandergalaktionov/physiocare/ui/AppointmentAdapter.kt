package edu.olexandergalaktionov.physiocare.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.olexandergalaktionov.physiocare.databinding.ItemAppointmentBinding
import edu.olexandergalaktionov.physiocare.model.Appointment
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class AppointmentAdapter(
    private val onItemClick: (Appointment) -> Unit,
) : ListAdapter<Appointment, AppointmentAdapter.AppointmentViewHolder>(AppointmentDiffCallback()) {

    class AppointmentViewHolder(
        private val binding: ItemAppointmentBinding,
        private val onItemClick: (Appointment) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(appointment: Appointment) {
            binding.tvDate.text = "Día: ${formatDate(appointment.date)}"
            binding.tvDiagnosis.text = "Diagnóstico: ${appointment.diagnosis ?: "N/A"}"
            binding.tvPhysio.text = "Fisio: ${appointment.physio ?: "Desconocido"}"

            binding.root.setOnClickListener {
                onItemClick(appointment)
            }
        }

        private fun formatDate(dateString: String?): String {
            return try {
                if (dateString == null) return "Desconocida"
                val date = OffsetDateTime.parse(dateString)
                date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            } catch (e: DateTimeParseException) {
                "Formato inválido"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val binding = ItemAppointmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppointmentViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}


class AppointmentDiffCallback : DiffUtil.ItemCallback<Appointment>() {
    override fun areItemsTheSame(oldItem: Appointment, newItem: Appointment): Boolean {
        return oldItem._id == newItem._id // usa tu campo identificador único
    }

    override fun areContentsTheSame(oldItem: Appointment, newItem: Appointment): Boolean {
        return oldItem == newItem
    }
}
