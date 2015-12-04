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
        state.CurrentScore = ScoreState(state, lm, dm);

        return state;
    }

    private static double ScoreState(TranslationState state, NgramLanguageModel lm, DistortionModel dm) {
        List<ScoredPhrasePairForSentence> phrases = TranslationState.BuildPhraseListFromState(state);

        if (lm != null && dm != null){
            return Decoder.StaticMethods.scoreHypothesis(phrases, lm, dm);
        }
        if (lm != null){
            // Need to add language model to score
            double phraseScore = 0;
            for(ScoredPhrasePairForSentence phrase : phrases){
                phraseScore += phrase.score;
            }
            double lmScore = Decoder.StaticMethods.scoreSentenceWithLm(Decoder.StaticMethods.extractEnglish(phrases), lm, EnglishWordIndexer.getIndexer());
            return phraseScore + lmScore;
        } else {
            double phraseScore = 0;
            for(ScoredPhrasePairForSentence phrase : phrases){
                phraseScore += phrase.score;
            }
            return phraseScore;
        }
    }

    public static TranslationState BuildInitialTranslationState(ScoredPhrasePairForSentence firstPair, Integer sentenceLength, NgramLanguageModel lm, DistortionModel dm){

        boolean[] flags = new boolean[sentenceLength];
        for(int i = 0; i < flags.length; i++){
            flags[i] = false;
        }

        for (int i = firstPair.getStart(); i < firstPair.getEnd(); i++){
            flags[i] = true;
        }

        TranslationState state = new TranslationState(flags, null, firstPair);
        state.CurrentScore = ScoreState(state, lm, dm);

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
