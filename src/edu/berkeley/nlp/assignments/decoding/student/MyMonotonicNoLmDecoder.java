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

	public MyMonotonicNoLmDecoder(PhraseTable tm, NgramLanguageModel lm, DistortionModel dm){
		// In this implementation, we don't care about the language or distortion model
		super(tm, null, null);
	}

	@Override
	public List<ScoredPhrasePairForSentence> decode(List<String> frenchSentence) {
		// Monotonic
		return DecodeFrenchSentence(frenchSentence, true);
	}
}
