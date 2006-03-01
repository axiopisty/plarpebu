package com.plarpebu.segments_clock;

/**
 * Seven Segment Digit
 * 
 * @author not attributable
 * @version 1.0
 */
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.util.StringTokenizer;

public class SevenSegDigit {

    public SevenSegDigit() {
        Xsize = 300;
        Ysize = 100;
        PrevXsize = 0;
        PrevYsize = 0;
        PrevNumDigits = 0;
        ForegroundColor = Color.red;
        BackgroundColor = Color.black;
        DigitAngle = 7;
        DigitThickness = 15;
        GapSize = 2;
        NumDigits = 0;
        SegmentA = new Point[5];
        SegmentB = new Point[5];
        SegmentC = new Point[5];
        SegmentD = new Point[5];
        SegmentE = new Point[5];
        SegmentF = new Point[5];
        SegmentG = new Point[7];
        Segments = (new Point[][] { SegmentA, SegmentB, SegmentC, SegmentD, SegmentE, SegmentF, SegmentG });
        SegmentMap = (new byte[] { DISPLAY_SEGMENTS_0, DISPLAY_SEGMENTS_1, DISPLAY_SEGMENTS_2, DISPLAY_SEGMENTS_3,
        DISPLAY_SEGMENTS_4, DISPLAY_SEGMENTS_5, DISPLAY_SEGMENTS_6, DISPLAY_SEGMENTS_7, DISPLAY_SEGMENTS_8,
        DISPLAY_SEGMENTS_9 });
        DisplayValue = new byte[6];
        PrevDisplayValue = new byte[6];
        ImgBuf = null;
        og = null;

        // DP1 and DP2 are arrays for displaying ":" By default, display ":"
        // between hours and minutes,
        // And between minutes and seconds
        DP1 = new boolean[6];
        DP2 = new boolean[6];

        DP1[1] = true;
        DP2[1] = true;
        DP1[3] = true;
        DP2[3] = true;

        TempSegmentA[0] = new Point(0, 200);
        TempSegmentA[1] = new Point(100, 200);
        TempSegmentA[2] = new Point(100, 200);
        TempSegmentA[3] = new Point(0, 200);
        TempSegmentA[4] = new Point(0, 200);
        TempSegmentB[0] = new Point(100, 100);
        TempSegmentB[1] = new Point(100, 200);
        TempSegmentB[2] = new Point(100, 200);
        TempSegmentB[3] = new Point(100, 100);
        TempSegmentB[4] = new Point(100, 100);
        TempSegmentC[0] = new Point(100, 0);
        TempSegmentC[1] = new Point(100, 100);
        TempSegmentC[2] = new Point(100, 100);
        TempSegmentC[3] = new Point(100, 0);
        TempSegmentC[4] = new Point(100, 0);
        TempSegmentD[0] = new Point(0, 0);
        TempSegmentD[1] = new Point(100, 0);
        TempSegmentD[2] = new Point(100, 0);
        TempSegmentD[3] = new Point(0, 0);
        TempSegmentD[4] = new Point(0, 0);
        TempSegmentE[0] = new Point(0, 0);
        TempSegmentE[1] = new Point(0, 100);
        TempSegmentE[2] = new Point(0, 100);
        TempSegmentE[3] = new Point(0, 0);
        TempSegmentE[4] = new Point(0, 0);
        TempSegmentF[0] = new Point(0, 100);
        TempSegmentF[1] = new Point(0, 200);
        TempSegmentF[2] = new Point(0, 200);
        TempSegmentF[3] = new Point(0, 100);
        TempSegmentF[4] = new Point(0, 100);
        TempSegmentG[0] = new Point(0, 100);
        TempSegmentG[1] = new Point(0, 100);
        TempSegmentG[2] = new Point(100, 100);
        TempSegmentG[3] = new Point(100, 100);
        TempSegmentG[4] = new Point(100, 100);
        TempSegmentG[5] = new Point(0, 100);
        TempSegmentG[6] = new Point(0, 100);
        ThickA[0] = new Point(0, 0);
        ThickA[1] = new Point(0, 0);
        ThickA[2] = new Point(-2, -2);
        ThickA[3] = new Point(2, -2);
        ThickA[4] = new Point(0, 0);
        ThickB[0] = new Point(0, 0);
        ThickB[1] = new Point(0, 0);
        ThickB[2] = new Point(-2, -2);
        ThickB[3] = new Point(-2, 1);
        ThickB[4] = new Point(0, 0);
        ThickC[0] = new Point(0, 0);
        ThickC[1] = new Point(0, 0);
        ThickC[2] = new Point(-2, -1);
        ThickC[3] = new Point(-2, 2);
        ThickC[4] = new Point(0, 0);
        ThickD[0] = new Point(0, 0);
        ThickD[1] = new Point(0, 0);
        ThickD[2] = new Point(-2, 2);
        ThickD[3] = new Point(2, 2);
        ThickD[4] = new Point(0, 0);
        ThickE[0] = new Point(0, 0);
        ThickE[1] = new Point(0, 0);
        ThickE[2] = new Point(2, -1);
        ThickE[3] = new Point(2, 2);
        ThickE[4] = new Point(0, 0);
        ThickF[0] = new Point(0, 0);
        ThickF[1] = new Point(0, 0);
        ThickF[2] = new Point(2, -2);
        ThickF[3] = new Point(2, 1);
        ThickF[4] = new Point(0, 0);
        ThickG[0] = new Point(0, 0);
        ThickG[1] = new Point(2, -1);
        ThickG[2] = new Point(-2, -1);
        ThickG[3] = new Point(0, 0);
        ThickG[4] = new Point(-2, 1);
        ThickG[5] = new Point(2, 1);
        ThickG[6] = new Point(0, 0);
        GapA[0] = new Point(1, 0);
        GapA[1] = new Point(-1, 0);
        GapA[2] = new Point(-1, 0);
        GapA[3] = new Point(1, 0);
        GapA[4] = new Point(1, 0);
        GapB[0] = new Point(0, 1);
        GapB[1] = new Point(0, -1);
        GapB[2] = new Point(0, -1);
        GapB[3] = new Point(0, 1);
        GapB[4] = new Point(0, 1);
        GapC[0] = new Point(0, 1);
        GapC[1] = new Point(0, -1);
        GapC[2] = new Point(0, -1);
        GapC[3] = new Point(0, 1);
        GapC[4] = new Point(0, 1);
        GapD[0] = new Point(1, 0);
        GapD[1] = new Point(-1, 0);
        GapD[2] = new Point(-1, 0);
        GapD[3] = new Point(1, 0);
        GapD[4] = new Point(1, 0);
        GapE[0] = new Point(0, 1);
        GapE[1] = new Point(0, -1);
        GapE[2] = new Point(0, -1);
        GapE[3] = new Point(0, 1);
        GapE[4] = new Point(0, 1);
        GapF[0] = new Point(0, 1);
        GapF[1] = new Point(0, -1);
        GapF[2] = new Point(0, -1);
        GapF[3] = new Point(0, 1);
        GapF[4] = new Point(0, 1);
        GapG[0] = new Point(1, 0);
        GapG[1] = new Point(1, 0);
        GapG[2] = new Point(-1, 0);
        GapG[3] = new Point(-1, 0);
        GapG[4] = new Point(-1, 0);
        GapG[5] = new Point(1, 0);
        GapG[6] = new Point(1, 0);
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 5; j++)
                Segments[i][j] = new Point(0, 0);

        }

        Segments[6][5] = new Point(0, 0);
        Segments[6][6] = new Point(0, 0);
    }

    void CalcSegments(double d, int i, int j, double d1, int k) {
        double d2 = 0.0D;
        int j1 = GapSize;
        double d4 = k / 2D;
        d2 = d;
        double d3 = Math.tan((3.1415000000000002D * d2) / 180D);
        for (int l = 0; l < 7; l++) {
            for (int i1 = 0; i1 < SegmentPoints[l]; i1++) {
                Segments[l][i1].x = (int) ((TempSegments[l][i1].x + Thicks[l][i1].x * d4 + (TempSegments[l][i1].y + Thicks[l][i1].y
                * d4)
                * d3)
                * d1 + i);
                Segments[l][i1].y = (int) ((TempSegments[l][i1].y + Thicks[l][i1].y * d4) * d1 + j);
                Segments[l][i1].x += j1 * Gaps[l][i1].x;
                Segments[l][i1].y += j1 * Gaps[l][i1].y;
                Segments[l][i1].y = (int) (200D * d1 + (2 * j)) - Segments[l][i1].y;
            }
        }
    }

    public void PaintSevenSegDisplay(Graphics g, boolean flag, Component component) {

        double d = 1.0D;
        double d2 = 7D;
        byte byte0 = 10;
        if (Xsize != PrevXsize || Ysize != PrevYsize || NumDigits != PrevNumDigits) flag = false;
        PrevXsize = Xsize;
        PrevYsize = Ysize;
        PrevNumDigits = NumDigits;
        d2 = DigitAngle;
        double d4 = Math.tan((d2 * 3.1415000000000002D) / 180D);
        double d3 = NumDigits * 150D - 50D;
        if (Xsize < 50 || Ysize < 50) byte0 = 0;
        double d1 = (double) Ysize / (double) Xsize;
        if (d1 < 200D / (d3 + 200D * d4))
            d = (Ysize - byte0 * 2) / 200D;
        else
            d = (Xsize - byte0 * 2) / (d3 + 200D * d4);
        int j1 = (int) (Xsize - (d3 + 200D * d4) * d) / 2;
        int k1 = (int) (Ysize - 200D * d) / 2;
        int i2 = (int) (150D * d);
        if (j1 < 0) j1 = byte0;
        if (!flag) {
            Point point = new Point(0, 0);
            ImgBuf = component.createImage(Xsize, Ysize);
            og = ImgBuf.getGraphics();
            og.setColor(BackgroundColor);
            og.fillRect(0, 0, Xsize, Ysize);
            for (int i = 0; i < NumDigits; i++) {
                for (int l = 0; l < 2; l++) {
                    if (l == 0) {
                        point.y = (int) (50D * d);
                        if (!DP1[i]) continue;
                    }
                    else {
                        point.y = (int) (150D * d);
                        if (!DP2[i]) continue;
                    }
                    point.x = (int) (j1 + (150 * i + 125) * d + point.y * d4);
                    point.y += k1;
                    point.y = (int) (200D * d + (2 * k1)) - point.y;
                    int j2 = (int) ((DigitThickness / 2) * d);
                    og.setColor(ForegroundColor);
                    if (j2 < 1) j2 = 1;
                    og.fillOval(point.x - j2, point.y - j2, j2 * 2, j2 * 2);
                }

            }

        }
        else if (ImgBuf == null) return;
        for (int i1 = 0; i1 < NumDigits; i1++) {
            int l1 = j1 + i1 * i2;
            if (!flag || DisplayValue[i1] != PrevDisplayValue[i1]) {
                CalcSegments(d2, l1, k1, d, DigitThickness);
                byte byte1 = 1;
                for (int j = 0; j < 7; j++) {
                    if ((DisplayValue[i1] & byte1) != 0 && !flag) {
                        int ai[] = new int[7];
                        int ai2[] = new int[7];
                        for (int k2 = 0; k2 < SegmentPoints[j]; k2++) {
                            ai[k2] = Segments[j][k2].x;
                            ai2[k2] = Segments[j][k2].y;
                        }

                        og.setColor(ForegroundColor);
                        Polygon polygon = new Polygon(ai, ai2, SegmentPoints[j]);
                        og.drawPolygon(polygon);
                        og.fillPolygon(polygon);
                    }
                    if (flag && ((DisplayValue[i1] ^ PrevDisplayValue[i1]) != 0) & (byte1 != 0)) {
                        int ai1[] = new int[7];
                        int ai3[] = new int[7];
                        for (int l2 = 0; l2 < SegmentPoints[j]; l2++) {
                            ai1[l2] = Segments[j][l2].x;
                            ai3[l2] = Segments[j][l2].y;
                        }

                        if ((DisplayValue[i1] & byte1) != 0)
                            og.setColor(ForegroundColor);
                        else
                            og.setColor(BackgroundColor);
                        Polygon polygon1 = new Polygon(ai1, ai3, SegmentPoints[j]);
                        og.drawPolygon(polygon1);
                        og.fillPolygon(polygon1);
                    }
                    byte1 <<= 1;
                }

            }
        }

        g.drawImage(ImgBuf, 0, 0, component);
        if (!flag) System.gc();
        for (int k = 0; k < 6; k++)
            PrevDisplayValue[k] = DisplayValue[k];

    }

    public int getNumDigits() {
        return NumDigits;
    }

    public void setNumDigits(int NumDigits) {
        this.NumDigits = NumDigits;
    }

    public int Xsize;

    public int Ysize;

    int PrevXsize;

    int PrevYsize;

    int PrevNumDigits;

    Color ForegroundColor;

    Color BackgroundColor;

    int DigitAngle;

    int DigitThickness;

    int GapSize;

    int NumDigits;

    static Point TempSegmentA[];

    static Point TempSegmentB[];

    static Point TempSegmentC[];

    static Point TempSegmentD[];

    static Point TempSegmentE[];

    static Point TempSegmentF[];

    static Point TempSegmentG[];

    Point SegmentA[];

    Point SegmentB[];

    Point SegmentC[];

    Point SegmentD[];

    Point SegmentE[];

    Point SegmentF[];

    Point SegmentG[];

    static Point ThickA[];

    static Point ThickB[];

    static Point ThickC[];

    static Point ThickD[];

    static Point ThickE[];

    static Point ThickF[];

    static Point ThickG[];

    static Point GapA[];

    static Point GapB[];

    static Point GapC[];

    static Point GapD[];

    static Point GapE[];

    static Point GapF[];

    static Point GapG[];

    static Point Gaps[][];

    static Point Thicks[][];

    static Point TempSegments[][];

    Point Segments[][];

    static int SegmentPoints[] = { 5, 5, 5, 5, 5, 5, 7 };

    static byte SEGMENT_A;

    static byte SEGMENT_B;

    static byte SEGMENT_C;

    static byte SEGMENT_D;

    static byte SEGMENT_E;

    static byte SEGMENT_F;

    static byte SEGMENT_G;

    static byte DISPLAY_SEGMENTS_0;

    static byte DISPLAY_SEGMENTS_1;

    static byte DISPLAY_SEGMENTS_2;

    static byte DISPLAY_SEGMENTS_3;

    static byte DISPLAY_SEGMENTS_4;

    static byte DISPLAY_SEGMENTS_5;

    static byte DISPLAY_SEGMENTS_6;

    static byte DISPLAY_SEGMENTS_7;

    static byte DISPLAY_SEGMENTS_8;

    static byte DISPLAY_SEGMENTS_9;

    byte SegmentMap[];

    byte DisplayValue[];

    byte PrevDisplayValue[];

    Image ImgBuf;

    Dimension ImgDim;

    Graphics og;

    boolean DP1[];

    boolean DP2[];

    static {
        TempSegmentA = new Point[5];
        TempSegmentB = new Point[5];
        TempSegmentC = new Point[5];
        TempSegmentD = new Point[5];
        TempSegmentE = new Point[5];
        TempSegmentF = new Point[5];
        TempSegmentG = new Point[7];
        ThickA = new Point[5];
        ThickB = new Point[5];
        ThickC = new Point[5];
        ThickD = new Point[5];
        ThickE = new Point[5];
        ThickF = new Point[5];
        ThickG = new Point[7];
        GapA = new Point[5];
        GapB = new Point[5];
        GapC = new Point[5];
        GapD = new Point[5];
        GapE = new Point[5];
        GapF = new Point[5];
        GapG = new Point[7];
        Gaps = (new Point[][] { GapA, GapB, GapC, GapD, GapE, GapF, GapG });
        Thicks = (new Point[][] { ThickA, ThickB, ThickC, ThickD, ThickE, ThickF, ThickG });
        TempSegments = (new Point[][] { TempSegmentA, TempSegmentB, TempSegmentC, TempSegmentD, TempSegmentE,
        TempSegmentF, TempSegmentG });
        SEGMENT_A = 1;
        SEGMENT_B = 2;
        SEGMENT_C = 4;
        SEGMENT_D = 8;
        SEGMENT_E = 16;
        SEGMENT_F = 32;
        SEGMENT_G = 64;
        DISPLAY_SEGMENTS_0 = (byte) (SEGMENT_A | SEGMENT_B | SEGMENT_C | SEGMENT_D | SEGMENT_E | SEGMENT_F);
        DISPLAY_SEGMENTS_1 = (byte) (SEGMENT_B | SEGMENT_C);
        DISPLAY_SEGMENTS_2 = (byte) (SEGMENT_A | SEGMENT_B | SEGMENT_D | SEGMENT_E | SEGMENT_G);
        DISPLAY_SEGMENTS_3 = (byte) (SEGMENT_A | SEGMENT_B | SEGMENT_C | SEGMENT_D | SEGMENT_G);
        DISPLAY_SEGMENTS_4 = (byte) (SEGMENT_B | SEGMENT_C | SEGMENT_F | SEGMENT_G);
        DISPLAY_SEGMENTS_5 = (byte) (SEGMENT_A | SEGMENT_C | SEGMENT_D | SEGMENT_F | SEGMENT_G);
        DISPLAY_SEGMENTS_6 = (byte) (SEGMENT_A | SEGMENT_C | SEGMENT_D | SEGMENT_E | SEGMENT_F | SEGMENT_G);
        DISPLAY_SEGMENTS_7 = (byte) (SEGMENT_A | SEGMENT_B | SEGMENT_C);
        DISPLAY_SEGMENTS_8 = (byte) (SEGMENT_A | SEGMENT_B | SEGMENT_C | SEGMENT_D | SEGMENT_E | SEGMENT_F | SEGMENT_G);
        DISPLAY_SEGMENTS_9 = (byte) (SEGMENT_A | SEGMENT_B | SEGMENT_C | SEGMENT_D | SEGMENT_F | SEGMENT_G);
    }

    public void setDisplayValue(String time) {
        StringTokenizer s = new StringTokenizer(time, ":");
        int i = Integer.parseInt(s.nextToken());
        int j = Integer.parseInt(s.nextToken());
        int k = Integer.parseInt(s.nextToken());

        DisplayValue[0] = SegmentMap[i / 10];
        DisplayValue[1] = SegmentMap[i % 10];
        DisplayValue[2] = SegmentMap[j / 10];
        DisplayValue[3] = SegmentMap[j % 10];
        DisplayValue[4] = SegmentMap[k / 10];
        DisplayValue[5] = SegmentMap[k % 10];
    }
}
