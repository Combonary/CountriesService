package com.pamtech.countriesservice.model

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName("Country")
data class Country(
    val code: String,
    val name: String,
    val emoji: String,
    val continentName: String
)

@OptIn(ExperimentalObjCName::class)
@ObjCName("CountryDetail")
data class CountryDetail(
    val code: String,
    val name: String,
    val native: String,
    val phone: String,
    val capital: String?,
    val currency: String?,
    val emoji: String,
    val continentName: String,
    val languages: List<String>
)

@OptIn(ExperimentalObjCName::class)
@ObjCName("Continent")
data class Continent(
    val code: String,
    val name: String
)

@OptIn(ExperimentalObjCName::class)
@ObjCName("State")
data class State(
    val code: String?,
    val name: String
)
