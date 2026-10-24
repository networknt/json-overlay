package com.networknt.jsonoverlay;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import tools.jackson.core.JsonPointer;
import tools.jackson.dataformat.yaml.YAMLMapper;

import com.networknt.jsonoverlay.model.TestModelParser;
import com.networknt.jsonoverlay.parser.LocationRecorderYamlFactory;
import com.networknt.jsonoverlay.parser.LocationRecorderYamlParser;

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
    void emptyAndCommentOnlyReferencesAreHandledResolutionFailures(@TempDir Path directory) throws Exception {
        Path root = directory.resolve("root.yaml");
        Files.writeString(root, "scalars:\n  empty:\n    $ref: 'empty.yaml'\n");
        for (String document : new String[]{"", "# comment only\n"}) {
            Files.writeString(directory.resolve("empty.yaml"), document);
            var model = TestModelParser.parse(root.toUri().toURL());
            assertNull(assertDoesNotThrow(() -> model.getScalar("empty")));
            var reference = Overlay.of(model.getScalars()).getReference("empty");
            assertTrue(reference.isInvalid());
            assertTrue(reference.getInvalidReason().getMessage().contains("empty"));
        }
    }

    @Test
    void readsYaml12ScalarsWithoutYaml11Coercion() throws Exception {
        var tree = new JsonLoader().loadWithLocations(
                "yesValue: yes\nnoValue: no\nonValue: on\noffValue: off\n"
                + "octal: 012\nbinary: 0b11\nunderscores: 1_000\ntilde: ~\n"
                + "mixedTrue: True\nupperTrue: TRUE\nmixedFalse: False\nupperFalse: FALSE\n"
                + "mixedNull: Null\nupperNull: NULL\nhex: 0x1F\nempty: null\n").getLeft();
        for (String name : new String[]{"yesValue", "noValue", "onValue", "offValue",
                "octal", "binary", "underscores", "tilde", "mixedTrue", "upperTrue", "mixedFalse", "upperFalse",
                "mixedNull", "upperNull", "hex"}) {
            assertTrue(tree.get(name).isString(), name);
        }
        assertEquals("012", tree.get("octal").asString());
        assertEquals("~", tree.get("tilde").asString());
        assertTrue(tree.get("empty").isNull());
    }

    @Test
    void preservesLocationsForAllParserInputVariants() throws Exception {
        var mapper = YAMLMapper.builder(new LocationRecorderYamlFactory()).build();
        byte[] bytes = "name: value\n".getBytes(StandardCharsets.UTF_8);
        try (var stream = mapper.createParser(new ByteArrayInputStream(bytes));
             var reader = mapper.createParser(new StringReader("name: value\n"));
             var array = mapper.createParser(bytes);
             var chars = mapper.createParser("name: value\n".toCharArray())) {
            for (var parser : new tools.jackson.core.JsonParser[]{stream, reader, array, chars}) {
                assertInstanceOf(LocationRecorderYamlParser.class, parser);
                assertEquals("value", mapper.readTree(parser).get("name").asString());
                var locations = ((LocationRecorderYamlParser) parser).getLocations();
                assertTrue(locations.containsKey(JsonPointer.compile("/name")));
            }
        }
    }

    @Test
    void nonCanonicalBooleanAndIntegerScalarsDoNotPopulateTypedOverlays() throws Exception {
        var tree = new JsonLoader().loadWithLocations("required: True\ndeprecated: FALSE\ncount: 0x1F\n").getLeft();
        var manager = new ReferenceManager();
        assertNull(BooleanOverlay.factory.create(tree.get("required"), null, manager)._get());
        assertNull(BooleanOverlay.factory.create(tree.get("deprecated"), null, manager)._get());
        assertNull(IntegerOverlay.factory.create(tree.get("count"), null, manager)._get());
        var canonical = new JsonLoader().loadWithLocations("required: true\ncount: 31\n").getLeft();
        assertEquals(Boolean.TRUE, BooleanOverlay.factory.create(canonical.get("required"), null, manager)._get());
        assertEquals(31, IntegerOverlay.factory.create(canonical.get("count"), null, manager)._get());
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
