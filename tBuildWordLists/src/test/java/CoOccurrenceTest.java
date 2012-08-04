import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.net.moon.tBuildWordLists.CoOccurrenceList;


public class CoOccurrenceTest {
	CoOccurrenceList coList1;

	@Before
	public void setUp() throws Exception {
		coList1 = new CoOccurrenceList();
	}

	@After
	public void tearDown() throws Exception {
		coList1 = null;
	}

	@Test
	public void testEmptyCoOccurrenceList() {
		assertTrue("New co-occurrence list is empty", coList1.length() == 0);
	}

	@Test
	public void testAlphaSort() {
		String[] testArray = { "abcd", "xzzy" };
		assertTrue("test word 'abcd' is before 'xzzy'", Arrays.equals(
				coList1.alphaSorted(testArray[1], testArray[0]), testArray));
		assertTrue(
				"words are cleaned before alphasorting",
				Arrays.equals(
						coList1.alphaSorted(" " + testArray[1].toUpperCase()
								+ " ", testArray[0]), testArray));
	}

	@Test
	public void testBuildKey() {
		assertTrue("key is cleaned, alpha sorted, '-' delimited", coList1
				.buildKey("xzzy", "ABCD").equals("abcd-xzzy"));

	}

	@Test
	public void testMissingCoOccurrenceFrequency() {
		assertTrue("Word not found",
				coList1.wordCoFrequency("xzzy", "zxyz") == 0);
	}

	@Test
	public void testCaseInsensitive() { // Words are case insensitive
		coList1.addOccurrence("xzzy", "abcd");
		coList1.addOccurrence("XZZY", "ABCD");
		assertTrue("coList1 has one pair in it", coList1.length() == 1);
		assertTrue("word pair 'xzzy','abcd' has count of 2",
				coList1.wordCoFrequency("xzzy", "abcd") == 2);
	}

	@Test
	public void testOrderInsensitive() { // Words are order insensitive
		coList1.addOccurrence("xzzy", "abcd");
		coList1.addOccurrence("ABCD", "xzzy");
		assertTrue("coList1 has one pair in it", coList1.length() == 1);
		assertTrue("word pair 'xzzy','abcd' has count of 2",
				coList1.wordCoFrequency("xzzy", "abcd") == 2);
		assertTrue("word pair 'abcd', 'xzzy' is the same and has count of 2",
				coList1.wordCoFrequency("abcd", "xzzy") == 2);
	}

	@Test
	public void testSentenceAdd() {
		coList1.addSentence(" trapped in a maze of twisty little passages ");
		assertTrue("Five words added, 10 coOccurances", coList1.length() == 6);
	}

	@Test
	public void testSentenceAddWithDuplicateWords() {
		coList1.addSentence(" duplicate words spagetti the sentence words spagetti");
		assertTrue("Four words added", coList1.length() == 6);
		assertTrue("frequency of spagetti-duplicate is 2",
				coList1.wordCoFrequency("spagetti", "duplicate") == 2);
		assertTrue("frequency of duplicate-the is 0",
				coList1.wordCoFrequency("duplicate", "the") == 0);
	}

	@Test
	public void testStopWords() {
		coList1.addOccurrence("the", "spider");
		assertTrue("Nothing added", coList1.length() == 0);
	}
}
