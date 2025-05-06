package edu.olexandergalaktionov.physiocare

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
import edu.olexandergalaktionov.physiocare.data.PhysioCareRepository
import edu.olexandergalaktionov.physiocare.databinding.ActivityMainBinding
import edu.olexandergalaktionov.physiocare.model.LoginState
import edu.olexandergalaktionov.physiocare.ui.AppointmentViewModel
import edu.olexandergalaktionov.physiocare.ui.AppointmentViewModelFactory
import edu.olexandergalaktionov.physiocare.ui.MainViewModel
import edu.olexandergalaktionov.physiocare.ui.MainViewModelFactory
import edu.olexandergalaktionov.physiocare.utils.SessionManager
import edu.olexandergalaktionov.physiocare.utils.checkConnection
import edu.olexandergalaktionov.physiocare.utils.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import edu.olexandergalaktionov.physiocare.ui.fragment.FragmentPhysioDashboard
import edu.olexandergalaktionov.physiocare.ui.fragment.FragmentPatientDashboard


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

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

        lifecycleScope.launch {
            val (token, username) = SessionManager(dataStore).sessionFlow.first()
            if (token == null) {
                showLoginDialog()
            } else {
                if (username != null) {
                    launchFragmentBasedOnRole(username)
                }
            }
        }

        lifecycleScope.launch {
            mainViewModel.loginState.collect { state ->
                when (state) {
                    is LoginState.Loading -> Log.i("LOGIN", "Cargando...")
                    is LoginState.Success -> {
                        Log.i("LOGIN", "Login OK: ${state.response.token}")
                        SessionManager(dataStore).saveSession(state.response.token!!, state.response.rol!!)
                        launchFragmentBasedOnRole(state.response.rol)
                    }
                    is LoginState.Error -> {
                        Toast.makeText(this@MainActivity, "Login fallido: ${state.message}", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
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

    private fun launchFragmentBasedOnRole(username: String) {
        // Aquí iría una llamada a tu endpoint para saber si es physio o patient
        // Puedes mockear por ahora con una condición
        val isPhysio = username.contains("torres") // TODO: Reemplaza con validación real

        val fragment = if (isPhysio) {
            FragmentPhysioDashboard()
        } else {
            FragmentPatientDashboard()
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, fragment)
            .commit()
    }
}
