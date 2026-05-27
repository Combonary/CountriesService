# CountriesService KMP Library

A robust Kotlin Multiplatform (KMP) library providing a type-safe, multiplatform client for the [Countries GraphQL API](https://github.com/trevorblades/countries), now with offline support and structured error handling.

## Features

- **Type-Safe GraphQL Layer**: Powered by Apollo Kotlin 5 for strongly-typed queries and data builders.
- **Offline Caching**: Integrated **Room DB** for seamless offline access and local data persistence.
- **Structured Error Handling**: Returns `LibraryResult` for all operations, providing clear visibility into `Network`, `Database`, or `Unknown` failures.
- **Unified Multiplatform Support**: Optimized for Android (JVM) and iOS (Native).
- **Asynchronous Suspend API**: Built on Kotlin Coroutines for modern concurrency.
- **Platform-Independent Testing**: Architecture designed for logic testing using DAO fakes in `commonTest`.

## Support

- **Android** (via JVM)
- **iOS** (via Kotlin/Native and Swift/Obj-C)

## Installation

Add the library to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation(projects.sharedLogic)
}
```

## Usage

The library requires a Room `AppDatabase` instance to handle caching.

### Initialization

#### Android
```kotlin
val database = getDatabaseBuilder(context).build()
val countriesApi = CountriesApi(database)
val repository = countriesApi.repository
```

#### iOS (Swift)
```swift
let database = AppDatabaseIosKt.getDatabaseBuilder().build()
let countriesApi = CountriesApi(database: database)
let repository = countriesApi.repository
```

### Fetching Data

```kotlin
import com.pamtech.countriesservice.model.LibraryResult

suspend fun loadCountries() {
    when (val result = repository.getCountries()) {
        is LibraryResult.Success -> {
            val countries = result.data
            countries.forEach { println("${it.emoji} ${it.name}") }
        }
        is LibraryResult.Failure -> {
            println("Error: ${result.error}")
        }
    }
}
```

## API Reference

### Result Models

- `LibraryResult<T>`: A sealed class representing `Success(data)` or `Failure(error)`.
- `LibraryError`: Includes `Network`, `Database`, and `Unknown` error types with underlying throwables.

### Domain Models

- `Country`: Basic info (code, name, emoji, continentName).
- `CountryDetail`: Comprehensive details including capital, currency, native name, and languages.
- `Continent`: Basic info (code, name).

### Repository Methods

- `getCountries()`: Returns `LibraryResult<List<Country>>`.
- `getCountry(code: String)`: Returns `LibraryResult<CountryDetail>`.
- `getContinents()`: Returns `LibraryResult<List<Continent>>`.

## Development

### Logic Testing
The library uses a DAO-centric architecture that allows for pure logic testing in `commonTest` without platform-specific instrumentation.

To run tests:
```bash
./gradlew :sharedLogic:allTests
```
