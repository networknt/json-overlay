# json-overlay

## Jackson 3 migration

Jackson types in the public API now use `tools.jackson.*`. Dependencies use the
`tools.jackson:jackson-bom:3.2.3` BOM, `tools.jackson.core` and
`tools.jackson.dataformat` coordinates; annotations remain
`com.fasterxml.jackson.annotation.*`. Jackson 3 requires Java 17 or newer; this
project retains its Java 25 build target. Mappers are immutable: configure them
with `YAMLMapper.builder(...)` before building.

OpenAPI YAML follows the Jackson 3 YAML 1.2 scalar resolver: `yes`, `no`, `on`,
`off`, `012`, `0b11`, `1_000`, and `~` are strings. Use `true`/`false`, ordinary
decimal numbers, and `null` for those scalar types. The loader preserves its
existing first-document behavior and source-location tracking. Jackson parse
exceptions are unchecked; callers handling malformed input should handle
`tools.jackson.core.JacksonException` as well as stream `IOException`s.

A repackaged/modified of [RepreZen JsonOverlay](https://github.com/RepreZen/JsonOverlay) with all dependencies and code generation removed.

[Stack Overflow](https://stackoverflow.com/questions/tagged/light-4j) |
[Google Group](https://groups.google.com/forum/#!forum/light-4j) |
[Gitter Chat](https://gitter.im/networknt/light-rest-4j) |
[Subreddit](https://www.reddit.com/r/lightapi/) |
[Youtube Channel](https://www.youtube.com/channel/UCHCRMWJVXw8iB7zKxF55Byw) |
[Documentation](https://doc.networknt.com/library/json-overlay/) |
[Contribution Guide](https://doc.networknt.com/contribute/) |

### License

The json-overlay is provided under the Eclipse Public License 1.0 following the parent [JsonOverlay](https://github.com/RepreZen/JsonOverlay)
