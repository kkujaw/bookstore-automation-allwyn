# Bookstore API Automation

API Automation Testing Assessment: Online Bookstore - Java with RestAssured implementation.

## Project Structure

- Main code: `src/main/java`
- Test code: `src/test/java`
- Build tool: Maven (`pom.xml`)
- CI workflow: `.github/workflows/bookstore-ci.yml`

## Technologies

- Java 17
- RestAssured framework for API testing
- JUnit 5 (Jupiter) for testing
- Allure for test reporting
- Maven for build and dependency management

## Writing and Running Tests

- Test classes are located in `src/test/java` and named `*Test.java` or `*Tests.java`.
- Use JUnit 5 annotations for tests.
- Allure annotations can be added for enhanced reporting.

### Run Tests Locally

1. Run all tests:
   ```bash
   mvn clean test

2. Run tests and generate Allure results:
   ```bash
   mvn clean verify

3. Generate Allure report:
   ```bash
   mvn allure:report

The generated report will be available in ```target/allure-test-report/index.html```.

4. Serve Allure report locally:
   ```bash
   mvn allure:serve

This will start a local server and open the report in your default web browser under the provided local URL.

## Continuous Integration (CI)

Configured in ```.github/workflows/bookstore-ci.yml``` using GitHub Actions.
On every push or pull request to main:

- Checks out the code
- Sets up JDK 17
- Runs mvn clean verify to build and test
- Generates Allure report (mvn allure:report)
- Uploads the Allure report as an artifact (target/allure-test-report) which can be downloaded from the GitHub Actions
  run summary. To serve the report locally, you can install allure app and then run
  `allure open <path-to-downloaded-allure-report>` after generating
  the report.

### Accessing Reports

- __CI__: Download the Allure report artifact from the GitHub Actions run summary.
- __Local__: After running mvn allure:report, open target/allure-test-report/index.html.
- __Serve locally__: Use mvn allure:serve for an interactive report.