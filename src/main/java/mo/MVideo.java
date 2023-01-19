package mo;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MVideo {
    FFmpegFrameGrabber grabber;
    FFmpegFrameRecorder recorder;
    CanvasFrame canvas;
    Frame frame;
    BufferedImage image;

    Morph picture;
    Morph.imageType type;

    public int load(String pathname, Morph.imageType type) throws FFmpegFrameGrabber.Exception {
        File file = new File(pathname);
        if (!file.exists())
            throw new IllegalArgumentException("File not found: " + pathname);

        grabber = new FFmpegFrameGrabber(pathname);
        grabber.start();
        this.type = type;
        picture = new Morph();
        return grabber.getLengthInFrames();
    }

    public int load(String in, Morph.imageType type, String out) throws FFmpegFrameGrabber.Exception, FFmpegFrameRecorder.Exception {
        int tmp = load(in, type);

        recorder = new FFmpegFrameRecorder(out, grabber.getImageWidth(), grabber.getImageHeight());
        recorder.setVideoBitrate(grabber.getVideoBitrate());
        recorder.setFrameRate(grabber.getFrameRate());
        recorder.setFormat(grabber.getFormat());
        recorder.setAudioCodec(grabber.getAudioCodec());
        recorder.setAudioBitrate(grabber.getAudioBitrate());
        recorder.setSampleRate(grabber.getSampleRate());
        recorder.setMetadata("title", "test");

        recorder.start();
        return tmp;
    }

    public Morph grab() throws FFmpegFrameGrabber.Exception {
        frame = grabber.grabImage();
        try (Java2DFrameConverter java2DConverter = new Java2DFrameConverter()) {
            image = java2DConverter.convert(frame);
        }
        picture.load(image, type);
        return picture;
    }

    public void save() throws IOException {
        image = picture.saveBuffer();

        try (Java2DFrameConverter java2DConverter = new Java2DFrameConverter()) {
            frame = java2DConverter.getFrame(image);
        }
        recorder.record(frame);
    }

    public void show() {
        if (canvas == null) {
            canvas = new CanvasFrame("Video");
            canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        }
        image = picture.saveBuffer();
        canvas.showImage(image);
    }
}
