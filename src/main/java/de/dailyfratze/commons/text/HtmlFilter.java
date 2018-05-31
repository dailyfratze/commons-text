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

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;

/**
 * Used for cleaning HTML input (whitelisting stuff).
 *
 * @author Michael J. Simons, 2014-12-26
 */
public class HtmlFilter implements TextFilter {

	private final Whitelist whitelist;

	/**
	 * Creates a new HtmlFilter for cleaning HTML documents with an optional whitelist of allowed tags.
	 * The default is a whitelist containing no elements. If some elements are allowed,
	 * additional default attributes for a, blockquote, q and img are added as well.
	 *
	 * @param allowedTags The allowed tags. Can be empty
	 */
	public HtmlFilter(final String... allowedTags) {
		this.whitelist = Whitelist.none();
		if (allowedTags.length != 0) {
			whitelist
					.addTags(allowedTags)
					.addAttributes("a", "href", "target").addProtocols("a", "href", "ftp", "http", "https", "mailto")
					.addAttributes("blockquote", "cite").addProtocols("blockquote", "cite", "http", "https")
					.addAttributes("q", "cite").addProtocols("q", "cite", "http", "https")
					.addAttributes("img", "align", "alt", "height", "src", "title", "width").addProtocols("img", "src", "http", "https");
		}
	}

	/**
	 * Strips all html tags from the text {@code dirtyText} but the
	 * {@code allowedTags}. The optional {@code baseUrl} allows for resolving
	 * relativ urls in the input text against an absolute url.
	 *
	 * @param dirtyText The dirty text
	 * @param baseUrl An optional base url for relative urls in
	 * {@code dirtyText}
	 * @return A cleand text (in UTF-8 encoding)
	 */
	public String apply(final String dirtyText, final Optional<String> baseUrl) {
		String rv = dirtyText;
		if (!(rv == null || rv.trim().isEmpty())) {
			final Cleaner cleaner = new Cleaner(this.whitelist);
			final Document cleanedDocument = cleaner.clean(Jsoup.parseBodyFragment(rv, baseUrl.orElse("")));
			cleanedDocument
					.outputSettings()
					.prettyPrint(false)
					.escapeMode(EscapeMode.xhtml)
					.charset(StandardCharsets.UTF_8);
			rv = Parser.unescapeEntities(cleanedDocument.body().html().trim(), true);
		}
		return rv;
	}
}
