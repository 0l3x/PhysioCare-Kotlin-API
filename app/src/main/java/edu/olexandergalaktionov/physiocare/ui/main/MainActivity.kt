package edu.olexandergalaktionov.physiocare.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
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
import edu.olexandergalaktionov.physiocare.model.Record
import edu.olexandergalaktionov.physiocare.ui.RecordDetailActivity
import edu.olexandergalaktionov.physiocare.ui.RecordViewModel
import edu.olexandergalaktionov.physiocare.ui.RecordViewModelFactory
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

/**
 * TODO:
 * 1. variables esenciales para el main
 * 2. viewmodels para la sesion, citas y records
 *
 * 3. onStart() para cargar datos(), dependiendo de los necesarios(rol)
 *
 * 4. onCreate() para inicializar el binding, lifecycle para verificar si hay sesion activa al iniciar la activity,
 * si no mandar a login else( cargar datos
 *
 * 5. manejar con el viewmodel el loginState y handle UI response
 * -lifecycle que ccomprueba que haya token y posteriormente carga datos()
 *
 * 6. funcion loadData() segun rol para el adapter
 *
 * 7. logica freshRefresh
 *
 * 8. configurar el toolbar de arriba con el login
 *
 * 9 about me
 *
 * -- ^^ oncreate() ^^ --
 *
 *
 *
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var appointmentAdapter: AppointmentAdapter
    //private lateinit var recordAdapter: RecordAdapter

    private var showingFutureAppointments = true

    private var currentView: ViewType = ViewType.APPOINTMENTS

    enum class ViewType {
        APPOINTMENTS,
        RECORDS
    }

    // para gestionar el login
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(PhysioCareRepository(SessionManager(dataStore)))
    }

    // para gestionar las citas
    private val appointmentViewModel: AppointmentViewModel by viewModels {
        AppointmentViewModelFactory(PhysioCareRepository(SessionManager(dataStore)))
    }

    // para gestionar los registros
    private val recordViewModel: RecordViewModel by viewModels {
        RecordViewModelFactory(PhysioCareRepository(SessionManager(dataStore)))
    }

    override fun onStart() {
        super.onStart()
        loadData()
    }

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

        // lifecycle para verificar si hay sesion activa al iniciar la activity,
        // * si no mandar a login else( cargar datos
        lifecycleScope.launch {
            val token = SessionManager(dataStore).sessionFlow.first().first
            if (token == null) {
                showLoginDialog()
            } else {
                loadData()
            }
        }

        // Observe login state and handle UI response
        lifecycleScope.launch {
            mainViewModel.loginState.collect { state ->
                when (state) {
                    is LoginState.Loading -> Log.i("LOGIN", "Loading")
                    is LoginState.Success -> {
                        Log.i("LOGIN", "Success: ${state.response.token}")

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
                        Log.e("LOGIN", "Error: ${state.message}")
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

        // Logica Botones
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
                        clearAppointments(getString(R.string.logout))
                        showLoginDialog()
                    }
                    true
                }

                else -> false
            }
        }

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

        binding.btnUpcoming.setOnClickListener {
            showingFutureAppointments = true
            observeAppointments()
        }

        binding.btnPast.setOnClickListener {
            showingFutureAppointments = false
            observeAppointments()
        }

        // Logica BottomNavigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_appointments -> {
                    currentView = ViewType.APPOINTMENTS
                    setupAppointmentAdapter()
                    loadData()
                    true
                }
                R.id.nav_records -> {
                    currentView = ViewType.RECORDS
                    lifecycleScope.launch {
                        try {
                            val records = recordViewModel.getAllRecords()
                            Log.i("RECORDS", "Records: $records")
                            val recordAdapter = RecordAdapter(records) { selectedRecord ->
                                val intent = Intent(this@MainActivity, RecordDetailActivity::class.java)
                                intent.putExtra("recordId", selectedRecord.id)
                                startActivity(intent)
                            }
                            binding.recyclerView.adapter = recordAdapter
                            binding.noDataText.visibility = if (records.isEmpty()) View.VISIBLE else View.GONE
                        } catch (e: Exception) {
                            Log.e("RECORDS", "Error: ${e.message}")
                            clearAppointments("Error al obtener registros: ${e.message}")
                        }
                    }
                    true
                }
                else -> false
            }
        }

    } // !! Fin create

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

    private fun loadData() {
        /**
         * 1. comprobar si hay token
         * 2. extraer el rol del token
         * 3. si es "patient" cargar los appointments / añadir botones / quitar menu record
         * 4. si es "physio" cargar los appointments / quitar boteones / añadir menu record y logica record
         */
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
                        val records = recordViewModel.getAllRecords()
                        val recordAdapter = RecordAdapter(records) { selectedRecord ->
                            val intent = Intent(this@MainActivity, RecordDetailActivity::class.java)
                            intent.putExtra("recordId", selectedRecord.id)
                            startActivity(intent)
                        }
                        binding.recyclerView.adapter = recordAdapter
                        binding.noDataText.visibility = if (records.isEmpty()) View.VISIBLE else View.GONE
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
                // se desactiva el refresh si no se detuvo antes
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

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

    private fun updateAppointmentList(appointments: List<Appointment>) {
        if (appointments.isEmpty()) {
            clearAppointments(getString(R.string.no_appointments))
        } else {
            appointmentAdapter.submitList(appointments)
            binding.noDataText.visibility = View.GONE
        }
    }

    private fun updateToolbarMenu() {
        lifecycleScope.launch {
            val (token, _) = SessionManager(dataStore).sessionFlow.first()
            val menu = binding.mToolbar.menu
            menu.findItem(R.id.action_login)?.isVisible = token == null
            menu.findItem(R.id.action_logout)?.isVisible = token != null
        }
    }

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

    private fun clearAppointments(message: String) {
        when (currentView) {
            ViewType.APPOINTMENTS -> {
                appointmentAdapter.submitList(emptyList())
            }
            ViewType.RECORDS -> {
                binding.recyclerView.adapter = RecordAdapter(emptyList()) {}
            }
        }
        binding.noDataText.visibility = View.VISIBLE
        binding.swipeRefresh.isRefreshing = false
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}
