# AMRO Recipe Finder
A REST API built in Spring Boot 3.3.0 for managing your favourite recipes.

## Installation
1. Clone the repository
2. Set up a PostgreSQL database
3. Change the database connection string, username & password in `src/main/resources/application.properties`
4. Make sure to have the following dependencies installed:
    - Java 17
    - Maven
    - PostgreSQL
5. Build the project by running `mvn clean install` in the root directory or just run the project by running `mvn spring-boot:run`

### Features
Allowing users to:
- Register a new user
- Login
- Create a new recipe (only as a logged-in user)
- Filter on recipes by description, ingredients, vegan and number of servings (only as a logged-in user)
- Update a recipe (only as a logged-in user)
- Delete a recipe (only as a logged-in user)
- Get a recipe by its id (only as a logged-in user)

## API Documentation
### Recipes API
The API documentation for Recipes in OpenAPI format can be found [here](https://app.swaggerhub.com/apis/PEKKARANTAAHO86/Recipe-finder-Recipe-API/1.0.0-oas3).
### Authorization API
The API documentation for Authorization in OpenAPI format can be found [here](https://app.swaggerhub.com/apis/PEKKARANTAAHO86/Recipe-finder-Auth-Api/1.0.0-oas3#/).

## Persistence
The API uses a PostgreSQL database for persistence. The database relationsships are the following:

![img](https://github.com/pecca86/armo-recipe-finder/blob/7777b5b143015e68e56e0df2090a06711599118e/documentation/schema_relations.png) 

## Architecture
The application is built using the Spring Boot framework. The application is divided into three layers: Controller, Service, and Repository. 
The Controller layer is responsible for handling incoming HTTP requests, the Service layer is responsible for the business logic, and 
the Repository layer is responsible for the database operations.

The communication from outside the application to the Controller layer is done through the REST API.

#### Architecture diagram
![img](https://github.com/pecca86/armo-recipe-finder/blob/7777b5b143015e68e56e0df2090a06711599118e/documentation/architecture.png)
