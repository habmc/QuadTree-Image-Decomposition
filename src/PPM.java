import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.io.File;
import java.io.PrintWriter;

/* Name: PhuongLinh Tran & Ha Phan
 * File: PPM.java
 * Desc:
 * This program includes several methods which can be used to read and write a PPM image.
 */
public class PPM {

    /**
     * Reads a PPM image into an array of Pixels
     * @param file the file name
     * @return an array of Pixels from original file
     * @throws FileNotFoundException
     */
    public static Pixel[][] readPPM(String file) throws FileNotFoundException {
        Scanner input = new Scanner(new File(file));
        input.nextLine(); // skips reading the PPM format
        String line = input.nextLine();
        while (line.length() == 0 || line.charAt(0) == '#') {
            line = input.nextLine();
        }
        int columns = Integer.parseInt(line.split(" ")[0]); // number of columns
        int row = Integer.parseInt(line.split(" ")[0]); // number of rows
        input.nextInt(); // skips reading the maximum value

        Pixel[][] colors = new Pixel[row][columns];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < columns; j++) {
                colors[i][j] = new Pixel(input.nextInt(),input.nextInt(),input.nextInt());
            }
        }
        return colors;
    }

    /**
     * Writes out a PPM image given an array of Pixels
     * @param filename the file name
     * @param colors an array of Pixels
     * @return a PPM file
     * @throws IOException
     */
    public static PrintWriter writePPM(String filename, Pixel[][] colors) throws IOException {
        PrintWriter out = new PrintWriter(filename);
        out.print("P3 ");
        out.println(colors[0].length + " " + colors.length + " 255");
        for (int i = 0; i < colors.length; i++) {
            for (int j = 0; j < colors[0].length; j++) {
                out.print(colors[i][j].getRed() + " " + colors[i][j].getGreen() + " " + colors[i][j].getBlue() + " ");
            }
            out.println();
        }
        out.close();
        return out;
    }
}
