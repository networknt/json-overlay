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
import java.net.MalformedURLException;
import java.util.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;

public class ScalarTests {
 
 	protected static final com.fasterxml.jackson.databind.node.JsonNodeFactory jfac = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance;

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class StringTests {

		public Collection<String> getValues() {
			return Lists.newArrayList("hello", "");
		}

		@ParameterizedTest
		@MethodSource("getValues")
		public void test(String value) throws MalformedURLException {
			new Instance(value).runAll();
		}

		private static class Instance extends ScalarTestBase<String> {
			Instance(String value) {
				super(StringOverlay.factory);
				this.value = value;
			}

			@Override
			protected JsonNode toJson(String value) {
				return value != null ? jfac.textNode(value) : MissingNode.getInstance();
			}
		}
	}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class BooleanTests {

		public Collection<Boolean> getValues() {
			return Lists.newArrayList(true, false);
		}

		@ParameterizedTest
		@MethodSource("getValues")
		public void test(Boolean value) throws MalformedURLException {
			new Instance(value).runAll();
		}

		private static class Instance extends ScalarTestBase<Boolean> {
			Instance(Boolean value) {
				super(BooleanOverlay.factory);
				this.value = value;
			}

			@Override
			protected JsonNode toJson(Boolean value) {
				return value != null ? jfac.booleanNode(value) : MissingNode.getInstance();
			}
		}
	}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class IntegerTests {

		public Collection<Integer> getValues() {
			return Lists.newArrayList(0, 1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE);
		}

		@ParameterizedTest
		@MethodSource("getValues")
		public void test(Integer value) throws MalformedURLException {
			new Instance(value).runAll();
		}

		private static class Instance extends ScalarTestBase<Integer> {
			Instance(Integer value) {
				super(IntegerOverlay.factory);
				this.value = value;
			}

			@Override
			protected JsonNode toJson(Integer value) {
				return value != null ? jfac.numberNode(value) : MissingNode.getInstance();
			}
		}
	}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class NumberTests {

		public Collection<Number> getValues() {
			return Lists.newArrayList( //
					BigDecimal.ZERO, BigDecimal.ONE, new BigDecimal("-1"),
					new BigDecimal("4323433423423423423424234234234234.342342313434253432342412342342342232"), //
					BigInteger.ZERO, BigInteger.ONE, new BigInteger("-1"), //
					new BigInteger("312371230981234712398471234912873491283471293847129348712349821374129347823"), //
					0.0d, 1.0d, -1.0d, Double.MAX_VALUE, -Double.MAX_VALUE, Double.MIN_VALUE, -Double.MIN_VALUE, //
					0.0, 1.0, -1.0, Float.MAX_VALUE, -Float.MAX_VALUE, Float.MIN_VALUE, -Float.MIN_VALUE, //
					0, 1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE, //
					0L, 1L, -1L, Long.MAX_VALUE, Long.MIN_VALUE, //
					Short.valueOf((short) 0), Short.valueOf((short) 1), Short.valueOf((short) -1),
					Short.valueOf(Short.MAX_VALUE), Short.valueOf(Short.MIN_VALUE) //
			);
		}

		@ParameterizedTest
		@MethodSource("getValues")
		public void test(Number value) throws MalformedURLException {
			new Instance(value).runAll();
		}

		private static class Instance extends ScalarTestBase<Number> {
			Instance(Number value) {
				super(NumberOverlay.factory);
				this.value = value;
			}

			@Override
			protected JsonNode toJson(Number value) {
				return numberToJson(value);
			}
		}

		// broken out so can be reused in PrimitiveTests
		public static JsonNode numberToJson(Number value) {
			if (value == null) {
				return MissingNode.getInstance();
			} else if (value instanceof BigDecimal) {
				return jfac.numberNode((BigDecimal) value);
			} else if (value instanceof BigInteger) {
				return jfac.numberNode((BigInteger) value);
			} else if (value instanceof Double) {
				return jfac.numberNode((Double) value);
			} else if (value instanceof Float) {
				return jfac.numberNode((Float) value);
			} else if (value instanceof Integer) {
				return jfac.numberNode((Integer) value);
			} else if (value instanceof Long) {
				return jfac.numberNode((Long) value);
			} else if (value instanceof Short) {
				return jfac.numberNode((Short) value);
			}
			throw new IllegalArgumentException();
		}
	}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class PrimitiveTests {

		public Collection<Object> getValues() {
			return Lists.newArrayList( //
					"hello", "", //
					BigDecimal.ZERO, BigDecimal.ONE, new BigDecimal("-1"),
					new BigDecimal("4323433423423423423424234234234234.342342313434253432342412342342342232"), //
					BigInteger.ZERO, BigInteger.ONE, new BigInteger("-1"), //
					new BigInteger("312371230981234712398471234912873491283471293847129348712349821374129347823"), //
					0.0d, 1.0d, -1.0d, Double.MAX_VALUE, -Double.MAX_VALUE, Double.MIN_VALUE, -Double.MIN_VALUE, //
					0.0, 1.0, -1.0, Float.MAX_VALUE, -Float.MAX_VALUE, Float.MIN_VALUE, -Float.MIN_VALUE, //
					0, 1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE, //
					0L, 1L, -1L, Long.MAX_VALUE, Long.MIN_VALUE, //
					Short.valueOf((short) 0), Short.valueOf((short) 1), Short.valueOf((short) -1),
					Short.valueOf(Short.MAX_VALUE), Short.valueOf(Short.MIN_VALUE) //
			);
		}

		@ParameterizedTest
		@MethodSource("getValues")
		public void test(Object value) throws MalformedURLException {
			new Instance(value).runAll();
		}

		private static class Instance extends ScalarTestBase<Object> {
			Instance(Object value) {
				super(PrimitiveOverlay.factory);
				this.value = value;
			}

			@Override
			protected JsonNode toJson(Object value) {
				if (value == null) {
					return MissingNode.getInstance();
				} else if (value instanceof Number) {
					return NumberTests.numberToJson((Number) value);
				} else if (value instanceof String) {
					return jfac.textNode((String) value);
				} else {
					throw new IllegalArgumentException();
				}
			}
		}
	}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class ObjectTests {

		public Collection<Object> getValues() {
			Map<String, Object> map = Maps.newHashMap();
			map.put("x", 1);
			map.put("y", null);
			map.put("z", Arrays.asList("a", "b", "c"));
			return Lists.newArrayList("foo", 1, 1.0, Arrays.asList(0, 1, 2),
					Arrays.<Object>asList(3, "blah", Arrays.asList(1, 2, 3)), map);
		}

		@ParameterizedTest
		@MethodSource("getValues")
		public void test(Object value) throws MalformedURLException {
			new Instance(value).runAll();
		}

		private static class Instance extends ScalarTestBase<Object> {
			Instance(Object value) {
				super(ObjectOverlay.factory);
				this.value = value;
			}

			private static final ObjectMapper mapper = new ObjectMapper();

			@Override
			protected JsonNode toJson(Object value) {
				return value != null ? mapper.convertValue(value, JsonNode.class) : MissingNode.getInstance();
			}

			public void testWithWrongJson() {
				// there's no "wrong json" for this overlay, so this test is a no-op
			}
		}
	}

