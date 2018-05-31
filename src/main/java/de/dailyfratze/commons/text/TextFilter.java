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

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Filters input text in some arbitrary ways.
 *
 * @author Michael J. Simons, 2015-01-03
 */
@FunctionalInterface
public interface TextFilter extends BiFunction<String, Optional<String>, String> {

	/**
	 * Replaces all line breaks with {@code <br />} tags.
	 */
	TextFilter AUTO_BR = (input, baseUrl) -> input == null || input.trim().isEmpty() ? input : input.replaceAll("(?:\r\n|\n)", "<br />");
}
