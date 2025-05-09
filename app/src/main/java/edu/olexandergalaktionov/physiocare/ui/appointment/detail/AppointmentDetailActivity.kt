package edu.olexandergalaktionov.physiocare.ui.appointment.detail

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import edu.olexandergalaktionov.physiocare.R
import edu.olexandergalaktionov.physiocare.data.PhysioCareRepository
import edu.olexandergalaktionov.physiocare.databinding.ActivityAppointmentDetailXBinding
import edu.olexandergalaktionov.physiocare.utils.SessionManager
import edu.olexandergalaktionov.physiocare.utils.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Actividad para mostrar los detalles de una cita.
 *
 * @author Olexandr Galaktionov Tsisar
 */
class AppointmentDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAppointmentDetailXBinding
    /// ID de la cita
    private lateinit var appointmentId: String

    /// ViewModel para gestionar la lógica de negocio relacionada con los detalles de una cita.
    private val viewModel: AppointmentDetailViewModel by viewModels {
        AppointmentDetailViewModelFactory(PhysioCareRepository(SessionManager(dataStore)))
    }

    /**
     * Método de creación de la actividad.
     *
     * @param savedInstanceState Estado guardado de la actividad.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAppointmentDetailXBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener el ID de la cita del Intent
        appointmentId = intent.getStringExtra("appointmentId") ?: run {
            Toast.makeText(this, "ID de cita no proporcionado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Cargar la cita
        lifecycleScope.launch {
            val token = SessionManager(dataStore).sessionFlow.first().first
            if (token != null) {
                viewModel.loadAppointment(appointmentId)
            }
        }

        // Mapeo de la vista
        lifecycleScope.launch {
            viewModel.appointment.collect { appt ->
                appt?.let {
                    val date = it.date?.let { d ->
                        Instant.parse(d).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE)
                    } ?: "Fecha no disponible"

                    binding.tvDate.text = getString(R.string.formatted_date, date)
                    binding.tvDiagnosis.text = getString(R.string.diagnosis_full, it.diagnosis)
                    binding.tvTreatment.text = getString(R.string.treatment_full, it.treatment)
                    binding.tvObservations.text = getString(R.string.observations_full, it.observations)
                    binding.tvPhysio.text = getString(
                        R.string.physio_name_full,
                        it.physioName ?: "Nombre",
                        it.physioSurname ?: "Apellido"
                    )

                }
            }
        }

        // Manejo de errores
        lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(this@AppointmentDetailActivity, it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}