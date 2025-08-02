# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Build and Test
- `./gradlew build` - Build the project and run tests
- `./gradlew test` - Run tests only
- `./gradlew bootRun` - Run the Spring Boot application locally
- `./gradlew clean` - Clean build artifacts
- `./gradlew bootBuildImage` - Build Docker image for deployment

### Database Setup
- Application uses PostgreSQL in production (configured via environment variables)
- Tests use H2 in-memory database automatically
- JPA auto-DDL is enabled (`spring.jpa.hibernate.ddl-auto=update`)

## Architecture Overview

### Core Application Structure
This is a Spring Boot 3.4.2 REST API server for a meal planning application with Google Calendar integration.

**Main packages:**
- `controller/` - REST endpoints for auth, meals, planning, shopping, calendar
- `service/` - Business logic including Google Calendar integration via `CalendarService`
- `entity/` - JPA entities for meals, plans, users, shopping lists
- `dto/` - Data transfer objects with MapStruct mapping
- `repository/` - Spring Data JPA repositories and REST repositories
- `config/` - Security, OAuth, and application configuration

### Key Features
- **Authentication**: Google OAuth2 integration with JWT tokens
- **Calendar Integration**: Google Calendar API for meal planning events
- **Family Groups**: Multi-user support with family group management
- **Meal Planning**: Recipe management with ingredients and shopping list generation
- **Shopping Lists**: Automatic generation from meal plans

### Technology Stack
- **Framework**: Spring Boot 3.4.2 with Spring Security, Spring Data JPA
- **Database**: PostgreSQL (production), H2 (tests)
- **Authentication**: OAuth2 with Google, Redis session storage
- **Mapping**: MapStruct for DTO conversions
- **Calendar**: Google Calendar API integration
- **Deployment**: Docker with Gradle bootBuildImage

### Security Configuration
- OAuth2 authentication with Google (`GoogleAuthenticationProvider`)
- JWT token handling (`GoogleJwtAuthenticationToken`)
- Redis-based session management
- Secure cookie configuration for production

### Database Design
- **Users**: `AppUser` with `FamilyGroup` relationships
- **Meals**: `Meal` entities with `Recipe`, `Ingredient`, and `MealTag`
- **Planning**: `Plan` entities linking meals to calendar events
- **Shopping**: `ShoppingListItem` generated from plans

### External Integrations
- **Google Calendar API**: Full integration for event management
- **Google OAuth**: Authentication and authorization
- **Redis**: Session storage and caching

### Development Notes
- Uses Java 21 with Spring Boot 3.4.2
- MapStruct annotation processors for compile-time DTO mapping
- Minimal test coverage (only basic context loading test exists)
- Configured for Kubernetes deployment
- Environment-based configuration for all external services

### API Structure
All endpoints are prefixed with `/api` (configured via `server.servlet.context-path`)
Main controller endpoints include authentication, meal management, planning, shopping lists, and calendar integration.

### Web Client Repository
This app is a backend server, it can serve client user interfaces.
The first of these is a web client. It's relative path is @../../WebStormProjects/meals_web_client
Any change that affects the front end backend interface should consider this repostiory