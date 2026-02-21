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
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.networknt.jsonoverlay.ResolutionException.ReferenceCycleException;
import com.networknt.jsonoverlay.model.TestModelParser;
import com.networknt.jsonoverlay.model.intf.Color;
import com.networknt.jsonoverlay.model.intf.Scalars;
import com.networknt.jsonoverlay.model.intf.TestModel;

public class ReferenceTests {

	private TestModel model;

	@BeforeEach
	public void setup() throws IOException {
		model = TestModelParser.parse(ReferenceTests.class.getResource("/refTest.yaml"));
	}

	@Test
	public void testRefAccess() {
		Assertions.assertEquals("Reference Test", model.getDescription());
		checkScalarsValues(model.getScalar("s1"), true, true);
		checkScalarsValues(model.getScalar("s2"), false, true);
		checkScalarsValues(model.getScalar("s3"), true, true);
		checkScalarsValues(model.getScalar("s4"), true, true);
		checkScalarsValues(model.getScalar("s5"), false, true);
		checkScalarsValues(model.getScalar("ext1"), false, false);
		checkScalarsValues(model.getScalar("ext2"), false, false);
		checkScalarsValues(model.getScalar("ext3"), true, true);
	}

	private void checkScalarsValues(Scalars s, boolean sharedRoot, boolean sharedValues) {
		Assertions.assertEquals("hello", s.getStringValue());
		Assertions.assertEquals(Integer.valueOf(10), s.getIntValue());
		Assertions.assertEquals(3.1, s.getNumberValue());
		Assertions.assertEquals(Boolean.TRUE, s.getBoolValue());
		Assertions.assertEquals(Arrays.asList(1, 2, 3), s.getObjValue());
		Assertions.assertEquals("abcde", s.getPrimValue());
		Assertions.assertEquals(Color.BLUE, s.getColorValue());
		checkScalarsSharing(s, sharedRoot, sharedValues);
	}

	private void checkScalarsSharing(Scalars s, boolean sharedRoot, boolean sharedValues) {
		Scalars s1 = model.getScalar("s1");
		if (sharedRoot) {
			Assertions.assertTrue(s1 == s, "Scalars objects should be shared");
		} else if (sharedValues) {
			Assertions.assertTrue(s1.getStringValue() == s.getStringValue(), "String value should be shared");
		}
	}

	@Test
	public void checkRecursion() {
		Assertions.assertTrue(model.getScalar("s6").getEmbeddedScalars() == model.getScalar("s5"));
		Assertions.assertTrue(model.getScalar("s7").getEmbeddedScalars() == model.getScalar("s7"));
		Assertions.assertTrue(model.getScalar("s8a").getEmbeddedScalars() == model.getScalar("s8b"));
		Assertions.assertTrue(model.getScalar("s8b").getEmbeddedScalars() == model.getScalar("s8a"));
		Assertions.assertTrue(model.getScalar("ext1") == model.getScalar("ext2"));
	}

	@Test
	public void checkBadRefs() {
		Assertions.assertNull(model.getScalar("badPointer"));
		checkBadRef(Overlay.of(model.getScalars()).getReference("badPointer"));
		checkBadRef(Overlay.of(model.getScalars()).getReference("cycle"));
		Assertions.assertTrue(Overlay.of(model.getScalars()).getReference("cycle")
				.getInvalidReason() instanceof ReferenceCycleException);
	}

	private void checkBadRef(Reference ref) {
		Assertions.assertTrue(ref.isInvalid());
		Assertions.assertTrue(ref.getInvalidReason() instanceof ResolutionException);
	}

	@Test
	public void testRoots() {
		Assertions.assertTrue(model == Overlay.of(model).getRoot());
		Assertions.assertTrue(model == Overlay.of(model.getScalar("s1")).getRoot());
		Scalars ext1 = model.getScalar("ext1");
		Assertions.assertTrue(ext1 == Overlay.of(ext1).getRoot());
		Assertions.assertTrue(model == Overlay.of(model.getScalar("s3")).getRoot());

		Assertions.assertTrue(model == Overlay.of(model).getModel());
		Assertions.assertTrue(model == Overlay.of(model.getScalar("s1")).getModel());
		Assertions.assertNull(Overlay.of(ext1).getModel());
		Assertions.assertTrue(model == Overlay.of(model.getScalar("s3")).getModel());
	}

	@Test
	public void testFind() {
		Assertions.assertTrue(model.getScalar("s1") == Overlay.of(model).find("/scalars/s1"));
		Assertions.assertTrue(model.getScalar("s3") == Overlay.of(model).find("/scalars/s1"));
		Assertions.assertTrue(model.getScalar("s3") == Overlay.of(model).find("/scalars/s3"));
		Assertions.assertTrue(model.getScalar("ext1") == Overlay.of(model).find("/scalars/ext1"));
		Assertions.assertTrue(model.getScalar("ext2") == Overlay.of(model).find("/scalars/ext1"));
		Assertions.assertTrue(model.getScalar("ext3") == Overlay.of(model).find("/scalars/s1"));
	}

	@Test
	public void testJsonRefs() {
		String url = getClass().getResource("/refTest.yaml").toString();
		String ext = getClass().getResource("/external.yaml").toString();
		Assertions.assertEquals(url, Overlay.of(model).getJsonReference());
		Assertions.assertEquals(url + "#/scalars/s1", Overlay.of(model.getScalars(), "s1").getJsonReference());
		Assertions.assertEquals(url + "#/scalars/s1/stringValue",
				Overlay.of(model.getScalar("s1"), "stringValue", String.class).getJsonReference());
		Assertions.assertEquals(url + "#/scalars/s1/stringValue",
				Overlay.of(model.getScalar("s2"), "stringValue", String.class).getJsonReference());
		Assertions.assertEquals(url + "#/scalars/s1", Overlay.of(model.getScalar("s3")).getJsonReference());
		Assertions.assertEquals(ext + "#/scalar1", Overlay.of(model.getScalar("ext1")).getJsonReference());
		Assertions.assertEquals(ext + "#/scalar1", Overlay.of(model.getScalar("ext2")).getJsonReference());
		Assertions.assertEquals(url + "#/scalars/s1", Overlay.of(model.getScalar("ext3")).getJsonReference());
	}
}
