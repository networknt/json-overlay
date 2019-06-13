module com.networknt.json.overlay {
    exports com.networknt.jsonoverlay;

    requires com.networknt.http.url;
    requires com.networknt.utility;

    requires org.slf4j;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
}