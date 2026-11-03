package com.networknt.jsonoverlay;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

class Jackson2FeatureDefaultsTest {
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
            String[] parts = name.split("\\.", 2);
            String namespace = parts[0].startsWith("Stream") ? "tools.jackson.core."
                    : "tools.jackson.databind." + (parts[0].equals("EnumFeature")
                    || parts[0].equals("JsonNodeFeature") || parts[0].equals("DateTimeFeature") ? "cfg." : "");
            Class<?> type = Class.forName(namespace + parts[0]);
            Object feature = java.util.Arrays.stream(type.getEnumConstants())
                    .filter(value -> ((Enum<?>) value).name().equals(parts[1])).findFirst().orElse(null);
            // This flag was added in Jackson 3.2. The measured Jackson 2 snapshot
            // also covers the supported 3.1 line, where there is no flag to check.
            if (feature == null && name.equals("MapperFeature.SORT_PROPERTIES_BY_INDEX")
                    && mapper.version().getMajorVersion() == 3 && mapper.version().getMinorVersion() == 1) {
                continue;
            }
            assertNotNull(feature, "Snapshot feature " + name + " is unavailable in Jackson " + mapper.version());
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
        AssertionError failure = assertThrows(AssertionError.class, () -> assertSnapshot(JsonOverlay.mapper, expected));
        assertTrue(failure.getMessage().contains(name));
    }

    @Test
    void retainsMeasuredJackson2FeatureDefaults() throws Exception {
        assertSnapshot(JsonOverlay.mapper, "/jackson-2.22.3-json-features.properties");
        var field = JsonLoader.class.getDeclaredField("yamlMapper");
        field.setAccessible(true);
        assertSnapshot((ObjectMapper) field.get(null), "/jackson-2.22.3-yaml-features.properties");
    }
}
