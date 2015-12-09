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
public class NonMonotonicDecoderBase {

    TranslationState MaxState;

    PhraseTable _phraseTable;
    NgramLanguageModel _languageModel;
    DistortionModel _distortionModel;

    public NonMonotonicDecoderBase(PhraseTable tm, NgramLanguageModel lm, DistortionModel dm){
        MaxState = null;
        _phraseTable = tm;
        _languageModel = lm;
        _distortionModel = dm;
    }

    protected void AddInitialStatesToBeam(PhraseTableForSentence phraseTableForSentence, int foreignSentenceLength, FastPriorityQueue[] beamArray, boolean monotonic) {

        int startPositionLimit = foreignSentenceLength; // (unlimited for non-monotonic approaches)
        if (monotonic){
            startPositionLimit = 1; // Monotonic approaches will simply look at the first phrases and choose them. Limit exploration to those
        }

        for (int startPosition = 0; startPosition < startPositionLimit; startPosition++){
            int phraseLengthLimit = Math.min(foreignSentenceLength - startPosition, phraseTableForSentence.getMaxPhraseLength());
            //System.out.println("Initially exploring starting " + startPosition + " for " + phraseLengthLimit);
            for(int phraseLength = 1; phraseLength <= phraseLengthLimit; phraseLength++){
                // Get phrases for this specific startPosition and length
                int left = startPosition;
                int right = startPosition + phraseLength;
                //System.out.println("Exploring " + left + " - " + right);
                List<ScoredPhrasePairForSentence> scoredPairs = phraseTableForSentence.getScoreSortedTranslationsForSpan(left, right);
                if (scoredPairs != null){
                    for(ScoredPhrasePairForSentence scoredPair : scoredPairs){
                        TranslationState state = TranslationState.BuildInitialTranslationState(scoredPair, foreignSentenceLength, _languageModel, _distortionModel);
                        if (state.IsFinal){
                            SetMaxState(state);
                        } else {
                            if (monotonic){
                                InsertInBeam(state, state.CurrentScore, beamArray);
                                continue;
                            }

                            if (!state.ShouldBeAvoided(_distortionModel.getDistortionLimit())){
                                InsertInBeam(state, state.CurrentScore, beamArray);
                            }
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

    public static List<StartAndEnd> GetAvailablePositionsAndLengths(boolean[] translatedFlags) {
        List<StartAndEnd> positionsAndLengths = new ArrayList<StartAndEnd>();
        for (int i = 0; i < translatedFlags.length; i++){
            if (translatedFlags[i] == false){
                int position = i;
                int length = 0;
                while(i < translatedFlags.length && translatedFlags[i] == false){
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
        int beamLength = 400;
        PhraseTableForSentence phraseTableForSentence = _phraseTable.initialize(frenchSentence);
        MaxState = null;

        FastPriorityQueue beamArray[] = new FastPriorityQueue[foreignSentenceLength];
        for(int i = 0; i < beamArray.length; i++){
            beamArray[i] = new FastPriorityQueue<TranslationState>();
        }

        AddInitialStatesToBeam(phraseTableForSentence, foreignSentenceLength, beamArray, monotonic);

        int iteration = 0;

        while(BeamStillHasElements(beamArray)){
            // System.out.println("Best translation for iteration " + iteration  +":\n\t" + beam.getFirst());
            // System.out.println("Sentence: ");
            // List<String> accumulatedSentence = Decoder.StaticMethods.extractEnglish(TranslationState.BuildPhraseListFromState(beam.getFirst()));
            // System.out.println(StrUtils.join(accumulatedSentence));

            // Get the list from the beam and erase it
            List<TranslationState> elementsToProcess = GetListFromBeam(beamArray, beamLength);

            for (TranslationState elementToProcess : elementsToProcess){
                // System.out.println(elementToProcess);
                // Find the next best place for the next phrase
                List<StartAndEnd> startAndEnds = GetAvailablePositionsAndLengths(elementToProcess.TranslatedFlags);
                for(StartAndEnd startAndEnd : startAndEnds){
                    for(int startPosition = startAndEnd.Start; startPosition < startAndEnd.End; startPosition++){
                        if (_distortionModel != null && (_distortionModel.getDistortionLimit() < Math.abs(startPosition - elementToProcess.Phrase.getEnd()))){
                            // If we're in range, iterate. If not, break
                            continue;
                        }

                        int phraseLengthLimit = Math.min(startAndEnd.End - startPosition, phraseTableForSentence.getMaxPhraseLength());
                        int leftStart = startPosition;
                        // System.out.println("Initially exploring starting " + leftStart + " for " + phraseLengthLimit);

                        for(int phraseLength = 1; phraseLength <= phraseLengthLimit; phraseLength++){
                            // Get phrases for this specific startPosition and length
                            int left = startPosition;
                            int right = startPosition + phraseLength;
                            // System.out.println("Phrases " + left + " - " + right);
                            List<ScoredPhrasePairForSentence> scoredPairs = phraseTableForSentence.getScoreSortedTranslationsForSpan(left, right);
                            if (scoredPairs != null){
                                for(ScoredPhrasePairForSentence scoredPair : scoredPairs){
                                    TranslationState state = TranslationState.BuildTranslationState(elementToProcess, scoredPair, _languageModel, _distortionModel);
                                    if (state.IsFinal){
                                        SetMaxState(state);
                                    } else {
                                        if (monotonic){
                                            InsertInBeam(state, state.CurrentScore, beamArray);
                                            continue;
                                        }

                                        if(!state.ShouldBeAvoided(_distortionModel.getDistortionLimit())){
                                            InsertInBeam(state, state.CurrentScore, beamArray);
                                        }
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
            iteration++;
        }

        TranslationState winnerFinalState = MaxState;
        List<ScoredPhrasePairForSentence> winnerPhrase = TranslationState.BuildPhraseListFromState(winnerFinalState);
        return winnerPhrase;
    }

    private List<TranslationState> GetListFromBeam(FastPriorityQueue[] beamArray, int beamLength) {

        List<TranslationState> list = new ArrayList<TranslationState>();

        for(int i = 0; i < beamArray.length; i++){
            FastPriorityQueue<TranslationState> beam = beamArray[i];
            int elementsToDequeue = Math.min(beamLength, beam.size());
            for (int e = 0; e < elementsToDequeue; e++){
                list.add(beam.removeFirst());
            }
            beamArray[i] = new FastPriorityQueue<TranslationState>();
        }

        return list;
    }

    private boolean BeamStillHasElements(FastPriorityQueue[] beamArray) {
        for(int i = 0; i < beamArray.length; i++){
            FastPriorityQueue<TranslationState> beam = beamArray[i];
            if (beam.size() > 0){
                return true;
            }
        }
        return false;
    }

    private void InsertInBeam(TranslationState state, double priority, FastPriorityQueue[] beamArray){
        int sentenceLengthSoFar = 0;
        for(int i = 0; i < state.TranslatedFlags.length; i++){
            if (state.TranslatedFlags[i] == true){
                sentenceLengthSoFar++;
            }
        }

        beamArray[sentenceLengthSoFar].setPriority(state, priority);
    }

    private TranslationState GetBestTranslation(FastPriorityQueue[] beamArray) {
        TranslationState bestTranslation = null;
        for(int i = 0; i < beamArray.length; i++){
            FastPriorityQueue<TranslationState> beam = beamArray[i];
            if (beam.size() > 0){
                TranslationState beamFirst = beam.getFirst();
                if (bestTranslation == null){
                    bestTranslation = beamFirst;
                    continue;
                }

                if (bestTranslation.CurrentScore < beamFirst.CurrentScore){
                    bestTranslation = beamFirst;
                }
            }

        }
        return bestTranslation;
    }
}
