import java.awt.*;

/* Name: PhuongLinh Tran & Ha Phan
 * File: QuadTree.java
 * Desc:
 * This program takes an array of image pixels and generate a quadtree, which is later used to
 * compress or run edge detection on the image
 */
public class QuadTree {
    private static final float[][] KERNEL = {{-1, -1, -1}, {-1, -8, -1}, {-1, -1, -1}}; // the weights set
    private static final int MAX_LEAVES = 128; // maximum number of leaves allowed in edge detection
    private static final int MAX_WEIGHTED_AVERAGE = 1500; // maximum weighted average
    private static final int MAX_SQUARED_ERROR = 75; // maximum squared error (in edge detection method)
    private Pixel[][] colors; // an array of Pixels
    private Node root; //root of tree
    private int leafCount; // number of leaves in the tree

    public QuadTree(Pixel[][] colors) {
        this.colors = colors;
        this.root = new Node(0, 0, colors[0].length, colors.length);
        this.leafCount = 1; // currently we only have the node
    }

    /**
     * Returns an array of Pixels
     * @return array of Pixels
     */
    public Pixel[][] getColors() {
        return colors;
    }

    /**
     * Returns the root of the quadtree
     * @return the root of the quadtree
     */
    public Node getRoot() {
        return root;
    }

    /**
     * Returns the number of leaves in the quadtree
     * @return the number of leaves
     */
    public int getLeafCount() {
        return leafCount;
    }

    /* Nested Node class */
    private class Node {
        private int xTop, yTop, xBot, yBot; // 4 coordinates of this cell
        private Node ne, nw, se, sw; // 4 quadrants of this cell

        public Node(int xTop, int yTop, int xBot, int yBot) {
            this.xTop = xTop;
            this.yTop = yTop;
            this.xBot = xBot;
            this.yBot = yBot;
        }

        /** Returns the size of this cell
         *
         * @return the number of pixels in this cell
         */
        public int size() {
            return (this.xBot - this.xTop) * (this.yBot - this.yTop);
        }

        /** Returns the mean Color of the cell
         *
         * @return the mean color
         */
        private Color mean() {
            int redSum = 0, greenSum = 0, blueSum = 0;
            for (int i = xTop; i < xBot; i++) {
                for (int j = yTop; j < yBot; j++) {
                    redSum += colors[i][j].getRed();
                    greenSum += colors[i][j].getGreen();
                    blueSum += colors[i][j].getBlue();
                }
            }
            return new Color(redSum / size(), greenSum / size(), blueSum / size());
        }

        /** Returns the squared error of the cell
         *
         * @return squared error
         */
        private double squaredError() {
            double errorSum = 0;
            Color mean = mean();
            for (int i = xTop; i < xBot; i++) {
                for (int j = yTop; j < yBot; j++) {
                    errorSum = errorSum + Math.pow(colors[i][j].getRed() - mean.getRed(), 2)
                            + Math.pow(colors[i][j].getGreen() - mean.getGreen(), 2)
                            + Math.pow(colors[i][j].getBlue() - mean.getBlue(), 2);
                }
            }
            return (errorSum / size());
        }

        /** Calculate weighted average of a 3x3 neighborhood around a single pixel */
        private double weightedAvg() {
            int redSum = 0;
            int greenSum = 0;
            int blueSum = 0;
            for (int i = 0; i < KERNEL.length; i++) {
                for (int j = 0; j < KERNEL[0].length; j++) {
                    int col = xTop-1+i;
                    int row = yTop-1+j;
                    if (col >= colors[0].length || row >= colors.length) {
                        col = colors[0].length-1;
                        row = colors.length-1;
                    }
                    if (col < 0)
                        col = xTop;
                    if (row<0)
                        row = yTop;

                    redSum += colors[col][row].getRed()*KERNEL[i][j];
                    greenSum += colors[col][row].getGreen()*KERNEL[i][j];
                    blueSum += colors[col][row].getBlue()*KERNEL[i][j];
                }
            }
            double weightedAvg = Math.sqrt((redSum*redSum) + (greenSum*greenSum) + (blueSum*blueSum));
            return weightedAvg;
        }
    }

    /** Uses the quadtree to compress image.
     *
     * @param threshold compression threshold
     * @param showOutline // whether to show outline or not
     */
    public void splitComp(double threshold, boolean showOutline) {
        Node n = splitRecComp(root, root.xTop, root.yTop, root.xBot, root.yBot, threshold, showOutline);
    }

