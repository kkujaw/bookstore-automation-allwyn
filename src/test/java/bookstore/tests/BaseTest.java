package bookstore.tests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasItem;

public class BaseTest {
    private static final String BASE_URL = System.getProperty("base.url", "https://fakerestapi.azurewebsites.net/api/v1");

    @BeforeAll
    static void setupRestAssured() {
        RestAssured.baseURI = BASE_URL;
    }

    public static RequestSpecification getRequestSpec() {
        return RestAssured.given()
                .header("Content-Type", "application/json");
    }

    public void assertInvalidIdError(ValidatableResponse response, String id) {
        response.statusCode(400)
                .body("errors.id", hasItem("The value '" + id + "' is not valid."));
    }

    public static Stream<Map<String, Object>> dataProvider(String jsonFilePath) {
        try (var inputStream = BaseTest.class.getClassLoader().getResourceAsStream(jsonFilePath)) {
            if (inputStream == null) {
                throw new RuntimeException("Resource not found: " + jsonFilePath);
            }
            Map<String, Object> data = new ObjectMapper().readValue(inputStream, new TypeReference<>() {
            });
            return Stream.of(data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load JSON data from: " + jsonFilePath, e);
        }
    }
}
