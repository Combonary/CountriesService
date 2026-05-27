package com.pamtech.countriesservice

import com.apollographql.apollo.ApolloClient
import com.pamtech.countriesservice.database.AppDatabase
import com.pamtech.countriesservice.repository.CountriesRepository
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName("CountriesApi")
class CountriesApi(database: AppDatabase) {
    private val apolloClient = ApolloClient.Builder()
        .serverUrl("https://countries.trevorblades.com/")
        .build()

    val repository: CountriesRepository = CountriesRepository(
        apolloClient = apolloClient,
        countryDao = database.countryDao(),
        continentDao = database.continentDao()
    )
}
