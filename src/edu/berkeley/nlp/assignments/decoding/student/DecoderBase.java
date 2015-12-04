package edu.berkeley.nlp.assignments.decoding.student;

import edu.berkeley.nlp.mt.decoder.Decoder;
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

    public DecoderBase(){
        MaxState = null;
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

    protected void AddInitialStatesToBean(int startPositionLimit, PhraseTableForSentence phraseTableForSentence, int foreignSentenceLength, FastPriorityQueue<TranslationState> bean) {
        for (int startPosition = 0; startPosition < /*This should be foreignSentenceLength, except in the case where we only start from the beginning */ startPositionLimit; startPosition++){
            int phraseLengthLimit = Math.min(foreignSentenceLength - startPosition, phraseTableForSentence.getMaxPhraseLength());
            for(int phraseLength = 1; phraseLength <= phraseLengthLimit; phraseLength++){
                // Get phrases for this specific startPosition and length
                List<ScoredPhrasePairForSentence> scoredPairs = phraseTableForSentence.getScoreSortedTranslationsForSpan(startPosition, startPosition + phraseLength);
                if (scoredPairs != null){
                    for(ScoredPhrasePairForSentence scoredPair : scoredPairs){
                        TranslationState state = TranslationState.BuildInitialTranslationState(scoredPair, foreignSentenceLength);
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

    protected List<ScoredPhrasePairForSentence> BuildPhraseListFromState(TranslationState first) {
        ArrayList<ScoredPhrasePairForSentence> phrases = new ArrayList<>();
        while(first != null){
            phrases.add(0, first.Phrase);
            first = first.PreviousState;
        }
        return phrases;
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
                positionsAndLengths.add(new StartAndEnd(position, length));
            }
        }
        return positionsAndLengths;
    }

    // Only used by Monotonic decoders
    public static StartAndEnd GetInitialPositionOnlyAtBeginningFromAvailableSpots(TranslationState state) {
        List<StartAndEnd> list = GetAvailablePositionsAndLengths(state);
        if (list.size() != 1){
            throw new StackOverflowError();
        }
        return list.get(0);
    }
}
