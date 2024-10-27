# Prime Number Finder API

This application provides an API to calculate prime numbers up to a specified limit using various algorithms. It allows users to control the calculation method and caching options through specific parameters.

## Technologies Used

- **Java Version**: 20
- **Framework**: Spring Boot
- **Database**: SQLite (for caching)
- **Testing**: JUnit, Mockito, Rest-Assured

## Features

- Find prime numbers up to a specified limit.
- Support for multiple algorithms for calculating primes.
- Caching results in a SQLite database for improved performance.
- Handles invalid input parameters gracefully with custom error responses.

## Setup Instructions

To set up the project locally, follow these steps:

### Prerequisites

- **Java Development Kit (JDK)**: Make sure you have JDK 20 installed. You can download it from the [official Oracle website](https://www.oracle.com/java/technologies/javase/jdk20-archive-downloads.html) or use a package manager.

- **Maven**: Ensure you have Maven installed to manage project dependencies. You can install it from the [official Maven website](https://maven.apache.org/download.cgi).

### Local setup

Follow the instuctions below sequentially to set up the project locally:
```bash
git clone https://github.com/LewisT543/msvc-primefinder.git

cd msvc-primefinder

mvn clean install

mvn test

mvn spring-boot:run
```

## API Endpoint

### Find Primes
**GET** `/api/find-primes`

### Parameters
| Parameter      | Type   | Required | Default Value | Description                                                                                      |
|----------------|--------|----------|---------------|--------------------------------------------------------------------------------------------------|
| `limit`        | `long` | Yes      | N/A           | The upper limit up to which primes will be calculated.                                         |
| `algo`         | `enum` | No       | `SMART`       | The algorithm to use for calculating primes. Options include: `NAIVE`, `SIEVE`, `SIEVE_BITSET`, `SIEVE_STREAMS`, `SEGMENTED_SIEVE`, `SEGMENTED_SIEVE_BITSET`, `SEGMENTED_SIEVE_STREAMS`, `SEGMENTED_SIEVE_CONCURRENT`, `SMART`. |
| `useCache`     | `boolean` | No   | `false`       | Indicates whether to use cached prime results.                                                  |
| `buildCache`   | `boolean` | No   | `false`       | Indicates whether to build a cache of the calculated primes for future requests.                |
| `withResult`   | `boolean` | No   | `true`        | Indicates whether to include the result in the response.                                        |

### Algorithms Overview
| Algorithm Name                          | Description                                                                                               |
|-----------------------------------------|-----------------------------------------------------------------------------------------------------------|
| `NAIVE`                                 | A simple, straightforward approach to find prime numbers, checking each number individually.              |
| `SIEVE`                                 | Implements the Sieve of Eratosthenes algorithm, an efficient method for finding all primes up to a limit. |
| `SIEVE_BITSET`                          | A bit-set based implementation of the Sieve of Eratosthenes for memory efficiency.                        |
| `SIEVE_STREAMS`                         | Uses Java Streams to implement the Sieve of Eratosthenes for a more functional programming approach.      |
| `SEGMENTED_SIEVE`                       | A segmented version of the Sieve algorithm that is more memory efficient for larger ranges.               |
| `SEGMENTED_SIEVE_BITSET`                | Combines segmented sieve with a bit-set for improved performance and memory usage.                        |
| `SEGMENTED_SIEVE_STREAMS`               | A segmented sieve using Java Streams.                                                                     |
| `SEGMENTED_SIEVE_CONCURRENT`            | A concurrent implementation of the segmented sieve for faster calculations using multiple threads.        |
| `SMART`                                 | Automatically chooses the best algorithm based on the limit provided (SIEVE or SEGMENTED_SIEVE_CONCURRENT).               |

## Example Queries

### Example 1: Find Primes up to 100
**Request:**

GET /find-primes?limit=100&algo=SIEVE

**Response:**
```json
{
  "result": [2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97],
  "numberOfPrimes": 25,
  "executionTimeMs": 5,
  "executionTimeNs": 5000000,
  "algorithmName": "SIEVE",
  "buildCache": false,
  "useCache": false,
  "timestamp": "2024-10-27T12:00:00"
}
```
### Example 2: Find Primes up to 100 with Caching and SMART algorithm
**Request:**

GET /find-primes?limit=100000&useCache=true&buildCache=true&algo=SMART

**Response:**
```json
{
  "result": [2, 3, 5, ...],
  "numberOfPrimes": 9592,
  "executionTimeMs": 519,
  "executionTimeNs": 519539900,
  "algorithmName": "SIEVE",
  "buildCache": true,
  "useCache": true,
  "timestamp": "2024-10-27T13:16:13.8735739"
}
```

### Example 3:  Invalid Parameter Handling
**Request:**

/find-primes?limit=test

**Response:**
```json
{
    "message": "Invalid value 'test' for parameter 'limit'. Please provide a valid limit less than or equal to: 9223372036854775807 (max long value)",
    "status": 400,
    "timestamp": "2024-10-27T13:18:01.8392119"
}
```