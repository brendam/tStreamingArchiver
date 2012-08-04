package au.net.moon.tBuildWordLists;
/**
 * WordList - data structure to store list of words
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * A list of <CODE>Word</CODE> objects.
 * 
 * @author Brenda Moon - brenda at moon.net.au
 * 
 */
public class WordList {
	private HashMap<String, Word> words;
	// split on any non alphanumeric character
	// NOTE: THIS NEEDS TO BE SAME AS IN COOCCURANCELIST.JAVA & WORD.JAVA
	// this is splitting off apostrophy's %27x (eg don%27t) so add &&^% to not
	// split on % character;
	static final Pattern splitCharacters = Pattern.compile("[\\W-[_]&&[^%]]");

	public WordList() {
		words = new HashMap<String, Word>();
	}

	/**
	 * Add a sentence of words to the word list.
	 * 
	 * @param sentence
	 *            group of words
	 * @param occursInKey
	 *            key to the object that the word came from (tweet id at the
	 *            moment)
	 */
	public void addSentence(String sentence, Object occursInKey) {
		if (sentence.length() > 0) {
			for (String word : splitCharacters.split(sentence)) {
				word = Word.cleanWord(word);
				if (word != null && !word.isEmpty()) {
					addWord(word, occursInKey);
				}
			}
		}
	}

	/**
	 * Add a single word to the word list.
	 * 
	 * @param word
	 *            word to add
	 * @param occursInKey
	 *            key to the object that the word came from (tweet id at the
	 *            moment)
	 */
	public void addWord(String word, Object occursInKey) {
		word = Word.cleanWord(word);
		Word tempWord;
		if (words.containsKey(word)) {
			tempWord = words.get(word);
		} else {
			tempWord = new Word();
		}
		tempWord.add(word, occursInKey);
		words.put(word, tempWord);
	}

	/**
	 * Return the frequency of the word.
	 * 
	 * @param word
	 *            word to get the occurance count of
	 * @return number of times the word occurs
	 */
	public int wordFrequency(String word) {
		word = Word.cleanWord(word);
		int counter = 0;
		if (words.containsKey(word)) {
			Word tempWord = words.get(word);
			counter = tempWord.getFrequency();
		}
		return counter;
	}

	/**
	 * Return the numberOfWords of most frequent words from the word list
	 * 
	 * @param numberOfWords
	 *            number of words to return
	 * @return list of most frequent words
	 */
	public String[] mostFrequentWords(int numberOfWords) {
		// return list of 'numberOfWords' words in descending frequency order
		String[] wordArray = null;
		if (words != null) {
			if (words.size() <= numberOfWords) {
				numberOfWords = words.size();
			}
			wordArray = new String[numberOfWords];
			ArrayList<Word> wordsList = new ArrayList<Word>();
			wordsList.addAll(words.values());
			Collections.sort(wordsList, new FrequencyComparator());
			for (int i = 0; i < numberOfWords; i++) {
				wordArray[i] = wordsList.get(i).text;
			}
			// this was to get it into alpha sort order instead of the frequency
			// order
			// Arrays.sort(wordArray);
		}
		return wordArray;
	}

	/**
	 * Return a list of the keys to the objects that the word occurs in (at the
	 * moment this is tweet ids)
	 * 
	 * @param word
	 *            word to lookup where it occurs in
	 * @return list of keys to the objects the word occurs in
	 */
	public Object[] occursIn(String word) {
		word = Word.cleanWord(word);
		Object[] occursIn;
		if (words.containsKey(word)) {
			Word tempWord = words.get(word);
			occursIn = tempWord.getOccursIn();
		} else {
			occursIn = null;
		}
		return occursIn;
	}

	/**
	 * Return the number of words in the list.
	 * 
	 * @return the number of words in the list
	 */
	public int length() {
		return words.size();
	}

	private class FrequencyComparator implements Comparator<Word> {
		public final int compare(Word word1, Word word2) {
			// put most frequent words at front of arrayList
			// decending frequency
			return word2.getFrequency() - word1.getFrequency();
		}
	}

	/**
	 * Truncate the word list to the numberOfWords most frequently occurring
	 * words.
	 * 
	 * @param numberOfWords
	 *            number of words to keep in the word list
	 */
	public void trimToMostFrequent(int numberOfWords) {
		// TODO: Write unit test for this
		if (words != null) {
			if (words.size() > numberOfWords) {
				ArrayList<Word> wordsList = new ArrayList<Word>();
				wordsList.addAll(words.values());
				Collections.sort(wordsList, new FrequencyComparator());
				// go backwards removing least frequent word until reach the
				// numberOfWords
				for (int i = words.size() - 1; i >= numberOfWords; i--) {
					// wordArray[i] = wordsList.get(i).text;
					words.remove(wordsList.get(i).text);
				}
			}
		}
	}
}
