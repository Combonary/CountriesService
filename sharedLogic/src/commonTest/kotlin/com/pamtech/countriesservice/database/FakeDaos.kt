package com.pamtech.countriesservice.database

class FakeCountryDao : CountryDao {
    private val countries = mutableMapOf<String, CountryEntity>()
    private val languages = mutableMapOf<String, MutableList<String>>()

    override suspend fun getAllCountries(): List<CountryEntity> = countries.values.toList()

    override suspend fun getCountriesByContinent(continentCode: String): List<CountryEntity> =
        countries.values.filter { it.continentCode == continentCode }

    override suspend fun getCountryByCode(code: String): CountryEntity? = countries[code]

    override suspend fun getLanguagesForCountry(code: String): List<String> = languages[code] ?: emptyList()

    override suspend fun insertCountries(countries: List<CountryEntity>) {
        countries.forEach { this.countries[it.code] = it }
    }

    override suspend fun insertCountry(country: CountryEntity) {
        countries[country.code] = country
    }

    override suspend fun insertLanguages(languages: List<CountryLanguageEntity>) {
        languages.forEach { 
            this.languages.getOrPut(it.countryCode) { mutableListOf() }.add(it.language)
        }
    }

    override suspend fun deleteLanguagesForCountry(code: String) {
        languages.remove(code)
    }
}

class FakeStatesDao : StatesDao {
    private val states = mutableMapOf<String, MutableList<StateEntity>>()

    override suspend fun getStatesForCountry(countryCode: String): List<StateEntity> = states[countryCode] ?: emptyList()

    override suspend fun insertStates(states: List<StateEntity>) {
        states.forEach { 
            this.states.getOrPut(it.countryCode) { mutableListOf() }.add(it)
        }
    }

    override suspend fun deleteStatesForCountry(countryCode: String) {
        states.remove(countryCode)
    }
}

class FakeContinentDao : ContinentDao {
    private val continents = mutableMapOf<String, ContinentEntity>()

    override suspend fun getAllContinents(): List<ContinentEntity> = continents.values.toList()

    override suspend fun insertContinents(continents: List<ContinentEntity>) {
        continents.forEach { this.continents[it.code] = it }
    }
}
