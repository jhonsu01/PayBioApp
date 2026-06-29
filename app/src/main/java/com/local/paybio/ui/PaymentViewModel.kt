package com.local.paybio.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.local.paybio.data.CatalogCountry
import com.local.paybio.data.PaymentCatalog
import com.local.paybio.data.PaymentMethod
import com.local.paybio.data.PaymentRepository
import com.local.paybio.ingest.IngestEngine
import com.local.paybio.ingest.IngestSuggestion
import com.local.paybio.ingest.PaymentClassifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface IngestUiState {
    data object Idle : IngestUiState
    data object Loading : IngestUiState
    data class Done(val suggestions: List<IngestSuggestion>) : IngestUiState
}

class PaymentViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = PaymentRepository(app)

    val catalog: List<CatalogCountry> = PaymentCatalog.load(app)

    val methods: StateFlow<List<PaymentMethod>> = repo.observeAll()
        .catch { emit(emptyList()) } // survive DB close during backup restore
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _ingest = MutableStateFlow<IngestUiState>(IngestUiState.Idle)
    val ingest: StateFlow<IngestUiState> = _ingest.asStateFlow()

    /** Carries a pre-filled draft from ingestion into the Add/Edit screen. */
    var pendingPrefill: PaymentMethod? = null

    fun ingestFromImage(uri: Uri) {
        viewModelScope.launch {
            _ingest.value = IngestUiState.Loading
            val out = IngestEngine.fromImage(getApplication(), uri)
            val suggestions = PaymentClassifier.classify(getApplication(), out.text, out.barcodes)
            _ingest.value = IngestUiState.Done(suggestions)
        }
    }

    fun ingestFromText(text: String) {
        viewModelScope.launch {
            _ingest.value = IngestUiState.Loading
            val suggestions = PaymentClassifier.classify(getApplication(), text, emptyList())
            _ingest.value = IngestUiState.Done(suggestions)
        }
    }

    fun resetIngest() {
        _ingest.value = IngestUiState.Idle
    }

    fun save(method: PaymentMethod) {
        viewModelScope.launch { repo.save(method) }
    }

    fun delete(method: PaymentMethod) {
        viewModelScope.launch { repo.delete(method) }
    }

    fun toggleFavorite(method: PaymentMethod) {
        viewModelScope.launch { repo.save(method.copy(isFavorite = !method.isFavorite)) }
    }

    suspend fun getById(id: Int): PaymentMethod? = repo.getById(id)
}
