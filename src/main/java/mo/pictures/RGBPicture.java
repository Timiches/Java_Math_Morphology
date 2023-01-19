package mo.pictures;

import mo.Compare;
import mo.Dot;
import mo.Morph;

import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;

public class RGBPicture extends Picture<Integer> {

    public static final int MAX_VALUE = Integer.MAX_VALUE;
    public static final int MIN_VALUE = Integer.MIN_VALUE;
    private static final int U_BYTE_MAX = 255; // U - unsigned (C++ style)
    private static final int U_BYTE_MIN = 0;

    public RGBPicture(BufferedImage source) {
        height = source.getHeight();
        width = source.getWidth();

        pict = new Integer[width][height];
        tempPict = new Integer[width][height];
        defPict = new Integer[width][height];

        super.source = source;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                pict[i][j] = MIN_VALUE;
                tempPict[i][j] = MIN_VALUE;
            }
        }
        type = Morph.imageType.RGB;
    }

    public RGBPicture(Integer[][] source, int width, int height) {
        super.height = height;
        super.width = width;

        pict = new Integer[width][height];
        tempPict = new Integer[width][height];
        defPict = new Integer[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                defPict[i][j] = source[i][j];
                tempPict[i][j] = MIN_VALUE;
            }
        }
        pict = source;
        type = Morph.imageType.RGB;
    }

    @Override
    public void setDef() {
        copy(pict, defPict);
    }

    @Override
    public void refresh() {
        pict = tempPict;
        tempPict = new Integer[width][height];
    }

    @Override
    public void invert() {
        int invR;
        int invG;
        int invB;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                invB = 255 - (pict[x][y] & 255);
                invG = 255 - (pict[x][y] >> 8 & 255);
                invR = 255 - (pict[x][y] >> 16 & 255);
                pict[x][y] = (invR & 255) << 16 | (invG & 255) << 8 | invB & 255;
            }
        }
    }

    @Override
    public Thread getThread(Dot begin, Dot end, int type, CountDownLatch countDownLatch) {
        switch (type) {
            case Morph.MORPH_LOAD:
                return new PictureThread(begin, end, countDownLatch, (int x, int y) -> pict[x][y] = source.getRGB(x, y));
            case Morph.MORPH_SAVE:
                return new PictureThread(begin, end, countDownLatch, (int x, int y) -> result.setRGB(x, y, pict[x][y]));
            case Morph.MORPH_DILATE:
                return new PictureThread(begin, end, countDownLatch, (int x, int y) -> tempPict[x][y] = checkSurrounding(x, y, (Integer comparable, Integer compareWith) -> comparable < compareWith, U_BYTE_MIN));
            case Morph.MORPH_ERODE:
                return new PictureThread(begin, end, countDownLatch, (int x, int y) -> tempPict[x][y] = checkSurrounding(x, y, (Integer comparable, Integer compareWith) -> comparable > compareWith, U_BYTE_MAX));
            default:
                throw new IllegalArgumentException("Current operation type not founded!");
        }
    }

    private int checkSurrounding(int x, int y, Compare<Integer> comparator, int startCompareNum) { //TODO optimize
        int control = startCompareNum;
        int temp;
        int tempBrightness;
        int res = pict[x][y];

        for (int i = 0; i < kernel.getKernelSize(); i++) {
            for (int j = 0; j < kernel.getKernelSize(); j++) {
                if (kernel.getKernelAt(i, j) == 0 || x - kernel.getKernelOffset() + i < 0 || y - kernel.getKernelOffset() + j < 0 || x + i > width || y + j > height)
                    continue;
                temp = pict[x - kernel.getKernelOffset() + i][y - kernel.getKernelOffset() + j];
                tempBrightness = getBrightness(temp);
                if (comparator.compare(control, tempBrightness) && kernel.getKernelAt(i, j) == 1) {
                    control = tempBrightness;
                    res = temp;
                }
            }
        }
        return res;
    }

    private int toGrayDot(int RGB) {
        return (int) ((RGB & 255) * 0.299 + (RGB >> 8 & 255) * 0.587 + (RGB >> 8 & 255) * 0.114);
    }

    private int setRGB (int r, int g,int b) {
        return (r  & 255) << 16 | (g & 255) << 8 | (b & 255);
    }

    private int getRed(int dot) {
        return dot >> 16 & 255;
    }

    private int getGreen(int dot) {
        return dot >> 8 & 255;
    }

    private int getBlue(int dot) {
        return dot & 255;
    }

    private int getBrightness(int dot) {
        int max = Math.max(getRed(dot), getGreen(dot));
        if (getBlue(dot) > max) {
            max = getBlue(dot);
        }

        return max;
    }

    private int darkening(int dot, int step){
        int d;
        d = setRGB(getRed(dot) - step, getGreen(dot) - step, getBlue(dot) - step);
        return d;
    }

