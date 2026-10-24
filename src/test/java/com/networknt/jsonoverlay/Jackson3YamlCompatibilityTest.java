package com.networknt.jsonoverlay;

import com.networknt.jsonoverlay.parser.LocationRecorderYamlFactory;
import com.networknt.jsonoverlay.parser.LocationRecorderYamlParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.networknt.jsonoverlay.model.TestModelParser;
import tools.jackson.core.JsonPointer;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class Jackson3YamlCompatibilityTest {
    public static class PrimitiveBean { public int number = 7; }

    @Test
    void preservesJsonMapperDefaults() {
        assertEquals(0, JsonOverlay.mapper.readValue("{\"number\":null}", PrimitiveBean.class).number);
        assertEquals(7, JsonOverlay.mapper.readValue("{} {}", PrimitiveBean.class).number);
        assertThrows(tools.jackson.databind.exc.UnrecognizedPropertyException.class,
                () -> JsonOverlay.mapper.readValue("{\"unknown\":true}", PrimitiveBean.class));
    }

    @Test
    void malformedReferencedYamlRemainsAHandledResolutionFailure(@TempDir Path directory) throws Exception {
        Path root = directory.resolve("root.yaml");
        Files.writeString(root, "scalars:\n  broken:\n    $ref: 'broken.yaml'\n");
        Files.writeString(directory.resolve("broken.yaml"), "[unterminated");
        var model = TestModelParser.parse(root.toUri().toURL());
        assertNull(assertDoesNotThrow(() -> model.getScalar("broken")));
        assertTrue(Overlay.of(model.getScalars()).getReference("broken").isInvalid());
    }

    @Test
    void readsYaml12ScalarsWithoutYaml11Coercion() throws Exception {
        var tree = new JsonLoader().loadWithLocations(
                "yesValue: yes\nnoValue: no\nonValue: on\noffValue: off\n"
                + "octal: 012\nbinary: 0b11\nunderscores: 1_000\ntilde: ~\nempty: null\n").getLeft();
        for (String name : new String[]{"yesValue", "noValue", "onValue", "offValue",
                "octal", "binary", "underscores", "tilde"}) {
            assertTrue(tree.get(name).isString(), name);
        }
        assertEquals("012", tree.get("octal").asString());
        assertEquals("~", tree.get("tilde").asString());
        assertTrue(tree.get("empty").isNull());
    }

    @Test
    void preservesLocationsForStreamReaderAndByteArrayInputs() throws Exception {
        var mapper = YAMLMapper.builder(new LocationRecorderYamlFactory()).build();
        byte[] bytes = "name: value\n".getBytes(StandardCharsets.UTF_8);
        try (var stream = mapper.createParser(new ByteArrayInputStream(bytes));
             var reader = mapper.createParser(new StringReader("name: value\n"));
             var array = mapper.createParser(bytes)) {
            for (var parser : new tools.jackson.core.JsonParser[]{stream, reader, array}) {
                assertInstanceOf(LocationRecorderYamlParser.class, parser);
                assertEquals("value", mapper.readTree(parser).get("name").asString());
                var locations = ((LocationRecorderYamlParser) parser).getLocations();
                assertTrue(locations.containsKey(JsonPointer.compile("/name")));
            }
        }
    }

    @Test
    void preservesDistinctStringNodeIdentitiesForLocationTracking() {
        var factory = MinSharingJsonNodeFactory.instance;
        assertNotSame(factory.stringNode("same"), factory.stringNode("same"));
    }

    @Test
    void preservesFirstDocumentBehavior() throws Exception {
        var tree = new JsonLoader().loadWithLocations("name: first\n---\nname: second\n").getLeft();
        assertEquals("first", tree.get("name").asString());
    }
}
