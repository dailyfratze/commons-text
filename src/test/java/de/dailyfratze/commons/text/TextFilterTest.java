/*
 * Copyright 2016-2018 michael-simons.eu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.dailyfratze.commons.text;

import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * @author Michael J. Simons, 2015-01-08
 */
@DisplayName("TextFilter")
class TextFilterTest {

	@Nested
	@DisplayName("AutoBr")
	class AutoBrTest {
		@Test
		@DisplayName("should handle null and empty input")
		public void shouldHandleNullAndEmptyInput() {
			var autoBrTextFilter = TextFilter.AUTO_BR;

			assertAll(
					() -> assertNull(autoBrTextFilter.apply(null, null)),
					() -> assertEquals("", autoBrTextFilter.apply("", null)),
					() -> assertEquals("  ", autoBrTextFilter.apply("  ", null)),
					() -> assertEquals("	", autoBrTextFilter.apply("	", null))
			);
		}

		@TestFactory
		@DisplayName("should handle valid input")
		public Stream<DynamicTest> shouldHandleValidInput() {
			return Stream.of(
					tuple("Das ist ein Test", "Das ist ein Test"),
					tuple("Das ist\n ein Test", "Das ist<br /> ein Test"),
					tuple("Das ist auch:\r\n\n\r\nJa!", "Das ist auch:<br /><br /><br />Ja!")
			).map(t -> dynamicTest(t.v1, () -> assertEquals(t.v2, TextFilter.AUTO_BR.apply(t.v1, null))));
		}
	}
}
