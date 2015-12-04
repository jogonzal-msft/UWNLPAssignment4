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
public class MyMonotomicWithLmDecoder extends DecoderBase implements Decoder {

    public MyMonotomicWithLmDecoder(PhraseTable tm, NgramLanguageModel lm, DistortionModel dm){
        // In this implementation, only the distortion model is null
        super(tm, lm, null);
    }

    @Override
    public List<ScoredPhrasePairForSentence> decode(List<String> frenchSentence) {
        // Monotonic
        return DecodeFrenchSentence(frenchSentence, true);
    }
}
