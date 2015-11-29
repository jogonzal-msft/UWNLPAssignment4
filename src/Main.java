import edu.berkeley.nlp.assignments.decoding.MtDecoderTester;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        MtDecoderTester.main(new String[]{
                "-path" ,".\\bigdata",
                // "-decoderType", "MONO_GREEDY"
                "-decoderType", "MONO_NOLM"
                // "-decoderType", "MONO_LM"
                // "-decoderType", "DIST_LM"
        });
    }
}
