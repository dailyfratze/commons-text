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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * @author Michael J. Simons, 2014-12-25
 */
@DisplayName("SmileyFilter")
class SmileyFilterTest {
	@Test
	public void buildPatternShouldWork() {
		var smileyService = new SmileyFilter("buildPatternTest", this.getClass().getResourceAsStream("/smilies/buildPatternTest.pak"));
		assertEquals("(?im)((?:\\s|^)+?|(?<=[\\p{Punct}&&[^\\\"]]+))((?:\\Q:-p\\E)|(?:\\Qq-:\\E)|(?:\\Q:p\\E)|(?:\\Qq:\\E))(?=\\s|\\p{Punct}|$)+?", smileyService.generateRegex().pattern());
	}

	@Test
	public void generateHtmlTagForShouldWork() {
		var smileyService = new SmileyFilter("generateHtmlTagFor", this.getClass().getResourceAsStream("/smilies/generateHtmlTagFor.pak"));
		assertAll(
				() -> assertEquals("<img class=\"dfs\" src=\"/images/smilies/generateHtmlTagFor/evil.gif\" alt=\"evil.gif\" title=\"}:-&gt;\" />", smileyService.generateHtmlTagFor("}:->")),
				() -> assertThrows(IllegalArgumentException.class, () -> smileyService.generateHtmlTagFor("<>"), "Unknown smiley code: <>")
		);
	}

	@Test
	@DisplayName("should handle null and empty input")
	public void shouldHandleNullAndEmptyInput() {
		var smileyService = new SmileyFilter("buildPatternTest", this.getClass().getResourceAsStream("/smilies/buildPatternTest.pak"));
		assertAll(
				() -> assertNull(smileyService.apply(null, null)),
				() -> assertEquals("", smileyService.apply("", null)),
				() -> assertEquals(" ", smileyService.apply(" ", null)),
				() -> assertEquals("	", smileyService.apply("	", null))
		);
	}

	@TestFactory
	@DisplayName("should handle valid input")
	public Stream<DynamicTest> generateSmiliesShouldWork() {
		final SmileyFilter smileyFilter = new SmileyFilter("standard2.0", this.getClass().getResourceAsStream("/smilies/standard2.0.pak"));

		var smiley1 = "<img class=\"dfs\" src=\"/images/smilies/standard2.0/smiley.gif\" alt=\"smiley.gif\" title=\":)\" />";
		var smiley2 = "<img class=\"dfs\" src=\"/images/smilies/standard2.0/evil.gif\" alt=\"evil.gif\" title=\"}:-&gt;\" />";
		var smiley3 = "<img class=\"dfs\" src=\"/images/smilies/standard2.0/grin.gif\" alt=\"grin.gif\" title=\":D\" />";
		var smiley4 = "<img class=\"dfs\" src=\"/images/smilies/standard2.0/darthvader.gif\" alt=\"darthvader.gif\" title=\":darthvader:\" />";
		var smiley5 = "<img class=\"dfs\" src=\"/images/smilies/standard2.0/grin.gif\" alt=\"grin.gif\" title=\":d\" />";

		var pt1 = Stream.of(
				tuple("das ist ein test", "das ist ein test"),
				tuple(":) das ist ein test  :)", smiley1 + " das ist ein test  " + smiley1),
				tuple(" :) das ist   :)   ein test :) ", " " + smiley1 + " das ist   " + smiley1 + "   ein test " + smiley1 + " "),
				tuple(" :):).:)\n:)\nasd", " " + smiley1 + smiley1 + "." + smiley1 + "\n" + smiley1 + "\nasd"),
				tuple(" :)das ist kein smiley", " :)das ist kein smiley"),
				tuple(" :):):}:->:::).:)\n:)\nasd", " " + smiley1 + smiley1 + ":" + smiley2 + "::" + smiley1 + "." + smiley1 + "\n" + smiley1 + "\nasd"),
				tuple(" :d :darthvader: :darthvader:so ist das  :D", " " + smiley5 + " " + smiley4 + " :darthvader:so ist das  " + smiley3),
				tuple("In diesem Text ist http://NichtsLustig.de aber sowas von nicht", "In diesem Text ist http://NichtsLustig.de aber sowas von nicht")

		).map(t -> dynamicTest(t.v1, () -> assertEquals(t.v2, smileyFilter.apply(t.v1, null))));

		var hlp = "Tweet 1 https://twitter.com/Astro_Alex/status/512976828924190720 und nun soetwas Assert.assertEquals(in, smileyFilter.generateSmilies(in, null)); \n"
				+ "\n"
				+ "evventuell auch\n"
				+ "\n"
				+ "Eine Liste von Filmen, die die Benutzer von \"Daily Fratze\":/ erstellt haben:\n"
				+ "\n"
				+ "aber warum?\n"
				+ "* blah\n"
				+ "* blub";
		var pt2 = Stream.of(dynamicTest("Langer Text", () -> assertEquals(hlp, smileyFilter.apply(hlp, "http://localhost:8080"))));
		return Stream.concat(pt1, pt2);
	}

	@Test
	@DisplayName("should handle valid input")
	public void generateSmiliesShouldWork2() {
		var smileyService = new SmileyFilter("length", this.getClass().getResourceAsStream("/smilies/length.pak"));

		var in = ":}-):}";
		var s1 = "<img class=\"dfs\" src=\"/images/smilies/length/blub.gif\" alt=\"blub.gif\" title=\":}-)\" />";
		var s2 = "<img class=\"dfs\" src=\"/images/smilies/length/blah.gif\" alt=\"blah.gif\" title=\":}\" />";

		assertEquals(s1 + s2, smileyService.apply(in, null));
	}
}