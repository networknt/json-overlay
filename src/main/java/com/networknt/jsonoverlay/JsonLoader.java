/*********************************************************************
 *  Copyright (c) 2017 ModelSolv, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     ModelSolv, Inc.
 *     - initial API and implementation and/or initial documentation
 **********************************************************************/
package com.networknt.jsonoverlay;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;
import com.networknt.jsonoverlay.parser.LocationRecorderYamlFactory;
import com.networknt.jsonoverlay.parser.LocationRecorderYamlParser;

public class JsonLoader {

	private static LocationRecorderYamlFactory yamlFactory = new LocationRecorderYamlFactory();

	private static ObjectMapper yamlMapper = Jackson2Defaults.configure(YAMLMapper.builder(yamlFactory))
			.nodeFactory(MinSharingJsonNodeFactory.instance).build();

	private Map<String, JsonNode> cache = new HashMap<>();
	private Map<String, Map<JsonPointer, PositionInfo>> positions = new HashMap<>();

	public JsonLoader() {
	}

	public JsonNode load(URL url) throws IOException {
		String urlString = url.toString();
		if (cache.containsKey(urlString)) {
			return cache.get(urlString);
		}
		try (InputStream in = url.openStream()) {
			try (Scanner scanner = new Scanner(in, "UTF-8")) {
				scanner.useDelimiter("\\Z");
				String json = scanner.hasNext() ? scanner.next() : "";
				return loadString(url, json);
			}
		}
	}

	public JsonNode loadString(URL url, String json) throws IOException, JacksonException {
		Pair<JsonNode, Map<JsonPointer, PositionInfo>> result = loadWithLocations(json);
		if (url != null) {
			cache.put(url.toString(), result.getLeft());
			positions.put(url.toString(), result.getRight());
		}
		return result.getLeft();
	}

	public Optional<PositionInfo> getPositionInfo(String url, JsonPointer pointer) {
		if (positions.containsKey(url)) {
			return Optional.ofNullable(positions.get(url).get(pointer));
		} else {
			return Optional.empty();
		}
	}

	public Pair<JsonNode, Map<JsonPointer, PositionInfo>> loadWithLocations(String json) throws IOException {
		JsonNode tree;
		Map<JsonPointer, PositionInfo> regions;
		try (LocationRecorderYamlParser parser = (LocationRecorderYamlParser) yamlMapper.createParser(fixTabs(json))) {
			tree = yamlMapper.readTree(parser);
			regions = parser.getLocations();
		}
		return Pair.of(tree, regions);
	}

	private String fixTabs(String json) {
		Pattern initialTabs = Pattern.compile("^(\\t+)", Pattern.MULTILINE);
		Matcher m = initialTabs.matcher(json);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, m.group(1).replaceAll("\\t", " "));
		}
		m.appendTail(sb);
		return sb.toString();
	}
}
