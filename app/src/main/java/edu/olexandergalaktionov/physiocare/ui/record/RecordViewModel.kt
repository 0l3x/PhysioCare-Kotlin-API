package edu.olexandergalaktionov.physiocare.ui.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import edu.olexandergalaktionov.physiocare.data.PhysioCareRepository

/**
 * ViewModel para manejar Records.
 */
class RecordViewModel(private val repository: PhysioCareRepository) : ViewModel() {

    /**
     * Consigue todos los Records.
     */
    suspend fun getAllRecords() = repository.getAllRecords().resultado ?: emptyList()

    /**
     * Consigue un Record por el ID de un paciente.
     */
    suspend fun getRecordByPatientId(id: String) = repository.getRecordByPatientId(id).resultado

}

/**
 * ViewModelFactory para crear instancias de RecordViewModel.
 */
class RecordViewModelFactory(private val repository: PhysioCareRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RecordViewModel(repository) as T
    }
}