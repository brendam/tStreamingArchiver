import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.net.moon.tBuildWordLists.WordList;


/**
 * @author brenda
 * 
 */
public class WordListTest {

	/**
	 * @throws java.lang.Exception
	 */
	private WordList wordList1;

	@Before
	public void setUp() throws Exception {
		wordList1 = new WordList();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		wordList1 = null;
	}

	@Test
	public void testEmptyWordList() {
		assertTrue("New wordList is empty", wordList1.length() == 0);
	}

	@Test
	public void testMissingWordFrequency() {
		assertTrue("Word not found", wordList1.wordFrequency("xzzy") == 0);
	}

	@Test
	public void testExistingWordFrequency() {
		wordList1.addWord("xzzy", 99);
		assertTrue("test word 'xzzy' has a count of 1",
				wordList1.wordFrequency("xzzy") == 1);
	}

	@Test
	public void testAddNewWord() {
		assertTrue("test word 'xzzy' has a count of 0",
				wordList1.wordFrequency("xzzy") == 0);
		wordList1.addWord("xzzy", 99);
		assertTrue("test word 'xzzy' has a count of 1",
				wordList1.wordFrequency("xzzy") == 1);
	}

	@Test
	public void testAddExistingWord() {
		wordList1.addWord("xzzy", 99);
		wordList1.addWord("xzzy", 100);
		assertTrue("wordList1 has a single word in it", wordList1.length() == 1);
		assertTrue("test word 'xzzy' has a count of 2",
				wordList1.wordFrequency("xzzy") == 2);
	}

	@Test
	public void testAddTwoDifferentWords() {
		wordList1.addWord("xzzy", 99);
		wordList1.addWord("abcd", 99);
		assertTrue("wordList1 has two words in it", wordList1.length() == 2);
		assertTrue("word 'xzzy' has count of 1",
				wordList1.wordFrequency("xzzy") == 1);
		assertTrue("word 'abcd' has count of 1",
				wordList1.wordFrequency("abcd") == 1);
	}

	@Test
	public void testCaseInsensitive() {
		// Words are case insensitive
		wordList1.addWord("xzzy", 99);
		wordList1.addWord("XZZY", 100);
		assertTrue("wordList1 has one word in it", wordList1.length() == 1);
		assertTrue("word 'xzzy' has count of 2",
				wordList1.wordFrequency("xzzy") == 2);
		assertTrue("word 'XZZY' has count of 2",
				wordList1.wordFrequency("XZZY") == 2);
	}

	@Test
	public void testTweetIds() {
		wordList1.addWord("xzzy", 99);
		wordList1.addWord("XZZY", 100);
		Object[] testArray = { 99, 100 };
		assertTrue("tweet ids are: " + testArray,
				Arrays.equals(wordList1.occursIn("xzzy"), testArray));
	}

	@Test
	public void testSentenceAdd() {
		wordList1.addSentence(" trapped in a maze of twisty littler passages ",
				99);
		assertTrue("Five words bigger than 2 letters added",
				wordList1.length() == 5);
	}

	@Test
	public void testSentenceAddWithDuplicateWords() {
		wordList1.addSentence(
				" duplicate words spaghetti the sentence words spaghetti", 99);
		// 'the' is a stop word and so is ignored
		assertTrue("Four words added", wordList1.length() == 4);
	}

	@Test
	public void testMostFrequentWords() {
		wordList1
				.addSentence(" word2 word1 word1 word2 word2 word2 word3 ", 99);
		assertTrue("There are 3 words", wordList1.length() == 3);
		assertTrue("Most frequent 5 words will only return 3",
				wordList1.mostFrequentWords(5).length == 3);
		// NOTE: Words are frequency sorted
		// System.out.println("length: " + wordList1.mostFrequentWords(2).length
		// + " most frequent: " + wordList1.mostFrequentWords(2)[0]
		// + " next most frequent: " + wordList1.mostFrequentWords(2)[1]);
		assertTrue("word2 is most frequent",
				wordList1.mostFrequentWords(2)[0].equalsIgnoreCase("word2"));
		assertTrue("word3 is least frequent",
				wordList1.mostFrequentWords(3)[2].equalsIgnoreCase("word3"));
		String[] returnedWords = wordList1.mostFrequentWords(2);
		assertTrue(returnedWords[0].equals("word2")
				&& returnedWords[1].equals("word1"));
	}
}
