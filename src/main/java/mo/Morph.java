package mo;

import mo.pictures.*;
import org.bytedeco.javacv.CanvasFrame;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

//TODO улучшеные алгоритмы конвертации
//TODO пекстр
//TODO –абота с видео
//TODO —н€тие изображени€ с внешней апаратуры
//TODO „е нить придумать с итерфейсом (добавить универсальный тип и убрать очень много повтор€ющегос€ кода)
//TODO –еализовать показ изображений(аналог сохранениню по типу как в видео)
//TODO по добавл€ть заполнение стандартными значени€ми, т.к. заменили типи даных их обьектными представлени€ми

public class Morph {
    public int width;
    public int height;
    public Picture picture;
    public final static int MAX_THREADS = 8;
    int type = 0;

    byte[][][] standardThinningKernelsSet = {
            {{-1, -1, -1}, {0, 1, 0}, {1, 1, 1}},
            {{0, -1, -1}, {1, 1, -1}, {1, 1, 0}},
            {{1, 0, -1}, {1, 1, -1}, {1, 0, -1}},
            {{1, 1, 0}, {1, 1, -1}, {0, -1, -1}},
            {{1, 1, 1}, {0, 1, 0}, {-1, -1, -1}},
            {{0, 1, 1}, {-1, 1, 1}, {-1, -1, 0}},
            {{-1, 0, 1}, {-1, 1, 1}, {-1, 0, 1}},
            {{-1, -1, 0}, {-1, 1, 1}, {0, 1, 1}}
    };

    byte[][][] notStandardVoronKernelsSet = {
            {{0, -1, 0}, {1, 1, 1}, {1, 1, 1}},
            {{1, 0, -1}, {1, 1, 0}, {1, 1, 1}},
            {{1, 1, 0}, {1, 1, -1}, {1, 1, 0}},
            {{1, 1, 1}, {1, 1, 0}, {1, 0, -1}},
            {{1, 1, 1}, {1, 1, 1}, {0, -1, 0}},
            {{1, 1, 1}, {0, 1, 1}, {-1, 0, 1}},
            {{0, 1, 1}, {-1, 1, 1}, {0, 1, 1}},
            {{-1, 0, 1}, {0, 1, 1}, {1, 1, 1}}
    };

    byte[][][] standardVoronKernelsSet = {
            {{0, -1, 0}, {0, 1, 0}, {1, 1, 1}},
            {{0, 0, -1}, {1, 1, 0}, {1, 1, 0}},
            {{1, 0, 0}, {1, 1, -1}, {1, 0, 0}},
            {{1, 1, 0}, {1, 1, 0}, {0, 0, -1}},
            {{1, 1, 1}, {0, 1, 0}, {0, -1, 0}},
            {{0, 1, 1}, {0, 1, 1}, {-1, 0, 0}},
            {{0, 0, 1}, {-1, 1, 1}, {0, 0, 1}},
            {{-1, 0, 0}, {0, 1, 1}, {0, 1, 1}}
    };

    int kernelsSetSize = 8;

    CountDownLatch countDownLatch;
    private ArrayList<Thread> threadList = new ArrayList<Thread>(4);

    public enum imageType {
        BW,
        GRAY,
        RGB
    }

    public static final int MORPH_LOAD = 0;
    public static final int MORPH_SAVE = 1;
    public static final int MORPH_DILATE = 2;
    public static final int MORPH_ERODE = 3;

    private Kernel checkKernel(Kernel kernel) {
        if (kernel == null) {
            System.err.println("Null-kernel has been received! Setting default 3x3 square kernel...");
            return new Kernel(Kernel.kernelShape.SQUARE, 3);
        }
        return kernel;
    }

    public void load(String pathname, imageType imageType) throws IOException {
        File file = new File(pathname);
        BufferedImage source = ImageIO.read(file);
        load(source, imageType);
    }

    public void load(BufferedImage source, imageType imageType) {
        width = source.getWidth();
        height = source.getHeight();

        switch (imageType) {
            case BW:
                picture = new BWPicture(source);
                type = BufferedImage.TYPE_BYTE_GRAY;
                break;
            case GRAY:
                picture = new GrayPicture(source);
                type = BufferedImage.TYPE_BYTE_GRAY;
                break;
            case RGB:
                picture = new RGBPicture(source);
                type = BufferedImage.TYPE_3BYTE_BGR;
                break;
            default:
                throw new IllegalArgumentException("Picture must br ether binary, gray or RGB");
        }

        parallelization(Morph.MORPH_LOAD);
        picture.setDef();
    }

