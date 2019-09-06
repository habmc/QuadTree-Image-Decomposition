import java.io.IOException;

/* Name: Ha Phan & PhuongLinh Tran
 * File: QuadTree.java
 * Desc:
 * This program takes in user's command and perform specified actions on input image
 */
public class Main {
    private static final String INPUT = "-i";// flag indicating file name
    private static final String OUTPUT_ROOT_NAME = "-o";// flag indicating output files root name
    private static final String COMPRESSION = "-c";// flag indicating compressing is specified
    private static final String EDGE_DETECTION = "-e";// indicating edge detection is requested
    private static final String OUTLINE = "-t";// flag indicating outlining is specified
    private static final String OUTPUT_FORMAT = ".ppm"; //output file format

    // Desired compression levels
    private static final double[] COMPRESSION_LEVELS = {0.002, 0.004, 0.01, 0.033,
            0.077, 0.2, 0.5, 0.75};

    public static void main(String args[]) throws IOException {

        String inFile = ""; // input file name
        String outFileRoot = ""; // output file name root
        boolean edgeDetection = false; // returns true if edge detection is requested
        boolean showOutline = false; // returns true if showing outline is requested
        boolean compression = false; // returns true if compression is requested

        /* Parses user's input */
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(INPUT)) {
                inFile = args[i+1];
                i++;
            }
            else if (args[i].equals(OUTPUT_ROOT_NAME)) {
                outFileRoot = args[i+1];
                i++;
            }
            else if (args[i].equals(COMPRESSION)) {
                compression = true;
            }
            else if (args[i].equals(EDGE_DETECTION)) {
                edgeDetection = true;
            }
            else if (args[i].equals(OUTLINE))
                showOutline = true;
        }

        /* performs compression when specified */
        if (compression) {
            for (int i = 0; i < COMPRESSION_LEVELS.length; i++) {
                Pixel[][] output = Compress.compress(COMPRESSION_LEVELS[i], inFile, showOutline);
                PPM.writePPM(outFileRoot.concat("-" + (i+1) + OUTPUT_FORMAT), output);
            }
        }

        /* performs edge detection when specified */
        if (edgeDetection) {
            Pixel[][] image = PPM.readPPM(inFile);
            QuadTree tree = new QuadTree(image);
            tree.splitED(showOutline);
            PPM.writePPM(outFileRoot.concat(OUTPUT_FORMAT), image);
        }
    }
}
