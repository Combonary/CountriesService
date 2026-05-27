package com.pamtech.countriesservice.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.exception.ApolloException
import com.pamtech.countriesservice.database.ContinentDao
import com.pamtech.countriesservice.database.CountryDao
import com.pamtech.countriesservice.database.CountryEntity
import com.pamtech.countriesservice.database.ContinentEntity
import com.pamtech.countriesservice.database.CountryLanguageEntity
import com.pamtech.countriesservice.graphql.GetContinentsQuery
import com.pamtech.countriesservice.graphql.GetCountriesQuery
import com.pamtech.countriesservice.graphql.GetCountryQuery
import com.pamtech.countriesservice.model.*
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName("CountriesRepository")
class CountriesRepository(
    private val apolloClient: ApolloClient,
    private val countryDao: CountryDao,
    private val continentDao: ContinentDao
) {

    suspend fun getCountries(): LibraryResult<List<Country>> {
        return safeCall(
            cacheCall = {
                countryDao.getAllCountries().map {
                    Country(
                        code = it.code,
                        name = it.name,
                        emoji = it.emoji,
                        continentName = it.continentName
                    )
                }
            },
            saveCache = { countries ->
                countryDao.insertCountries(countries.map {
                    CountryEntity(
                        code = it.code,
                        name = it.name,
                        emoji = it.emoji,
                        continentName = it.continentName
                    )
                })
            },
            networkCall = {
                val response = apolloClient.query(GetCountriesQuery()).execute()
                response.data?.countries?.map {
                    Country(
                        code = it.code,
                        name = it.name,
                        emoji = it.emoji,
                        continentName = it.continent.name
                    )
                } ?: throw Exception("Empty response")
            }
        )
    }

    suspend fun getCountry(code: String): LibraryResult<CountryDetail> {
        return safeCall(
            cacheCall = {
                val entity = countryDao.getCountryByCode(code)
                if (entity != null) {
                    val languages = countryDao.getLanguagesForCountry(code)
                    CountryDetail(
                        code = entity.code,
                        name = entity.name,
                        native = entity.native ?: "",
                        phone = entity.phone ?: "",
                        capital = entity.capital,
                        currency = entity.currency,
                        emoji = entity.emoji,
                        continentName = entity.continentName,
                        languages = languages
                    )
                } else null
            },
            saveCache = { detail ->
                countryDao.insertCountry(
                    CountryEntity(
                        code = detail.code,
                        name = detail.name,
                        emoji = detail.emoji,
                        continentName = detail.continentName,
                        native = detail.native,
                        phone = detail.phone,
                        capital = detail.capital,
                        currency = detail.currency
                    )
                )
                countryDao.deleteLanguagesForCountry(detail.code)
                countryDao.insertLanguages(detail.languages.map {
                    CountryLanguageEntity(detail.code, it)
                })
            },
            networkCall = {
                val response = apolloClient.query(GetCountryQuery(code)).execute()
                response.data?.country?.let {
                    CountryDetail(
                        code = it.code,
                        name = it.name,
                        native = it.native,
                        phone = it.phone,
                        capital = it.capital,
                        currency = it.currency,
                        emoji = it.emoji,
                        continentName = it.continent.name,
                        languages = it.languages.map { lang -> lang.name ?: "" }
                    )
                } ?: throw Exception("Country not found")
            }
        )
    }

    suspend fun getContinents(): LibraryResult<List<Continent>> {
        return safeCall(
            cacheCall = {
                continentDao.getAllContinents().map {
                    Continent(
                        code = it.code,
                        name = it.name
                    )
                }
            },
            saveCache = { continents ->
                continentDao.insertContinents(continents.map {
                    ContinentEntity(
                        code = it.code,
                        name = it.name
                    )
                })
            },
            networkCall = {
                val response = apolloClient.query(GetContinentsQuery()).execute()
                response.data?.continents?.map {
                    Continent(
                        code = it.code,
                        name = it.name
                    )
                } ?: throw Exception("Empty response")
            }
        )
    }

    private suspend fun <T> safeCall(
        cacheCall: suspend () -> T?,
        saveCache: suspend (T) -> Unit,
        networkCall: suspend () -> T
    ): LibraryResult<T> {
        return try {
            val networkData = networkCall()
            saveCache(networkData)
            LibraryResult.Success(networkData)
        } catch (e: Exception) {
            val cachedData = cacheCall()
            if (cachedData != null && (cachedData !is List<*> || cachedData.isNotEmpty())) {
                LibraryResult.Success(cachedData)
            } else {
                val error = when (e) {
                    is ApolloException -> LibraryError.Network("Network error: ${e.message}", e)
                    else -> LibraryError.Unknown(e.message ?: "Unknown error", e)
                }
                LibraryResult.Failure(error)
            }
        }
    }
}
