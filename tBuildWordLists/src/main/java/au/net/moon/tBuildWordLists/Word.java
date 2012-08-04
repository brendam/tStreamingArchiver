package au.net.moon.tBuildWordLists;
/**
 * Word - data structure for each word
 * Copyright (C) 2012 Brenda Moon 
 * 
 * This program is free software; you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the Free Software 
 * Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 **/

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * A word, its frequency and the list of tweets that it occurs in
 * 
 * @author Brenda Moon - brenda at moon.net.au
 * 
 */
public class Word {
	String text;
	private int frequency;
	ArrayList<Object> occursIn;

	// static final Pattern removeCharacters = Pattern
	// .compile("&quot;|\\.|@|:|-|,|?|^\\s+|\\s+$");
	// split on any non alphanumeric character
	// NOTE: THIS NEEDS TO BE SAME AS IN WORDLIST.JAVA AND COOCURRANCELIST.JAVA
	// static final Pattern invalidCharacters = Pattern.compile("[\\W-[_]]");
	// this is splitting off apostrophy's %27x (eg don%27t) so add &&^% to not
	// split on % character;
	static final Pattern invalidCharacters = Pattern.compile("[\\W-[_]&&[^%]]");

	static final Pattern apostropheMatcher = Pattern.compile("%27");

	public Word() {
		frequency = 0;
		occursIn = new ArrayList<Object>();
	}

	/**
	 * Add a new word, or update the frequency of an existing word.
	 * 
	 * @param word
	 *            word to add
	 * @param fromObjectKey
	 *            id of the tweet the word occurs in the text of
	 */
	public void add(String word, Object fromObjectKey) {
		// Assumes that the word has already been 'cleaned' because otherwise
		// objects get created for null words. But maybe I should
		// be calling cleanWord(word) here to be safe?
		if (text == null || text.equals(word)) {
			if (!occursIn.contains(fromObjectKey)) {
				occursIn.add(fromObjectKey);
			}
			text = word;
			frequency++;
		} else {
			if (!text.equals(word)) {
				System.err
						.println("wordList: Can't change the word stored in a word class object");
				System.err.println("wordList: existing word: " + text
						+ " not updated by new word: " + word);
			}
		}
	}

	/**
	 * Clean the word - anything that isn't a character or a digit is invalid
	 * 
	 * @param word
	 * @return the cleaned word
	 */
	static public String cleanWord(String word) {
		// changed to compiled replace to try to improve memory management
		if (invalidCharacters.matcher(word).find()) {
			System.err
					.println("wordList: words cannot contain non alphanumeric characters: >"
							+ word + "< (they will be removed)");
			word = invalidCharacters.matcher(word).replaceAll("");
		}
		// decide what to do with apostrophes - for now I'm just
		// removing them
		word = apostropheMatcher.matcher(word).replaceAll("");
		// check for other "%nn" special characters
		word = word.toLowerCase();
		if (word.length() > 30) {
			// truncate the word - not sure how to show that they have been
			// truncated?
			word = word.substring(0, 30);
		}
		if (word.length() <= StopWords.minWordSize
				|| !StopWords.includedWord(word)) {
			word = null;
		}
		return word;
	}

	/**
	 * Return the list of tweets the word occurs in as an <CODE>Array</CODE>
	 * 
	 * @return list of tweets the word occurs in.
	 */
	public Object[] getOccursIn() {
		return occursIn.toArray();
	}

	/**
	 * Return the frequency of the word
	 * 
	 * @return count of occurances of the word
	 */
	public int getFrequency() {
		return frequency;
	}
}
