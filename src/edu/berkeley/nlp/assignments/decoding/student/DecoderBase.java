package edu.berkeley.nlp.assignments.decoding.student;

import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.mt.decoder.DistortionModel;
import edu.berkeley.nlp.mt.phrasetable.PhraseTable;
import edu.berkeley.nlp.mt.phrasetable.PhraseTableForSentence;
import edu.berkeley.nlp.mt.phrasetable.ScoredPhrasePairForSentence;
import edu.berkeley.nlp.util.FastPriorityQueue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jogonzal on 12/3/2015.
 */
public class DecoderBase {

    TranslationState MaxState;

    PhraseTable _phraseTable;
    NgramLanguageModel _languageModel;
    DistortionModel _distortionModel;

    public DecoderBase(PhraseTable tm, NgramLanguageModel lm, DistortionModel dm){
        MaxState = null;
        _phraseTable = tm;
        _languageModel = lm;
        _distortionModel = dm;
    }

    private static Integer BeanSize = 2000;
    public static List<TranslationState> GetListFromBean(FastPriorityQueue<TranslationState> bean){
        int elementsToDequeue = Math.min(BeanSize, bean.size());
        ArrayList<TranslationState> list = new ArrayList<TranslationState>(elementsToDequeue);
        for (int i = 0; i < elementsToDequeue; i++){
            list.add(bean.removeFirst());
        }
        return list;
    }

    protected void AddInitialStatesToBean(PhraseTableForSentence phraseTableForSentence, int foreignSentenceLength, FastPriorityQueue<TranslationState> bean, boolean monotonic) {

        int startPositionLimit = foreignSentenceLength; // (unlimited for non-monotonic approaches)
        if (monotonic){
            startPositionLimit = 1; // Monotonic approaches will simply look at the first phrases and choose them. Limit exploration to those
        }

        for (int startPosition = 0; startPosition < startPositionLimit; startPosition++){
            int phraseLengthLimit = Math.min(foreignSentenceLength - startPosition, phraseTableForSentence.getMaxPhraseLength());
            for(int phraseLength = 1; phraseLength <= phraseLengthLimit; phraseLength++){
                // Get phrases for this specific startPosition and length
                List<ScoredPhrasePairForSentence> scoredPairs = phraseTableForSentence.getScoreSortedTranslationsForSpan(startPosition, startPosition + phraseLength);
                if (scoredPairs != null){
                    for(ScoredPhrasePairForSentence scoredPair : scoredPairs){
                        TranslationState state = TranslationState.BuildInitialTranslationState(scoredPair, foreignSentenceLength, _languageModel, _distortionModel);
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

    protected void SetMaxState(TranslationState state) {
        if (MaxState == null){
            MaxState = state;
        } else
        {
            if (state.CurrentScore > MaxState.CurrentScore){
                MaxState = state;
            }
        }
    }

    public static class StartAndEnd{
        public Integer Start;
        public Integer End;
        public StartAndEnd(int start, int end){
            Start = start;
            End = end;
        }
    }

    public static List<StartAndEnd> GetAvailablePositionsAndLengths(TranslationState elementsToProcess) {
        List<StartAndEnd> positionsAndLengths = new ArrayList<StartAndEnd>();
        for (int i = 0; i < elementsToProcess.TranslatedFlags.length; i++){
            if (elementsToProcess.TranslatedFlags[i] == false){
                int position = i;
                int length = 0;
                while(i < elementsToProcess.TranslatedFlags.length && elementsToProcess.TranslatedFlags[i] == false){
                    length++;
                    i++;
                }
                positionsAndLengths.add(new StartAndEnd(position, position + length));
            }
        }
        return positionsAndLengths;
    }

    protected List<ScoredPhrasePairForSentence> DecodeFrenchSentence(List<String> frenchSentence, boolean monotonic) {
        int foreignSentenceLength = frenchSentence.size();
        PhraseTableForSentence phraseTableForSentence = _phraseTable.initialize(frenchSentence);
        FastPriorityQueue<TranslationState> bean = new FastPriorityQueue<TranslationState>();
        MaxState = null;

        AddInitialStatesToBean(phraseTableForSentence, foreignSentenceLength, bean, monotonic);

        while(bean.size() > 0){
            // Get the list from the bean and erase it
            List<TranslationState> elementsToProcess = GetListFromBean(bean);
            bean = new FastPriorityQueue<TranslationState>();

            for (TranslationState elementToProcess : elementsToProcess){
                // Find the next best place for the next phrase
                List<StartAndEnd> startAndEnds = GetAvailablePositionsAndLengths(elementToProcess);
                for(StartAndEnd startAndEnd : startAndEnds){
                    for(int startPosition = startAndEnd.Start; startPosition < startAndEnd.End; startPosition++){
                        int phraseLengthLimit = Math.min(startAndEnd.End - startPosition, phraseTableForSentence.getMaxPhraseLength());
                        for(int phraseLength = 1; phraseLength <= phraseLengthLimit; phraseLength++){
                            // Get phrases for this specific startPosition and length
                            List<ScoredPhrasePairForSentence> scoredPairs = phraseTableForSentence.getScoreSortedTranslationsForSpan(startPosition, startPosition + phraseLength);
                            if (scoredPairs != null){
                                for(ScoredPhrasePairForSentence scoredPair : scoredPairs){
                                    TranslationState state = TranslationState.BuildTranslationState(elementToProcess, scoredPair, _languageModel, _distortionModel);
                                    if (state.IsFinal){
                                        SetMaxState(state);
                                    } else {
                                        bean.setPriority(state, state.CurrentScore);
                                    }
                                }
                            }
                        }
                        if (monotonic){
                            // Monotonic decoders should stop at the first iteration here
                            break;
                        }
                    }
                }
            }
        }

        TranslationState winnerFinalState = MaxState;
        List<ScoredPhrasePairForSentence> winnerPhrase = TranslationState.BuildPhraseListFromState(winnerFinalState);
        return winnerPhrase;
    }
}
