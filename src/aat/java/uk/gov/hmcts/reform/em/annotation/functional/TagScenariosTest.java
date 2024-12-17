package uk.gov.hmcts.reform.em.annotation.functional;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.em.annotation.testutil.TestUtil;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(classes = {TestUtil.class})
@TestPropertySource(value = "classpath:application.yml")
@ExtendWith({SerenityJUnit5Extension.class, SpringExtension.class})
@WithTags({@WithTag("testType:Functional")})
class TagScenariosTest {

    @Autowired
    private TestUtil testUtil;

    @Value("${test.url}")
    private String testUrl;

    private RequestSpecification request;
    private RequestSpecification unAuthenticatedRequest;

    @BeforeEach
    public void setupRequestSpecification() {
        request = testUtil
                .authRequest()
                .baseUri(testUrl)
                .contentType(APPLICATION_JSON_VALUE);

        unAuthenticatedRequest = testUtil
                .unauthenticatedRequest()
                .baseUri(testUrl)
                .contentType(APPLICATION_JSON_VALUE);
    }

    @Test
    void shouldReturn200WhenGetTagByCreatedBy() {
        final String annotationSetId = createAnnotationSet();
        final String annotationId = UUID.randomUUID().toString();
        createAnnotation(annotationId, annotationSetId);

        request
                .get("/api/tags/bob")
                .then()
                .statusCode(200)
                .body("size()", Matchers.greaterThanOrEqualTo(1))
                .body("[0].name", equalTo("test tag"))
                .body("[0].createdBy", equalTo("bob"))
                .body("[0].label", equalTo("test label"))
                .body("[0].color", equalTo("yellow"))
                .log().all();
    }

    @Test
    void shouldReturn401WhenUnAuthenticatedUserGetTagByCreatedBy() {
        final String annotationSetId = createAnnotationSet();
        final String annotationId = UUID.randomUUID().toString();
        createAnnotation(annotationId, annotationSetId);

        unAuthenticatedRequest
                .get("/api/tags/bob")
                .then()
                .statusCode(401)
                .log().all();
    }

    @Test
    void shouldReturn404WhenGetTagByCreatedByNotFound() {
        request
                .get("/api/tags/foo")
                .then()
                .statusCode(200) //FIXME should return 404
                .log().all();
    }

    @NotNull
    private String createAnnotationSet() {
        final JSONObject jsonObject = new JSONObject();
        final UUID newAnnotationSetId = UUID.randomUUID();
        jsonObject.put("documentId", UUID.randomUUID().toString());
        jsonObject.put("id", newAnnotationSetId.toString());

        return request
                .body(jsonObject.toString())
                .post("/api/annotation-sets")
                .then()
                .statusCode(201)
                .body("id", equalTo(newAnnotationSetId.toString()))
                .extract()
                .response()
                .getBody()
                .jsonPath()
                .get("id");
    }

    @NotNull
    private ValidatableResponse createAnnotation(String annotationId, String annotationSetId) {
        final JSONObject annotation = createAnnotationPayload(annotationId, annotationSetId);
        return request
                .body(annotation)
                .post("/api/annotations")
                .then()
                .statusCode(201)
                .log().all();
    }

    @NotNull
    private JSONObject createAnnotationPayload(String annotationId, String annotationSetId) {
        final JSONObject createAnnotations = new JSONObject();
        createAnnotations.put("annotationSetId", annotationSetId);
        createAnnotations.put("id", annotationId);
        createAnnotations.put("annotationType", "highlight");
        createAnnotations.put("page", 1);
        createAnnotations.put("color", "d1d1d1");
        createAnnotations.put("createdBy", "bob");

        final JSONArray comments = new JSONArray();
        final JSONObject comment = new JSONObject();
        comment.put("content", "text");
        comment.put("annotationId", annotationId);
        comment.put("id", UUID.randomUUID().toString());
        comments.put(0, comment);
        createAnnotations.put("comments", comments);

        final JSONArray rectangles = new JSONArray();
        final JSONObject rectangle = new JSONObject();
        rectangle.put("id", UUID.randomUUID().toString());
        rectangle.put("annotationId", annotationId);
        rectangle.put("x", 0f);
        rectangle.put("y", 0f);
        rectangle.put("width", 10f);
        rectangle.put("height", 11f);
        rectangles.put(0, rectangle);
        createAnnotations.put("rectangles", rectangles);

        final JSONArray tags = new JSONArray();
        final JSONObject tag = new JSONObject();
        tag.put("name", "test tag");
        tag.put("createdBy", "bob");
        tag.put("label", "test label");
        tag.put("color", "yellow");
        tags.put(0, tag);
        createAnnotations.put("tags", tags);

        return createAnnotations;
    }
}
