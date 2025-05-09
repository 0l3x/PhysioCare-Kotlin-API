package edu.olexandergalaktionov.physiocare.ui.record

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import edu.olexandergalaktionov.physiocare.R
import edu.olexandergalaktionov.physiocare.databinding.ActivityRecordDetailBinding
import edu.olexandergalaktionov.physiocare.ui.adapters.AppointmentAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import edu.olexandergalaktionov.physiocare.data.PhysioCareRepository
import edu.olexandergalaktionov.physiocare.ui.appointment.AppointmentViewModel
import edu.olexandergalaktionov.physiocare.ui.appointment.AppointmentViewModelFactory
import edu.olexandergalaktionov.physiocare.ui.appointment.detail.AppointmentDetailActivity
import edu.olexandergalaktionov.physiocare.utils.SessionManager
import edu.olexandergalaktionov.physiocare.utils.checkConnection
import edu.olexandergalaktionov.physiocare.utils.dataStore
import edu.olexandergalaktionov.physiocare.utils.isPhysio
import kotlinx.coroutines.flow.first
import java.util.Calendar

/**
 * Actividad para mostrar los detalles de un expediente.
 * Permite ver y gestionar las citas del paciente.
 * @author Olexandr Galaktionov Tsisar
 */
class RecordDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordDetailBinding
    private lateinit var appointmentAdapter: AppointmentAdapter
    private var recordId: String? = null

    // Para gestionar las citas
    private val appointmentViewModel: AppointmentViewModel by viewModels {
        AppointmentViewModelFactory(PhysioCareRepository(SessionManager(dataStore)))
    }

    /**
     * Método de creación de la actividad.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRecordDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recordId = intent.getStringExtra("patientId")

        // Se configura SwipeRefresh
        binding.swipeRefresh.setOnRefreshListener {
            loadAppointments()
        }

        // Carga inicial
        loadAppointments()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detalle del expediente"

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        appointmentAdapter = AppointmentAdapter(
            onItemClick = { appointment ->
                val intent = Intent(this@RecordDetailActivity, AppointmentDetailActivity::class.java)
                intent.putExtra("appointmentId", appointment._id)
                startActivity(intent)
            },
            onDeleteClick = { appointment ->
                lifecycleScope.launch {
                    val userId = SessionManager(dataStore).userIdFlow.first()
                    if (isPhysio && appointment._id != null) {
                        appointmentViewModel.deleteAppointmentById(appointment._id, userId!!)
                        loadAppointments()
                    }
                }
            },
            isPhysio = isPhysio
        )
        binding.recyclerView.adapter = appointmentAdapter

        lifecycleScope.launch {
            val rol = SessionManager(dataStore).roleFlow.first()
            isPhysio = rol == "physio"
        }

    } // fin create

    /**
     * Carga las citas del paciente.
     * Si no hay conexión a Internet, muestra un mensaje y limpia la lista de citas.
     * Si el usuario no está autenticado, muestra un mensaje y limpia la lista de citas.
     * Si se produce un error al cargar las citas, muestra un mensaje de error.
     */
    @SuppressLint("SetTextI18n")
    private fun loadAppointments() {
        lifecycleScope.launch {
            binding.swipeRefresh.isRefreshing = true

            if (!checkConnection(this@RecordDetailActivity)) {
                clearAppointments(getString(R.string.no_internet))
                return@launch
            }

            val sessionManager = SessionManager(dataStore)
            val token = sessionManager.sessionFlow.first().first

            if (token == null) {
                clearAppointments(getString(R.string.not_logged))
                return@launch
            }

            try {
                val record = RecordViewModelFactory(PhysioCareRepository(SessionManager(dataStore)))
                    .create(RecordViewModel::class.java)
                    .getRecordByPatientId(recordId!!)
                if (record != null) {
                    val allPhysios = appointmentViewModel.getAllPhysios()
                    val physioMap = allPhysios.associateBy({ it._id }, { it.fullName })

                    val enrichedAppointments = record.appointments?.map { appointment ->
                        appointment.copy(
                            physio = physioMap[appointment.physio] ?: "Desconocido"
                        )
                    }
                    appointmentAdapter.submitList(enrichedAppointments)

                    // Mostrar datos del paciente
                    binding.tvPatientName.text = "Nombre: ${record.patient.name} ${record.patient.surname}"
                    binding.tvPatientBirth.text = "Nacimiento: ${record.patient.birthDate?.take(10)}"
                    binding.tvPatientAddress.text = "Dirección: ${record.patient.address}"
                    binding.tvMedicalRecord.text = "Historial: ${if (record.medicalRecord?.isNotBlank() == true) record.medicalRecord else "Sin historial registrado"}"

                    binding.swipeRefresh.isRefreshing = false
                }
            } catch (e: Exception) {
                Toast.makeText(this@RecordDetailActivity, "Error al cargar citas", Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    /**
     * Muestra un diálogo para añadir una nueva cita.
     * @param recordId ID del registro al que se añadirá la cita.
     */
    private fun showAddAppointmentDialog(recordId: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_appointment, null)
        val datePicker = dialogView.findViewById<DatePicker>(R.id.datePicker)
        val spinnerPhysio = dialogView.findViewById<Spinner>(R.id.spinnerPhysio)
        val editDiagnosis = dialogView.findViewById<EditText>(R.id.editDiagnosis)
        val editTreatment = dialogView.findViewById<EditText>(R.id.editTreatment)
        val editObservations = dialogView.findViewById<EditText>(R.id.editObservations)

        lifecycleScope.launch {
            val token = SessionManager(dataStore).sessionFlow.first().first

            if (token.isNullOrEmpty()) {
                Toast.makeText(this@RecordDetailActivity, "Sesión caducada. Por favor, inicia sesión de nuevo.", Toast.LENGTH_LONG).show()
            }

            try {
                val fisios = appointmentViewModel.getAllPhysios()
                val fisioNames = fisios.map { it.fullName }
                val adapter = ArrayAdapter(this@RecordDetailActivity, android.R.layout.simple_spinner_item, fisioNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerPhysio.adapter = adapter

                AlertDialog.Builder(this@RecordDetailActivity)
                    .setTitle("Nueva cita")
                    .setView(dialogView)
                    .setPositiveButton("Guardar") { _, _ ->
                        val calendar = Calendar.getInstance().apply {
                            set(datePicker.year, datePicker.month, datePicker.dayOfMonth, 12, 0)
                        }

                        // Para formatear la fecha a "yyyy-MM-dd"
                        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        formatter.timeZone = java.util.TimeZone.getTimeZone("UTC")
                        val date = formatter.format(calendar.time)


                        val selectedPhysio = fisios[spinnerPhysio.selectedItemPosition]
                        val diagnosis = editDiagnosis.text.toString()
                        val treatment = editTreatment.text.toString()
                        val observations = editObservations.text.toString()

                        // Guardar la cita
                        lifecycleScope.launch {
                            val success = appointmentViewModel.postAppointmentToRecord(
                                recordId,
                                selectedPhysio._id,
                                diagnosis,
                                treatment,
                                observations,
                                date
                            )
                            if (success) {
                                Toast.makeText(this@RecordDetailActivity, "Cita añadida", Toast.LENGTH_SHORT).show()
                                loadAppointments()
                            } else {
                                Toast.makeText(this@RecordDetailActivity, "Error al guardar", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()

            } catch (e: Exception) {
                Toast.makeText(this@RecordDetailActivity, "Error al obtener fisioterapeutas: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Crea el menú de opciones.
     * @param menu Menú a inflar.
     * @return true si se crea el menú, false en caso contrario.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_record_detail, menu)
        return true
    }

    /**
     * Maneja la selección de opciones del menú.
     * @param item Opción seleccionada.
     * @return true si se maneja la opción, false en caso contrario.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_appointment -> {
                showAddAppointmentDialog(recordId!!)
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Limpia la lista de citas y muestra un mensaje.
     * @param message Mensaje a mostrar.
     */
    private fun clearAppointments(message: String) {
        appointmentAdapter.submitList(emptyList())
        binding.noDataText.visibility = View.VISIBLE
        binding.swipeRefresh.isRefreshing = false
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}