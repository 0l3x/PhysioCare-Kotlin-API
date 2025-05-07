package edu.olexandergalaktionov.physiocare

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import edu.olexandergalaktionov.physiocare.data.PhysioCareRepository
import edu.olexandergalaktionov.physiocare.databinding.ActivityMainBinding
import edu.olexandergalaktionov.physiocare.model.LoginState
import edu.olexandergalaktionov.physiocare.ui.AppointmentAdapter
import edu.olexandergalaktionov.physiocare.ui.AppointmentDetailActivityX
import edu.olexandergalaktionov.physiocare.ui.AppointmentViewModel
import edu.olexandergalaktionov.physiocare.ui.AppointmentViewModelFactory
import edu.olexandergalaktionov.physiocare.ui.MainViewModel
import edu.olexandergalaktionov.physiocare.ui.MainViewModelFactory
import edu.olexandergalaktionov.physiocare.utils.SessionManager
import edu.olexandergalaktionov.physiocare.utils.checkConnection
import edu.olexandergalaktionov.physiocare.utils.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appointmentAdapter: AppointmentAdapter

    private var isPatient = false
    private var patientId: String? = null
    private var showingFutureAppointments = true

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(PhysioCareRepository(SessionManager(dataStore)))
    }

    private val appointmentViewModel: AppointmentViewModel by viewModels {
        AppointmentViewModelFactory(PhysioCareRepository(SessionManager(dataStore)))
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

        appointmentAdapter = AppointmentAdapter { appointment ->
            val intent = Intent(this@MainActivity, AppointmentDetailActivityX::class.java)
            intent.putExtra("appointmentId", appointment._id)
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = appointmentAdapter

        binding.btnUpcoming.setOnClickListener {
            showingFutureAppointments = true
            appointmentAdapter.submitList(appointmentViewModel.futureAppointments.value)
        }

        binding.btnPast.setOnClickListener {
            showingFutureAppointments = false
            appointmentAdapter.submitList(appointmentViewModel.pastAppointments.value)
        }

        lifecycleScope.launch {
            val token = SessionManager(dataStore).sessionFlow.first().first
            val role = SessionManager(dataStore).roleFlow.first()
            val userId = SessionManager(dataStore).userIdFlow.first()

            if (token == null) {
                showLoginDialog()
            } else {
                updateToolbarMenu()
                isPatient = role == "patient"
                patientId = userId

                if (isPatient && patientId != null) {
                    appointmentViewModel.fetchAppointmentsByPatient(patientId!!)
                }
            }
        }

        lifecycleScope.launch {
            mainViewModel.loginState.collect { state ->
                when (state) {
                    is LoginState.Loading -> Log.i("LOGIN", "Cargando...")
                    is LoginState.Success -> {
                        SessionManager(dataStore).saveSession(
                            state.response.token!!,
                            state.response.rol!!,
                            state.response.usuarioId!!,
                            state.response.rol
                        )
                        isPatient = state.response.rol == "patient"
                        patientId = state.response.usuarioId
                        if (isPatient) {
                            appointmentViewModel.fetchAppointmentsByPatient(patientId!!)
                        }
                    }
                    is LoginState.Error -> {
                        Toast.makeText(this@MainActivity, "Login fallido: ${state.message}", Toast.LENGTH_SHORT).show()
                        mainViewModel.resetLoginState()
                    }
                    else -> {}
                }
            }
        }

        lifecycleScope.launch {
            appointmentViewModel.futureAppointments.collect {
                if (isPatient && showingFutureAppointments) {
                    appointmentAdapter.submitList(it)
                }
            }
        }

        lifecycleScope.launch {
            appointmentViewModel.pastAppointments.collect {
                if (isPatient && !showingFutureAppointments) {
                    appointmentAdapter.submitList(it)
                }
            }
        }

        lifecycleScope.launch {
            appointmentViewModel.error.collect { err ->
                err?.let { Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show() }
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                if (!checkConnection(this@MainActivity)) {
                    Toast.makeText(this@MainActivity, "Sin conexión", Toast.LENGTH_SHORT).show()
                    binding.swipeRefresh.isRefreshing = false
                    return@launch
                }

                patientId?.let {
                    appointmentViewModel.fetchAppointmentsByPatient(it)
                }
                binding.swipeRefresh.isRefreshing = false
            }
        }

        binding.mToolbar.inflateMenu(R.menu.main_menu)
        updateToolbarMenu()

        binding.mToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_login -> {
                    showLoginDialog()
                    true
                }

                R.id.action_logout -> {
                    lifecycleScope.launch {
                        SessionManager(dataStore).clearSession()
                        mainViewModel.logout()
                        updateToolbarMenu()
                        appointmentAdapter.submitList(emptyList())
                        Toast.makeText(this@MainActivity, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                    }
                    true
                }

                else -> false
            }
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
}
