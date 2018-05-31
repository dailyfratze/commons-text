/**
 * *****************************************************************************
 * Copyright (c) 2007, 2010 David Green and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: David Green - initial API and implementation
 ******************************************************************************
 */
package de.dailyfratze.commons.text;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.eclipse.mylyn.internal.wikitext.core.util.XML11Char;
import org.eclipse.mylyn.wikitext.core.util.XmlStreamWriter;

/**
 * A default implementation of {@link XmlStreamWriter} that creates XML
 * character output.
 *
 * @author David Green
 * @author Michael J. Simons
 * @since 1.0
 */
final class DefaultUTF84bAwareXmlStreamWriter extends XmlStreamWriter {

	private PrintWriter printWriter;

	private final Map<String, String> prefixToUri = new HashMap<>();

	private final Map<String, String> uriToPrefix = new HashMap<>();

	private boolean inEmptyElement = false;

	private boolean inStartElement = false;

	private final Stack<String> elements = new Stack<>();

	private char xmlHederQuoteChar = '\'';

	DefaultUTF84bAwareXmlStreamWriter(final OutputStream out) throws UnsupportedEncodingException {
		this.printWriter = createUtf8PrintWriter(out);
	}

	DefaultUTF84bAwareXmlStreamWriter(final Writer out) {
		this.printWriter = new PrintWriter(out);
	}

	DefaultUTF84bAwareXmlStreamWriter(final Writer out, final char xmlHeaderQuoteChar) {
		this.printWriter = new PrintWriter(out);
		this.xmlHederQuoteChar = xmlHeaderQuoteChar;
	}

	protected PrintWriter createUtf8PrintWriter(final OutputStream out) throws UnsupportedEncodingException {
		return new java.io.PrintWriter(new OutputStreamWriter(out, "UTF8")); //$NON-NLS-1$
	}

	@Override
	public void close() {
		if (printWriter != null) {
			closeElement();
			flush();
		}
		printWriter = null;
	}

	@Override
	public void flush() {
		printWriter.flush();
	}

	@Override
	public String getPrefix(final String uri) {
		return uriToPrefix.get(uri);
	}

	public Object getProperty(final String name) throws IllegalArgumentException {
		return null;
	}

	@Override
	public void setDefaultNamespace(final String uri) {
		setPrefix("", uri); //$NON-NLS-1$
	}

	@Override
	public void setPrefix(final String prefix, final String uri) {
		prefixToUri.put(prefix, uri);
		uriToPrefix.put(uri, prefix);
	}

	@Override
	public void writeAttribute(final String localName, final String value) {
		printWriter.write(' ');
		printWriter.write(localName);
		printWriter.write("=\""); //$NON-NLS-1$
		if (value != null) {
			attrEncode(value);
		}
		printWriter.write("\""); //$NON-NLS-1$
	}

	@Override
	public void writeAttribute(final String namespaceURI, final String localName, final String value) {
		printWriter.write(' ');
		String prefix = uriToPrefix.get(namespaceURI);
		if (prefix != null && prefix.length() > 0) {
			printWriter.write(prefix);
			printWriter.write(':');
		}
		printWriter.write(localName);
		printWriter.write("=\""); //$NON-NLS-1$
		if (value != null) {
			attrEncode(value);
		}
		printWriter.write("\""); //$NON-NLS-1$
	}

	@Override
	public void writeAttribute(final String prefix, final String namespaceURI, final String localName, final String value) {
		printWriter.write(' ');
		if (prefix != null && prefix.length() > 0) {
			printWriter.write(prefix);
			printWriter.write(':');
		}
		printWriter.write(localName);
		printWriter.write("=\""); //$NON-NLS-1$
		if (value != null) {
			attrEncode(value);
		}
		printWriter.write("\""); //$NON-NLS-1$
	}

	private void attrEncode(final String value) {
		if (value == null) {
			return;
		}
		printEscaped(printWriter, value, true);
	}

	private void encode(final String text) {
		if (text == null) {
			return;
		}
		printEscaped(printWriter, text, false);
	}

