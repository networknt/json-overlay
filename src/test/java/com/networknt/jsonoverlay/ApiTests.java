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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.networknt.jsonoverlay.model.TestModelParser;
import com.networknt.jsonoverlay.model.impl.TestModelImpl;
import com.networknt.jsonoverlay.model.intf.Color;
import com.networknt.jsonoverlay.model.intf.TestModel;

public class ApiTests {

	private TestModel model;

	@BeforeEach
	public void setup() throws IOException {
		this.model = TestModelParser.parse(getClass().getResource("/apiTestModel.yaml"));
	}

	@Test
	public void testScalarApi() {
		Assertions.assertEquals("Model description", model.getDescription());
		model.setDescription("Model Description");
		Assertions.assertEquals("Model Description", model.getDescription());
		Assertions.assertEquals(Integer.valueOf(10), model.getWidth());
		Assertions.assertNull(model.getHeight());
		model.setHeight(20);
		Assertions.assertEquals(Integer.valueOf(20), model.getHeight());
		Assertions.assertEquals(Color.GREEN, model.getColor());
		model.setColor(Color.BLUE);
		Assertions.assertEquals(Color.BLUE, model.getColor());
		model.setColor(null);
		Assertions.assertNull(model.getColor());
		Assertions.assertEquals(Arrays.asList("A", "B"), getEntryKeys());
	}

	@Test
	public void testListApi() {
		Assertions.assertTrue(model.hasIntegers());
		checkIntegers(1, 2, 3, 4, 5);
		checkIntegersPaths();
		Assertions.assertEquals(Integer.valueOf(1), model.getInteger(0));
		model.removeInteger(1);
		model.addInteger(6);
		model.setInteger(0, 100);
		model.insertInteger(1, 200);
		checkIntegers(100, 200, 3, 4, 5, 6);
		checkIntegersPaths();
		Assertions.assertEquals("Title for item 1", model.getItem(0).getTitle());
		Assertions.assertEquals("Title for item 2", model.getItem(1).getTitle());
	}

	@Test
	public void testMapApi() {
		Assertions.assertTrue(model.hasNamedIntegers());
		Assertions.assertTrue(model.hasNamedInteger("I"));
		Assertions.assertFalse(model.hasNamedInteger("X"));
		Assertions.assertEquals(Integer.valueOf(1), model.getNamedInteger("I"));
		checkNamedIntegerNames("I", "II", "III", "IV", "V");
		checkNamedIntegers(1, 2, 3, 4, 5);
		model.removeNamedInteger("I");
		model.setNamedInteger("X", 10);
		model.setNamedInteger("II", 22);
		checkNamedIntegerNames("II", "III", "IV", "V", "X");
		checkNamedIntegers(22, 3, 4, 5, 10);
		Assertions.assertEquals("Title for entry A", model.getEntry("A").getTitle());
		Assertions.assertEquals("Title for entry B", model.getEntry("B").getTitle());
	}

	@Test
	public void testPathInParent() {
		Assertions.assertEquals("description", Overlay.of((TestModelImpl) model, "description", String.class).getPathInParent());
		Assertions.assertEquals("0", Overlay.of(model.getItems(), 0).getPathInParent());
		Assertions.assertEquals("A", Overlay.of(model.getEntries(), "A").getPathInParent());
	}

	@Test
	public void testRoot() {
		Assertions.assertTrue(model == Overlay.of(model).getRoot());
		Assertions.assertTrue(model == Overlay.of(model, "description", String.class).getRoot());
		Assertions.assertTrue(model == Overlay.of(model, "integers", ListOverlay.class).getRoot());
		Assertions.assertTrue(model == Overlay.of(model, "namedIntegers", MapOverlay.class).getRoot());
		Assertions.assertTrue(model == Overlay.of(model.getEntries(), "A").getRoot());
		Assertions.assertTrue(model == Overlay.of(model.getItems(), 0).getRoot());

		Assertions.assertTrue(model == Overlay.of(model).getModel());
		Assertions.assertTrue(model == Overlay.of(model, "description", String.class).getModel());
		Assertions.assertTrue(model == Overlay.of(model, "integers", ListOverlay.class).getModel());
		Assertions.assertTrue(model == Overlay.of(model, "namedIntegers", MapOverlay.class).getModel());
		Assertions.assertTrue(model == Overlay.of(model.getEntries(), "A").getModel());
		Assertions.assertTrue(model == Overlay.of(model.getItems(), 0).getModel());
	}

