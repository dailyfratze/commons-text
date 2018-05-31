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

import static java.lang.String.format;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import org.jsoup.nodes.Entities;

/**
 * @author Michael J. Simons, 2014-12-25
 */
public final class SmileyFilter implements TextFilter {

	/**
	 * Pattern used to separate different smiley codes for one smilie in the pak
	 * file.
	 */
	private static final String CODE_SEPARATOR_PATTERN = Pattern.quote("=+:");

	/**
	 * The name of the current smileyPack.
	 */
	private String smileyPack;

	/**
	 * The current smiley index. This map contains the smiley codes (like :*) as
	 * keys and the corresponding image file as value. This is an unmodifiable
	 * map.
	 */
	private Map<String, String> index;

	/**
	 * The current reversed smiley index. This index contains the name of the
	 * image file as key and the list of all codes available for this image as
	 * value. This is an unmodifiable map.
	 */
	private Map<String, List<String>> reverseIndex;

	/**
	 * The regular expression for processing texts and adding smilies to them.
	 */
	private Pattern regex;

	/**
	 * Initialises a smiley service with the given smileyPack.
	 *
	 * @param smileyPack        The smiley pack in use
	 * @param smileyPackContent The content of the pack
	 */
	public SmileyFilter(final String smileyPack, final InputStream smileyPackContent) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(smileyPackContent, StandardCharsets.UTF_8))) {
			this.index = reader
					.lines()
					.map(String::trim)
					.filter(line -> !line.startsWith("#"))
					.map(line -> line.split(CODE_SEPARATOR_PATTERN))
					.filter(values -> values.length >= 2)
					.flatMap(values -> {
						final Builder<Map.Entry<String, String>> streamBuilder = Stream.builder();
						for (int i = 1; i < values.length; ++i) {
							streamBuilder.add(new AbstractMap.SimpleImmutableEntry<>(values[i].trim().toLowerCase(), values[0]));
						}
						return streamBuilder.build();
					})
					.collect(collectingAndThen(
							toMap(Map.Entry::getKey, Map.Entry::getValue),
							Collections::unmodifiableMap
					));

			// Create the reverse index by grouping all codes by their filename
			this.reverseIndex = index.entrySet().stream()
					.collect(collectingAndThen(
							groupingBy(Map.Entry::getValue, mapping(Map.Entry::getKey, toList())),
							Collections::unmodifiableMap
					));

			this.regex = this.generateRegex();
		} catch (Exception ex) {
			throw new IllegalArgumentException(format("Unreadable smiley pack '%s': %s", smileyPack, ex.getMessage()));
		}

		this.smileyPack = smileyPack;
	}

	/**
	 * Generate the regex from all entries in the smiley index. The codes are
	 * sorted by descending length and then "or'd" into non-capturing groups. A
	 * smiley starts with a blank or at the beginning of a line and ends with a
	 * blank, punctuation (that doesn't fit a code) or the end of a line.
	 *
	 * @return A regex for all smilies in the index
	 */
	Pattern generateRegex() {
		return Pattern.compile(format("(?im)((?:\\s|^)+?|(?<=[\\p{Punct}&&[^\\\"]]+))(%s)(?=\\s|\\p{Punct}|$)+?",
				index
						.keySet()
						.stream()
						.sorted(Comparator.comparingInt(String::length).reversed())
						.map(key -> format("(?:%s)", Pattern.quote(key)))
						.collect(Collectors.joining("|"))
				)
		);
	}

	/**
	 * Retrieves the filename for the given code from the index, html escapes
	 * the code to bild a title and creates an image tag.
	 *
	 * @param code The code for which an image tag should be created
	 * @return An html image tag
	 * @throws IllegalArgumentException if the code is unknown
	 */
	String generateHtmlTagFor(final String code) {
		final String trimmedLowerCode = code.trim().toLowerCase();
		if (!this.index.containsKey(trimmedLowerCode)) {
			throw new IllegalArgumentException(format("Unknown smiley code: %s", trimmedLowerCode));
		}

		final String file = this.index.get(trimmedLowerCode);
		final String title = Entities.escape(code);
		return format("<img class=\"dfs\" src=\"/images/smilies/%s/%s\" alt=\"%s\" title=\"%s\" />", this.smileyPack, file, file, title);
	}

	/**
	 * Replaces all occurences of smiley codes inside the string {@code in} with
	 * html tags either containing images or other things that can better
	 * represents smilies.
	 * <br>
	 * Smilies can occur solitär, multiple times in a row etc. If they are
	 * followed immediatly by word, they won't be converted.
	 *
	 * @param in      The text in which smilies should be generated
	 * @param baseUrl An optional base url for resolving relative urls
	 * @return A text with images instead of smilie codes
	 */
	@Override
	public String apply(final String in, final Optional<String> baseUrl) {
		if (in == null) {
			return null;
		}
		final StringBuffer rv = new StringBuffer();
		final Matcher m = this.regex.matcher(in);
		while (m.find()) {

			final StringBuffer replacement = new StringBuffer();
			final Optional<String> g1 = Optional.ofNullable(m.group(1));
			final Optional<String> g2 = Optional.of(m.group(2));

			// Start of a smilie: Blank or the beginning of a line
			if (g1.isPresent() && !g1.get().isEmpty()) {
				replacement.append(g1.get());
			}

			// The smilie code itself
			if (!g2.get().isEmpty()) {
				replacement.append(generateHtmlTagFor(g2.get()));
			}

			m.appendReplacement(rv, replacement.toString());
		}
		m.appendTail(rv);
		return rv.toString();
	}
}
