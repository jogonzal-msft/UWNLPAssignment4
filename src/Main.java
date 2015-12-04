import edu.berkeley.nlp.assignments.decoding.MtDecoderTester;
import edu.berkeley.nlp.assignments.decoding.student.TranslationState;
import edu.berkeley.nlp.util.FastPriorityQueue;
import edu.berkeley.nlp.util.GeneralPriorityQueue;

import java.util.PriorityQueue;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        //TestPriorityQueue();
        MtDecoderTester.main(new String[]{
                "-path" ,".\\bigdata",
                // "-decoderType", "MONO_GREEDY"
                "-decoderType", "MONO_NOLM"
                // "-decoderType", "MONO_LM"
                // "-decoderType", "DIST_LM"
        });
    }

    public static void TestPriorityQueue(){
        FastPriorityQueue<String> queue = new FastPriorityQueue<>();
        queue.setPriority("Jorge", 0.3);
        queue.setPriority("Jorge3", 0.5);
        queue.setPriority("Jorge2", 0.4);
        System.out.println("CurrentQueue" + queue);
        System.out.println("Removing last");
        System.out.println("CurrentQueue" + queue);

    }
}
