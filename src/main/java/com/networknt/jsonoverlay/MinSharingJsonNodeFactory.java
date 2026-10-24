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

import java.math.BigDecimal;
import java.math.BigInteger;

import tools.jackson.databind.node.BigIntegerNode;
import tools.jackson.databind.node.DecimalNode;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.FloatNode;
import tools.jackson.databind.node.IntNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.LongNode;
import tools.jackson.databind.node.NumericNode;
import tools.jackson.databind.node.ShortNode;
import tools.jackson.databind.node.StringNode;
import tools.jackson.databind.node.ValueNode;

public class MinSharingJsonNodeFactory extends JsonNodeFactory {

	private static final long serialVersionUID = 1L;

	public static MinSharingJsonNodeFactory instance = new MinSharingJsonNodeFactory();

	@Override
	public NumericNode numberNode(byte v) {
		return new IntNode(v);
	}

	@Override
	public ValueNode numberNode(Byte value) {
		return value != null ? new IntNode(value) : nullNode();
	}

	@Override
	public NumericNode numberNode(short v) {
		return new ShortNode(v);
	}

	@Override
	public ValueNode numberNode(Short value) {
		return value != null ? new ShortNode(value) : nullNode();
	}

	@Override
	public NumericNode numberNode(int v) {
		return new IntNode(v);
	}

	@Override
	public ValueNode numberNode(Integer value) {
		return value != null ? new IntNode(value) : nullNode();
	}

	@Override
	public NumericNode numberNode(long v) {
		return new LongNode(v);
	}

	@Override
	public ValueNode numberNode(Long value) {
		return value != null ? new LongNode(value) : nullNode();
	}

	@Override
	public NumericNode numberNode(BigInteger v) {
		return v != null ? new BigIntegerNode(v) : null;
	}

	@Override
	public NumericNode numberNode(float v) {
		return new FloatNode(v);
	}

	@Override
	public ValueNode numberNode(Float value) {
		return value != null ? new FloatNode(value) : nullNode();
	}

	@Override
	public NumericNode numberNode(double v) {
		return new DoubleNode(v);
	}

	@Override
	public ValueNode numberNode(Double value) {
		return value != null ? new DoubleNode(value) : nullNode();
	}

	@Override
	public NumericNode numberNode(BigDecimal v) {
		return v != null ? new DecimalNode(v) : null;
	}

	@Override
	public StringNode stringNode(String text) {
		return text != null ? new StringNode(text) : null;
	}
}
