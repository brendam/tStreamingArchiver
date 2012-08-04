package au.net.moon.tBuildWordLists;
/**
 * StopWords - data structure for list of words to ignore when indexing
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

import java.util.regex.Pattern;

/**
 * Singleton object to hold the list of words to ignore when indexing (stop
 * words).
 * 
 * @author Brenda Moon - brenda at moon.net.au
 * 
 */
public class StopWords {
	// this is a singleton object
	public static final int minWordSize = 2;
	private static Pattern p;
	// list of stopwords based on MySQL full text search stopwords
	// http://meta.wikimedia.org/wiki/MySQL_4.0.20_stop_word_list
	// I've removed all apostrophes making single words instead
	// I've added my extra 'web filtering' ones as well -
	// www|http|com|net|edu|quot
	private static final String defaultStopWords = "www|http|com|net|edu|quot|as|able|about|above|according|accordingly|across|actually|after|afterwards|again|against|aint|all|allow|allows|almost|alone|along|already|also|although|always|am|among|amongst|an|and|another|any|anybody|anyhow|anyone|anything|anyway|anyways|anywhere|apart|appear|appreciate|appropriate|are|arent|around|as|aside|ask|asking|associated|at|available|away|awfully|be|became|because|become|becomes|becoming|been|before|beforehand|behind|being|believe|below|beside|besides|best|better|between|beyond|both|brief|but|by|cmon|cs|came|can|cant|cannot|cant|cause|causes|certain|certainly|changes|clearly|co|com|come|comes|concerning|consequently|consider|considering|contain|containing|contains|corresponding|could|couldnt|course|currently|definitely|described|despite|did|didnt|different|do|does|doesnt|doing|dont|done|down|downwards|during|each|edu|eg|eight|either|else|elsewhere|enough|entirely|especially|et|etc|even|ever|every|everybody|everyone|everything|everywhere|ex|exactly|example|except|far|few|fifth|first|five|followed|following|follows|for|former|formerly|forth|four|from|further|furthermore|get|gets|getting|given|gives|go|goes|going|gone|got|gotten|greetings|had|hadnt|happens|hardly|has|hasnt|have|havent|having|he|hes|hello|help|hence|her|here|heres|hereafter|hereby|herein|hereupon|hers|herself|hi|him|himself|his|hither|hopefully|how|howbeit|however|id|ill|im|ive|ie|if|ignored|immediate|in|inasmuch|inc|indeed|indicate|indicated|indicates|inner|insofar|instead|into|inward|is|isnt|it|itd|itll|its|its|itself|just|keep|keeps|kept|know|knows|known|last|lately|later|latter|latterly|least|less|lest|let|lets|like|liked|likely|little|look|looking|looks|ltd|mainly|many|may|maybe|me|mean|meanwhile|merely|might|more|moreover|most|mostly|much|must|my|myself|name|namely|nd|near|nearly|necessary|need|needs|neither|never|nevertheless|new|next|nine|no|nobody|non|none|noone|nor|normally|not|nothing|novel|now|nowhere|obviously|of|off|often|oh|ok|okay|old|on|once|one|ones|only|onto|or|other|others|otherwise|ought|our|ours|ourselves|out|outside|over|overall|own|particular|particularly|per|perhaps|placed|please|plus|possible|presumably|probably|provides|que|quite|qv|rather|rd|re|really|reasonably|regarding|regardless|regards|relatively|respectively|right|said|same|saw|say|saying|says|second|secondly|see|seeing|seem|seemed|seeming|seems|seen|self|selves|sensible|sent|serious|seriously|seven|several|shall|she|should|shouldnt|since|six|so|some|somebody|somehow|someone|something|sometime|sometimes|somewhat|somewhere|soon|sorry|specified|specify|specifying|still|sub|such|sup|sure|ts|take|taken|tell|tends|th|than|thank|thanks|thanx|that|thats|thats|the|their|theirs|them|themselves|then|thence|there|theres|thereafter|thereby|therefore|therein|theres|thereupon|these|they|theyd|theyll|theyre|theyve|think|third|this|thorough|thoroughly|those|though|three|through|throughout|thru|thus|to|together|too|took|toward|towards|tried|tries|truly|try|trying|twice|two|un|under|unfortunately|unless|unlikely|until|unto|up|upon|us|use|used|useful|uses|using|usually|value|various|very|via|viz|vs|want|wants|was|wasnt|way|we|wed|well|were|weve|welcome|well|went|were|werent|what|whats|whatever|when|whence|whenever|where|wheres|whereafter|whereas|whereby|wherein|whereupon|wherever|whether|which|while|whither|who|whos|whoever|whole|whom|whose|why|will|willing|wish|with|within|without|wont|wonder|would|would|wouldnt|yes|yet|you|youd|youll|youre|youve|your|yours|yourself|yourselves|zero";
	private static StopWords stopWordsRef;
	private static String currentStopWords;

	private StopWords() {
	}

	/**
	 * Add a stop word.
	 * 
	 * @param word
	 *            word to add to the stop word list
	 */
	public static void AddStopWord(String word) {
		if (stopWordsRef == null) {
			stopWordsRef = new StopWords();
			currentStopWords = defaultStopWords;
		}
		currentStopWords += "|" + word.toLowerCase();
		p = Pattern.compile(currentStopWords);
	}

	/**
	 * Remove a stop word.
	 * 
	 * @param word
	 *            word to remove from the stop word list
	 */
	public static void RemoveStopWord(String word) {
		if (stopWordsRef == null) {
			stopWordsRef = new StopWords();
			currentStopWords = defaultStopWords;
		}
		if (currentStopWords.indexOf(word) == 0) {
			currentStopWords.replace(word.toLowerCase() + "|", "");
		} else if (currentStopWords.indexOf(word.toLowerCase()) > 0) {
			currentStopWords.replace("|" + word.toLowerCase(), "");
		} else {
			System.err.println("StopWords: RemoveStopWord(" + word
					+ ") failed, word didn't exist in stopWords list");
		}
		p = Pattern.compile(currentStopWords);
	}

	/**
	 * Determine if a word is a valid word, not in the stop words list.
	 * 
	 * @param word
	 *            word to check
	 * @return <CODE>true</CODE> if the word is NOT in the stop word list,
	 *         <CODE>false</CODE> otherwise
	 */
	public static Boolean includedWord(String word) {
		if (stopWordsRef == null) {
			stopWordsRef = new StopWords();
			p = Pattern.compile(defaultStopWords);
		}
		return !p.matcher(word).matches();
	}

	// make sure object stays a singleton
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}
