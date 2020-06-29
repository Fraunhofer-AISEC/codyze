/*
 * Copyright (c) 2002-2012, the original author or authors.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */

/**
 * This class is a c/p backport of JLine 2.14.6 (commit a27f3bdd6df899224a3dc9d9f3a6511c6230c0b3).
 *
 * It removes the trailing space after tab completions.
 */

package de.fraunhofer.aisec.analysis;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.python.jline.console.ConsoleReader;
import org.python.jline.console.CursorBuffer;
import org.python.jline.console.completer.CompletionHandler;

import java.io.IOException;
import java.util.*;

/**
 * A {@link CompletionHandler} that deals with multiple distinct completions
 * by outputting the complete list of possibilities to the console. This
 * mimics the behavior of the
 * <a href="http://www.gnu.org/directory/readline.html">readline</a> library.
 *
 * @author <a href="mailto:mwp1@cornell.edu">Marc Prud'hommeaux</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.3
 */
public class CandidateListCompletionHandler implements CompletionHandler {
	private boolean printSpaceAfterFullCompletion = true;

	public boolean getPrintSpaceAfterFullCompletion() {
		return printSpaceAfterFullCompletion;
	}

	public void setPrintSpaceAfterFullCompletion(boolean printSpaceAfterFullCompletion) {
		this.printSpaceAfterFullCompletion = printSpaceAfterFullCompletion;
	}

	@Override
	public boolean complete(final ConsoleReader reader, final List<CharSequence> candidates, final int pos) throws IOException {
		CursorBuffer buf = reader.getCursorBuffer();

		// if there is only one completion, then fill in the buffer
		if (candidates.size() == 1) {
			CharSequence value = candidates.get(0);

			// fail if the only candidate is the same as the current buffer
			if (value.equals(buf.toString())) {
				return false;
			}

			setBuffer(reader, value, pos);

			return true;
		} else if (candidates.size() > 1) {
			String value = getUnambiguousCompletions(candidates);
			setBuffer(reader, value, pos);
		}

		printCandidates(reader, candidates);

		// redraw the current console buffer
		reader.drawLine();

		return true;
	}

	public static void setBuffer(final ConsoleReader reader, final CharSequence value, final int offset) throws IOException {
		while ((reader.getCursorBuffer().cursor > offset) && reader.backspace()) {
			// empty
		}

		reader.putString(value);
		reader.setCursorPosition(offset + value.length());
	}

	/**
	 * Print out the candidates. If the size of the candidates is greater than the
	 * {@link ConsoleReader#getAutoprintThreshold}, they prompt with a warning.
	 *
	 * @param candidates the list of candidates to print
	 */
	public static void printCandidates(final ConsoleReader reader, Collection<CharSequence> candidates) throws IOException {
		Set<CharSequence> distinct = new HashSet<CharSequence>(candidates);

		if (distinct.size() > reader.getAutoprintThreshold()) {
			//noinspection StringConcatenation
			reader.println();
			reader.print(Messages.DISPLAY_CANDIDATES.format(candidates.size()));
			reader.flush();

			int c;

			String noOpt = Messages.DISPLAY_CANDIDATES_NO.format();
			String yesOpt = Messages.DISPLAY_CANDIDATES_YES.format();
			char[] allowed = { yesOpt.charAt(0), noOpt.charAt(0) };

			while ((c = reader.readCharacter(allowed)) != -1) {
				String tmp = new String(new char[] { (char) c });

				if (noOpt.startsWith(tmp)) {
					reader.println();
					return;
				} else if (yesOpt.startsWith(tmp)) {
					break;
				} else {
					reader.beep();
				}
			}
		}

		// copy the values and make them distinct, without otherwise affecting the ordering. Only do it if the sizes differ.
		if (distinct.size() != candidates.size()) {
			Collection<CharSequence> copy = new ArrayList<CharSequence>();

			for (CharSequence next : candidates) {
				if (!copy.contains(next)) {
					copy.add(next);
				}
			}

			candidates = copy;
		}

		reader.println();
		reader.printColumns(candidates);
	}

	/**
	 * Returns a root that matches all the {@link String} elements of the specified {@link List},
	 * or null if there are no commonalities. For example, if the list contains
	 * <i>foobar</i>, <i>foobaz</i>, <i>foobuz</i>, the method will return <i>foob</i>.
	 */
	private @Nullable String getUnambiguousCompletions(final List<CharSequence> candidates) {
		if (candidates == null || candidates.isEmpty()) {
			return null;
		}

		// convert to an array for speed
		String[] strings = candidates.toArray(new String[candidates.size()]);

		String first = strings[0];
		StringBuilder candidate = new StringBuilder();

		for (int i = 0; i < first.length(); i++) {
			if (startsWith(first.substring(0, i + 1), strings)) {
				candidate.append(first.charAt(i));
			} else {
				break;
			}
		}

		return candidate.toString();
	}

	/**
	 * @return true is all the elements of <i>candidates</i> start with <i>starts</i>
	 */
	private boolean startsWith(final String starts, final String[] candidates) {
		for (String candidate : candidates) {
			if (!candidate.startsWith(starts)) {
				return false;
			}
		}

		return true;
	}
}

enum Messages {
	DISPLAY_CANDIDATES,
	DISPLAY_CANDIDATES_YES,
	DISPLAY_CANDIDATES_NO,
	;

	private static final ResourceBundle bundle = ResourceBundle.getBundle(CandidateListCompletionHandler.class.getName(), Locale.getDefault());

	public String format(final Object... args) {
		if (bundle == null)
			return "";
		else
			return String.format(bundle.getString(name()), args);
	}
}
