import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.net.moon.tBuildWordLists.Word;


public class WordTest {
	Word word;

	@Before
	public void setUp() throws Exception {
		word = new Word();
	}

	@After
	public void tearDown() throws Exception {
		word = null;
	}

	@Test
	public void testCleanWord() {
		assertTrue("test word will be white space trimmed and lowercase", Word
				.cleanWord(" XZZY ").equals("xzzy"));
		assertTrue("non alpha characters will be removed",
				Word.cleanWord("Hello.world").equals("helloworld"));
	}

}
