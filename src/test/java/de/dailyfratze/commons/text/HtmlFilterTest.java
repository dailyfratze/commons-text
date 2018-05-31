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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * @author Michael J. Simons, 2014-12-26
 */
class HtmlFilterTest {
	@Test
	@DisplayName("should handle null and empty input")
	public void shouldHandleNullAndEmptyInput() {
		var htmlFilter = new HtmlFilter();

		assertAll(
				() -> assertNull(htmlFilter.apply(null, Optional.empty())),
				() -> assertEquals("", htmlFilter.apply("", Optional.empty())),
				() -> assertEquals(" ", htmlFilter.apply(" ", Optional.empty())),
				() -> assertEquals("	", htmlFilter.apply("	", Optional.empty()))
		);
	}

	@TestFactory
	@DisplayName("should handle valid input")
	public Stream<DynamicTest> cleanShouldWork() {
		var pt1 = Stream.of(
				tuple("<strong>a b &#x1f44d; &#x1f44f; 👍</strong>", "a b 👍 👏 👍"),
				tuple("vorher *👍 👏* _nachher_ \"👍\":http://planet-punk.de", "vorher *👍 👏* _nachher_ \"👍\":http://planet-punk.de"),
				tuple("Das ist ein Test", "Das ist ein Test"),
				tuple("12&34", "12&34"),
				tuple("öäü…&ÖÄÜß", "öäü…&ÖÄÜß"),
				tuple("blah \" <a <3 and 3>' http://simons.ac?blah=blub&foo=bar", "blah \" ' http://simons.ac?blah=blub&foo=bar"),
				tuple("<3 }:-> <3", "<3 }:-> <3"),
				tuple("this is <not a > tag", "this is  tag")
		).map(t -> dynamicTest(t.v1, () -> assertEquals(t.v2, new HtmlFilter().apply(t.v1, Optional.empty()))));

		var pt2 = Stream.of(
				tuple("Das ist ein <xx>langer</xx> Test zu <a href=\"http://simons.ac\">simons.ac</a>.", "Das ist ein langer Test zu <a href=\"http://simons.ac\">simons.ac</a>.", new String[]{"a"}),
				tuple("<p>blah <del>foo</del>bar <3  E></p>", "<p>blah <del>foo</del>bar <3  E></p>", new String[]{"p", "del"})
		).map(t -> dynamicTest(t.v1, () -> assertEquals(t.v2, new HtmlFilter(t.v3).apply(t.v1, Optional.empty()))));

		return Stream.concat(pt1, pt2);
	}
}