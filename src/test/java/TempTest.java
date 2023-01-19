import mo.Kernel;
import mo.MVideo;
import mo.Morph;

import java.io.IOException;

public class TempTest {
//    public static void main(String[] args) throws IOException {
//        Morph image;
//        Kernel kernel;
//
//        final int BORDER_SENSITIVITY = 30;
//        Morph.imageType type = Morph.imageType.RGB;
//        final String OUT = "test_2.jpg";
//        final String IN = "4K.jpg";
//
//        image = new Morph();
//        image.load(IN, type);
//        kernel = Kernel.createKernel(Kernel.kernelShape.SQUARE, 3);
//
//        image.border(kernel);
//        image.toBWPicture(BORDER_SENSITIVITY);
//
//        image.save(OUT);
//    }

    public static void main(String[] args) throws IOException {
        final String IN = "Vtest_2.mp4";
        final String OUT = "Vtest_3.wmv";
        Morph.imageType type = Morph.imageType.GRAY;

        Morph image;
        MVideo video = new MVideo();
        Kernel kernel = Kernel.createKernel(Kernel.kernelShape.SQUARE, 5);

        int frames = video.load(IN, type, OUT);
        for (int i = 0; i < frames; i++) {
            image = video.grab();
            image.topHat(kernel);
            video.save();
        }
    }
}
