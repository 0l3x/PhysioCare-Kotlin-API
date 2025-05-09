package edu.olexandergalaktionov.physiocare.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import edu.olexandergalaktionov.physiocare.R
import edu.olexandergalaktionov.physiocare.data.PhysioCareRepository
import edu.olexandergalaktionov.physiocare.databinding.ActivityMainBinding
import edu.olexandergalaktionov.physiocare.model.Appointment
import edu.olexandergalaktionov.physiocare.model.LoginState
import edu.olexandergalaktionov.physiocare.ui.record.RecordViewModel
import edu.olexandergalaktionov.physiocare.ui.record.RecordViewModelFactory
import edu.olexandergalaktionov.physiocare.ui.adapters.AppointmentAdapter
import edu.olexandergalaktionov.physiocare.ui.adapters.RecordAdapter
import edu.olexandergalaktionov.physiocare.ui.appointment.detail.AppointmentDetailActivity
import edu.olexandergalaktionov.physiocare.ui.appointment.AppointmentViewModel
import edu.olexandergalaktionov.physiocare.ui.appointment.AppointmentViewModelFactory
import edu.olexandergalaktionov.physiocare.utils.SessionManager
import edu.olexandergalaktionov.physiocare.utils.checkConnection
import edu.olexandergalaktionov.physiocare.utils.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import edu.olexandergalaktionov.physiocare.utils.isPhysio
import edu.olexandergalaktionov.physiocare.model.Record

