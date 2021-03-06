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
package com.networknt.jsonoverlay.model.impl;

import javax.annotation.Generated;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.jsonoverlay.*;
import com.networknt.jsonoverlay.model.intf.Entry;
import com.networknt.jsonoverlay.model.intf.TestModel;

public class EntryImpl extends PropertiesOverlay<Entry> implements Entry {

	@Generated("com.reprezen.jsonoverlay.gen.CodeGenerator")
	public EntryImpl(JsonNode json, JsonOverlay<?> parent, ReferenceManager refMgr) {
		super(json, parent, factory, refMgr);
	}

	@Generated("com.reprezen.jsonoverlay.gen.CodeGenerator")
	public EntryImpl(Entry entry, JsonOverlay<?> parent, ReferenceManager refMgr) {
		super(entry, parent, factory, refMgr);
	}

	// Title
	@Override
	@Generated("com.reprezen.jsonoverlay.gen.CodeGenerator")
	public String getTitle() {
		return _get("title", String.class);
	}

	@Override
	@Generated("com.reprezen.jsonoverlay.gen.CodeGenerator")
	public void setTitle(String title) {
		_setScalar("title", title, String.class);
	}

	@Generated("com.reprezen.jsonoverlay.gen.CodeGenerator")
	public static final String F_title = "title";

	@Override
	@Generated("com.reprezen.jsonoverlay.gen.CodeGenerator")
	protected void _elaborateJson() {
		super._elaborateJson();
		_createScalar("title", "title", StringOverlay.factory);
	}

	@Generated("com.reprezen.jsonoverlay.gen.CodeGenerator")
	public static OverlayFactory<Entry> factory = new OverlayFactory<Entry>() {

		@Override
		protected Class<? extends JsonOverlay<? super Entry>> getOverlayClass() {
			return EntryImpl.class;
		}

		@Override
		public JsonOverlay<Entry> _create(Entry entry, JsonOverlay<?> parent, ReferenceManager refMgr) {
			JsonOverlay<?> overlay;
			overlay = new EntryImpl(entry, parent, refMgr);
			@SuppressWarnings("unchecked")
			JsonOverlay<Entry> castOverlay = (JsonOverlay<Entry>) overlay;
			return castOverlay;
		}

		@Override
		public JsonOverlay<Entry> _create(JsonNode json, JsonOverlay<?> parent, ReferenceManager refMgr) {
			JsonOverlay<?> overlay;
			overlay = new EntryImpl(json, parent, refMgr);
			@SuppressWarnings("unchecked")
			JsonOverlay<Entry> castOverlay = (JsonOverlay<Entry>) overlay;
			return castOverlay;
		}

		@Override
		protected boolean isExtendedType() {
			return false;
		}
	};

	@Generated("com.reprezen.jsonoverlay.gen.CodeGenerator")
	private static Class<? extends Entry> getSubtypeOf(Entry entry) {
		return Entry.class;
	}

	@Generated("com.reprezen.jsonoverlay.gen.CodeGenerator")
	private static Class<? extends Entry> getSubtypeOf(JsonNode json) {
		return Entry.class;
	}

	@Override
	@Generated("com.reprezen.jsonoverlay.gen.CodeGenerator")
	public Class<?> _getModelType() {
		return TestModel.class;
	}

	@Override
	@Generated("com.reprezen.jsonoverlay.gen.CodeGenerator")
	protected OverlayFactory<?> _getFactory() {
		return factory;
	}

	@Generated("com.reprezen.jsonoverlay.gen.CodeGenerator")
	public static <OV extends IJsonOverlay<?>> Builder<Entry> builder(OV modelMember) {
		return new Builder<Entry>(factory, modelMember);
	}

	@Generated("com.reprezen.jsonoverlay.gen.CodeGenerator")
	public static <OV extends IJsonOverlay<?>> Entry create(OV modelMember) {
		return (Entry) builder(modelMember).build();
	}
}