    //если использовать jpg, при сохранении изображени€, то будут шумы, в отличии от png, однако результирующее
    //изображение будет большего размера.
    public BufferedImage saveBuffer() {
        BufferedImage result = new BufferedImage(picture.getWidth(), picture.getHeight(), type);

        picture.save(result);
        parallelization(Morph.MORPH_SAVE);

        return result;
    }

    public void saveFile(String pathname, BufferedImage bufferedImage) throws IOException {
        File file2 = new File(pathname);
        ImageIO.write(bufferedImage, "png", file2);
    }

    public void save(String pathname) throws IOException {
        BufferedImage result = saveBuffer();
        saveFile(pathname, result);
    }

    public void show() {
        CanvasFrame canvas = new CanvasFrame("Video");
        canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        canvas.showImage(saveBuffer());
    }

    protected void parallelization(int type) {
        int rest = width % MAX_THREADS;
        int a = width - rest;
        countDownLatch = new CountDownLatch(MAX_THREADS + 1);
        int b = 0;
        int e = a / MAX_THREADS;
        try {
            for (int i = 0; i < MAX_THREADS; i++) {
                if (rest != 0) {
                    e++;
                    rest--;
                }
                Dot begin = new Dot(b, 0);
                Dot end = new Dot(e, height);
                threadList.add(i, picture.getThread(begin, end, type, countDownLatch));
                threadList.get(i).start();
                b = e;
                e += a / MAX_THREADS;
            }
            countDownLatch.countDown();
            countDownLatch.await();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void invert() {
        picture.invert();
    }

    public void dilate(Kernel kernel) {
        picture.setKernel(checkKernel(kernel));
        parallelization(MORPH_DILATE);
        picture.refresh();
    }

    public void erode(Kernel kernel) {
        picture.setKernel(checkKernel(kernel));
        parallelization(MORPH_ERODE);
        picture.refresh();
    }

    public void open(Kernel kernel) {
        erode(kernel);
        dilate(kernel);
    }

    public void close(Kernel kernel) {
        dilate(kernel);
        erode(kernel);
    }

    public void border(Kernel kernel) {
        erode(kernel);
        picture.border();
    }

    public void topHat(Kernel kernel) {
        erode(kernel);
        dilate(kernel);
        picture.topHat();
    }

    public void bottomHat(Kernel kernel) {
        dilate(kernel);
        erode(kernel);
        picture.bottomHat();
    }

    public void fill() {
        picture.fill();
    }

    public void hitOrMiss(byte[][] customKernel) {
        if (picture.getType() != imageType.BW)
            throw new IllegalArgumentException("Picture must be BW while using hit-or-miss operation");
        picture.hitOrMiss(customKernel);
        picture.refresh();
    }

    public void skeleton() {
        boolean flag;
        boolean temp;

        do {
            flag = false;
            for (int k = 0; k < kernelsSetSize; k++) {
                temp = picture.thin(standardThinningKernelsSet[k]);
                flag = flag | temp;
            }
        } while (flag);
    }

    public void thin() {
        for (int k = 0; k < kernelsSetSize; k++) {
            picture.thin(standardThinningKernelsSet[k]);
        }
    }

    public void thick() {
        picture.invert();
        for (int k = 0; k < kernelsSetSize; k++) {
            picture.thin(standardThinningKernelsSet[k]);
        }
        picture.invert();
    }

    public void voronStandard(Kernel kernel) {
        voron(kernel, standardVoronKernelsSet);
    }

    public void voronNotStandard(Kernel kernel) {
        voron(kernel, notStandardVoronKernelsSet);
    }

    private void voron(Kernel kernel, byte[][][] kernelSet) {
        boolean flag;
        boolean temp;

        erode(kernel);
        picture.border();
        picture.voron();
        picture.invert();

        do {
            flag = false;
            for (int k = 0; k < kernelsSetSize; k++) {
                temp = picture.thin(kernelSet[k]);
                flag = flag | temp;
            }
        } while (flag);
    }

    public void pecstrum(Kernel kernel, int iterations){
        for(int i = 0; i < iterations; i++){
            picture.setKernel(checkKernel(kernel));
            parallelization(MORPH_ERODE);
            System.out.println("Pecstr for A" + i + " = " + picture.pecstrum());
            picture.refresh();
        }
    }

    public void toGrayPicture() {
        picture = picture.toGray();
    }

    public void toBWPicture(int sensitivity) {
        picture = picture.toBW(sensitivity);
    }
}
