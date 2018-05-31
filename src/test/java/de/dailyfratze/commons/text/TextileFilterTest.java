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
 * @author Michael J. Simons, 2014-12-27
 */
class TextileFilterTest {
	@Test
	@DisplayName("should handle null and empty input")
	public void shouldHandleNullAndEmptyInput() {
		var textileFilter = new TextileFilter();

		assertAll(
				() -> assertNull(textileFilter.apply(null, Optional.empty())),
				() -> assertEquals("", textileFilter.apply("", Optional.empty())),
				() -> assertEquals(" ", textileFilter.apply(" ", Optional.empty())),
				() -> assertEquals("	", textileFilter.apply("	", Optional.empty()))
		);
	}

	@TestFactory
	@DisplayName("should handle valid input")
	public Stream<DynamicTest> filteringShouldWork() {
		var textileFilter = new TextileFilter();
		return Stream.of(
				tuple(
						"@das ist code@",
						"<p>\n"
								+ "	<code>das ist code</code>\n"
								+ "</p>"
				),
				tuple(
						"vorher *👍 👏* _nachher_ \"👍\":http://planet-punk.de",
						"<p>vorher \n"
								+ "	<strong>👍 👏</strong> \n"
								+ "	<em>nachher</em> \n"
								+ "	<a href=\"http://planet-punk.de\">👍</a>\n"
								+ "</p>"),
				tuple(
						"* das ist ein stern, nicht?",
						"<ul>\n"
								+ "	<li>das ist ein stern, nicht?</li>\n"
								+ "</ul>"
				),
				tuple(
						"==*==das ist ein stern",
						"<p>==*==das ist ein stern</p>"
				),
				tuple(
						"==*== das ist ein stern, jetzt aber?",
						"<p>* das ist ein stern, jetzt aber?</p>"
				),
				tuple(
						"Das ist ein langer Test\n\n-durchgestrichener absatz mit blah blah und einem Zeilenumbruch-\n+und noch etwas text danach+",
						"<p>Das ist ein langer Test</p>\n"
								+ "<p>\n"
								+ "	<del>durchgestrichener absatz mit blah blah und einem Zeilenumbruch</del>\n"
								+ "	<br/>\n"
								+ "	<ins>und noch etwas text danach</ins>\n"
								+ "</p>"
				),
				tuple(
						"aber \"die da vom bloeden Institut\":http://www.statoek.wiso.uni-goettingen.de/cms/user/index.php?lang=de&section=teaching.ss2008.statistics haben",
						"<p>aber \n"
								+ "	<a href=\"http://www.statoek.wiso.uni-goettingen.de/cms/user/index.php?lang=de&amp;section=teaching.ss2008.statistics\">die da vom bloeden Institut</a> haben\n"
								+ "</p>"
				),
				tuple(
						new HtmlFilter("a").apply("aber \"die da vom bloeden Institut\":http://www.statoek.wiso.uni-goettingen.de/cms/user/index.php?lang=de&section=teaching.ss2008.statistics haben\n\n"
								+ "\"Test 1 textile\":http://planet-punk.de/?s=queen&section=2\n\n"
								+ "<a href=\"http://planet-punk.de/?s=queen&section=2\">Test 2 ancho2r</a>", Optional.empty()),
						"<p>aber \n"
								+ "	<a href=\"http://www.statoek.wiso.uni-goettingen.de/cms/user/index.php?lang=de&amp;section=teaching.ss2008.statistics\">die da vom bloeden Institut</a> haben\n"
								+ "</p>\n"
								+ "<p>\n"
								+ "	<a href=\"http://planet-punk.de/?s=queen&amp;section=2\">Test 1 textile</a>\n"
								+ "</p>\n"
								+ "<p><a href=\"http://planet-punk.de/?s=queen&section=2\">Test 2 ancho2r</a></p>"
				),
				tuple(
						"\"P1000177.JPG\":/my/attachments/7999",
						"<p>\n"
								+ "	<a href=\"/my/attachments/7999\">P1000177.JPG</a>\n"
								+ "</p>"
				)).map(t -> dynamicTest(t.v1, () -> assertEquals(t.v2, textileFilter.apply(t.v1, Optional.empty()))));
	}
}