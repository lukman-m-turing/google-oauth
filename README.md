# google-oauth Project

This project uses Spring booot, Java 21, and Maven.

This project is an OAuth 2.1 client application that retreives info on behalf of users from a protected resource server, OAuth and OpenID Connect with state, csrf protection and PKCE for Google.
Sign In/Sign Up with Google (OIDC) and a Connect to Youtube Playlist (OAuth 2.1)

## Running the application in dev mode

You can run your application in dev mode:
```shell script
./mvnw clean compile spring-boot:dev
```
To run tests, do not run via ```./mvnw test``` as this only runs unit tests. To run integration tests in the true sense of the word. Further ([reading](https://maven.apache.org/surefire/maven-failsafe-plugin/))
```
  Run all test suites (unit & integration) via
./mvnw verify
```

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `google-oauth.jar` file in the `target/` directory.

The application is now runnable using `java -jar target/google-oauth.jar`.
