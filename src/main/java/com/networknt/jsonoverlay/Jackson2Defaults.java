package com.networknt.jsonoverlay;

import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.MapperBuilder;

/** Jackson 2.22.3 feature settings shared by the JSON and YAML mappers. */
final class Jackson2Defaults {
    private Jackson2Defaults() { }

    static <M extends ObjectMapper, B extends MapperBuilder<M, B>> B configure(B builder) {
        // configureForJackson2 is documented as incomplete; retain the remaining
        // shared-feature differences measured against Jackson 2.22.3 explicitly.
        return builder.configureForJackson2()
                .enable(MapperFeature.DEFAULT_VIEW_INCLUSION)
                .disable(MapperFeature.ALLOW_VOID_VALUED_PROPERTIES)
                .enable(SerializationFeature.FAIL_ON_ORDER_MAP_BY_INCOMPARABLE_KEY)
                .disable(StreamReadFeature.USE_FAST_DOUBLE_PARSER, StreamReadFeature.USE_FAST_BIG_NUMBER_PARSER);
    }
}