    /** A recursive method used to split cells when they don't meet the threshold
     *
     * @param n the current cell
     * @param xTop smaller x coordinate
     * @param yTop smaller y coordinate
     * @param xBot greater x coordinate
     * @param yBot greater y coordinate
     * @param threshold compression threshold
     * @param showOutline // whether to show outline or not
     * @return the root
     */
    private Node splitRecComp(Node n, int xTop, int yTop, int xBot, int yBot, double threshold, boolean showOutline) {
        if (n.squaredError() < threshold) {
            Color p = n.mean();

            /* Draws outline of quadtree cells */
            if (showOutline) {
                for (int i = yTop; i < yBot; i++) {
                    colors[xTop][i].setBlue(64);
                    colors[xTop][i].setGreen(64);
                    colors[xTop][i].setRed(64);
                }
                for (int i = yTop; i < yBot; i++) {
                    colors[xBot - 1][i].setBlue(64);
                    colors[xBot - 1][i].setGreen(64);
                    colors[xBot - 1][i].setRed(64);
                }
                for (int i = xTop; i < xBot; i++) {
                    colors[i][yBot - 1].setBlue(64);
                    colors[i][yBot - 1].setRed(64);
                    colors[i][yBot - 1].setGreen(64);
                }
                for (int i = xTop; i < xBot; i++) {
                    colors[i][yTop].setGreen(64);
                    colors[i][yTop].setBlue(64);
                    colors[i][yTop].setRed(64);
                }

                /* Set pixels to the mean color */
                for (int i = xTop + 1; i < xBot - 1; i++) {
                    for (int j = yTop + 1; j < yBot - 1; j++) {
                        colors[i][j].setRed(p.getRed());
                        colors[i][j].setGreen(p.getGreen());
                        colors[i][j].setBlue(p.getBlue());
                    }
                }

            }
            else {
                for (int i = xTop; i < xBot; i++) {
                    for (int j = yTop; j < yBot; j++) {
                        colors[i][j].setRed(p.getRed());
                        colors[i][j].setGreen(p.getGreen());
                        colors[i][j].setBlue(p.getBlue());
                    }
                }
            }
            return n;
        }

        else {

            /* Splits 1x2 cell */
            if ((n.xBot - n.xTop == 1) && n.yBot - n.yTop == 2) {
                leafCount++;
                n.sw = new Node(xTop, yTop, xBot, yTop + 1);
                n.nw = new Node(xTop, yTop + 1, xBot, yBot);
                return n;
            }

            /* Splits 2x1 cell */
            if ((n.xBot - n.xTop == 2) && n.yBot - n.yTop == 1) {
                leafCount++;
                n.sw = new Node(xTop, yTop, xBot + 1, yBot);
                n.nw = new Node(xTop + 1, yTop, xBot, yBot);
                return n;
            }

            /* Returns 1x1 cell */
            if ((n.xBot - n.xTop <= 1 && n.yBot - n.yTop <= 1)) {
                return n;
            }

            /* Splits into 4 further quadrants */
            else {
                leafCount += 3;
                n.nw = new Node(xTop, yTop, xTop + (xBot - xTop) / 2, yTop + (yBot - yTop) / 2);
                n.ne = new Node(xTop + (xBot - xTop) / 2, yTop, xBot, yTop + (yBot - yTop) / 2);
                n.sw = new Node(xTop, yTop + (yBot - yTop) / 2, xTop + (xBot - xTop) / 2, yBot);
                n.se = new Node(xTop + (xBot - xTop) / 2, yTop + (yBot - yTop) / 2, xBot, yBot);
                splitRecComp(n.ne, n.ne.xTop, n.ne.yTop, n.ne.xBot, n.ne.yBot, threshold, showOutline);
                splitRecComp(n.nw, n.nw.xTop, n.nw.yTop, n.nw.xBot, n.nw.yBot, threshold, showOutline);
                splitRecComp(n.sw, n.sw.xTop, n.sw.yTop, n.sw.xBot, n.sw.yBot, threshold, showOutline);
                splitRecComp(n.se, n.se.xTop, n.se.yTop, n.se.xBot, n.se.yBot, threshold, showOutline);
            }
            return n;
        }
    }

    /**
     * Uses quadtree to do edge detection.
     */
    public void splitED(boolean showOutline) {
        splitRecED(root, root.xTop, root.yTop, root.xBot, root.yBot, showOutline);
    }

