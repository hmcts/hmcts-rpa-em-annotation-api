package uk.gov.hmcts.reform.em.annotation.functional;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.em.EmTestConfig;
import uk.gov.hmcts.reform.em.annotation.testutil.TestUtil;

import java.util.UUID;

@SpringBootTest(classes = {TestUtil.class, EmTestConfig.class})
@PropertySource(value = "classpath:application.yml")
@RunWith(SpringRunner.class)
public class AnnotationSetScenarios {

    @Autowired
    TestUtil testUtil;

    @Value("${test.url}")
    String testUrl;

    private String documentId = UUID.randomUUID().toString();

    @Test
    public void testFilterAnnotationSetSuccess() {

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("documentId", documentId);
        jsonObject.put("id", UUID.randomUUID().toString());

        testUtil
                .authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(jsonObject.toString())
                .request("POST", testUrl + "/api/annotation-sets")
                .then()
                .statusCode(201);

        testUtil
                .authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .request("GET", testUrl + "/api/annotation-sets/filter?documentId=" + documentId)
                .then()
                .statusCode(200);
    }

    @Test
    public void testFilterAnnotationSetNotFound() {
        testUtil
                .authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .request("GET", testUrl + "/api/annotation-sets/filter?documentId=" + "1234")
                .then()
                .statusCode(404);
    }
}
