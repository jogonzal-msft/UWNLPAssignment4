package edu.berkeley.nlp.assignments.decoding.student;

import edu.berkeley.nlp.mt.phrasetable.ScoredPhrasePairForSentence;

/**
 * Created by jogonzal on 12/3/2015.
 */
public class TranslationState {
    public boolean[] TranslatedFlags;
    public double CurrentScore;
    public TranslationState PreviousState;
    public boolean IsFinal;
    ScoredPhrasePairForSentence Phrase;

    public TranslationState(double score, boolean[] translatedFlags, TranslationState previousState, ScoredPhrasePairForSentence phrase){
        TranslatedFlags = translatedFlags;
        CurrentScore = score;
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

    public static TranslationState BuildTranslationState(TranslationState previousState, ScoredPhrasePairForSentence phrasePair){
        double score = previousState.CurrentScore + phrasePair.score;
        boolean[] translatedFlags = new boolean[previousState.TranslatedFlags.length];

        for(int i = 0; i < translatedFlags.length; i++){
            translatedFlags[i] = previousState.TranslatedFlags[i];
        }
        for (int i = phrasePair.getStart(); i < phrasePair.getEnd(); i++){
            translatedFlags[i] = true;
        }

        return new TranslationState(score, translatedFlags, previousState, phrasePair);
    }

    public static TranslationState BuildInitialTranslationState(ScoredPhrasePairForSentence firstPair, Integer sentenceLength){

        boolean[] flags = new boolean[sentenceLength];
        for(int i = 0; i < flags.length; i++){
            flags[i] = false;
        }

        for (int i = firstPair.getStart(); i < firstPair.getEnd(); i++){
            flags[i] = true;
        }

        return new TranslationState(firstPair.score, flags, null, firstPair);
    }
}
