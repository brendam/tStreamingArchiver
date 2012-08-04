package au.net.moon.tBuildWordLists;
/**
 * CoOccurenceList - data structure for list of words which occur together
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * List of words which occur together and the number of times they do so.
 * 
 * @author Brenda Moon - brenda at moon.net.au
 * 
 */
public class CoOccurrenceList {
	private HashMap<String, Integer> coList;

	// split on any non alphanumeric character
	// NOTE: THIS NEEDS TO BE SAME AS IN WORDLIST.JAVA AND WORD.JAVA
	// this is splitting off apostrophy's %27x (eg don%27t) so add &&^% to not
	// split on % character;
	static final Pattern splitCharacters = Pattern.compile("[\\W-[_]&&[^%]]");

	public CoOccurrenceList() {
		coList = new HashMap<String, Integer>();
	}

	/**
	 * Return the number of items in the co-occurrence list
	 * 
	 * @return count of the number of items in the co-occurrence list
	 */
	public int length() {
		return coList.size();
	}

	/**
	 * Return all the word pairs as a string.
	 * 
	 * @return all of the word pairs
	 */
	public String allPairs() {
		return coList.keySet().toString();
	}

	/**
	 * Return all the word pairs as a key set.
	 * 
	 * @return all of the word pairs
	 */
	public Set<String> keySet() {
		return coList.keySet();
	}

	/**
	 * Add a word co-occurrence pair.
	 * 
	 * @param word1
	 *            first word
	 * @param word2
	 *            second word
	 */
	public void addOccurrence(String word1, String word2) {
		String key = buildKey(word1, word2);
		if (key != null && !key.isEmpty()) {
			int counter = wordCoFrequency(word1, word2);
			counter++;
			coList.put(key, counter);
		}
	}

	/**
	 * Add all the co-occurring words from a sentence
	 * 
	 * @param sentence
	 *            the list of words
	 */
	public void addSentence(String sentence) {
		if (sentence.length() > 0) {
			ArrayList<String> tempWords = new ArrayList<String>();
			for (String word : splitCharacters.split(sentence)) {
				tempWords.add(word);
				// work out how to add all the word pairs, excluding
				// same words and only doing it once
				if (tempWords.size() > 1) {
					for (int i = 0; i < tempWords.size() - 1; i++) {
						addOccurrence(tempWords.get(i), word);
					}
				}
			}
			System.out.println(" adding sentence with words: "
					+ tempWords.size() + " to coList size: " + coList.size());
		}
	}

	/**
	 * Add the co-occurring words from a sentence only if they are in the most
	 * frequent words list.
	 * 
	 * @param sentence
	 *            the list of words
	 * @param mostFrequentWords
	 *            list of most frequent words
	 */
	public void addSentence(String sentence, String[] mostFrequentWords) {
		if (sentence.length() > 0) {
			ArrayList<String> tempWords = new ArrayList<String>();
			Set<String> validWords = new HashSet<String>(
					Arrays.asList(mostFrequentWords));
			for (String word : splitCharacters.split(sentence)) {
				if (validWords.contains(word)) {
					tempWords.add(word);
					// add all the word pairs, excluding
					// same words and only doing it once
					if (tempWords.size() > 1) {
						for (int i = 0; i < tempWords.size() - 1; i++) {
							addOccurrence(tempWords.get(i), word);
						}
					}
				}
			}
			// System.out.println("CoOccurrenceList: adding sentence with words: "
			// + tempWords.size() + " to coList size: " + coList.size());
		}
	}

	/**
	 * Return the frequency that two words co-occur
	 * 
	 * @param word1
	 *            the first word
	 * @param word2
	 *            the second word
	 * @return number of times they co-occur
	 */
	public int wordCoFrequency(String word1, String word2) {
		int counter = 0;
		String key = buildKey(word1, word2);
		if (coList.containsKey(key)) {
			counter = coList.get(key);
		}
		return counter;
	}

	/**
	 * Return the key from the two words. Words are 'cleaned' during
	 * alphaSorted, so it returns null if either is not valid
	 * 
	 * @param word1
	 *            the first word
	 * @param word2
	 *            the second word
	 * @return the two word key or blank if either of the words are not valid
	 */
	public String buildKey(String word1, String word2) {
		String key = "";
		String[] sortedWords = alphaSorted(word1, word2);
		if (sortedWords != null) {
			key = sortedWords[0] + "-" + sortedWords[1];
		}
		return key;
	}

	/**
	 * Sort the words into ascending order after first 'cleaning' them of
	 * invalid characters.
	 * 
	 * @param word1
	 *            the first word
	 * @param word2
	 *            the second word
	 * @return the words in sorted order or <CODE>null</CODE> if either word was
	 *         not valid, or if they were identical.
	 */
	public String[] alphaSorted(String word1, String word2) {
		word1 = Word.cleanWord(word1);
		word2 = Word.cleanWord(word2);
		String[] sortedWords = new String[2];
		if (word1 == null || word2 == null || word1.isEmpty()
				|| word2.isEmpty()) {
			sortedWords = null;
		} else {
			// already lowercase, so don't need to ignore case
			int compare = (word1.compareTo(word2));
			if (compare == 0) {
				// System.err.println("CoOccurrenceList: words are identical");
				sortedWords = null;
			} else if (compare < 0) {
				sortedWords[0] = word1;
				sortedWords[1] = word2;
			} else if (compare > 0) {
				sortedWords[0] = word2;
				sortedWords[1] = word1;
			} else {
				System.err
						.println("CoOccurrenceList: unexpected compare result: "
								+ compare);
				System.exit(-1);
			}
		}
		return sortedWords;
	}

	/**
	 * Remove word pairs from the co-occurrence list unless both of them are in
	 * the most frequent words list
	 * 
	 * @param words
	 *            the words to keep
	 */
	public void trimToWords(String[] words) {
		// TODO: write unit test for this
		if (coList != null && words.length > 0) {
			Set<String> wordPairKeys = coList.keySet();
			Set<String> validWords = new HashSet<String>(Arrays.asList(words));
			String key;
			String[] keyParts = new String[2];
			Iterator<String> it = wordPairKeys.iterator();
			while (it.hasNext()) {
				key = it.next();
				keyParts = key.split("-");
				if (!(validWords.contains(keyParts[0]) && validWords
						.contains(keyParts[1]))) {
					// if both words are not in the words list anymore then
					// remove them from the co-occurrence list
					it.remove();
				}
			}
		}
	}
}
