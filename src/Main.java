import edu.berkeley.nlp.assignments.decoding.MtDecoderTester;
import edu.berkeley.nlp.assignments.decoding.student.DecoderBase;
import edu.berkeley.nlp.util.FastPriorityQueue;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello World!");

        RunUnitTests();

        MtDecoderTester.main(new String[]{
                "-path" ,".\\bigdata",
                // "-decoderType", "MONO_GREEDY"
                // "-decoderType", "MONO_NOLM"
                // "-decoderType", "MONO_LM"
                "-decoderType", "DIST_LM"
        });
    }

    private static void RunUnitTests() {
        TestGetStartEnd();
        TestPriorityQueue();
    }

    // Unit tests:

    public static void TestPriorityQueue(){
        FastPriorityQueue<String> queue = new FastPriorityQueue<>();
        queue.setPriority("Jorge", 0.3);
        queue.setPriority("Jorge3", 0.5);
        queue.setPriority("Jorge2", 0.4);
        System.out.println("CurrentQueue" + queue);
        System.out.println("Removing last");
        System.out.println("CurrentQueue" + queue);
    }

    public static void TestGetStartEnd(){
        boolean[] arr = new boolean[]{true, false, true, true, false, false, false, false};
        List<DecoderBase.StartAndEnd> startAndEnd = DecoderBase.GetAvailablePositionsAndLengths(arr);
        if (startAndEnd.size() != 2){
            throw new StackOverflowError();
        }
    }
}
