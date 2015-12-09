package edu.berkeley.nlp.assignments.decoding.student;

import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.mt.decoder.Decoder;
import edu.berkeley.nlp.mt.decoder.DistortionModel;
import edu.berkeley.nlp.mt.phrasetable.PhraseTable;
import edu.berkeley.nlp.mt.phrasetable.ScoredPhrasePairForSentence;

import java.util.List;

/**
 * Created by jogonzal on 11/28/2015.
 */
public class MyDistortingWithLmDecoder extends NonMonotonicDecoderBase implements Decoder {

    public MyDistortingWithLmDecoder(PhraseTable tm, NgramLanguageModel lm, DistortionModel dm){
        // In this implementation, none of the 3 models are null
        super(tm, lm, dm);
    }

    @Override
    public List<ScoredPhrasePairForSentence> decode(List<String> frenchSentence) {
        // Not monotonic
        return DecodeFrenchSentence(frenchSentence, false);
    }
}
