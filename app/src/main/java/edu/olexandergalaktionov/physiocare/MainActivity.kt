package edu.olexandergalaktionov.physiocare

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
import edu.olexandergalaktionov.physiocare.data.PhysioCareRepository
import edu.olexandergalaktionov.physiocare.databinding.ActivityMainBinding
import edu.olexandergalaktionov.physiocare.model.Appointment
import edu.olexandergalaktionov.physiocare.model.LoginState
import edu.olexandergalaktionov.physiocare.ui.AppointmentAdapter
import edu.olexandergalaktionov.physiocare.ui.AppointmentViewModel
import edu.olexandergalaktionov.physiocare.ui.AppointmentViewModelFactory
import edu.olexandergalaktionov.physiocare.ui.MainViewModel
import edu.olexandergalaktionov.physiocare.ui.MainViewModelFactory
import edu.olexandergalaktionov.physiocare.utils.SessionManager
import edu.olexandergalaktionov.physiocare.utils.checkConnection
import edu.olexandergalaktionov.physiocare.utils.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant

import java.time.LocalDate
import java.time.ZoneOffset


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var allAppointments = listOf<Appointment>()
    private var isPatient = false
    private lateinit var appointmentAdapter: AppointmentAdapter

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

        appointmentAdapter = AppointmentAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = appointmentAdapter

        lifecycleScope.launch {
            val (token, username) = SessionManager(dataStore).sessionFlow.first()
            Log.i("SESSION", "Token: $token, Username: $username")

                SessionManager(dataStore).clearSession() // <- BORRA LA SESIÓN para probar el login
            Log.i("Sesion", "Sesión borrada")

            if (token == null) {
                showLoginDialog()
            } else {
                loadAppointments()
            }
        }

        lifecycleScope.launch {
            mainViewModel.loginState.collect { state ->
                when (state) {
                    is LoginState.Loading -> Log.i("LOGIN", "Cargando...")
                    is LoginState.Success -> {
                        Log.i("LOGIN", "Login OK: ${state.response.token}")
                        SessionManager(dataStore).saveSession(state.response.token!!, state.response.rol!!)
                        isPatient = state.response.rol == "patient"
                        loadAppointments()
                    }
                    is LoginState.Error -> {
                        Toast.makeText(this@MainActivity, "Login fallido: ${state.message}", Toast.LENGTH_SHORT).show()
                        mainViewModel.resetLoginState()
                    }
                    else -> {}
                }
            }
        }

        binding.btnUpcoming.setOnClickListener {
            filterAppointments(upcoming = true)
            Log.i("FILTER", "Filtrando citas futuras")
        }
        binding.btnPast.setOnClickListener {
            filterAppointments(upcoming = false)
            Log.i("FILTER", "Filtrando citas pasadas")
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
                        mainViewModel.logout() // si tienes esa función, opcional
                        updateToolbarMenu()
                        appointmentAdapter.submitList(emptyList())
                        Toast.makeText(this@MainActivity, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                    }
                    true
                }

                else -> false
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                if (!checkConnection(this@MainActivity)) {
                    Toast.makeText(this@MainActivity, "Sin conexión", Toast.LENGTH_SHORT).show()
                    binding.swipeRefresh.isRefreshing = false
                    return@launch
                }

                appointmentViewModel.fetchAppointments()
                appointmentViewModel.appointmentList.collect { appointments ->
                    allAppointments = appointments
                    if (isPatient) {
                        filterAppointments(upcoming = true)
                    } else {
                        appointmentAdapter.submitList(allAppointments)
                    }
                    binding.swipeRefresh.isRefreshing = false
                    return@collect
                }
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

    private fun loadAppointments() {
        lifecycleScope.launch {
            appointmentViewModel.fetchAppointments()
            appointmentViewModel.appointmentList.collect { appointments ->
                allAppointments = appointments
                if (isPatient) {
                    binding.filterButtons.visibility = View.VISIBLE
                    filterAppointments(upcoming = true)
                } else {
                    binding.filterButtons.visibility = View.GONE
                    appointmentAdapter.submitList(allAppointments)
                }
            }
        }
    }

    private fun filterAppointments(upcoming: Boolean) {
        val today = LocalDate.now()

        val filtered = allAppointments.filter {
            try {
                val date = Instant.parse(it.date).atZone(ZoneOffset.UTC).toLocalDate()
                if (upcoming) date.isAfter(today) || date.isEqual(today)
                else date.isBefore(today)
            } catch (e: Exception) {
                false // Si el formato está mal, lo ignoramos
            }
        }

        appointmentAdapter.submitList(filtered)
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
