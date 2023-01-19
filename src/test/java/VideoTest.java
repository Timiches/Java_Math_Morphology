import mo.Kernel;
import mo.Morph;
import mo.MVideo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class VideoTest {

    long start;
    long finish;

    long localStart;
    long localFinish;

    long operationStart;
    long operationFinish;

    MVideo video;
    Morph picture;
    Kernel kernel;
    String testName = " ";

//        Morph.imageType type = Morph.imageType.RGB; // не сохраняет в таком формате, только показывает
    Morph.imageType type = Morph.imageType.GRAY;
//    Morph.imageType type = Morph.imageType.BW;


//    static final String IN = "Vtest_1.wmv";
//    static final String IN = "Vtest_2.mp4";
//    static final String IN = "Vtest_4.mp4";
    static final String IN = "Vtest_5.mp4";
    static final String OUT = "Vtest_3.wmv";
    //    static final String OUT = "Vtest_3.mp4";
    public static final int BORDER_SENSITIVITY = 15;

    @BeforeEach
    void init() {
        start = System.currentTimeMillis();
        video = new MVideo();
    }

    @AfterEach
    void deInit() {
        finish = System.currentTimeMillis();
        System.out.println(testName + " execution time: " + (finish - start) + " milli sec");
        System.out.print("Local time: " + (localFinish - localStart) + " milli sec");
    }

    @Test
    void testNewDilateVideo() throws IOException, InterruptedException {
        kernel = Kernel.createKernel(Kernel.kernelShape.SQUARE, 3);

        localStart = System.currentTimeMillis();

        int frames = video.load(IN, type, OUT);

        for (int i = 0; i < frames; i++) {
            picture = video.grab();
            picture.invert();
            picture.dilate(kernel);
            video.save();
        }

        localFinish = System.currentTimeMillis();
        testName = "testLoadAndSaveVideo";
    }

//    @Test
//    void testLoadShowVideo() throws IOException, InterruptedException {
//        localStart = System.currentTimeMillis();
//
//        video.loadVideo(IN, type);
//        video.showVideo();
//
//        localFinish = System.currentTimeMillis();
//        testName = "testLoadAndSaveVideo";
//    }

//    @Test
//    void testLoadSaveVideo() throws IOException {
//        localStart = System.currentTimeMillis();
//
//        video.loadVideo(IN, type);
//        video.saveVideo(OUT);
//
//        localFinish = System.currentTimeMillis();
//        testName = "testLoadAndSaveVideo";
//    }

//    @Test
//    void testInvertImage() throws IOException {
//        video.loadVideo(IN, type);
//
//        localStart = System.currentTimeMillis();
//        video.invertVideo();
//        localFinish = System.currentTimeMillis();
//
//        video.saveVideo(OUT);
//
//        testName = "testDilateImage";
//    }

//    @Test
//    void testDilateImage() throws IOException {
//        video.loadVideo(IN, type);
//        kernel = Kernel.createKernel(Kernel.kernelShape.SQUARE, 3);
//
//        localStart = System.currentTimeMillis();
//        video.dilateVideo(kernel);
//        localFinish = System.currentTimeMillis();
//
//        video.saveVideo(OUT);
//
//        testName = "testDilateImage";
//    }
//
//    @Test
//    void testErodeImage() throws IOException {
//        video.loadVideo(IN, type);
//
//        localStart = System.currentTimeMillis();
//        kernel = Kernel.createKernel(Kernel.kernelShape.SQUARE, 3);
//        video.erodeVideo(kernel);
//        localFinish = System.currentTimeMillis();
//
//        video.saveVideo(OUT);
//        testName = "testErodeImage";
//    }

//    @Test
//    void testBorderImage() throws IOException {
//        video.loadVideo(IN, type);
//        kernel = Kernel.createKernel(Kernel.kernelShape.SQUARE, 3);
//
//        localStart = System.currentTimeMillis();
//        video.borderVideo(kernel);
//        video.toBWPictureVideo(BORDER_SENSITIVITY);
//        localFinish = System.currentTimeMillis();
//
//        video.saveVideo(OUT);
//
//        testName = "testBorderImage";
//    }

//    @Test
//    void testOpenImage() throws IOException {
//        video.loadVideo(IN, type);
//
//        localStart = System.currentTimeMillis();
//        kernel = Kernel.createKernel(Kernel.kernelShape.SQUARE, 3);
//        video.openVideo(kernel);
//        localFinish = System.currentTimeMillis();
//
//        video.saveVideo(OUT);
//
//        testName = "testOpenImage";
//    }
//
//    @Test
//    void testCloseImage() throws IOException {
//        video.loadVideo(IN, type);
//
//        localStart = System.currentTimeMillis();
//        kernel = Kernel.createKernel(Kernel.kernelShape.SQUARE, 3);
//        video.closeVideo(kernel);
//        localFinish = System.currentTimeMillis();
//
//        video.saveVideo(OUT);
//
//        testName = "testCloseImage";
//    }

}
