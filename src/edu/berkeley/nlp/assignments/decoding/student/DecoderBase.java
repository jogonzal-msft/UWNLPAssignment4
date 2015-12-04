package edu.berkeley.nlp.assignments.decoding.student;

import edu.berkeley.nlp.util.FastPriorityQueue;
import edu.berkeley.nlp.util.PriorityQueue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jogonzal on 12/3/2015.
 */
public class DecoderBase {
    private static Integer BeanSize = 2000;
    public static List<TranslationState> GetListFromBean(FastPriorityQueue<TranslationState> bean){
        int elementsToDequeue = Math.min(BeanSize, bean.size());
        ArrayList<TranslationState> list = new ArrayList<TranslationState>(elementsToDequeue);
        for (int i = 0; i < elementsToDequeue; i++){
            list.add(bean.removeFirst());
        }
        return list;
    }
}
