package edu.berkeley.nlp.assignments.decoding.student;

import edu.berkeley.nlp.langmodel.EnglishWordIndexer;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.mt.decoder.Decoder;
import edu.berkeley.nlp.mt.decoder.DistortionModel;
import edu.berkeley.nlp.mt.phrasetable.ScoredPhrasePairForSentence;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jogonzal on 12/3/2015.
 */
public class TranslationState {
    public boolean[] TranslatedFlags;
    public double CurrentScore;
    public TranslationState PreviousState;
    public boolean IsFinal;
    ScoredPhrasePairForSentence Phrase;

    public List<Integer> pastTwoWords;

    public TranslationState(boolean[] translatedFlags, TranslationState previousState, ScoredPhrasePairForSentence phrase){
        TranslatedFlags = translatedFlags;
        PreviousState = previousState;
        IsFinal = true;
        Phrase = phrase;
        for (int i = 0; i < TranslatedFlags.length; i++){
            if (!TranslatedFlags[i]){
                IsFinal = false;
                break;
            }
        }

        pastTwoWords = new ArrayList<Integer>(2);
        TranslationState stateToLookInto = previousState;
        while(pastTwoWords.size() < 2 && stateToLookInto != null){
            int[] english = stateToLookInto.Phrase.english.indexedEnglish;
            int addingOffset = english.length - 1;
            while(pastTwoWords.size() < 2 && addingOffset >= 0){
                pastTwoWords.add(0, english[addingOffset]);
            }
            stateToLookInto = previousState.PreviousState;
        }
        while (pastTwoWords.size() < 2){
            pastTwoWords.add(0, EnglishWordIndexer.getIndexer().addAndGetIndex(NgramLanguageModel.START));
        }
    }

    public static TranslationState BuildTranslationState(TranslationState previousState, ScoredPhrasePairForSentence phrasePair, NgramLanguageModel lm, DistortionModel dm){
        boolean[] translatedFlags = new boolean[previousState.TranslatedFlags.length];

        for(int i = 0; i < translatedFlags.length; i++){
            translatedFlags[i] = previousState.TranslatedFlags[i];
        }
        for (int i = phrasePair.getStart(); i < phrasePair.getEnd(); i++){
            translatedFlags[i] = true;
        }

        TranslationState state = new TranslationState(translatedFlags, previousState, phrasePair);
        state.CurrentScore = ScoreStateInefficient(state, lm, dm);

        return state;
    }

    private static double ScoreStateInefficient(TranslationState state, NgramLanguageModel lm, DistortionModel dm) {
        List<ScoredPhrasePairForSentence> phrases = TranslationState.BuildPhraseListFromState(state);

        if (lm != null && dm != null){
            return Decoder.StaticMethods.scoreHypothesis(phrases, lm, dm);
        }

        if (lm != null) {
            // Need to add language model to score
            double phraseScore = 0;
            for (ScoredPhrasePairForSentence phrase : phrases) {
                phraseScore += phrase.score;
            }
            double lmScore = Decoder.StaticMethods.scoreSentenceWithLm(Decoder.StaticMethods.extractEnglish(phrases), lm, EnglishWordIndexer.getIndexer());
            return phraseScore + lmScore;
        } else {
            double phraseScore = 0;
            for (ScoredPhrasePairForSentence phrase : phrases) {
                phraseScore += phrase.score;
            }
            return phraseScore;
        }
    }

    /*private static double ScoreStateEfficient(TranslationState state, NgramLanguageModel lm, DistortionModel dm) {
        double accumulatedScore = 0;
        if (state.PreviousState != null){
            accumulatedScore = state.PreviousState.CurrentScore;
        }

        // Phrase score
        double phraseScore = state.Phrase.score;
        accumulatedScore += phraseScore;

        // LM score
        if (lm != null){
            int[] arr = new int[state.Phrase.english.indexedEnglish.length + 2];
            arr[0] = state.pastTwoWords.get(0);
            arr[1] = state.pastTwoWords.get(1);
            for(int i = 0; i < state.Phrase.english.indexedEnglish.length; i++){
                arr[2 + i] = state.Phrase.english.indexedEnglish[i];
            }
            double lmScore = lm.getNgramLogProbability(arr, 2, 2 + state.Phrase.english.indexedEnglish.length);
            accumulatedScore += lmScore;
        }

        // DM score
        if (dm != null){
            // Need to add language model to score
            int previousEnd = 0;
            if (state.PreviousState != null){
                previousEnd = state.PreviousState.Phrase.getEnd();
            }
            double distortionModel = dm.getDistortionScore(previousEnd, state.Phrase.getStart());
            accumulatedScore += distortionModel;
        }

        return accumulatedScore;
    }*/

    public static TranslationState BuildInitialTranslationState(ScoredPhrasePairForSentence firstPair, Integer sentenceLength, NgramLanguageModel lm, DistortionModel dm){

        boolean[] flags = new boolean[sentenceLength];
        for(int i = 0; i < flags.length; i++){
            flags[i] = false;
        }

        for (int i = firstPair.getStart(); i < firstPair.getEnd(); i++){
            flags[i] = true;
        }

        TranslationState state = new TranslationState(flags, null, firstPair);
        state.CurrentScore = ScoreStateInefficient(state, lm, dm);

        return state;
    }

    public static List<ScoredPhrasePairForSentence> BuildPhraseListFromState(TranslationState first) {
        ArrayList<ScoredPhrasePairForSentence> phrases = new ArrayList<>();
        while(first != null){
            phrases.add(0, first.Phrase);
            first = first.PreviousState;
        }
        return phrases;
    }
}
