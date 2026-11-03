package com.networknt.jsonoverlay;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.opentest4j.AssertionFailedError;
import tools.jackson.core.Version;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

class Jackson2FeatureDefaultsTest {
    private static final Map<String, FeatureSince> FEATURE_INTRODUCED_IN = Map.of(
            "MapperFeature.SORT_PROPERTIES_BY_INDEX", new FeatureSince(3, 2));

    private record FeatureSince(int major, int minor) {
        boolean isAfter(Version version) {
            return version.getMajorVersion() < major
                    || (version.getMajorVersion() == major && version.getMinorVersion() < minor);
        }
    }

    @FunctionalInterface
    private interface FeatureClassLookup {
        Class<?> load(String name) throws ClassNotFoundException;
    }

    private Optional<Object> resolveFeature(String name, Version version, FeatureClassLookup lookup,
                                            Consumer<String> reportOmission) {
        String[] parts = name.split("\\.", 2);
        String namespace = parts[0].startsWith("Stream") ? "tools.jackson.core."
                : "tools.jackson.databind." + (parts[0].equals("EnumFeature")
                || parts[0].equals("JsonNodeFeature") || parts[0].equals("DateTimeFeature") ? "cfg." : "");
        String diagnostic = "Snapshot feature " + name + " is unavailable in Jackson " + version;
        Class<?> type;
        try {
            type = lookup.load(namespace + parts[0]);
        } catch (ClassNotFoundException e) {
            throw new AssertionFailedError(diagnostic + ": feature class is absent", e);
        }
        assertTrue(type.isEnum(), diagnostic + ": feature class is not an enum");
        Optional<Object> feature = java.util.Arrays.<Object>stream(type.getEnumConstants())
                .filter(value -> ((Enum<?>) value).name().equals(parts[1])).findFirst();
        if (feature.isEmpty()) {
            FeatureSince introduced = FEATURE_INTRODUCED_IN.get(name);
            if (introduced != null && introduced.isAfter(version)) {
                reportOmission.accept("Snapshot entry not verified: " + name + " is absent in Jackson " + version
                        + "; introduced in " + introduced.major() + "." + introduced.minor()
                        + ". This flag is not checked by the snapshot.");
                return Optional.empty();
            }
            throw new AssertionFailedError(diagnostic);
        }
        return feature;
    }

    private void assertSnapshot(ObjectMapper mapper, String resource) throws Exception {
        Properties expected = new Properties();
        try (InputStream input = getClass().getResourceAsStream(resource)) {
            assertNotNull(input, resource);
            expected.load(input);
        }
        assertSnapshot(mapper, expected);
    }

    private void assertSnapshot(ObjectMapper mapper, Properties expected) throws Exception {
        assertFalse(expected.isEmpty());
        for (String name : expected.stringPropertyNames()) {
            Optional<Object> resolved = resolveFeature(name, mapper.version(), Class::forName, System.out::println);
            if (resolved.isEmpty()) continue;
            Object feature = resolved.get();
            boolean checked = false;
            for (Method method : mapper.getClass().getMethods()) {
                if (method.getName().equals("isEnabled") && method.getParameterCount() == 1
                        && method.getParameterTypes()[0].isInstance(feature)) {
                    assertEquals(Boolean.parseBoolean(expected.getProperty(name)), method.invoke(mapper, feature), name);
                    checked = true;
                    break;
                }
            }
            assertTrue(checked, "Feature must be checked: " + name);
        }
    }

    @Test
    void rejectsUnknownSnapshotFeatures() {
        Properties expected = new Properties();
        String name = "MapperFeature.NOT_A_REAL_FEATURE";
        expected.setProperty(name, "false");
        AssertionError failure = assertThrows(AssertionError.class, () -> assertSnapshot(JsonOverlay.mapper, expected));
        assertTrue(failure.getMessage().contains(name));
        assertTrue(failure.getMessage().contains(JsonOverlay.mapper.version().toString()));
    }

    @Test
    void stillChecksAvailableSnapshotFeatures() {
        Properties expected = new Properties();
        String name = "DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES";
        expected.setProperty(name, "false"); // The compatibility mapper enables this feature.
        AssertionFailedError failure = assertThrows(AssertionFailedError.class,
                () -> assertSnapshot(JsonOverlay.mapper, expected));
        assertTrue(failure.getMessage().contains(name));
        assertEquals(Boolean.FALSE, failure.getExpected().getValue());
        assertEquals(Boolean.TRUE, failure.getActual().getValue());
    }

