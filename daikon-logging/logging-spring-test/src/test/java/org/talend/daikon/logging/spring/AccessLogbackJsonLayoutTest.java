package org.talend.daikon.logging.spring;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ResourceUtils;
import org.talend.daikon.logging.ecs.EcsFields;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = SimpleApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
public class AccessLogbackJsonLayoutTest {

    @Value("${local.server.port}")
    private int port;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
    }

    File getAccessLogFile() throws IOException {
        return ResourceUtils.getFile("classpath:logback-access.log");
    }

    Path getPathToAccessLogFile() throws IOException {
        return Paths.get(getAccessLogFile().toURI());
    }

    @Test
    public void testGetSimple() throws IOException {
        given().auth().preemptive().basic("test", "foobar").header("Accept", "application/json").header("user-agent", "Chrome")
                .when().get("/hello").then().statusCode(200);
        Optional<String> firstMessage = Files.lines(getPathToAccessLogFile()).reduce((first, second) -> second);

        JsonNode jsonNode = objectMapper.readTree(firstMessage.get());
        assertNotNull(jsonNode.get("ecs.version"));
        assertNotNull(jsonNode.get(EcsFields.EVENT_KIND.fieldName));
        assertNotNull(jsonNode.get(EcsFields.EVENT_CATEGORY.fieldName));
        assertNotNull(jsonNode.get(EcsFields.EVENT_TYPE.fieldName));
        assertNotNull(jsonNode.get(EcsFields.MESSAGE.fieldName));

    }

    @Test
    public void testGetSimpleAddSpanAndTraceId() throws IOException {
        given().auth().preemptive().basic("test", "foobar").header("user-agent", "Chrome")
                .header("x-b3-spanId", UUID.randomUUID()).header("x-b3-traceId", UUID.randomUUID()).when().get("/hello").then()
                .statusCode(200);

        Optional<String> secondMessage = Files.lines(getPathToAccessLogFile()).reduce((first, second) -> second);
        JsonNode jsonNode = objectMapper.readTree(secondMessage.get());
        assertNotNull(jsonNode.get(EcsFields.TRACE_ID.fieldName));
        assertNotNull(jsonNode.get(EcsFields.SPAN_ID.fieldName));
        assertNotNull(jsonNode.get(EcsFields.EVENT_DURATION.fieldName));
        assertNotNull(jsonNode.get(EcsFields.EVENT_START.fieldName));
        assertNotNull(jsonNode.get(EcsFields.EVENT_END.fieldName));
    }
}
