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

import java.io.CharArrayReader;
import java.io.InputStream;
import java.io.Reader;

import tools.jackson.core.io.IOContext;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.dataformat.yaml.YAMLFactory;
import tools.jackson.dataformat.yaml.YAMLParser;

public class LocationRecorderYamlFactory extends YAMLFactory {

	private static final long serialVersionUID = 1L;

	@Override
	protected YAMLParser _createParser(ObjectReadContext readCtxt, IOContext ctxt, InputStream in) {
		return _createParser(readCtxt, ctxt, _createReader(in, null, ctxt));
	}

	@Override
	protected YAMLParser _createParser(ObjectReadContext readCtxt, IOContext ctxt, Reader r) {
		return new LocationRecorderYamlParser(readCtxt, ctxt, _getBufferRecycler(),
				readCtxt.getStreamReadFeatures(_streamReadFeatures),
				readCtxt.getFormatReadFeatures(_formatReadFeatures), _loadSettings, r);
	}

	@Override
	protected YAMLParser _createParser(ObjectReadContext readCtxt, IOContext ctxt,
			char[] data, int offset, int len, boolean recyclable) {
		return _createParser(readCtxt, ctxt, new CharArrayReader(data, offset, len));
	}

	@Override
	protected YAMLParser _createParser(ObjectReadContext readCtxt, IOContext ctxt,
			byte[] data, int offset, int len) {
		return _createParser(readCtxt, ctxt, _createReader(data, offset, len, null, ctxt));
	}
}
