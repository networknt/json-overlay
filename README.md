# json-overlay

## Jackson 3 migration

**Breaking API, for the next major release only.** `JsonNode`, `JsonPointer`,
`PositionInfo` location constructors, overlay signatures, and parser/factory
signatures now expose Jackson 3 types. This is not binary or source compatible
with Jackson 2 callers. The development version remains `2.4.2-SNAPSHOT` by the
coordinated migration plan; it must not be published as a compatible 2.4.x update.
Migrate and rebuild consumers (starting with openapi-parser) and coordinate the
major-version/release cutover before releasing these changes from `master`.

Jackson types in the public API now use `tools.jackson.*`. Dependencies use the
`tools.jackson:jackson-bom:3.2.3` BOM, `tools.jackson.core` and
`tools.jackson.dataformat` coordinates; annotations remain
`com.fasterxml.jackson.annotation.*`. Jackson 3 requires Java 17 or newer; this
project retains its Java 25 build target. Mappers are immutable: configure them
with `YAMLMapper.builder(...)` before building.

Jackson 3.2.3's YAML resolver reads `yes`, `no`, `on`, `off`, `True`, `TRUE`,
`False`, `FALSE`, `Null`, `NULL`, `012`, `0b11`, `0x1F`, `1_000`, and `~` as strings.
This is the measured Jackson resolver behavior; do not assume all YAML 1.2
spellings are recognized. Use lowercase `true`/`false`, ordinary decimal
numbers without a leading zero, and lowercase `null` for those scalar types.

Typed overlays do not coerce string nodes: `required: True` or `deprecated: True`
no longer supplies a BooleanOverlay value, and `count: 0x1F` no longer supplies an
IntegerOverlay value. Rewrite them as `required: true` and `count: 31` before the
cutover. Audit deployed OpenAPI documents as well as bundled fixtures.

Shared Jackson feature flags are measured against 2.22.3 and regression-tested;
the supported Jackson 2 compatibility builder profile plus explicit supplements
replace duplicated manual mapper settings. This does not promise complete
behavioral equivalence for removed, renamed, or new-only features or YAML scalars.
The loader preserves its
existing first-document behavior and source-location tracking. Jackson parse
exceptions are unchecked; callers handling malformed input should handle
`tools.jackson.core.JacksonException` as well as stream `IOException`s.
Empty and comment-only referenced documents are handled resolution failures.
SnakeYAML Engine is a direct dependency because the public parser constructor
accepts its `LoadSettings` type.

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
