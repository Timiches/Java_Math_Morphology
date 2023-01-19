package mo.pictures;

import mo.Dot;
import mo.Kernel;
import mo.Morph;

import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;

public class BWPicture extends Picture<Boolean> {
    public static final boolean MAX_VALUE = true;
    public static final boolean MIN_VALUE = false;
    public static final int WHITE = 255 << 16 | 255 << 8 | 255;
    public static final int BLACK = 0;

    byte[][][] standardKernelsSet = {
            {{0, 0, 0}, {-1, 1, 0}, {1, -1, 0}},
            {{-1, -1, 0}, {1, 1, 0}, {-1, -1, 0}},
            {{1, -1, 0}, {-1, 1, 0}, {0, 0, 0}},
            {{-1, 1, -1}, {-1, 1, -1}, {0, 0, 0}},
            {{0, -1, 1}, {0, 1, -1}, {0, 0, 0}},
            {{0, -1, -1}, {0, 1, 1}, {0, -1, -1}},
            {{0, 0, 0}, {0, 1, -1}, {0, -1, 1}},
            {{0, 0, 0}, {-1, 1, -1}, {-1, 1, -1}}
    };

    Boolean[][] mask;
    Boolean[][] dilatePoints;

    public BWPicture(BufferedImage source) {
        height = source.getHeight();
        width = source.getWidth();

        pict = new Boolean[width][height];
        tempPict = new Boolean[width][height];
        defPict = new Boolean[width][height];

        this.source = source;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                pict[i][j] = MIN_VALUE;
                tempPict[i][j] = MIN_VALUE;
            }
        }
        type = Morph.imageType.BW;
    }

    public BWPicture(Boolean[][] source, int width, int height) {
        this.height = height;
        this.width = width;

        pict = new Boolean[width][height];
        tempPict = new Boolean[width][height];
        defPict = new Boolean[width][height];

        pict = source;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                tempPict[i][j] = MIN_VALUE;
                defPict[i][j] = source[i][j];
            }
        }
        type = Morph.imageType.BW;
    }

    @Override
    public void setDef() {
        copy(pict, defPict);
    }

    @Override
    public void refresh() {
        pict = tempPict;
        tempPict = new Boolean[width][height];
    }

    @Override
    public void invert() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pict[x][y] = !pict[x][y];

            }
        }
    }

    @Override
    public Thread getThread(Dot begin, Dot end, int type, CountDownLatch countDownLatch) {
        switch (type) {
            case Morph.MORPH_LOAD:
                return new PictureThread(begin, end, countDownLatch, (int x, int y) -> {
                    int RGB = source.getRGB(x, y);
                    pict[x][y] = ((RGB & 255) * 0.299 + (RGB >> 8 & 255) * 0.587 + (RGB >> 8 & 255) * 0.114) > 128;
                });
            case Morph.MORPH_SAVE:
                return new PictureThread(begin, end, countDownLatch, (int x, int y) -> result.setRGB(x, y, pict[x][y] ? WHITE : BLACK));
            case Morph.MORPH_DILATE:
                return new PictureThread(begin, end, countDownLatch, (int x, int y) -> tempPict[x][y] = checkSurrounding(x, y, pict, MIN_VALUE));
            case Morph.MORPH_ERODE:
                return new PictureThread(begin, end, countDownLatch, (int x, int y) -> tempPict[x][y] = checkSurrounding(x, y, pict, MAX_VALUE));
            default:
                throw new IllegalArgumentException("Current operation type not founded!");
        }
    }

    private boolean checkSurrounding(int x, int y, Boolean[][] from, Boolean startCompareNum) {
        boolean temp;
        for (int i = 0; i < kernel.getKernelSize(); i++) {
            for (int j = 0; j < kernel.getKernelSize(); j++) {
                if (kernel.getKernelAt(i, j) == 0 || x - kernel.getKernelOffset() + i < 0 || y - kernel.getKernelOffset() + j < 0 || x + i > width || y + j > height)
                    continue;
                temp = from[x - kernel.getKernelOffset() + i][y - kernel.getKernelOffset() + j]; //!!!
                if ((temp != startCompareNum) == (kernel.getKernelAt(i, j) == 1)) {
                    return temp;
                }
            }
        }
        return startCompareNum;
    }

    @Override
    public void border() {
        matrixNotEqualsOperation(pict, defPict, pict);
        copy(pict, defPict);
    }

    @Override
    public void fill() {
        mask = new Boolean[width][height];
        dilatePoints = new Boolean[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                mask[x][y] = MIN_VALUE;
                dilatePoints[x][y] = MIN_VALUE;
            }
        }

        pict = removeUselessDots(tempPict);
        findObjectsCenter();

        kernel = new Kernel(Kernel.kernelShape.CROSS, 3);

        int ts = 0; //TODO переименовать
        boolean flag;

        do {
            flag = false;

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    tempPict[x][y] = checkSurrounding(x, y, dilatePoints, MIN_VALUE);
                }
            }

            dilatePoints = tempPict;
            tempPict = new Boolean[width][height];

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if ((dilatePoints[x][y] == pict[x][y]) && pict[x][y]) {
                        dilatePoints[x][y] = false;
                    } else if ((dilatePoints[x][y] != pict[x][y]) && pict[x][y]) {
                        flag = true;
                    }
                }
            }
            if (!flag) {
                break;
            }
            ts++;
        } while (ts < 100);

        matrixOrOperation(dilatePoints, pict, pict);
    }

    @Override
    public void hitOrMiss(byte[][] kernel) {
        int length = kernel.length;
        int offset = length / 2;
        boolean temp;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                boolean ret = true;

                for (int i = 0; i < length; i++) {
                    for (int j = 0; j < length; j++) {
                        if (kernel[i][j] == 0 || x - offset + i < 0 || y - offset + j < 0 || x + i > width || y + j > height)
                            continue;
                        temp = pict[x - offset + i][y - offset + j];
                        if (temp != (kernel[i][j] == 1)) {
                            ret = false;
                        }
                    }
                }

                tempPict[x][y] = ret;

            }
        }
    }

    @Override
    public boolean thin(byte[][] kernel) {
        boolean flag = false;
        hitOrMiss(kernel);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (pict[x][y] && tempPict[x][y]) {
                    pict[x][y] = MIN_VALUE;
                    flag = true;
                }
            }
        }
        return flag;
    }

    @Override
    public void voron() {
        mask = new Boolean[width][height];
        dilatePoints = new Boolean[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                mask[x][y] = MIN_VALUE;
                dilatePoints[x][y] = MIN_VALUE;
            }
        }

        pict = removeUselessDots(tempPict);
        findObjectsCenter();
        pict = dilatePoints;
    }

    @Override
    public float pecstrum() {
        float totalArea = height * width;
        float minuend = 0.0F;
        float subtrahend = 0.0F;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(pict[x][y]){
                    minuend++;
                }
                if (tempPict[x][y]){
                    subtrahend++;
                }
            }
        }
        return (minuend - subtrahend) / totalArea;
    }

    @Override
    public GrayPicture toGray() {
        Byte[][] temp = new Byte[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                temp[x][y] = pict[x][y] ? GrayPicture.MAX_VALUE : GrayPicture.MIN_VALUE;
            }
        }
        return new GrayPicture(temp, width, height);
    }

    private void matrixNotEqualsOperation(Boolean[][] minuend, Boolean[][] subtrahend, Boolean[][] difference) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                difference[x][y] = minuend[x][y] != subtrahend[x][y];
            }
        }
    }

    private void matrixAndOperation(Boolean[][] minuend, Boolean[][] subtrahend, Boolean[][] difference) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                difference[x][y] = minuend[x][y] && subtrahend[x][y];
            }
        }
    }

    private void matrixOrOperation(Boolean[][] minuend, Boolean[][] subtrahend, Boolean[][] difference) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                difference[x][y] = minuend[x][y] || subtrahend[x][y];
            }
        }
    }

    @Override
    public BWPicture toBW(int sensitivity) {
        return this;
    }

    private void edgeDetector(Dot startPoint) {
        int xCenter = startPoint.getX();
        int yCenter = startPoint.getY();
        mask[xCenter][yCenter] = true;

        int xSum = xCenter;
        int ySum = yCenter;
        int dotsCount = 1;

        Dot tempDot;

        do {
            tempDot = check(xCenter, yCenter);

            if (xCenter == tempDot.getX() && yCenter == tempDot.getY())
                break;

            xCenter = tempDot.getX();
            yCenter = tempDot.getY();

            xSum += xCenter;
            ySum += yCenter;
            dotsCount++;

        } while (xCenter != startPoint.getX() || yCenter != startPoint.getY());

        if (!mask[xSum / dotsCount][ySum / dotsCount])
            dilatePoints[xSum / dotsCount][ySum / dotsCount] = true;
    }

    private Boolean[][] removeUselessDots(Boolean[][] from) {

        Boolean[][] temp = new Boolean[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                temp[i][j] = MIN_VALUE;
            }
        }

        for (int k = 0; k < 8; k++) {
            hitOrMiss(standardKernelsSet[k]);
            matrixOrOperation(from, temp, temp);
        }
        return temp;
    }

    private void findObjectsCenter() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (!mask[x][y] && pict[x][y])
                    edgeDetector(new Dot(x, y));
            }
        }
    }

    Dot check(int xCtr, int yCtr) {
        int x = xCtr - 1;
        int y = yCtr - 1;

        int xEnd = x + 2;
        int yEnd = y + 2;

        while (x <= xEnd) {
            if (x < 0 || y < 0 || x >= width || y >= height) {
                x++;
                continue;
            }
            if (pict[x][y] && !mask[x][y]) {
                mask[x][y] = true;
                return new Dot(x, y);
            }
            x++;
        }
        x--;
        while (y <= yEnd) {
            if (x < 0 || y < 0 || x >= width || y >= height) {
                y++;
                continue;
            }
            if (pict[x][y] && !mask[x][y]) {
                mask[x][y] = true;
                return new Dot(x, y);
            }
            y++;
        }
        y--;
        while (x >= xEnd - 2) {
            if (x < 0 || y < 0 || x >= width || y >= height) {
                x--;
                continue;
            }
            if (pict[x][y] && !mask[x][y]) {
                mask[x][y] = true;
                return new Dot(x, y);
            }
            x--;
        }
        x++;
        while (y >= yEnd - 2) {
            if (x < 0 || y < 0 || x >= width || y >= height) {
                y--;
                continue;
            }
            if (pict[x][y] && !mask[x][y]) {
                mask[x][y] = true;
                return new Dot(x, y);
            }
            y--;
        }
        y++;
        return new Dot(xCtr, yCtr);
    }


}
