package edu.berkeley.nlp.assignments.decoding.student;

import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.mt.decoder.Decoder;
import edu.berkeley.nlp.mt.decoder.DistortionModel;
import edu.berkeley.nlp.mt.phrasetable.PhraseTable;
import edu.berkeley.nlp.mt.phrasetable.PhraseTableForSentence;
import edu.berkeley.nlp.mt.phrasetable.ScoredPhrasePairForSentence;
import edu.berkeley.nlp.util.FastPriorityQueue;

import java.util.List;

/**
 * Created by jogonzal on 11/28/2015.
 */
public class MyMonotonicNoLmDecoder extends DecoderBase implements Decoder {

	PhraseTable _phraseTable;

	public MyMonotonicNoLmDecoder(PhraseTable tm, NgramLanguageModel lm, DistortionModel dm){
		// In this implementation, we don't care about the language or distortion model
		super();

		_phraseTable = tm;
	}

	@Override
	public List<ScoredPhrasePairForSentence> decode(List<String> frenchSentence) {
		int foreignSentenceLength = frenchSentence.size();
		PhraseTableForSentence phraseTableForSentence = _phraseTable.initialize(frenchSentence);
 		FastPriorityQueue<TranslationState> bean = new FastPriorityQueue<TranslationState>();
		MaxState = null;

		// Since this is a "dumber" decoder, we just need to add all the first states (hence the parameter "1") in it from the phraseTable
		AddInitialStatesToBean(1, phraseTableForSentence, foreignSentenceLength, bean);

		while(bean.size() > 0){
			// Get the list from the bean and erase it
			List<TranslationState> elementsToProcess = GetListFromBean(bean);
			bean = new FastPriorityQueue<TranslationState>();

			for (TranslationState elementToProcess : elementsToProcess){
				// Find the next best place for the next phrase
				StartAndEnd startAndEnd = GetInitialPositionOnlyAtBeginningFromAvailableSpots(elementToProcess);
				int phraseLengthLimit = Math.min(foreignSentenceLength - startAndEnd.Start, phraseTableForSentence.getMaxPhraseLength());
				for(int phraseLength = 1; phraseLength <= phraseLengthLimit; phraseLength++){
					// Get phrases for this specific startPosition and length
					List<ScoredPhrasePairForSentence> scoredPairs = phraseTableForSentence.getScoreSortedTranslationsForSpan(startAndEnd.Start, startAndEnd.Start + phraseLength);
					if (scoredPairs != null){
						for(ScoredPhrasePairForSentence scoredPair : scoredPairs){
							TranslationState state = TranslationState.BuildTranslationState(elementToProcess, scoredPair);
							if (state.IsFinal){
								SetMaxState(state);
							} else {
								bean.setPriority(state, state.CurrentScore);
							}
						}
					}
				}
			}
		}

		TranslationState winnerFinalState = MaxState;
		List<ScoredPhrasePairForSentence> winnerPhrase = BuildPhraseListFromState(winnerFinalState);
		return winnerPhrase;
	}
}
