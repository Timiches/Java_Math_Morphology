import mo.Kernel;
import mo.Morph;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static java.lang.Thread.sleep;

public class MainTest {
    public static final int BORDER_SENSITIVITY = 10;

    long start;
    long finish;

    long localStart;
    long localFinish;

    long operationStart;
    long operationFinish;

    Morph image = new Morph();
    Kernel kernel;
    String testName = " ";

        Morph.imageType type = Morph.imageType.RGB;
//    Morph.imageType type = Morph.imageType.GRAY;
//    Morph.imageType type = Morph.imageType.BW;

    //    static final String IN = "test_1.jpg";
//    static final String IN = "test_3.jpg";
//    static final String IN = "test_5.jpg";
//    static final String IN = "test_6.jpg";
//    static final String IN = "test_7.jpg";
//    static final String IN = "test_8.png";
//    static final String IN = "test_8_2.jpg";
//    static final String IN = "test_9.jpg";
//    static final String IN = "test_10.jpg";
//    static final String IN = "test_11.jpg";
    static final String IN = "test_12.jpg";
    static final String OUT = "test_2.jpg";

//    static final String IN = "720p.jpg";
//    static final String IN = "1080p.jpg";
//    static final String IN = "4K.jpg";
//    static final String IN = "ttt.jpg";

    @BeforeEach
    void init() {
        start = System.currentTimeMillis();
        image = new Morph();
    }

    @AfterEach
    void deInit() {
        finish = System.currentTimeMillis();
        System.out.println(testName + " execution time: " + (finish - start) + " milli sec");
//        System.out.print("Local time: " + (localFinish - localStart) + " milli sec");
    }

//    @Test
//    void SpeedTest() throws IOException {
//        kernel = Kernel.createKernel(Kernel.kernelShape.SQUARE, 3);
//
//        for (int i = 0; i < 10; i++) {
//            localStart = System.currentTimeMillis();
//            image.load(IN, type);
//
//            operationStart = System.currentTimeMillis();
//            image.dilate(kernel);
//            operationFinish = System.currentTimeMillis();
//
//            image.save(OUT);
//            localFinish = System.currentTimeMillis();
//
//            testName = "SpeedTest";
//            System.out.print((operationFinish - operationStart) + "|" + (localFinish - localStart) + ", ");
//        }
//
//    }

//    @Test
//    void testSpeedImage() throws IOException {
//        image.load(IN, type);
//        kernel = Kernel.createKernel(Kernel.kernelShape.SQUARE, 3);
//        image.load(IN, type);
//        for (int i = 0; i < 10; i++) {
//            localStart = System.currentTimeMillis();
//            image.open(kernel);
//            localFinish = System.currentTimeMillis();
//            System.out.print((localFinish - localStart) - 40 + " ");
//        }
//        image.save(OUT);
//
//        testName = "testSpeedImage";
//    }

//    @Test
//    void testPecstrImage() throws IOException {
//        image.load(IN, type);
//        kernel = Kernel.createKernel(Kernel.kernelShape.CROSS, 3);
//
//        localStart = System.currentTimeMillis();
//        image.pecstrum(kernel, 3);
//        localFinish = System.currentTimeMillis();
//
////        image.save(OUT);
//        testName = "testPecstrImage";
//    }