    /** A recursive method used to split cells until they're of some certain size
     *
     * @param n the current cell
     * @param xTop smaller x coordinate
     * @param yTop smaller y coordinate
     * @param xBot greater x coordinate
     * @param yBot greater y coordinate
     */
    private void splitRecED(Node n, int xTop, int yTop, int xBot, int yBot, boolean showOutline) {
        /* Uses the convolution filter for small enough nodes */
        if (n.size() <= MAX_LEAVES) {

            /* Draws outline of quadtree cells */
            if (showOutline) {
                for (int i = yTop; i < yBot; i++) {
                    colors[xTop][i].setBlue(64);
                    colors[xTop][i].setGreen(64);
                    colors[xTop][i].setRed(64);
                }
                for (int i = yTop; i < yBot; i++) {
                    colors[xBot - 1][i].setBlue(64);
                    colors[xBot - 1][i].setGreen(64);
                    colors[xBot - 1][i].setRed(64);
                }
                for (int i = xTop; i < xBot; i++) {
                    colors[i][yBot - 1].setBlue(64);
                    colors[i][yBot - 1].setRed(64);
                    colors[i][yBot - 1].setGreen(64);
                }
                for (int i = xTop; i < xBot; i++) {
                    colors[i][yTop].setGreen(64);
                    colors[i][yTop].setBlue(64);
                    colors[i][yTop].setRed(64);
                }

                for (int i = xTop+1; i < xBot-1; i++) {
                    for (int j = yTop+1; j < yBot-1; j++) {
                        if ( new Node(i, j, i + 1, j + 1).weightedAvg() >= MAX_WEIGHTED_AVERAGE) {
                            colors[i][j].setBlue(0);
                            colors[i][j].setGreen(0);
                            colors[i][j].setRed(0);

                        } else {
                            colors[i][j].setBlue(255);
                            colors[i][j].setGreen(255);
                            colors[i][j].setRed(255);
                        }
                    }
                }
            }
            else {
                for (int i = xTop; i < xBot; i++) {
                    for (int j = yTop; j < yBot; j++) {
                        Node newest = new Node(i, j, i + 1, j + 1);
                        if (newest.weightedAvg() >= MAX_WEIGHTED_AVERAGE) {
                            colors[i][j].setBlue(0);
                            colors[i][j].setGreen(0);
                            colors[i][j].setRed(0);

                        } else {
                            colors[i][j].setBlue(255);
                            colors[i][j].setGreen(255);
                            colors[i][j].setRed(255);
                        }
                    }
                }
            }
        }

        else {
            if (n.squaredError() < MAX_SQUARED_ERROR) {

                /* Draws outline of quadtree cells */
                if (showOutline) {
                    for (int i = yTop; i < yBot; i++) {
                        colors[xTop][i].setBlue(64);
                        colors[xTop][i].setGreen(64);
                        colors[xTop][i].setRed(64);
                    }
                    for (int i = yTop; i < yBot; i++) {
                        colors[xBot - 1][i].setBlue(64);
                        colors[xBot - 1][i].setGreen(64);
                        colors[xBot - 1][i].setRed(64);
                    }
                    for (int i = xTop; i < xBot; i++) {
                        colors[i][yBot - 1].setBlue(64);
                        colors[i][yBot - 1].setRed(64);
                        colors[i][yBot - 1].setGreen(64);
                    }
                    for (int i = xTop; i < xBot; i++) {
                        colors[i][yTop].setGreen(64);
                        colors[i][yTop].setBlue(64);
                        colors[i][yTop].setRed(64);
                    }
                    for (int i = xTop + 1; i < xBot - 1; i++) {
                        for (int j = yTop + 1; j < yBot - 1; j++) {
                            colors[i][j].setRed(0);
                            colors[i][j].setGreen(0);
                            colors[i][j].setBlue(0);
                        }
                    }
                }
                else {
                    for (int i = xTop; i < xBot; i++) {
                        for (int j = yTop; j < yBot; j++) {
                            colors[i][j].setRed(0);
                            colors[i][j].setGreen(0);
                            colors[i][j].setBlue(0);
                        }
                    }
                }
            }
            else {
                leafCount += 3;
                n.nw = new Node(xTop, yTop, xTop + (xBot - xTop) / 2, yTop + (yBot - yTop) / 2);
                n.ne = new Node(xTop + (xBot - xTop) / 2, yTop, xBot, yTop + (yBot - yTop) / 2);
                n.sw = new Node(xTop, yTop + (yBot - yTop) / 2, xTop + (xBot - xTop) / 2, yBot);
                n.se = new Node(xTop + (xBot - xTop) / 2, yTop + (yBot - yTop) / 2, xBot, yBot);
                splitRecED(n.ne, n.ne.xTop, n.ne.yTop, n.ne.xBot, n.ne.yBot, showOutline);
                splitRecED(n.nw, n.nw.xTop, n.nw.yTop, n.nw.xBot, n.nw.yBot, showOutline);
                splitRecED(n.sw, n.sw.xTop, n.sw.yTop, n.sw.xBot, n.sw.yBot, showOutline);
                splitRecED(n.se, n.se.xTop, n.se.yTop, n.se.xBot, n.se.yBot, showOutline);
            }
        }
    }
}
