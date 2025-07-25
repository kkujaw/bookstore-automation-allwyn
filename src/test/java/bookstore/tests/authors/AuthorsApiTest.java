package bookstore.tests.authors;

import bookstore.models.Author;
import bookstore.tests.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Tests for the Authors API.
 * Covers CRUD operations and edge cases.
 */
public class AuthorsApiTest extends BaseTest {

    // 1. GET /api/v1/Authors– Retrieve a list of all authors.
    @Test
    void getAllAuthors_shouldReturnListOfAuthors() {
        getRequestSpec()
                .get("/Authors")
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body(matchesJsonSchemaInClasspath("fakeRestApiSchema.json"));
    }


    // 2. GET /api/v1/Authors/{id}– Retrieve details of a specific author by their ID.
    @Test
    void getAuthor_id_shouldReturn_ok() {
        getRequestSpec()
                .get("/Authors/1")
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("id", is(1))
                .body(matchesJsonSchemaInClasspath("fakeRestApiSchema.json"));
    }

    @Test
    void getAuthor_invalidId_shouldReturn_error() {
        getRequestSpec()
                .get("/Authors/1223")
                .then()
                .statusCode(404)
                .body("title", is("Not Found"));
    }


    // 3. POST /api/v1/Authors– Add a new author to the system.
    @ParameterizedTest
    @MethodSource("authorProvider")
    void createAuthor_shouldReturn_ok(Map<String, Object> author) {
        getRequestSpec()
                .body(author)
                .post("/Authors")
                .then()
                .statusCode(is(200))
                .body(is(not(empty())), is(notNullValue()))
                .body("id", is(author.get("id")))
                .body("idBook", is(author.get("idBook")))
                .body("firstName", is(author.get("firstName")))
                .body("lastName", is(author.get("lastName")))
                .body(matchesJsonSchemaInClasspath("fakeRestApiSchema.json"));
    }

    @Test
    void createAuthor_withEmptyBody_shouldReturn_error() {
        getRequestSpec()
                .body("")
                .post("/Authors")
                .then()
                .statusCode(is(400))
                .body("errors.\"\"", hasItem("A non-empty request body is required."));
    }


    // 4. PUT /api/v1/Authors/{id}– Update an existing author’s details.
    @Test
    void updateAuthorDetails_shouldReturnOk() {
        Response author = getRequestSpec()
                .get("/Authors/1")
                .then()
                .statusCode(200)
                .extract().response();
        var updatedFirstName = "UpdatedFirstName";
        var updatedLastName = "UpdatedFirstName";

        Author existingAuthor = author.as(Author.class);
        Map<String, Object> update = new HashMap<>();
        update.put("id", existingAuthor.getId());
        update.put("idBook", existingAuthor.getIdBook());
        update.put("firstName", updatedFirstName);
        update.put("lastName", updatedLastName);
        getRequestSpec()
                .body(update)
                .put("/Authors/" + existingAuthor.getId())
                .then()
                .statusCode(200)
                .body("id", is(existingAuthor.getId()))
                .body("idBook", is(existingAuthor.getIdBook()))
                .body("firstName", is(updatedFirstName))
                .body("lastName", is(updatedFirstName));

        // Validate that updated author data is correct - GET the author again - simulate bug detection
        getRequestSpec()
                .get("/Authors/" + existingAuthor.getId())
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("fakeRestApiSchema.json"))
                .body("id", is(existingAuthor.getId()))
                .body("idBook", is(existingAuthor.getIdBook()))
                // change to actual updated values
                .body("firstName", is(updatedFirstName))
                .body("lastName", is(updatedFirstName));
    }

    @Test
    void updateAuthorDetails_invalidId_shouldReturnError() {
        String wrongId = "wrongId";
        assertInvalidIdError(getRequestSpec()
                        .body("{}")
                        .put("/Authors/" + wrongId)
                        .then()
                        .statusCode(400),
                wrongId);
    }


    // 5. DELETE /api/v1/Authors/{id}– Delete an author by their ID.
    @ParameterizedTest
    @MethodSource("authorProvider")
    void deleteAuthor_shouldReturnOk(Map<String, Object> author) {
        Response createdAuthor = getRequestSpec()
                .body(author)
                .post("/Authors")
                .then()
                .statusCode(is(200))
                .extract().response();
        Author authorToDelete = createdAuthor.as(Author.class);

        getRequestSpec()
                .delete("/Authors/" + authorToDelete.getId())
                .then()
                .statusCode(200);
    }

    @Test
    void deleteAuthor_withInvalidId_shouldReturnError() {
        String wrongId = "wrongId";
        assertInvalidIdError(
                getRequestSpec()
                        .delete("/Authors/" + wrongId)
                        .then()
                        .statusCode(400),
                wrongId
        );
    }

    @Test
    void deleteAuthor_withEmptyId_shouldReturnError() {
        getRequestSpec()
                .delete("/Authors/")
                .then()
                .statusCode(405);
    }

    static Stream<Map<String, Object>> authorProvider() {
        return dataProvider("testdata/tempAuthor.json");
    }
}
