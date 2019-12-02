package origami;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.scijava.nativelib.NativeLoader;

import org.opencv.videoio.VideoCapture;

public class Origami {

    public static BufferedImage matToBufferedImage(Mat frame) {
        int type = 0;
        if (frame.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else if (frame.channels() == 3) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage image = new BufferedImage(frame.width(), frame.height(), type);
        WritableRaster raster = image.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        byte[] data = dataBuffer.getData();
        frame.get(0, 0, data);
        return image;
    }

    public static Mat bufferedImagetoMat(BufferedImage image) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", byteArrayOutputStream);
        byteArrayOutputStream.flush();
        return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.IMREAD_UNCHANGED);
    }

    private static final int BUFFER_SIZE = 8192;

    private static long copy(InputStream source, OutputStream sink) throws IOException {
        long nread = 0L;
        byte[] buf = new byte[BUFFER_SIZE];
        int n;
        while ((n = source.read(buf)) > 0) {
            sink.write(buf, 0, n);
            nread += n;
        }
        return nread;
    }

    public static Mat urlToMat(String url) throws IOException {
        return urlToMat(url, Imgcodecs.IMREAD_UNCHANGED);
    }

    public static Mat urlToMat(String url, int flag) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (InputStream in = new URL(url).openStream()) {
            copy(in, byteArrayOutputStream);
        }
        byteArrayOutputStream.flush();
        return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), flag);
    }


    public static void init() {
        try {
            NativeLoader.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            System.out.println("Loaded:" + Core.NATIVE_LIBRARY_NAME);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Mat grabOne(int camId) {
        VideoCapture vc = new VideoCapture(camId);
        Mat img1 = new Mat();
        try {Thread.sleep(500);} catch (Exception e) {}
        vc.read(img1);
        vc.release();
        return img1;
    }
    public static Mat grabOne() {
        return grabOne(0);
    }

    public static Mat resize(Mat marcel, int resizeFactor) {
        Mat smallMarcel = new Mat();
        Imgproc.resize(marcel, smallMarcel, new Size(marcel.width()/resizeFactor, marcel.height()/resizeFactor));
        return smallMarcel;
    }

    /**
     * Usage:
     * <pre>
     * public static void main(String[] args) throws IOException {
     *     NativeLoader.loadLibrary(Core.NATIVE_LIBRARY_NAME);
     *     Mat mat1 = urlToMat("https://raw.githubusercontent.com/hellonico/origami/master/doc/origami.jpg");
     *     BufferedImage bi = matToBufferedImage(mat1);
     *     Mat mat2 = bufferedImagetoMat(bi);
     *     Imgcodecs.imwrite("origami.jpg", mat2);
     * }
     * </pre>
     **/
}