    @Test
    void testVoronImage() throws IOException {
        image.load(IN, type);
        kernel = Kernel.createKernel(Kernel.kernelShape.CROSS, 3);

        image.border(kernel);
        image.toBWPicture(BORDER_SENSITIVITY);

        localStart = System.currentTimeMillis();
        image.voronStandard(kernel);
//        image.voronNotStandard(kernel);

        localFinish = System.currentTimeMillis();

        image.save(OUT);
        testName = "testVoronImage";
    }

//    @Test
//    void testFillImage() throws IOException {
//        image.load(IN, type);
//        kernel = Kernel.createKernel(Kernel.kernelShape.CROSS, 3);
//
//        localStart = System.currentTimeMillis();
//        image.border(kernel);
//        image.fill();
//
//        localFinish = System.currentTimeMillis();
//
//        image.save(OUT);
//        testName = "testFillImage";
//    }
//
//    @Test
//    void testThickImage() throws IOException {
//        image.load(IN, type);
//
//        localStart = System.currentTimeMillis();
//        kernel = Kernel.createKernel(Kernel.kernelShape.SQUARE, 3);
//
//        image.border(kernel);
//        image.toBWPicture(BORDER_SENSITIVITY);
//        image.thick();
//        localFinish = System.currentTimeMillis();
//
//        image.save(OUT);
//
//        testName = "testThickImage";
//    }
//
//    @Test
//    void testThinImage() throws IOException {
//        image.load(IN, type);
//
//        localStart = System.currentTimeMillis();
//
//        image.thin();
//        localFinish = System.currentTimeMillis();
//
//        image.save(OUT);
//
//        testName = "testThinImage";
//    }
//
//    @Test
//    void testSkeletonImage() throws IOException {
//        image.load(IN, type);
//
//        localStart = System.currentTimeMillis();
//        kernel = Kernel.createKernel(Kernel.kernelShape.SQUARE, 3);
//
//        image.border(kernel);
//        image.toBWPicture(BORDER_SENSITIVITY);
//        image.skeleton();
//        localFinish = System.currentTimeMillis();
//
//        image.save(OUT);
//
//        testName = "testSkeletonImage";
//    }
//
//    @Test
//    void testHitOrMissImage() throws IOException {
////        byte[][] t = {{0, 1, 0}, {-1, 1, 1}, {-1, -1, 0}};
////        byte[][] t = {{0,-1,0},{-1,-1,1},{0,1,1}};
//        byte[][] t = {{1, 1, 0}, {1, 1, -1}, {0, -1, -1}};
////        byte[][][] t = {
////                {{0, 0, 0}, {-1, 1, -1}, {1, 1, 1}},
////                {{-1, 0, 0}, {1, 1, 0}, {1, 1, -1}},
////                {{1, -1, 0}, {1, 1, 0}, {1, -1, 0}},
////                {{1, 1, -1}, {1, 1, 0}, {-1, 0, 0}},
////                {{1, 1, 1}, {-1, 1, -1}, {0, 0, 0}},
////                {{-1, 1, 1}, {0, 1, 1}, {0, 0, -1}},
////                {{0, -1, 1}, {0, 1, 1}, {0, -1, 1}},
////                {{0, 0, -1}, {0, 1, 1}, {-1, 1, 1}}
////        };
//
//        image.load(IN, type);
//        kernel = Kernel.createKernel(Kernel.kernelShape.SQUARE, 3);
//
//        localStart = System.currentTimeMillis();
//        image.border(kernel);
//
//        image.close(kernel);
//        image.open(kernel);
//
//        image.toBWPicture(BORDER_SENSITIVITY);
//        image.hitOrMiss(t);
//        localFinish = System.currentTimeMillis();
//
//        image.save(OUT);
//        testName = "testHitOrMissImage";
//    }
//
//    @Test
//    void testBottomHatImage() throws IOException {
//        image.load(IN, type);
//        kernel = Kernel.createKernel(Kernel.kernelShape.SQUARE, 9);
//
//        localStart = System.currentTimeMillis();
//        image.bottomHat(kernel);
//        localFinish = System.currentTimeMillis();
//
//        image.save(OUT);
//
//        testName = "testBottomHatImage";
//    }
//
//    @Test
//    void testTopHatImage() throws IOException {
//        kernel = Kernel.createKernel(Kernel.kernelShape.SQUARE, 9);
//
//        image.load(IN,type);
//        localStart = System.currentTimeMillis();
//
//        image.topHat(kernel);
//
//        localFinish = System.currentTimeMillis();
//        image.save(OUT);
//
//        testName = "testTopHatImage";
//    }
//
//    @Test
//    void testBorderImage() throws IOException {
//        image.load(IN, type);
//        kernel = Kernel.createKernel(Kernel.kernelShape.SQUARE, 3);
//
//        localStart = System.currentTimeMillis();
//        image.border(kernel);
////        image.toBWPicture(BORDER_SENSITIVITY);
//        image.open(kernel);
//
//        localFinish = System.currentTimeMillis();
//
//        image.save(OUT);
//
//        testName = "testBorderImage";
//    }
//
//    @Test
//    void testOpenImage() throws IOException {
//        image.load(IN, type);
//        kernel = Kernel.createKernel(Kernel.kernelShape.SQUARE, 9);
//
//        image.open(kernel);
//        image.close(kernel);
//
//        image.save(OUT);
//
//        testName = "testOpenImage";
//    }
//
//    @Test
//    void testCloseImage() throws IOException {
//        image.load(IN, type);
//        kernel = Kernel.createKernel(Kernel.kernelShape.SQUARE, 9);
//
//        localStart = System.currentTimeMillis();
//        image.close(kernel);
//        localFinish = System.currentTimeMillis();
//
//        image.save(OUT);
//
//        testName = "testCloseImage";
//    }
//
//    @Test
//    void testLoadAndSaveImage() throws IOException {
//        image.load(IN, type);
////        image.toBWPicture(BORDER_SENSITIVITY);
//        image.save(OUT);
//
//        testName = "testLoadAndSaveImage";
//    }

//    @Test
//    void testDilateImage() throws IOException {
//        image.load(IN, type);
//        kernel = Kernel.createKernel(Kernel.kernelShape.SQUARE, 9);
//
//        localStart = System.currentTimeMillis();
//        image.dilate(kernel);
//        localFinish = System.currentTimeMillis();
//
//        image.save(OUT);
//
//        testName = "testDilateImage";
//    }
////
//    @Test
//    void testInvertImage() throws IOException {
//        image.load(IN, type);
//
//        localStart = System.currentTimeMillis();
//        image.invert();
//        localFinish = System.currentTimeMillis();
//
//        image.save(OUT);
//
//        testName = "testDilateImage";
//    }
//
//    @Test
//    void testErodeImage() throws IOException {
//        image.load(IN, type);
//
//        localStart = System.currentTimeMillis();
//        kernel = Kernel.createKernel(Kernel.kernelShape.SQUARE, 9);
//        image.erode(kernel);
//        localFinish = System.currentTimeMillis();
//
//        image.save(OUT);
//        testName = "testErodeImage";
//    }
}