//    private void matrixSubs(int[][] minuend, int[][] subtrahend, int[][] difference) { //!!??!!
//        int dif;
//        int max = U_BYTE_MIN;
//        int min = U_BYTE_MAX;
//        int step;
//        int[][] brightnessMap = new int[width][height];
//
//        for (int x = 0; x < width; x++) {
//            for (int y = 0; y < height; y++) {
//                dif = Math.abs(getBrightness(minuend[x][y]) - getBrightness(subtrahend[x][y]));
//                if (dif > max)
//                    max = dif;
//                if (dif < min)
//                    min = dif;
//                brightnessMap[x][y] = dif;
//            }
//        }
//
//        step = U_BYTE_MAX / (max - min);
//        min *= step;
//
//        for (int x = 0; x < width; x++) {
//            for (int y = 0; y < height; y++) {
//                difference[x][y] = darkening(difference[x][y], brightnessMap[x][y] * step - min);// <- ??
//            }
//        }
//    }

    //direct subtraction
    private void matrixSubs(Integer[][] minuend, Integer[][] subtrahend, Integer[][] difference) {
        int r;
        int g;
        int b;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                r = Math.abs(getRed(minuend[x][y]) - getRed(subtrahend[x][y]));
                g = Math.abs(getGreen(minuend[x][y]) - getGreen(subtrahend[x][y]));
                b = Math.abs(getBlue(minuend[x][y]) - getBlue(subtrahend[x][y]));
                difference[x][y] = setRGB(r,g,b);
            }
        }
    }

    @Override
    public void border() {
        matrixSubs(defPict, pict, pict);
        copy(pict, defPict);
    }

    @Override
    public void topHat() {
        matrixSubs(defPict, pict, pict);
        copy(pict, defPict);
    }

    @Override
    public void bottomHat() {
        matrixSubs(pict, defPict, pict);
        copy(pict, defPict);
    }

    @Override
    public float pecstrum() {
        float totalArea = height * width;
        float minuend = 0.0F;
        float subtrahend = 0.0F;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                minuend += getBrightness(pict[x][y]) / 255.0F;
                subtrahend += getBrightness(tempPict[x][y]) / 255.0F;
            }
        }
        return (minuend - subtrahend) / totalArea;
    }

    @Override
    public GrayPicture toGray() {
        Byte[][] temp = new Byte[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                temp[x][y] = (byte) (toGrayDot(pict[x][y]) - 128);
            }
        }
        return new GrayPicture(temp, width, height);
    }

    @Override
    public BWPicture toBW(int sensitivity) {
        if (sensitivity > 255 || sensitivity < 0) {
            throw new IllegalArgumentException("Sensitivity should be > 0 and < 255");
        }

        Boolean[][] bwImg = new Boolean[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (getBrightness(pict[x][y]) > sensitivity) {
                    bwImg[x][y] = BWPicture.MAX_VALUE;
                } else {
                    bwImg[x][y] = BWPicture.MIN_VALUE;
                }
            }
        }
        return new BWPicture(bwImg, width, height);
    }

}
