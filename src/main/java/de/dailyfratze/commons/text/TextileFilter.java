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

import java.io.IOException;
import java.io.StringWriter;
import java.util.Optional;

import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import org.eclipse.mylyn.wikitext.core.util.FormattingXMLStreamWriter;
import org.eclipse.mylyn.wikitext.textile.core.TextileLanguage;

/**
 * Provides an api to Mylyns Textile-Wikitext Module. The api is - different to
 * the default Textile Api, UTF8 Multibyte aware and doesn' destroy such
 * characters.
 *
 * @author Michael J. Simons, 2014-12-27
 */
public final class TextileFilter implements TextFilter {

	/**
	 * Parses the incoming string as textile content and returns a formatted
	 * html document.
	 *
	 * @param textileContent Content in Textile format
	 * @param baseUrl        An optional base url for resolving relative urls
	 * @return Formatted document
	 */
	@Override
	public String apply(final String textileContent, final String baseUrl) {
		String rv = textileContent;
		if (!(rv == null || rv.trim().isEmpty())) {
			try (final StringWriter out = new StringWriter()) {
				final MarkupParser textileParser = new MarkupParser(new TextileLanguage(), new HtmlDocumentBuilder(
						new FormattingXMLStreamWriter(new DefaultUTF84bAwareXmlStreamWriter(out)) {
							@Override
							protected boolean preserveWhitespace(final String elementName) {
								return elementName.equals("pre") || elementName.equals("code");
							}
						}
				));
				textileParser.parse(rv, false);
				out.flush();
				rv = out.toString();
			} catch (IOException e) {
				// I'm pretty sure that the StringWriter won't cause any problems
			}
		}

		return rv;
	}
}
