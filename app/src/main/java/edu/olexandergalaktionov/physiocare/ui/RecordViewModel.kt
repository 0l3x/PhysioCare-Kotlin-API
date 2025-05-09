package edu.olexandergalaktionov.physiocare.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import edu.olexandergalaktionov.physiocare.data.PhysioCareRepository

class RecordViewModel(private val repository: PhysioCareRepository) : ViewModel() {

    suspend fun getAllRecords() = repository.getAllRecords().resultado ?: emptyList()

    suspend fun getRecordByPatientId(id: String) = repository.getRecordByPatientId(id).resultado

}

class RecordViewModelFactory(private val repository: PhysioCareRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RecordViewModel(repository) as T
    }
}