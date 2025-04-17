package com.example.journeybuddy.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.CountryRepository
import kotlinx.coroutines.launch

class CountryViewModel(private val countryRepository: CountryRepository) : ViewModel() {

    // Variable pour stocker la liste des pays
    private val _countries = mutableStateOf<List<String>>(emptyList())
    val countries: State<List<String>> = _countries

    // Variable pour gérer l'état de chargement
    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    // Fonction pour récupérer les pays en fonction des intérêts
    fun getCountriesFromApi(interests: List<String>) {
        // Mettre l'état de chargement à vrai avant de récupérer les données
        _loading.value = true

        // Lancer la récupération des données en arrière-plan
        viewModelScope.launch {
            try {
                // Appeler le repository pour obtenir les pays en fonction des intérêts
                val country = countryRepository.getCountry(interests)
                // Mettre à jour la liste des pays une fois les données récupérées
                _countries.value = emptyList()
            } catch (e: Exception) {
                // En cas d'erreur, tu peux mettre à jour l'état pour gérer les erreurs si nécessaire
                _countries.value = emptyList()
            } finally {
                // Réinitialiser l'état de chargement à faux une fois le traitement terminé
                _loading.value = false
            }
        }
    }
}
