package edu.olexandergalaktionov.physiocare.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.olexandergalaktionov.physiocare.databinding.ItemAppointmentBinding
import edu.olexandergalaktionov.physiocare.model.Appointment
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Adapter para mostrar una lista de citas.
 *
 * @param onItemClick Función que se llama al hacer clic en un elemento de la lista.
 * @param onDeleteClick Función que se llama al hacer clic en el botón de eliminar.
 * @param isPhysio Indica si el usuario es un fisioterapeuta.
 */
class AppointmentAdapter(
    private val onItemClick: (Appointment) -> Unit,
    private val onDeleteClick: ((Appointment) -> Unit)? = null,
    var isPhysio: Boolean
) : ListAdapter<Appointment, AppointmentAdapter.AppointmentViewHolder>(AppointmentDiffCallback()) {

    inner class AppointmentViewHolder(
        private val binding: ItemAppointmentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(appointment: Appointment) {
            // Mapeamos los datos a la vista
            binding.tvDate.text = "Día: ${formatDate(appointment.date)}"
            val fullName = listOfNotNull(appointment.physioName, appointment.physioSurname).joinToString(" ")
            binding.tvPhysio.text = "Fisio: ${if (fullName.isBlank()) appointment.physio else fullName}"

            // Click al detalle
            binding.root.setOnClickListener {
                onItemClick(appointment)
            }

            // Mostrar u ocultar botón eliminar según rol
            binding.btnDelete.apply {
                visibility = if (isPhysio) View.VISIBLE else View.GONE
                setOnClickListener {
                    onDeleteClick?.invoke(appointment)
                }
            }
        }

        /**
         * Funcion que formatea la fecha de la cita.
         */
        private fun formatDate(dateString: String?): String {
            return try {
                if (dateString == null) return "Desconocida"
                val date = OffsetDateTime.parse(dateString)
                date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            } catch (e: DateTimeParseException) {
                "Formato inválido"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val binding = ItemAppointmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppointmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
/**
 * Callback para calcular las diferencias entre dos listas de citas.
 */
class AppointmentDiffCallback : DiffUtil.ItemCallback<Appointment>() {
    override fun areItemsTheSame(oldItem: Appointment, newItem: Appointment): Boolean {
        return oldItem._id == newItem._id
    }

    override fun areContentsTheSame(oldItem: Appointment, newItem: Appointment): Boolean {
        return oldItem == newItem
    }
}
