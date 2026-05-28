package com.pamtech.countriesservice.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.QueueTestNetworkTransport
import com.apollographql.apollo.testing.enqueueTestResponse
import com.pamtech.countriesservice.database.FakeContinentDao
import com.pamtech.countriesservice.database.FakeCountryDao
import com.pamtech.countriesservice.database.FakeStatesDao
import com.pamtech.countriesservice.graphql.GetContinentsQuery
import com.pamtech.countriesservice.graphql.GetCountriesByContinentQuery
import com.pamtech.countriesservice.graphql.GetCountriesQuery
import com.pamtech.countriesservice.graphql.GetCountryQuery
import com.pamtech.countriesservice.graphql.GetStatesQuery
import com.pamtech.countriesservice.graphql.builder.Data
import com.pamtech.countriesservice.graphql.builder.buildContinent
import com.pamtech.countriesservice.graphql.builder.buildCountry
import com.pamtech.countriesservice.graphql.builder.buildLanguage
import com.pamtech.countriesservice.graphql.builder.buildState
import com.pamtech.countriesservice.model.LibraryResult
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CountriesRepositoryTest {

    private lateinit var countryDao: FakeCountryDao
    private lateinit var continentDao: FakeContinentDao
    private lateinit var statesDao: FakeStatesDao

    @BeforeTest
    fun setup() {
        countryDao = FakeCountryDao()
        continentDao = FakeContinentDao()
        statesDao = FakeStatesDao()
    }

    @OptIn(ApolloExperimental::class)
    @Test
    fun getCountriesMapping() = runTest {
        val apolloClient = ApolloClient.Builder()
            .networkTransport(QueueTestNetworkTransport())
            .build()
        val repository = CountriesRepository(apolloClient, countryDao, continentDao, statesDao)

        val query = GetCountriesQuery()
        val testData = GetCountriesQuery.Companion.Data {
            countries = listOf(
                buildCountry {
                    code = "AD"
                    name = "Andorra"
                    emoji = "🇦🇩"
                    continent = buildContinent {
                        name = "Europe"
                    }
                }
            )
        }
        apolloClient.enqueueTestResponse(query, testData)

        val result = repository.getCountries()
        assertTrue(result is LibraryResult.Success)
        val countries = result.data

        assertEquals(1, countries.size)
        assertEquals("AD", countries[0].code)
        assertEquals("Andorra", countries[0].name)
        assertEquals("🇦🇩", countries[0].emoji)
        assertEquals("Europe", countries[0].continentName)
    }

    @OptIn(ApolloExperimental::class)
    @Test
    fun getCountryMapping() = runTest {
        val apolloClient = ApolloClient.Builder()
            .networkTransport(QueueTestNetworkTransport())
            .build()
        val repository = CountriesRepository(apolloClient, countryDao, continentDao, statesDao)

        val query = GetCountryQuery("AD")
        val testData = GetCountryQuery.Companion.Data {
            country = buildCountry {
                code = "AD"
                name = "Andorra"
                native = "Andorra"
                phone = "376"
                capital = "Andorra la Vella"
                currency = "EUR"
                emoji = "🇦🇩"
                continent = buildContinent {
                    name = "Europe"
                }
                languages = listOf(
                    buildLanguage {
                        name = "Catalan"
                    }
                )
            }
        }
        apolloClient.enqueueTestResponse(query, testData)

        val result = repository.getCountry("AD")
        assertTrue(result is LibraryResult.Success)
        val country = result.data

        assertNotNull(country)
        assertEquals("AD", country.code)
        assertEquals("Andorra", country.name)
        assertEquals("Andorra la Vella", country.capital)
        assertEquals("EUR", country.currency)
        assertEquals(1, country.languages.size)
        assertEquals("Catalan", country.languages[0])
    }

    @OptIn(ApolloExperimental::class)
    @Test
    fun getContinentsMapping() = runTest {
        val apolloClient = ApolloClient.Builder()
            .networkTransport(QueueTestNetworkTransport())
            .build()
        val repository = CountriesRepository(apolloClient, countryDao, continentDao, statesDao)

        val query = GetContinentsQuery()
        val testData = GetContinentsQuery.Companion.Data {
            continents = listOf(
                buildContinent {
                    code = "EU"
                    name = "Europe"
                }
            )
        }
        apolloClient.enqueueTestResponse(query, testData)

        val result = repository.getContinents()
        assertTrue(result is LibraryResult.Success)
        val continents = result.data

        assertEquals(1, continents.size)
        assertEquals("EU", continents[0].code)
        assertEquals("Europe", continents[0].name)
    }

    @OptIn(ApolloExperimental::class)
    @Test
    fun getCountriesEmptyResponse() = runTest {
        val apolloClient = ApolloClient.Builder()
            .networkTransport(QueueTestNetworkTransport())
            .build()
        val repository = CountriesRepository(apolloClient, countryDao, continentDao, statesDao)

        val query = GetCountriesQuery()
        val testData = GetCountriesQuery.Companion.Data {
            countries = emptyList()
        }
        apolloClient.enqueueTestResponse(query, testData)

        val result = repository.getCountries()
        assertTrue(result is LibraryResult.Success)
        val countries = result.data

        assertTrue(countries.isEmpty())
    }

    @OptIn(ApolloExperimental::class)
    @Test
    fun getStatesMapping() = runTest {
        val apolloClient = ApolloClient.Builder()
            .networkTransport(QueueTestNetworkTransport())
            .build()
        val repository = CountriesRepository(apolloClient, countryDao, continentDao, statesDao)

        val query = GetStatesQuery("US")
        val testData = GetStatesQuery.Companion.Data {
            country = buildCountry {
                states = listOf(
                    buildState {
                        code = "CA"
                        name = "California"
                    },
                    buildState {
                        code = "NY"
                        name = "New York"
                    }
                )
            }
        }
        apolloClient.enqueueTestResponse(query, testData)

        val result = repository.getStates("US")
        assertTrue(result is LibraryResult.Success)
        val states = result.data

        assertEquals(2, states.size)
        assertEquals("CA", states[0].code)
        assertEquals("California", states[0].name)
        assertEquals("NY", states[1].code)
        assertEquals("New York", states[1].name)
    }

    @OptIn(ApolloExperimental::class)
    @Test
    fun getCountriesByContinentMapping() = runTest {
        val apolloClient = ApolloClient.Builder()
            .networkTransport(QueueTestNetworkTransport())
            .build()
        val repository = CountriesRepository(apolloClient, countryDao, continentDao, statesDao)

        val query = GetCountriesByContinentQuery(com.apollographql.apollo.api.Optional.present("AF"))
        val testData = GetCountriesByContinentQuery.Companion.Data {
            countries = listOf(
                buildCountry {
                    code = "NG"
                    name = "Nigeria"
                    emoji = "🇳🇬"
                    continent = buildContinent {
                        code = "AF"
                        name = "Africa"
                    }
                }
            )
        }
        apolloClient.enqueueTestResponse(query, testData)

        val result = repository.getCountriesByContinent("AF")
        assertTrue(result is LibraryResult.Success)
        val countries = result.data

        assertEquals(1, countries.size)
        assertEquals("NG", countries[0].code)
        assertEquals("Nigeria", countries[0].name)
    }
}