    @Test
    void reportsKnownFeatureAbsenceBeforeItsIntroduction() {
        String name = "MapperFeature.SORT_PROPERTIES_BY_INDEX";
        for (int minor : new int[]{0, 1}) {
            List<String> omissions = new ArrayList<>();
            Version version = version(3, minor);
            // An enum without the new flag simulates the older Jackson line.
            assertTrue(resolveFeature(name, version, ignored -> OlderFeatures.class, omissions::add).isEmpty());
            assertEquals(1, omissions.size());
            assertTrue(omissions.get(0).contains(name));
            assertTrue(omissions.get(0).contains(version.toString()));
            assertTrue(omissions.get(0).contains("introduced in 3.2"));
        }
    }

    @Test
    void requiresTheFeatureAtAndAfterItsIntroduction() {
        String name = "MapperFeature.SORT_PROPERTIES_BY_INDEX";
        for (Version version : List.of(version(3, 2), version(3, 3), version(4, 0))) {
            List<String> omissions = new ArrayList<>();
            AssertionFailedError failure = assertThrows(AssertionFailedError.class,
                    () -> resolveFeature(name, version, ignored -> OlderFeatures.class, omissions::add));
            assertTrue(failure.getMessage().contains(name));
            assertTrue(failure.getMessage().contains(version.toString()));
            assertTrue(omissions.isEmpty());
        }
    }

    @Test
    void returnsAvailableFeatureEvenOnAnOlderVersion() {
        String name = "MapperFeature.SORT_PROPERTIES_BY_INDEX";
        for (Version version : List.of(version(3, 0), version(3, 1), version(3, 2))) {
            List<String> omissions = new ArrayList<>();
            assertSame(NewerFeatures.SORT_PROPERTIES_BY_INDEX,
                    resolveFeature(name, version, ignored -> NewerFeatures.class, omissions::add).orElseThrow());
            assertTrue(omissions.isEmpty());
        }
    }

    @Test
    void rejectsMissingFeatureClassesAndNonEnumTypesWithContext() {
        String name = "MissingFeature.FLAG";
        Version version = JsonOverlay.mapper.version();
        AssertionFailedError missingClass = assertThrows(AssertionFailedError.class,
                () -> resolveFeature(name, version, ignored -> { throw new ClassNotFoundException(); }, ignored -> fail()));
        assertTrue(missingClass.getMessage().contains(name));
        assertTrue(missingClass.getMessage().contains(version.toString()));
        assertInstanceOf(ClassNotFoundException.class, missingClass.getCause());
        AssertionFailedError nonEnum = assertThrows(AssertionFailedError.class,
                () -> resolveFeature(name, version, ignored -> String.class, ignored -> fail()));
        assertTrue(nonEnum.getMessage().contains(name));
        assertTrue(nonEnum.getMessage().contains(version.toString()));
        assertTrue(nonEnum.getMessage().contains("not an enum"));
    }

    @Test
    void checksIndexFlagValueWhenItExists() throws Exception {
        String name = "MapperFeature.SORT_PROPERTIES_BY_INDEX";
        List<String> omissions = new ArrayList<>();
        Optional<Object> feature = resolveFeature(name, JsonOverlay.mapper.version(), Class::forName, omissions::add);
        if (feature.isPresent()) {
            Properties expected = new Properties();
            boolean actual = JsonOverlay.mapper.isEnabled((tools.jackson.databind.MapperFeature) feature.get());
            expected.setProperty(name, Boolean.toString(!actual));
            AssertionFailedError failure = assertThrows(AssertionFailedError.class,
                    () -> assertSnapshot(JsonOverlay.mapper, expected));
            assertEquals(!actual, failure.getExpected().getValue());
            assertEquals(actual, failure.getActual().getValue());
            assertTrue(omissions.isEmpty());
        } else {
            assertEquals(1, omissions.size());
        }
    }

    @Test
    void preservesIndexedPropertyOrdering() throws Exception {
        var field = JsonLoader.class.getDeclaredField("yamlMapper");
        field.setAccessible(true);
        for (ObjectMapper mapper : List.of(JsonOverlay.mapper, (ObjectMapper) field.get(null))) {
            assertEquals("{\"first\":\"a\",\"second\":\"b\",\"unindexed\":\"c\"}",
                    mapper.valueToTree(new IndexedProperties()).toString());
        }
    }

    private static Version version(int major, int minor) {
        return new Version(major, minor, 0, null, null, null);
    }

    private enum OlderFeatures { OTHER }
    private enum NewerFeatures { SORT_PROPERTIES_BY_INDEX }

    static class IndexedProperties {
        @JsonProperty(index = 2) public String second = "b";
        public String unindexed = "c";
        @JsonProperty(index = 1) public String first = "a";
    }

    @Test
    void retainsMeasuredJackson2FeatureDefaults() throws Exception {
        assertSnapshot(JsonOverlay.mapper, "/jackson-2.22.3-json-features.properties");
        var field = JsonLoader.class.getDeclaredField("yamlMapper");
        field.setAccessible(true);
        assertSnapshot((ObjectMapper) field.get(null), "/jackson-2.22.3-yaml-features.properties");
    }
}
