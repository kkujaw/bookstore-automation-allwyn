package bookstore.tests.books;

import bookstore.models.Book;
import bookstore.tests.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Tests for the Books API.
 * Covers CRUD operations and edge cases.
 */
public class BooksApiTest extends BaseTest {

    // 1. GET /api/v1/Books – Retrieve a list of all books.
    @Test
    void getAllBooks_shouldReturnListOfBooks() {
        getRequestSpec()
                .get("/Books")
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body(matchesJsonSchemaInClasspath("fakeRestApiSchema.json"));
    }

    // 2. GET /api/v1/Books/{id} – Retrieve details of a specific book by its ID.
    @Test
    void getBook_id_shouldReturn_ok() {
        int bookId = 1; // Assuming book with ID 1 exists
        getRequestSpec()
                .get("/Books/" + bookId)
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("id", is(bookId))
                .body(matchesJsonSchemaInClasspath("fakeRestApiSchema.json"));
    }

    @Test
    void getBook_invalidId_shouldReturn_error() {
        getRequestSpec()
                .get("/Books/99999")
                .then()
                .statusCode(404)
                .body("title", is("Not Found"));
    }

    // 3. POST /api/v1/Books – Add a new book to the system.
    @ParameterizedTest
    @MethodSource("bookProvider")
    void createBook_shouldReturn_ok(Map<String, Object> book) {
        getRequestSpec()
                .body(book)
                .post("/Books")
                .then()
                .statusCode(is(200))
                .body(is(not(empty())), is(notNullValue()))
                .body("id", is(0))
                .body("title", is(book.get("title")))
                .body("description", is(book.get("description")))
                .body("pageCount", is(book.get("pageCount")))
                .body("excerpt", is(book.get("excerpt")))
                .body("publishDate", is(book.get("publishDate")))
                .body(matchesJsonSchemaInClasspath("fakeRestApiSchema.json"));
    }

    @Test
    void createBook_withEmptyBody_shouldReturn_error() {
        getRequestSpec()
                .body("")
                .post("/Books")
                .then()
                .statusCode(is(400))
                .body("errors.\"\"", hasItem("A non-empty request body is required."));
    }

    // 4. PUT /api/v1/Books/{id} – Update an existing book by its ID.
    @ParameterizedTest
    @MethodSource("bookProvider")
    void updateBookDetails_shouldReturnOk(Map<String, Object> book) {
        String updatedTitle = "Updated Title";
        String updatedDescription = "Updated Description";
        int bookId = 1; // Assuming a book with ID 1 exists

        // Get existing Book Details
        Response oldBook = getRequestSpec()
                .get("/Books/" + bookId)
                .then()
                .statusCode(200)
                .extract().response();

        Book existingBook = oldBook.as(Book.class);

        // Update Book Details
        book.put("id", existingBook.id);
        book.put("title", updatedTitle);
        book.put("description", updatedDescription);
        book.put("pageCount", existingBook.pageCount);
        book.put("excerpt", existingBook.excerpt);
        book.put("publishDate", existingBook.publishDate);

        getRequestSpec()
                .body(book)
                .put("/Books/" + existingBook.id)
                .then()
                .statusCode(200)
                .body("id", is(existingBook.id))
                .body("title", is(updatedTitle))
                .body("description", is(updatedDescription));
    }

    @Test
    void updateBookDetails_invalidId_shouldReturnError() {
        String wrongId = "wrongId";
        assertInvalidIdError(getRequestSpec()
                        .body("{}")
                        .put("/Books/" + wrongId)
                        .then()
                        .statusCode(400)
                , wrongId);
    }

    // 5. DELETE /api/v1/Books/{id} – Delete a book by its ID.
    @ParameterizedTest
    @MethodSource("bookProvider")
    void deleteBook_shouldReturnOk(Map<String, Object> book) {
        // Create a book first
        Response createdBook = getRequestSpec()
                .body(book)
                .post("/Books")
                .then()
                .statusCode(is(200))
                .extract().response();
        Book bookToDelete = createdBook.as(Book.class);

        // Delete
        getRequestSpec()
                .delete("/Books/" + bookToDelete.id)
                .then()
                .statusCode(200);
    }

    @Test
    void deleteBook_withInvalidId_shouldReturnError() {
        String wrongId = "wrongId";
        assertInvalidIdError(getRequestSpec()
                        .delete("/Books/" + wrongId)
                        .then()
                        .statusCode(400)
                , wrongId);
    }

    @Test
    void deleteBook_withEmptyId_shouldReturnError() {
        getRequestSpec()
                .delete("/Books/")
                .then()
                .statusCode(405)
                .body(is(emptyOrNullString()));
    }

    static Stream<Map<String, Object>> bookProvider() {
        return dataProvider("testdata/tempBook.json");
    }
}