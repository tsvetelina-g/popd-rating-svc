# POPd Rating Service

A Spring Boot-based REST API microservice for managing movie ratings. This service is part of the POPd movie review and rating platform, allowing users to rate movies (1-10 scale), retrieve ratings, and get comprehensive rating statistics.

## Features

POPd Rating Service provides:

- **Rate Movies** - Create or update movie ratings (1-10 scale) for any user and movie combination
- **Retrieve Ratings** - Get specific ratings by user and movie ID
- **Delete Ratings** - Remove ratings when needed
- **Movie Statistics** - Get average rating and total rating count for movies
- **User Statistics** - Track how many movies a user has rated
- **Latest Ratings** - Retrieve a user's most recently updated ratings (limited to 20)

## Tech Stack

- **Framework**: Spring Boot 3.4.0
- **Java Version**: 17
- **Database**: MySQL 8 (production), H2 (testing)
- **ORM**: Spring Data JPA / Hibernate
- **Build Tool**: Maven
- **Validation**: Jakarta Validation
- **Utilities**: Lombok

## Prerequisites

- **Java 17** or higher
- **Maven 3.6+** (included via Maven Wrapper)
- **MySQL 8.0+** (for production)
- **MySQL server** running on localhost:3306
- **IntelliJ IDEA** (recommended IDE)

## Installation & Setup

### Open the project in IntelliJ IDEA

1. Open IntelliJ IDEA
2. Select **File → Open**
3. Navigate to the project directory and select it
4. IntelliJ will automatically detect it as a Maven project and import dependencies

### Configure the database

Update `src/main/resources/application.properties` with your MySQL credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/popd_rating_svc?createDatabaseIfNotExist=true
spring.datasource.username=your_username
spring.datasource.password=your_password
```

The application will automatically create the database `popd_rating_svc` if it doesn't exist.

### Wait for Maven to sync

- IntelliJ will automatically download dependencies
- Wait for the Maven sync to complete (check the bottom-right status bar)

## Running the Application

### Using IntelliJ IDEA

#### Locate the main class

Navigate to `src/main/java/app/popdratingsvc/PopdRatingSvcApplication.java`

#### Run the application

1. Right-click on `PopdRatingSvcApplication.java`
2. Select **Run 'PopdRatingSvcApplication'**
3. Or use the green play button next to the main method
4. Or press `Shift + F10`

#### Access the application

- The application will be available at **http://localhost:8084**
- Check the IntelliJ console for startup logs and any errors

## Configuration

### Application Properties

Key configuration options in `application.properties`:

- **Database**: MySQL connection settings with auto-create database
- **JPA**: Hibernate DDL auto-update enabled
- **Server**: Runs on port 8084
- **Logging**: Configured for Hibernate entity persister errors

### Integration with POPd Application

This microservice is designed to be consumed by the main POPd MVC application via REST API calls. Ensure this service is running and accessible when using the full POPd platform.

## Testing

### Running Tests in IntelliJ IDEA

#### Run all tests

1. Right-click on the `src/test/java` folder
2. Select **Run 'All Tests'**
3. Or use `Ctrl + Shift + F10` (Windows/Linux) or `Cmd + Shift + R` (Mac)

#### Run a specific test class

1. Open the test file (e.g., `RatingServiceUTest.java`, `RatingControllerApiTest.java`)
2. Right-click on the class name or the file
3. Select **Run 'ClassName'**
4. Or click the green play button next to the class declaration

#### Run a specific test method

1. Click the green play button next to the test method
2. Or right-click on the method and select **Run 'methodName()'**

#### View test results

- Test results appear in the **Run** tool window at the bottom
- Green checkmarks indicate passed tests
- Red X marks indicate failed tests with error details

The test suite includes:

- **Unit tests** for services (`RatingServiceUTest.java`)
- **Integration tests** for controllers (`RatingControllerApiTest.java`)
- **End-to-end integration tests** (`UpsertRatingITest.java`)
- **Application context test** (`PopdRatingSvcApplicationTests.java`)

Test database uses H2 in-memory database (configured in `src/test/resources/application.properties`), so no database setup is required for running tests.

## Project Structure

```
src/
├── main/
│   ├── java/app/popdratingsvc/
│   │   ├── model/          # Entity models (Rating)
│   │   ├── repository/     # JPA repositories
│   │   ├── service/        # Business logic services
│   │   ├── web/            # REST controllers, DTOs, and mappers
│   │   │   ├── dto/        # Data Transfer Objects
│   │   │   └── mapper/     # DTO mappers
│   │   ├── exception/      # Custom exceptions
│   │   └── PopdRatingSvcApplication.java
│   └── resources/
│       └── application.properties
└── test/
    ├── java/               # Test classes
    └── resources/
        └── application.properties  # Test configuration (uses H2 in-memory DB)
```

## API Endpoints

All endpoints are prefixed with `/api/v1`.

### Rating Management

- **POST** `/api/v1/ratings` - Create or update a rating
  - Request body: `RatingRequest` (userId, movieId, value)
  - Response: `RatingResponse` (201 Created)

- **GET** `/api/v1/ratings/{userId}/{movieId}` - Get a specific rating by user and movie
  - Response: `RatingResponse` (200 OK)

- **DELETE** `/api/v1/ratings/{userId}/{movieId}` - Delete a rating
  - Response: 204 No Content

### Statistics

- **GET** `/api/v1/ratings/{movieId}/stats` - Get movie rating statistics
  - Response: `MovieRatingStatsResponse` (averageRating, allRatingsCount)

- **GET** `/api/v1/ratings/{userId}/user` - Get user rating statistics
  - Response: `UserRatingStatsResponse` (moviesRatedCount)

- **GET** `/api/v1/ratings/{userId}/latest-ratings` - Get latest ratings by user (limited to 20)
  - Response: `List<RatingResponse>` (200 OK)

### Error Handling

- **404 Not Found** - Returned when a rating or resource is not found
  - Response: `ErrorResponse` with error message

## Notes

- Ratings use a **1-10 scale** (integer values)
- Each user can have only **one rating per movie** (enforced by unique constraint)
- The service automatically tracks `createdOn` and `updatedOn` timestamps
- Database schema is automatically created/updated via Hibernate DDL auto mode
- The application runs on port **8084** by default
- Tests use an in-memory H2 database, so no database setup is required for running tests
