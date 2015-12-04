package edu.berkeley.nlp.assignments.decoding.student;

import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.mt.decoder.Decoder;
import edu.berkeley.nlp.mt.decoder.DistortionModel;
import edu.berkeley.nlp.mt.phrasetable.PhraseTable;
import edu.berkeley.nlp.mt.phrasetable.PhraseTableForSentence;
import edu.berkeley.nlp.mt.phrasetable.ScoredPhrasePairForSentence;
import edu.berkeley.nlp.util.FastPriorityQueue;

import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jogonzal on 11/28/2015.
 */
public class MyMonotonicNoLmDecoder extends DecoderBase implements Decoder {

	PhraseTable _phraseTable;

	public MyMonotonicNoLmDecoder(PhraseTable tm, NgramLanguageModel lm, DistortionModel dm){
		// In this implementation, we don't care about the language model
		super();

		_phraseTable = tm;
	}

	@Override
	public List<ScoredPhrasePairForSentence> decode(List<String> frenchSentence) {
		int foreignSentenceLength = frenchSentence.size();
		PhraseTableForSentence phraseTableForSentence = _phraseTable.initialize(frenchSentence);
 		FastPriorityQueue<TranslationState> bean = new FastPriorityQueue<TranslationState>();
		FastPriorityQueue<TranslationState> finalBean = new FastPriorityQueue<TranslationState>();

		// Since this is a "dumber" decoder, we just need to add all the first states in it from the phraseTable
		AddInitialStatesToBean(1, phraseTableForSentence, foreignSentenceLength, bean, finalBean);


		while(bean.size() > 0 && finalBean.size() < 2000){
			// Get the list from the bean and erase it
			List<TranslationState> elementsToProcess = GetListFromBean(bean);
			bean = new FastPriorityQueue<TranslationState>();

			for (TranslationState elementToProcess : elementsToProcess){
				// Find the next best place for the next phrase
				int startPosition = GetInitialPositionFromAvailableSpots(elementToProcess);
				int phraseLengthLimit = Math.min(foreignSentenceLength - startPosition, phraseTableForSentence.getMaxPhraseLength());
				for(int phraseLength = 1; phraseLength <= phraseLengthLimit; phraseLength++){
					// Get phrases for this specific startPosition and length
					List<ScoredPhrasePairForSentence> scoredPairs = phraseTableForSentence.getScoreSortedTranslationsForSpan(startPosition, startPosition + phraseLength);
					if (scoredPairs != null){
						for(ScoredPhrasePairForSentence scoredPair : scoredPairs){
							TranslationState state = TranslationState.BuildTranslationState(elementToProcess, startPosition, phraseLength, scoredPair);
							if (state.IsFinal){
								finalBean.setPriority(state, state.CurrentScore);
							} else {
								bean.setPriority(state, state.CurrentScore);
							}
						}
					}
				}
			}
		}

		TranslationState winnerFinalState = finalBean.getFirst();
		List<ScoredPhrasePairForSentence> winnerPhrase = BuildPhraseListFromState(winnerFinalState);
		return winnerPhrase;
	}

	private List<ScoredPhrasePairForSentence> BuildPhraseListFromState(TranslationState first) {
		ArrayList<ScoredPhrasePairForSentence> phrases = new ArrayList<>();
		while(first != null){
			phrases.add(0, first.Phrase);
			first = first.PreviousState;
		}
		return phrases;
	}

	private int GetInitialPositionFromAvailableSpots(TranslationState elementsToProcess) {
		for (int i = 0; i < elementsToProcess.TranslatedFlags.length; i++){
			if (elementsToProcess.TranslatedFlags[i] == false){
				return i;
			}
		}
		throw new StackOverflowError();
	}

	private void AddInitialStatesToBean(int startPositionLimit, PhraseTableForSentence phraseTableForSentence, int foreignSentenceLength, FastPriorityQueue<TranslationState> bean, FastPriorityQueue<TranslationState> finalBean) {
		for (int startPosition = 0; startPosition < /*This should be foreignSentenceLength, except in the case where we only start from the beginning */ startPositionLimit; startPosition++){
			int phraseLengthLimit = Math.min(foreignSentenceLength - startPosition, phraseTableForSentence.getMaxPhraseLength());
			for(int phraseLength = 1; phraseLength <= phraseLengthLimit; phraseLength++){
				// Get phrases for this specific startPosition and length
				List<ScoredPhrasePairForSentence> scoredPairs = phraseTableForSentence.getScoreSortedTranslationsForSpan(startPosition, startPosition + phraseLength);
				if (scoredPairs != null){
					for(ScoredPhrasePairForSentence scoredPair : scoredPairs){
						TranslationState state = TranslationState.BuildInitialTranslationState(scoredPair, foreignSentenceLength);
						if (state.IsFinal){
							finalBean.setPriority(state, state.CurrentScore);
						} else {
							bean.setPriority(state, state.CurrentScore);
						}
					}
				}
			}
		}
	}
}
