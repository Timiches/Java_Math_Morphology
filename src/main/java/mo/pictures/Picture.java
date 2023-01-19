package mo.pictures;

import mo.Dot;
import mo.Kernel;
import mo.Morph;

import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;

public abstract class Picture<T> {
    T[][] pict;
    T[][] tempPict;
    T[][] defPict;

    int height = 0;
    int width = 0;

    BufferedImage source;
    BufferedImage result;
    Kernel kernel;
    Morph.imageType type;

    public int getHeight() {
        return height;
    }


    public int getWidth() {
        return width;
    }

    public Morph.imageType getType() {
        return type;
    }

    public void setKernel(Kernel kernel) {
        this.kernel = kernel;
    }

    protected void copy(T[][] from, T[][] to) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                to[x][y] = from[x][y];
            }
        }
    }

    public abstract void setDef();

    public void save(BufferedImage result) {
        this.result = result;
    }

    public abstract void refresh();

    public abstract void invert();

    public abstract Thread getThread(Dot begin, Dot end, int type, CountDownLatch countDownLatch);

    public abstract void border();

    public void topHat() {
        throw new IllegalStateException("You can`t use top-hat operation to BW image!");
    }

    public void bottomHat() {
        throw new IllegalStateException("You can`t use bottom-hat operation to BW image!");
    }

    public void fill() {
        throw new IllegalStateException("Exception at fill operation: the picture must be BW");
    }

    public void hitOrMiss(byte[][] kernel) {
        throw new IllegalStateException("Exception at hit-and-miss operation: the picture must be BW");
    }


    public boolean thin(byte[][] kernel) {
        throw new IllegalStateException("Exception at thinning operation: the picture must be BW");
    }


    public void voron() {
        throw new IllegalStateException("Exception at voron operation: the picture must be BW");
    }

    public abstract float pecstrum();

    public abstract GrayPicture toGray();

    public abstract BWPicture toBW(int sensitivity);
}
