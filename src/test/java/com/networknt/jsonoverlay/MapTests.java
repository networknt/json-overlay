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

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

public class MapTests {

	private OverlayFactory<Map<String, Integer>> factory = MapOverlay.getFactory(IntegerOverlay.factory, null);

	private final ReferenceManager refMgr = new ReferenceManager();

	private final JsonNodeFactory jfac = JsonNodeFactory.instance;
	private final char a = 'A';

	@Test
	public void testMapFromValues() {
		Map<String, Integer> map = Maps.newLinkedHashMap();
		for (int i = 0; i < 10; i++) {
			map.put(Character.toString((char) (a + i)), i);
		}
		doChecks((MapOverlay<Integer>) factory.create(map, null, refMgr));
	}

	@Test
	public void testMapFromJson() {
		ObjectNode obj = jfac.objectNode();
		for (int i = 0; i < 10; i++) {
			obj.set(Character.toString((char) (a + i)), jfac.numberNode(i));
		}
		doChecks((MapOverlay<Integer>) factory.create(obj, null, refMgr));
	}

	private void doChecks(MapOverlay<Integer> overlay) {
		// initial content: A=>0, B=>1, ..., C=>10
		Assertions.assertEquals(10, overlay.size());
		checkKeys(overlay, "A", "B", "C", "D", "E", "F", "G", "H", "I", "J");
		overlay.remove("A");
		overlay.remove("E");
		overlay.remove("J");
		// now B=>1, .. D=>4, F=>6, ..., I=>9
		Assertions.assertEquals(7, overlay.size());
		checkKeys(overlay, "B", "C", "D", "F", "G", "H", "I");
		overlay.set("A", 0);
		overlay.set("E", 4);
		overlay.set("J", 9);
		// now complete again, but A, E, and J are final keys
		Assertions.assertEquals(10, overlay.size());
		checkKeys(overlay, "B", "C", "D", "F", "G", "H", "I", "A", "E", "J");
		MapOverlay<Integer> copy = (MapOverlay<Integer>) overlay._copy();
		Assertions.assertFalse(overlay == copy, "Copy operation should yield different object");
		Assertions.assertEquals(overlay, copy);
		for (String key : overlay.value.keySet()) {
			Assertions.assertFalse(overlay._getOverlay(key) == copy._getOverlay(key), "Copy operation should create copy of each map entry");
		}
		copy.remove("B");
		copy.set("B", 1);
		Assertions.assertEquals(overlay, copy);
		Assertions.assertFalse(overlay.equals(copy, true), "Key order difference not detected");
		Assertions.assertTrue(overlay == overlay._getRoot());
		JsonOverlay<Integer> valueOverlay = overlay._getOverlay("B");
		Assertions.assertTrue(overlay == valueOverlay._getRoot());
		Assertions.assertNull(Overlay.of(overlay).getModel());
	}

	private void checkKeys(MapOverlay<Integer> overlay, String... keys) {
		int i = 0;
		for (String key : overlay._get().keySet()) {
			Assertions.assertEquals(keys[i++], key);
			Assertions.assertEquals(Integer.valueOf(key.charAt(0) - a), overlay._get().get(key));
		}
	}
}
