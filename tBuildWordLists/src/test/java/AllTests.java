import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ WordTest.class, WordListTest.class, CoOccurrenceTest.class,
		StopWordsTest.class })
public class AllTests {
}
