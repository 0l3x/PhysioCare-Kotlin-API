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
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.olexandergalaktionov.physiocare.R
import edu.olexandergalaktionov.physiocare.data.PhysioCareRepository
import edu.olexandergalaktionov.physiocare.databinding.ActivityMainBinding
import edu.olexandergalaktionov.physiocare.model.LoginState
import edu.olexandergalaktionov.physiocare.ui.adapters.AppointmentAdapter
import edu.olexandergalaktionov.physiocare.ui.appointment.detail.AppointmentDetailActivity
import edu.olexandergalaktionov.physiocare.ui.appointment.AppointmentViewModel
import edu.olexandergalaktionov.physiocare.ui.appointment.AppointmentViewModelFactory
import edu.olexandergalaktionov.physiocare.ui.PhysioViewModel
import edu.olexandergalaktionov.physiocare.ui.PhysioViewModelFactory
import edu.olexandergalaktionov.physiocare.ui.adapters.RecordAdapter
import edu.olexandergalaktionov.physiocare.utils.SessionManager
import edu.olexandergalaktionov.physiocare.utils.checkConnection
import edu.olexandergalaktionov.physiocare.utils.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appointmentAdapter: AppointmentAdapter
    private val sessionManager by lazy { SessionManager(dataStore) }

    private var isPatient = false
    private var patientId: String? = null
    private var showingFutureAppointments = true

    // Gestiona la sesión del usuario
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(PhysioCareRepository(sessionManager))
    }

    // Gestiona las citas del paciente
    private val appointmentViewModel: AppointmentViewModel by viewModels {
        AppointmentViewModelFactory(PhysioCareRepository(sessionManager))
    }

    // Gestiona las citas del fisioterapeuta
    private val physioViewModel: PhysioViewModel by viewModels {
        PhysioViewModelFactory(PhysioCareRepository(sessionManager))
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

        lifecycleScope.launch {
            val token = sessionManager.sessionFlow.first().first
            if (token == null) {
                showLoginDialog()
            } else {
                loadData()
            }
        }

        setupRecyclerView()
        setupButtons()
        setupToolbar()
        observeViewModels()
        handleSessionOnLaunch()
        setupBottomNavigationView()
    }

    private fun setupBottomNavigationView() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_appointments -> {
                    showAppointments()  // Muestra las citas
                    true
                }
                R.id.nav_records -> {
                    showRecords()  // Muestra los registros
                    true
                }
                else -> false
            }
        }
    }

    private fun showAppointments() {
        // Lógica para mostrar citas
        loadData()
        Toast.makeText(this, "Citas seleccionadas", Toast.LENGTH_SHORT).show()
    }

    private fun showRecords() {
        // Lógica para mostrar registros
        loadRecords()
        Toast.makeText(this, "Registros seleccionados", Toast.LENGTH_SHORT).show()
    }

    private fun loadRecords() {
        lifecycleScope.launch {
            physioViewModel.loadRecordsForPhysio()  // Cargar los registros del fisioterapeuta

            physioViewModel.records.collect { records ->
                if (records.isNotEmpty()) {
                    Log.i("RECORDS", "Registros recibidos: ${records.size}")
                    // Asignar el adapter con los registros
                    val recordAdapter = RecordAdapter(records)
                    binding.recyclerView.adapter = recordAdapter
                    binding.noDataText.visibility = View.GONE
                } else {
                    binding.noDataText.visibility = View.VISIBLE
                }
            }
        }
    }


    private fun handleSessionOnLaunch() {
        lifecycleScope.launch {
            mainViewModel.loginState.collect { state ->
                when (state) {
                    is LoginState.Loading -> Log.i("LOGIN", "Cargando...")
                    is LoginState.Success -> handleLoginSuccess(state)
                    is LoginState.Error -> {
                        Toast.makeText(this@MainActivity, "Login fallido: ${state.message}", Toast.LENGTH_SHORT).show()
                        mainViewModel.resetLoginState()
                    }
                    else -> {}
                }
            }
        }
    }


    private fun setupRecyclerView() {
        // Verifica si es fisioterapeuta
        val isPhysio = !isPatient

        appointmentAdapter = AppointmentAdapter(
            onItemClick = { appointment ->
                if (!showingFutureAppointments) {
                    val intent = Intent(this, AppointmentDetailActivity::class.java)
                    intent.putExtra("appointmentId", appointment._id)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Solo puedes ver los detalles de consultas ya realizadas", Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteClick = { appointment ->
                // Solo ejecutamos si no es null y es rol de fisioterapeuta
                if (isPhysio && appointment._id != null && patientId != null) {
                    physioViewModel.deleteAppointmentById(appointment._id, patientId!!)
                }
            },
            isPhysio = isPhysio // Pasa el valor de isPhysio //TODO
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = appointmentAdapter
    }



    private fun setupButtons() {
        binding.btnUpcoming.setOnClickListener {
            showingFutureAppointments = true
            appointmentAdapter.submitList(appointmentViewModel.futureAppointments.value)
        }

        binding.btnPast.setOnClickListener {
            showingFutureAppointments = false
            appointmentAdapter.submitList(appointmentViewModel.pastAppointments.value)
        }

        binding.swipeRefresh.setOnRefreshListener {
            loadData()
        }
    }


    private fun setupToolbar() {
        binding.mToolbar.inflateMenu(R.menu.main_menu)
        updateToolbarMenu()

        binding.mToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_login -> {
                    showLoginDialog(); true
                }
                R.id.action_logout -> {
                    lifecycleScope.launch {
                        sessionManager.clearSession()
                        mainViewModel.logout()
                        updateToolbarMenu()
                        appointmentAdapter.submitList(emptyList())
                        binding.filterButtons.visibility = View.GONE
                        Toast.makeText(this@MainActivity, "Sesión cerrada", Toast.LENGTH_SHORT).show()
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
    }


    private fun observeViewModels() {
        // Observa los cambios en las citas del fisioterapeuta
        lifecycleScope.launch {
            physioViewModel.appointments.collect {
                Log.i("VIEWMODEL_COLLECT", "isPatient: $isPatient - citas recibidas: ${it.size}")
                if (!isPatient) {
                    appointmentAdapter.submitList(it)
                    updateEmptyView(it.size)
                }
            }
        }

        // Observa los cambios en las citas del paciente
        lifecycleScope.launch {
            appointmentViewModel.futureAppointments.collect {
                if (isPatient && showingFutureAppointments) {
                    appointmentAdapter.submitList(it)
                    updateEmptyView(it.size)
                }
            }
        }

        // Observa los cambios en las citas pasadas del paciente
        lifecycleScope.launch {
            appointmentViewModel.pastAppointments.collect {
                if (isPatient && !showingFutureAppointments) {
                    appointmentAdapter.submitList(it)
                    updateEmptyView(it.size)
                }
            }
        }

        // Observa los cambios en el estado de error
        lifecycleScope.launch {
            appointmentViewModel.error.collect {
                it?.let { msg -> Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show() }
            }
        }

        lifecycleScope.launch {
            physioViewModel.records.collect { records ->
                if (records.isNotEmpty()) {
                    // Asignar el adaptador con los registros
                    val recordAdapter = RecordAdapter(records)
                    binding.recyclerView.adapter = recordAdapter
                    binding.noDataText.visibility = View.GONE
                } else {
                    binding.noDataText.visibility = View.VISIBLE
                }
            }
        }
    }


    private fun handleLoginSuccess(state: LoginState.Success) {
        lifecycleScope.launch {
            sessionManager.saveSession(
                state.response.token!!,
                state.response.usuarioId!!,
                state.response.rol!!
            )
            isPatient = state.response.rol == "patient"
            patientId = state.response.usuarioId

            if (isPatient) {
                appointmentViewModel.fetchAppointmentsByPatient(patientId!!)
                observeViewModels()
                binding.filterButtons.visibility = View.VISIBLE
            } else {
                physioViewModel.loadAppointmentsForPhysio(patientId!!)
                observeViewModels()
                binding.filterButtons.visibility = View.GONE
            }

            updateToolbarMenu()
        }
    }


    private fun loadData() {
        lifecycleScope.launch {
            binding.swipeRefresh.isRefreshing = true
            Log.i("LOAD_DATA", "Cargando datos...")

            if (!checkConnection(this@MainActivity)) {
                Toast.makeText(this@MainActivity, "Sin conexión", Toast.LENGTH_SHORT).show()
                clearAppointments()
                binding.swipeRefresh.isRefreshing = false
                return@launch
            }

            val token = sessionManager.sessionFlow.first().first
            val role = sessionManager.roleFlow.first()
            val userId = sessionManager.userIdFlow.first()

            Log.i("LOAD_DATA", "Token: $token, Rol: $role, ID: $userId")

            if (token == null || role == null || userId == null) {
                clearAppointments()
                showLoginDialog()
                binding.swipeRefresh.isRefreshing = false
                return@launch
            }

            isPatient = role == "patient"
            patientId = userId

            try {
                if (isPatient) {
                    appointmentViewModel.fetchAppointmentsByPatient(userId)
                    observeViewModels()
                    Log.i("LOAD_DATA", "Citas del paciente cargadas")
                    binding.filterButtons.visibility = View.VISIBLE
                } else {
                    physioViewModel.loadAppointmentsForPhysio(userId)
                    showingFutureAppointments = false // Permite al fisio ver todas las citas
                    observeViewModels()
                    Log.i("LOAD_DATA", "Citas del fisioterapeuta cargadas")
                    binding.filterButtons.visibility = View.GONE
                }
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 401) {// Si ocurre un error token
                    sessionManager.clearSession()  // Limpiar la sesión
                    mainViewModel.logout()
                    updateToolbarMenu()
                    showLoginDialog()  // Mostrar el cuadro de diálogo de login
                } else {
                    Log.e("LOAD_DATA", "Error: ${e.message}")
                    Toast.makeText(this@MainActivity, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                }
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }


    private fun clearAppointments() {
        appointmentAdapter.submitList(emptyList())
    }


    private fun updateToolbarMenu() {
        lifecycleScope.launch {
            val (token, _) = sessionManager.sessionFlow.first()
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
            .setTitle("Login")
            .setView(view)
            .setPositiveButton("Login") { _, _ ->
                val username = etUsername.text.toString()
                val password = etPassword.text.toString()

                if (!checkConnection(this)) {
                    Toast.makeText(this, "Sin conexión", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                mainViewModel.login(username, password)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun updateEmptyView(listSize: Int) {
        binding.noDataText.visibility = if (listSize == 0) View.VISIBLE else View.GONE
    }
}