	@Test
	public void testPropNames() {
		Assertions.assertEquals(Sets.newHashSet("description", "width", "height", "entries", "items", "integers", "namedIntegers",
				"color", "scalars"), Sets.newHashSet(Overlay.of(model).getPropertyNames()));
		Assertions.assertEquals(Sets.newHashSet("title"), Sets.newHashSet(Overlay.of(model.getEntries(), "A").getPropertyNames()));
		Assertions.assertEquals(Sets.newHashSet("title"), Sets.newHashSet(Overlay.of(model.getItems(), 0).getPropertyNames()));
	}

	@Test
	public void testFind() {
		checkScalarFind("description", String.class, "/description");
		checkScalarFind("width", Integer.class, "/width");
		checkScalarFind("width", Integer.class, "/width");
		checkScalarFind("color", Color.class, "/color");
		Assertions.assertTrue(Overlay.of(model.getItems(), 0).getOverlay() == Overlay.of(model).find("/items/0"));
		Assertions.assertTrue(Overlay.of(model.getItems(), 1).getOverlay() == Overlay.of(model).find("/items/1"));
		Assertions.assertFalse(Overlay.of(model.getItems(), 1).getOverlay() == Overlay.of(model).find("/items/0"));
		Assertions.assertTrue(
				Overlay.of(model.getNamedIntegers(), "I").getOverlay() == Overlay.of(model).find("/namedIntegers/I"));
		Assertions.assertTrue(
				Overlay.of(model.getNamedIntegers(), "II").getOverlay() == Overlay.of(model).find("/namedIntegers/II"));
		Assertions.assertFalse(
				Overlay.of(model.getNamedIntegers(), "I").getOverlay() == Overlay.of(model).find("/namedIntegers/II"));
	}

	@Test
	public void testPathFromRoot() {
		Assertions.assertEquals("/description", Overlay.of(model, "description", String.class).getPathFromRoot());
		Assertions.assertEquals("/width", Overlay.of(model, "width", Integer.class).getPathFromRoot());
		Assertions.assertEquals("/color", Overlay.of(model, "color", Color.class).getPathFromRoot());
		Assertions.assertEquals("/items/0", Overlay.of(model.getItems(), 0).getPathFromRoot());
		Assertions.assertEquals("/items/0/title", Overlay.of(model.getItem(0), "title", String.class).getPathFromRoot());
		Assertions.assertEquals("/entries", Overlay.of(model.getEntries()).getPathFromRoot());
		Assertions.assertEquals("/entries/A", Overlay.of(model.getEntries(), "A").getPathFromRoot());
	}

	@Test
	public void testJsonRefs() {
		String url = getClass().getResource("/apiTestModel.yaml").toString();
		Assertions.assertEquals(url + "#/description", Overlay.of(model, "description", String.class).getJsonReference());
		Assertions.assertEquals(url + "#/width", Overlay.of(model, "width", Integer.class).getJsonReference());
		Assertions.assertEquals(url + "#/color", Overlay.of(model, "color", Color.class).getJsonReference());
		Assertions.assertEquals(url + "#/items/0", Overlay.of(model.getItems(), 0).getJsonReference());
		Assertions.assertEquals(url + "#/items/0/title", Overlay.of(model.getItem(0), "title", String.class).getJsonReference());
		Assertions.assertEquals(url + "#/entries", Overlay.of(model.getEntries()).getJsonReference());
		Assertions.assertEquals(url + "#/entries/A", Overlay.of(model.getEntries(), "A").getJsonReference());
	}

	private List<String> getEntryKeys() {
		return Lists.newArrayList(model.getEntries().keySet());
	}

	private void checkIntegers(Integer... integers) {
		Assertions.assertEquals(Arrays.asList(integers), model.getIntegers());
	}

	private void checkIntegersPaths() {
		for (int i = 0; i < model.getIntegers().size(); i++) {
			Assertions.assertEquals(Integer.toString(i), Overlay.of(model.getIntegers(), i).getPathInParent());
		}
	}

	private void checkNamedIntegerNames(String... names) {
		Assertions.assertEquals(Arrays.asList(names), Lists.newArrayList(model.getNamedIntegers().keySet()));
	}

	private void checkNamedIntegers(Integer... integers) {
		Assertions.assertEquals(Arrays.asList(integers), Lists.newArrayList(model.getNamedIntegers().values()));
	}

	private <V> void checkScalarFind(String field, Class<V> fieldType, String path) {
		Assertions.assertTrue(Overlay.of(model, field, fieldType).getOverlay() == Overlay.of(model).find(path));
	}
}
