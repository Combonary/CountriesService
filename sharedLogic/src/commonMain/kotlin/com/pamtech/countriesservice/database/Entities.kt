package com.pamtech.countriesservice.database

import androidx.room.*

@Entity
data class CountryEntity(
    @PrimaryKey val code: String,
    val name: String,
    val emoji: String,
    val continentName: String,
    val native: String? = null,
    val phone: String? = null,
    val capital: String? = null,
    val currency: String? = null
)

@Entity
data class ContinentEntity(
    @PrimaryKey val code: String,
    val name: String
)

@Entity(primaryKeys = ["countryCode", "language"])
data class CountryLanguageEntity(
    val countryCode: String,
    val language: String
)

@Dao
interface CountryDao {
    @Query("SELECT * FROM CountryEntity")
    suspend fun getAllCountries(): List<CountryEntity>

    @Query("SELECT * FROM CountryEntity WHERE code = :code")
    suspend fun getCountryByCode(code: String): CountryEntity?

    @Query("SELECT language FROM CountryLanguageEntity WHERE countryCode = :code")
    suspend fun getLanguagesForCountry(code: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountries(countries: List<CountryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountry(country: CountryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLanguages(languages: List<CountryLanguageEntity>)
    
    @Query("DELETE FROM CountryLanguageEntity WHERE countryCode = :code")
    suspend fun deleteLanguagesForCountry(code: String)
}

@Dao
interface ContinentDao {
    @Query("SELECT * FROM ContinentEntity")
    suspend fun getAllContinents(): List<ContinentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContinents(continents: List<ContinentEntity>)
}
