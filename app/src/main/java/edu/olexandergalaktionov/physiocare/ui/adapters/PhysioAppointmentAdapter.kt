package edu.olexandergalaktionov.physiocare.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.olexandergalaktionov.physiocare.databinding.ItemPhysioAppointmentBinding
import edu.olexandergalaktionov.physiocare.model.Appointment

class PhysioAppointmentAdapter(
    private val appointments: List<Appointment>,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<PhysioAppointmentAdapter.PhysioAppointmentViewHolder>() {

    inner class PhysioAppointmentViewHolder(val binding: ItemPhysioAppointmentBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhysioAppointmentViewHolder {
        val binding = ItemPhysioAppointmentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PhysioAppointmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhysioAppointmentViewHolder, position: Int) {
        val appointment = appointments[position]
        holder.binding.apply {
            tvDate.text = appointment.date ?: "Sin fecha"
            tvDiagnosis.text = appointment.diagnosis ?: "Sin diagnóstico"
            tvTreatment.text = appointment.treatment ?: "Sin tratamiento"
            tvObservations.text = appointment.observations ?: "Sin observaciones"

            btnDelete.setOnClickListener {
                appointment._id?.let { onDeleteClick(it) }
            }
        }
    }

    override fun getItemCount(): Int = appointments.size
}