	@Override
	public void writeCData(final String data) {
		closeElement();
		printWriter.write("<![CDATA["); //$NON-NLS-1$
		printWriter.write(data);
		printWriter.write("]]>"); //$NON-NLS-1$
	}

	@Override
	public void writeCharacters(final String text) {
		closeElement();
		encode(text);
	}

	public void writeCharactersUnescaped(final String text) {
		closeElement();
		printWriter.print(text);
	}

	@Override
	public void writeLiteral(final String literal) {
		writeCharactersUnescaped(literal);
	}

	@Override
	public void writeCharacters(final char[] text, final int start, final int len) {
		closeElement();
		encode(new String(text, start, len));
	}

	@Override
	public void writeComment(final String data) {
		closeElement();
		printWriter.write("<!-- "); //$NON-NLS-1$
		printWriter.write(data);
		printWriter.write(" -->"); //$NON-NLS-1$
	}

	@Override
	public void writeDTD(final String dtd) {
		printWriter.write(dtd);
	}

	@Override
	public void writeDefaultNamespace(final String namespaceURI) {
		writeAttribute("xmlns", namespaceURI); //$NON-NLS-1$
	}

	private void closeElement() {
		if (inEmptyElement) {
			printWriter.write("/>"); //$NON-NLS-1$
			inEmptyElement = false;
		} else if (inStartElement) {
			printWriter.write(">"); //$NON-NLS-1$
			inStartElement = false;
		}
	}

	@Override
	public void writeEmptyElement(final String localName) {
		closeElement();
		inEmptyElement = true;
		printWriter.write('<');
		printWriter.write(localName);
	}

	@Override
	public void writeEmptyElement(final String namespaceURI, final String localName) {
		closeElement();
		inEmptyElement = true;
		String prefix = uriToPrefix.get(namespaceURI);
		printWriter.write('<');
		if (prefix != null && prefix.length() > 0) {
			printWriter.write(prefix);
			printWriter.write(':');
		}
		printWriter.write(localName);
	}

	@Override
	public void writeEmptyElement(final String prefix, final String localName, final String namespaceURI) {
		closeElement();
		inEmptyElement = true;
		printWriter.write('<');
		if (prefix != null && prefix.length() > 0) {
			printWriter.write(prefix);
			printWriter.write(':');
		}
		printWriter.write(localName);
	}

	@Override
	public void writeEndDocument() {
		if (!elements.isEmpty()) {
			throw new IllegalStateException(elements.size() + " elements not closed"); //$NON-NLS-1$
		}
	}

	@Override
	public void writeEndElement() {
		closeElement();
		if (elements.isEmpty()) {
			throw new IllegalStateException();
		}
		String name = elements.pop();
		printWriter.write('<');
		printWriter.write('/');
		printWriter.write(name);
		printWriter.write('>');
	}

	@Override
	public void writeEntityRef(final String name) {
		closeElement();
		printWriter.write('&');
		printWriter.write(name);
		printWriter.write(';');
	}

	@Override
	public void writeNamespace(final String prefix, final String namespaceURI) {
		if (prefix == null || prefix.length() == 0) {
			writeAttribute("xmlns", namespaceURI); //$NON-NLS-1$
		} else {
			writeAttribute("xmlns:" + prefix, namespaceURI); //$NON-NLS-1$
		}
	}

	@Override
	public void writeProcessingInstruction(final String target) {
		closeElement();
	}

	@Override
	public void writeProcessingInstruction(final String target, final String data) {
		closeElement();

	}

	@Override
	public void writeStartDocument() {
		printWriter.write(processXmlHeader("<?xml version='1.0' ?>")); //$NON-NLS-1$
	}

