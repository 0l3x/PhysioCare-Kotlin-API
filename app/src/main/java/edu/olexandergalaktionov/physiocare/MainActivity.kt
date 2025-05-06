package edu.olexandergalaktionov.physiocare

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import edu.olexandergalaktionov.physiocare.data.PhysioCareRepository
import edu.olexandergalaktionov.physiocare.databinding.ActivityMainBinding
import edu.olexandergalaktionov.physiocare.model.LoginRequest
import edu.olexandergalaktionov.physiocare.ui.AppointmentAdapter
import edu.olexandergalaktionov.physiocare.ui.AppointmentViewModel
import edu.olexandergalaktionov.physiocare.ui.AppointmentViewModelFactory
import edu.olexandergalaktionov.physiocare.utils.SessionManager
import edu.olexandergalaktionov.physiocare.utils.dataStore
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

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

        // Observe login state and handle UI response
        lifecycleScope.launch {
            // Simulate a login request
            val loginRequest = LoginRequest("laura.torres", "1234")
            val response = PhysioCareRepository(SessionManager(dataStore)).login(loginRequest)

            if (response.token != null) {
                // Handle successful login
                Log.d("MainActivity", "Login successful: ${response.token}")

                val adapter = AppointmentAdapter()
                binding.recyclerView.adapter = adapter

                appointmentViewModel.fetchAppointments()

                lifecycleScope.launch {
                    appointmentViewModel.appointmentList.collect { appointments ->
                        adapter.submitList(appointments)
                        Log.d("MainActivity", "Appointments: $appointments")

                        val repository = PhysioCareRepository(SessionManager(dataStore))
                        val records = repository.getAllRecords()
                        for (record in records.resultado!!) {
                            Log.d("RECORD", "ID: ${record.id}, Patient: ${record.patient}, Note: ${record.medicalRecord}")
                        }
                    }
                }


            } else {
                // Handle login error
                Log.d("MainActivity", "Login failed: ${response.error}")
            }



        }


    }


}