/**
 * Clase principal de la aplicación PhysioCare.
 *
 * Muestra las citas y expedientes dependiendo del rol del usuario (patient o physio).
 * Gestiona la lógica de login, carga de datos, toolbar, botones de filtro y navegación inferior.
 *
 * Esta activity es responsable de:
 * - Verificar si hay sesión activa y mostrar el diálogo de login si es necesario.
 * - Mostrar la lista de citas del paciente o todas las del fisioterapeuta.
 * - Permitir al fisioterapeuta ver expedientes y buscarlos por nombre o apellido.
 * - Mostrar mensajes adecuados si no hay conexión, sesión o datos.
 * - Permite al fisioterapeuta eliminar citas; al paciente ver los detalles de las citas pasadas,
 *  y tambien permite refrescar la lista de citas, o expedientes si eres fisioterapeuta.
 *
 * @author Olexandr Galaktionov Tsisar
 * @version 1.0
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appointmentAdapter: AppointmentAdapter
    private var currentView: ViewType = ViewType.APPOINTMENTS
    private var allRecords: List<Record> = emptyList()
    private var showingFutureAppointments = true

    /**
     * enum para distinguir entre las vistas de citas y registros
     */
    enum class ViewType {
        APPOINTMENTS,
        RECORDS
    }

    /**
     * ViewModel para gestionar el estado de inicio de sesión
     */
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(PhysioCareRepository(SessionManager(dataStore)))
    }

    /**
     * ViewModel para gestionar las citas.
     */
    private val appointmentViewModel: AppointmentViewModel by viewModels {
        AppointmentViewModelFactory(PhysioCareRepository(SessionManager(dataStore)))
    }

    /**
     * ViewModel para gestionar los registros.
     */
    private val recordViewModel: RecordViewModel by viewModels {
        RecordViewModelFactory(PhysioCareRepository(SessionManager(dataStore)))
    }

    /**
     * Cuando la actividad se inicia carga la información de las citas
     */
    override fun onStart() {
        super.onStart()
        loadData()
    }

    /**
     * Inicializa la actividad principal.
     * Configura el binding, el toolbar, el adaptador de citas y la lógica de inicio de sesión.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // lifecycle para verificar si hay sesion activa al iniciar la activity
        lifecycleScope.launch {
            val token = SessionManager(dataStore).sessionFlow.first().first
            if (token == null) {
                showLoginDialog()
            } else {
                loadData()
            }
        }

        // Observa el login state y maneja la UI response
        lifecycleScope.launch {
            mainViewModel.loginState.collect { state ->
                when (state) {
                    is LoginState.Loading -> Log.i("LOGIN", "Loading")
                    is LoginState.Success -> {

                        isPhysio = state.response.rol == "physio"
                        currentView = ViewType.APPOINTMENTS
                        setupAppointmentAdapter()

                        updateToolbarMenu()

                        val token = SessionManager(dataStore).sessionFlow.first().first
                        if (token != null) {
                            loadData()
                        }
                    }
                    is LoginState.Error -> {
                        Toast.makeText(this@MainActivity, "Error de login: ${state.message}", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }

        setupAppointmentAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // Logica Refresh
        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                val token = SessionManager(dataStore).sessionFlow.first().first
                if (token == null) {
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(this@MainActivity, getString(R.string.not_logged), Toast.LENGTH_SHORT).show()
                    showLoginDialog()
                } else {
                    loadData()
                }
            }
        }

        // Toolbar menu
        binding.mToolbar.inflateMenu(R.menu.main_menu)
        updateToolbarMenu()

        // Logica Botones de sesion
        binding.mToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_login -> {
                    showLoginDialog()
                    true
                }

                R.id.action_logout -> {
                    lifecycleScope.launch {
                        mainViewModel.logout()
                        binding.filterButtons.visibility = View.GONE
                        binding.bottomNavigation.visibility = View.GONE
                        binding.searchViewRecords.visibility = View.GONE
                        clearAppointments(getString(R.string.logout))
                        showLoginDialog()
                    }
                    true
                }

                else -> false
            }
        }

        // Acerca de
        binding.mToolbar.setNavigationOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Acerca de")
                .setMessage("""
                    Autor: Olexandr Galaktionov Tsisar
                    Curso/Grupo: 2º DAM/DAW
                    Año académico: 2024-2025
                """.trimIndent())
                .setPositiveButton(getString(R.string.accept), null)
                .show()
        }

        // Muestra citas futuras
        binding.btnUpcoming.setOnClickListener {
            showingFutureAppointments = true
            observeAppointments()
        }

        // Muestra citas pasadas
        binding.btnPast.setOnClickListener {
            showingFutureAppointments = false
            observeAppointments()
        }

        // Logica BottomNavigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_appointments -> {
                    currentView = ViewType.APPOINTMENTS
                    binding.searchViewRecords.visibility = View.GONE
                    setupAppointmentAdapter()
                    loadData()
                    true
                }
                R.id.nav_records -> {
                    currentView = ViewType.RECORDS
                    binding.searchViewRecords.visibility = View.VISIBLE
                    lifecycleScope.launch {
                        try {
                            val records = recordViewModel.getAllRecords()
                            val recordAdapter = RecordAdapter(records)
                            binding.recyclerView.adapter = recordAdapter
                            binding.noDataText.visibility = if (records.isEmpty()) View.VISIBLE else View.GONE
                        } catch (e: Exception) {
                            clearAppointments("Error al obtener registros: ${e.message}")
                        }
                    }
                    true
                }
                else -> false
            }
        }

    } // !! Fin create

    /**
     * Prepara el adaptador de citas con su lógica de clic y borrado.
     */
    private fun setupAppointmentAdapter() {
        appointmentAdapter = AppointmentAdapter(
            onItemClick = { appointment ->
                if (!showingFutureAppointments) {
                    val intent = Intent(this@MainActivity, AppointmentDetailActivity::class.java)
                    intent.putExtra("appointmentId", appointment._id)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Solo puedes ver los detalles de citas pasadas", Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteClick = { appointment ->
                lifecycleScope.launch {
                    val userId = SessionManager(dataStore).userIdFlow.first()
                    if (isPhysio && appointment._id != null) {
                        appointmentViewModel.deleteAppointmentById(appointment._id, userId!!)
                    }
                }
            },
            isPhysio = isPhysio
        )
        binding.recyclerView.adapter = appointmentAdapter
    }

    /**
     * Carga los datos de citas o registros según el rol del usuario.
     * 1. Comprueba si hay token
     * 2. Extrae el rol del token
     * . Si es "patient" carga los appointments / añade botones / quita menu record
     * . si es "physio" carga los appointments tmb / quita botones / añade menu record y gestiona record
     */
    private fun loadData() {
        lifecycleScope.launch {
            binding.swipeRefresh.isRefreshing = true

            // COMPROBAR CONEXIÓN
            if (!checkConnection(this@MainActivity)) {
                clearAppointments(getString(R.string.no_internet))
                return@launch
            }

            val sessionManager = SessionManager(dataStore)
            val token = sessionManager.sessionFlow.first().first
            val rol = sessionManager.roleFlow.first()
            val userId = sessionManager.userIdFlow.first()

            if (token == null) {
                clearAppointments(getString(R.string.not_logged))
                return@launch
            }

            isPhysio = rol == "physio"
            setupAppointmentAdapter()

            try {
                when(currentView) {
                    ViewType.APPOINTMENTS -> {
                        if(rol == "patient") {
                            appointmentViewModel.fetchAppointmentsByPatient(userId!!)
                            binding.filterButtons.visibility = View.VISIBLE
                            binding.bottomNavigation.visibility = View.GONE
                            observeAppointments()
                        } else if(rol == "physio") {
                            appointmentViewModel.loadAppointmentsForPhysio(userId!!)
                            binding.filterButtons.visibility = View.GONE
                            binding.bottomNavigation.visibility = View.VISIBLE

                            appointmentViewModel.appointments.collect { appointments ->
                                showingFutureAppointments = false // Fisio puede ver el detalle de todas las citas
                                updateAppointmentList(appointments)
                                binding.swipeRefresh.isRefreshing = false
                            }

                        }
                    }

                    ViewType.RECORDS -> {
                        if (token.isEmpty()) {
                            clearAppointments(getString(R.string.not_logged))
                            return@launch
                        }

                        binding.searchViewRecords.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                            override fun onQueryTextSubmit(query: String?): Boolean {
                                return false
                            }
                            override fun onQueryTextChange(newText: String?): Boolean {
                                filterRecords(newText.orEmpty())
                                return true
                            }
                        })

                        try {
                            allRecords = recordViewModel.getAllRecords()
                            val recordAdapter = RecordAdapter(allRecords)
                            binding.recyclerView.adapter = recordAdapter
                            binding.noDataText.visibility = if (allRecords.isEmpty()) View.VISIBLE else View.GONE
                        } catch (e: Exception) {
                            clearAppointments("Error al obtener registros: ${e.message}")
                        }
                    }
                }
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 401) {
                    mainViewModel.logout()
                    clearAppointments(getString(R.string.closed_session))
                } else {
                    clearAppointments("Error al obtener cafés: ${e.message}")
                }
            } catch (e: Exception) {
                clearAppointments("Error inesperado: ${e.message}")
            } finally {
                binding.swipeRefresh.isRefreshing = false // se desactiva el refresh si no se detuvo antes
            }
        }
    }

    /**
     * Filtra la lista de expedientes según nombre o apellido del paciente.
     */
    private fun filterRecords(query: String) {
        val filtered = allRecords.filter {
            it.patient.name?.contains(query, ignoreCase = true) == true ||
            it.patient.surname?.contains(query, ignoreCase = true) == true
        }
        val adapter = RecordAdapter(filtered)
        binding.recyclerView.adapter = adapter
        binding.noDataText.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    /**
     * Observa la lista de citas y las actualiza según el filtro (futuras/pasadas).
     */
    private fun observeAppointments() {
        lifecycleScope.launch {
            if (showingFutureAppointments) {
                appointmentViewModel.futureAppointments.collect { appointments ->
                    updateAppointmentList(appointments)
                }
            } else {
                appointmentViewModel.pastAppointments.collect { appointments ->
                    updateAppointmentList(appointments)
                }
            }
        }
    }

    /**
     * Actualiza el RecyclerView con una nueva lista de citas.
     */
    private fun updateAppointmentList(appointments: List<Appointment>) {
        if (appointments.isEmpty()) {
            clearAppointments(getString(R.string.no_appointments))
        } else {
            appointmentAdapter.submitList(appointments)
            binding.noDataText.visibility = View.GONE
        }
    }

    /**
     * Actualiza el menú del toolbar según el estado de la sesión.
     */
    private fun updateToolbarMenu() {
        lifecycleScope.launch {
            val (token, _) = SessionManager(dataStore).sessionFlow.first()
            val menu = binding.mToolbar.menu
            menu.findItem(R.id.action_login)?.isVisible = token == null
            menu.findItem(R.id.action_logout)?.isVisible = token != null
        }
    }

    /**
     * Muestra el diálogo de login.
     */
    private fun showLoginDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_login, null)
        val etUsername = view.findViewById<EditText>(R.id.etUsername)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.login))
            .setView(view)
            .setPositiveButton(R.string.accept) { _, _ ->
                val username = etUsername.text.toString()
                val password = etPassword.text.toString()

                if (!checkConnection(this)) {
                    Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                mainViewModel.login(username, password)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    /**
     * Limpia los datos mostrados y muestra un mensaje.
     */
    private fun clearAppointments(message: String) {
        when (currentView) {
            ViewType.APPOINTMENTS -> {
                appointmentAdapter.submitList(emptyList())
            }
            ViewType.RECORDS -> {
                binding.recyclerView.adapter = RecordAdapter(emptyList())
            }
        }
        binding.noDataText.visibility = View.VISIBLE
        binding.swipeRefresh.isRefreshing = false
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}