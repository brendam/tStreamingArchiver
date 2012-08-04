import static org.junit.Assert.assertTrue;

import org.junit.Test;

import au.net.moon.tBuildWordLists.StopWords;


public class StopWordsTest {
	// @Test
	// public void testMinimumWordSize() {
	// assertTrue("Minimum word size is 2", StopWords.minWordSize == 2);
	// }

	@Test
	public void testStopWords() {
		assertTrue("Word 'the' will return false for includedWord()",
				StopWords.includedWord("the") == false);
		assertTrue("Word 'spagetti' will return true for includedWord()",
				StopWords.includedWord("spagetti"));
	}
}