	@Override
	public void writeStartDocument(final String version) {
		printWriter.write(processXmlHeader("<?xml version='" + version + "' ?>")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public void writeStartDocument(final String encoding, final String version) {
		printWriter.write(processXmlHeader("<?xml version='" + version + "' encoding='" + encoding + "' ?>")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	public void writeStartElement(final String localName) {
		closeElement();
		inStartElement = true;
		elements.push(localName);
		printWriter.write('<');
		printWriter.write(localName);
	}

	@Override
	public void writeStartElement(final String namespaceURI, final String localName) {
		closeElement();
		inStartElement = true;
		String prefix = uriToPrefix.get(namespaceURI);
		printWriter.write('<');
		if (prefix != null && prefix.length() > 0) {
			printWriter.write(prefix);
			printWriter.write(':');
			elements.push(prefix + ':' + localName);
		} else {
			elements.push(localName);
		}
		printWriter.write(localName);
	}

	@Override
	public void writeStartElement(final String prefix, final String localName, final String namespaceURI) {
		closeElement();
		inStartElement = true;
		elements.push(localName);
		printWriter.write('<');
		if (prefix != null && prefix.length() > 0) {
			printWriter.write(prefix);
			printWriter.write(':');
		}
		printWriter.write(localName);
	}

	public char getXmlHederQuoteChar() {
		return xmlHederQuoteChar;
	}

	public void setXmlHederQuoteChar(final char xmlHederQuoteChar) {
		this.xmlHederQuoteChar = xmlHederQuoteChar;
	}

	private String processXmlHeader(final String header) {
		return xmlHederQuoteChar == '\'' ? header : header.replace('\'', xmlHederQuoteChar);
	}

	private static void printEscaped(final PrintWriter writer, final CharSequence s, final boolean attribute) {
		int length = s.length();

		try {
			for (int x = 0; x < length; ++x) {
				char ch = s.charAt(x);
				if (x < length - 1 && Character.isSurrogatePair(ch, s.charAt(x + 1))) {
					writer.write(new String(new int[]{Character.codePointAt(s, x++)}, 0, 1));
				} else {
					printEscaped(writer, ch, attribute);
				}
			}
		} catch (IOException ioe) {
			throw new IllegalStateException();
		}
	}

	/**
	 * Print an XML character in its escaped form.
	 *
	 * @param writer The writer to which the character should be printed.
	 * @param ch the character to print.
	 * @throws IOException
	 */
	private static void printEscaped(final PrintWriter writer, final int ch, final boolean attribute) throws IOException {

		String ref = getEntityRef(ch, attribute);
		if (ref != null) {
			writer.write('&');
			writer.write(ref);
			writer.write(';');
		} else if (ch == '\r' || ch == 0x0085 || ch == 0x2028) {
			printHex(writer, ch);
		} else if ((ch >= ' ' && ch != 160 && isUtf8Printable((char) ch) && XML11Char.isXML11ValidLiteral(ch))
				|| ch == '\t' || ch == '\n' || ch == '\r') {
			writer.write((char) ch);
		} else {
			printHex(writer, ch);
		}
	}

	/**
	 * Escapes chars
	 */
	static void printHex(final PrintWriter writer, final int ch) throws IOException {
		writer.write("&#x"); //$NON-NLS-1$
		writer.write(Integer.toHexString(ch));
		writer.write(';');
	}

	protected static String getEntityRef(final int ch, final boolean attribute) {
		// Encode special XML characters into the equivalent character
		// references.
		// These five are defined by default for all XML documents.
		switch (ch) {
			case '<':
				return "lt"; //$NON-NLS-1$
			case '>':
				if (!attribute) {
					// bug 302291: text containing CDATA produces invalid HTML
					return "gt"; //$NON-NLS-1$
				}
			case '"':
				if (attribute) {
					return "quot"; //$NON-NLS-1$
				}
				break;
			case '&':
				return "amp"; //$NON-NLS-1$
			default:
				return null;

			// WARN: there is no need to encode apostrophe, and doing so has an
			// adverse
			// effect on XHTML documents containing javascript with some browsers.
			// case '\'':
			// return "apos";
		}
		return null;
	}

	protected static boolean isUtf8Printable(final char ch) {
		// fall-back method here.
		// If the character is not printable, print as character reference.
		// Non printables are below ASCII space but not tab or line
		// terminator, ASCII delete, or above a certain Unicode threshold.

		return (ch >= ' ' && ch <= 0x10FFFF && ch != 0xF7) || ch == '\n' || ch == '\r' || ch == '\t';
	}
}
