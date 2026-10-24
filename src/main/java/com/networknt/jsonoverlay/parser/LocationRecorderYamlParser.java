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
package com.networknt.jsonoverlay.parser;

import static tools.jackson.core.JsonToken.PROPERTY_NAME;

import java.io.Reader;
import java.util.Map;

import tools.jackson.core.TokenStreamLocation;
import tools.jackson.core.JsonPointer;
import tools.jackson.core.JsonToken;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.io.IOContext;
import tools.jackson.core.util.BufferRecycler;
import tools.jackson.dataformat.yaml.YAMLParser;
import org.snakeyaml.engine.v2.api.LoadSettings;
import com.networknt.jsonoverlay.PositionInfo;

public class LocationRecorderYamlParser extends YAMLParser {

	private final LocationProcessor processor = new LocationProcessor();

	public LocationRecorderYamlParser(ObjectReadContext readCtxt, IOContext ctxt, BufferRecycler br,
			int parserFeatures, int formatFeatures, LoadSettings loadSettings, Reader reader) {
		super(readCtxt, ctxt, br, parserFeatures, formatFeatures, loadSettings, reader);
	}

	public Map<JsonPointer, PositionInfo> getLocations() {
		return processor.getLocations();
	}

	@Override
	public JsonToken nextToken() {
		JsonToken token = super.nextToken();
		if (token == null) {
			return null;
		}
		String fieldName = token == PROPERTY_NAME ? currentName() : null;
		TokenStreamLocation tokenStart = currentTokenLocation();
		TokenStreamLocation tokenEnd = currentLocation();
		processor.processTokenLocation(token, fieldName, tokenStart, tokenEnd);
		return token;
	}
}
