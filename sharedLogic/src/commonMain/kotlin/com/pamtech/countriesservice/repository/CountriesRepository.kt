package com.pamtech.countriesservice.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.exception.ApolloException
import com.pamtech.countriesservice.database.*
import com.pamtech.countriesservice.graphql.GetContinentsQuery
import com.pamtech.countriesservice.graphql.GetCountriesQuery
import com.pamtech.countriesservice.graphql.GetCountryQuery
import com.pamtech.countriesservice.graphql.GetStatesQuery
import com.pamtech.countriesservice.model.*
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName("CountriesRepository")
class CountriesRepository(
    private val apolloClient: ApolloClient,
    private val countryDao: CountryDao,
    private val continentDao: ContinentDao,
    private val statesDao: StatesDao
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
                // Note: We don't have continentCode here in the simplified Country model,
                // this might need careful handling depending on use case.
                // For now, we assume we update what we have.
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

    suspend fun getCountriesByContinent(continentCode: String): LibraryResult<List<Country>> {
        return safeCall(
            cacheCall = {
                countryDao.getCountriesByContinent(continentCode).map {
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
                        continentName = it.continentName,
                        continentCode = continentCode
                    )
                })
            },
            networkCall = {
                val response = apolloClient.query(com.pamtech.countriesservice.graphql.GetCountriesByContinentQuery(com.apollographql.apollo.api.Optional.present(continentCode))).execute()
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
                // Note: continentCode is required now.
                // We need to fetch it from network or it.continent.code
                // For now, we'll need to adjust the network mapping to pass it here.
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

    suspend fun getStates(countryCode: String): LibraryResult<List<State>> {
        return safeCall(
            cacheCall = {
                statesDao.getStatesForCountry(countryCode).map {
                    State(
                        code = it.code,
                        name = it.name
                    )
                }
            },
            saveCache = { states ->
                statesDao.deleteStatesForCountry(countryCode)
                statesDao.insertStates(states.map {
                    StateEntity(
                        countryCode = countryCode,
                        code = it.code,
                        name = it.name
                    )
                })
            },
            networkCall = {
                val response = apolloClient.query(GetStatesQuery(countryCode)).execute()
                response.data?.country?.states?.map {
                    State(
                        code = it.code,
                        name = it.name
                    )
                } ?: throw Exception("Country or states not found")
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
