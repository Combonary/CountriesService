package com.pamtech.countriesservice.model

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName("LibraryResult")
sealed class LibraryResult<out T> {
    data class Success<out T>(val data: T) : LibraryResult<T>()
    data class Failure(val error: LibraryError) : LibraryResult<Nothing>()
}

@OptIn(ExperimentalObjCName::class)
@ObjCName("LibraryError")
sealed class LibraryError {
    data class Network(val message: String, val throwable: Throwable) : LibraryError()
    data class Database(val message: String, val throwable: Throwable) : LibraryError()
    data class Unknown(val message: String, val throwable: Throwable) : LibraryError()
}