	public static enum XEnum {
		A, B, C
	}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class EnumTests {

		public Collection<XEnum> getValues() {
			return Arrays.asList(XEnum.values());
		}

		@ParameterizedTest
		@MethodSource("getValues")
		public void test(XEnum value) throws MalformedURLException {
			new Instance(value).runAll();
		}

		private static class Instance extends ScalarTestBase<XEnum> {
			Instance(XEnum value) {
				super(XEnumOverlay.factory);
				this.value = value;
			}

			@Override
			protected JsonNode toJson(XEnum value) {
				return value != null ? jfac.textNode(value.name()) : MissingNode.getInstance();
			}
		}

		public static class XEnumOverlay extends EnumOverlay<XEnum> {

			public XEnumOverlay(JsonNode json, JsonOverlay<?> parent, ReferenceManager refMgr) {
				super(json, parent, factory, refMgr);
			}

			public XEnumOverlay(XEnum value, JsonOverlay<?> parent, ReferenceManager refMgr) {
				super(value, parent, factory, refMgr);
			}

			@Override
			protected Class<XEnum> getEnumClass() {
				return XEnum.class;
			}

			@Override
			protected OverlayFactory<XEnum> _getFactory() {
				return factory;
			}

			public static OverlayFactory<XEnum> factory = new OverlayFactory<XEnum>() {
				@Override
				protected Class<? extends JsonOverlay<? super XEnum>> getOverlayClass() {
					return XEnumOverlay.class;
				}

				@Override
				public JsonOverlay<XEnum> _create(XEnum value, JsonOverlay<?> parent, ReferenceManager refMgr) {
					return new XEnumOverlay(value, parent, refMgr);
				}

				@Override
				public JsonOverlay<XEnum> _create(JsonNode json, JsonOverlay<?> parent, ReferenceManager refMgr) {
					return new XEnumOverlay(json, parent, refMgr);
				}
			};
		}
	}
}
