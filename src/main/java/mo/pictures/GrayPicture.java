package mo.pictures;

import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;

import mo.*;

public class GrayPicture extends Picture<Byte> {
    public static final byte MAX_VALUE = Byte.MAX_VALUE;
    public static final byte MIN_VALUE = Byte.MIN_VALUE;

    public GrayPicture(BufferedImage source) {
        height = source.getHeight();
        width = source.getWidth();

        pict = new Byte[width][height];
        tempPict = new Byte[width][height];
        defPict = new Byte[width][height];

        this.source = source;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                pict[i][j] = MIN_VALUE;
                tempPict[i][j] = MIN_VALUE;
            }
        }
        type = Morph.imageType.GRAY;
    }

    public GrayPicture(Byte[][] source, int width, int height) {
        this.height = height;
        this.width = width;

        pict = new Byte[width][height];
        tempPict = new Byte[width][height];
        defPict = new Byte[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                defPict[i][j] = source[i][j];
                tempPict[i][j] = MIN_VALUE;
            }
        }
        pict = source;
        type = Morph.imageType.GRAY;
    }


    @Override
    public void setDef() {
        copy(pict, defPict);
    }

    @Override
    public void refresh() {
        pict = tempPict;
        tempPict = new Byte[width][height];
    }

    @Override
    public void invert() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pict[x][y] = (byte) ((pict[x][y] * -1) - 1);

            }
        }
    }

    @Override
    public Thread getThread(Dot begin, Dot end, int type, CountDownLatch countDownLatch) { //TODO realizovat проверки
        switch (type) {
            case Morph.MORPH_LOAD:
                return new PictureThread(begin, end, countDownLatch, (int x, int y) -> {
                    int RGB = source.getRGB(x, y);
                    pict[x][y] = (byte) (((RGB & 255) * 0.299 + (RGB >> 8 & 255) * 0.587 + (RGB >> 8 & 255) * 0.114) - 128);
                });
            case Morph.MORPH_SAVE:
                return new PictureThread(begin, end, countDownLatch, (int x, int y) -> result.setRGB(x, y, ((pict[x][y] + 128) & 255) << 16 | ((pict[x][y] + 128) & 255) << 8 | ((pict[x][y] + 128) & 255)));
            case Morph.MORPH_DILATE:
                return new PictureThread(begin, end, countDownLatch, (int x, int y) -> tempPict[x][y] = checkSurrounding(x, y, (Byte comparable, Byte compareWith) -> comparable < compareWith, MIN_VALUE));
            case Morph.MORPH_ERODE:
                return new PictureThread(begin, end, countDownLatch, (int x, int y) -> tempPict[x][y] = checkSurrounding(x, y, (Byte comparable, Byte compareWith) -> comparable > compareWith, MAX_VALUE));
            default:
                throw new IllegalArgumentException("Current operation type not founded!");
        }
    }

    private byte checkSurrounding(int x, int y, Compare<Byte> comparator, byte startCompareNum) {
        byte control = startCompareNum;
        byte temp;
        for (int i = 0; i < kernel.getKernelSize(); i++) {
            for (int j = 0; j < kernel.getKernelSize(); j++) {
                if (kernel.getKernelAt(i, j) == 0 || x - kernel.getKernelOffset() + i < 0 || y - kernel.getKernelOffset() + j < 0 || x + i > width || y + j > height)
                    continue;
                temp = pict[x - kernel.getKernelOffset() + i][y - kernel.getKernelOffset() + j]; //!!!
                if (comparator.compare(control, temp) && kernel.getKernelAt(i, j) == 1) {
                    control = temp;
                }
            }
        }
        return control;
    }

    @Override
    public void border() {
        matrixSubs(pict, defPict, pict);
        copy(pict, defPict);
    }

    @Override
    public void topHat() {
        matrixSubs(pict, defPict, pict);
        maximize();
        copy(pict, defPict);
    }

    @Override
    public void bottomHat() {
        matrixSubs(pict, defPict, pict);
        maximize();
        copy(pict, defPict);
    }

    @Override
    public float pecstrum() {
        float totalArea = height * width;
        float minuend = 0.0F;
        float subtrahend = 0.0F;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                minuend += pict[x][y] / 255.0F;
                subtrahend += tempPict[x][y] / 255.0F;
            }
        }
        return (minuend - subtrahend) / totalArea;
    }

    @Override
    public GrayPicture toGray() {
        return this;
    }

    @Override
    public BWPicture toBW(int sensitivity) {
        if (sensitivity > 255 || sensitivity < 0) {
            throw new IllegalArgumentException("Sensitivity should be > 0 and < 255");
        }
        Boolean[][] bwImg = new Boolean[width][height];
        byte threshold = (byte) (sensitivity - 128);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (pict[x][y] > threshold) {
                    bwImg[x][y] = BWPicture.MAX_VALUE;
                } else {
                    bwImg[x][y] = BWPicture.MIN_VALUE;
                }
            }
        }
        return new BWPicture(bwImg, width, height);
    }

    private void matrixSubs(Byte[][] minuend, Byte[][] subtrahend, Byte[][] difference) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                difference[x][y] = (byte) (Math.abs(minuend[x][y] - subtrahend[x][y]) - 128);
            }
        }
    }

    private void maximize() {
        byte max = MIN_VALUE;
        byte min = MAX_VALUE;
        int step;
        int res;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (pict[x][y] > max)
                    max = pict[x][y];
                else if (pict[x][y] < min)
                    min = pict[x][y];
            }
        }

        step = 255 / (max - min);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                res = pict[x][y] + 128;
                res -= min;
                res *= step;
                pict[x][y] = (byte) (res - 128);
            }
        }
    }
}
