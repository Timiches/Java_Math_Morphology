package mo;

public class Kernel {
    byte[][] kernelStructure;
    private int kernelSize;
    private int kernelOffset;

    public enum kernelShape {
        SQUARE,
        CROSS,
        RHOMBUS,
        ELLIPSE
    }

    public Kernel(kernelShape type, int size) {
        kernelSize = size;
        kernelOffset = kernelSize / 2;
        double ellipseError = 0.2;

        kernelStructure = new byte[size][size];
        switch (type) {
            case SQUARE:
                for (int x = 0; x < kernelSize; x++) {
                    for (int y = 0; y < kernelSize; y++) {
                        kernelStructure[x][y] = 1;
                    }
                }
                break;
            case CROSS:
                for (int x = 0; x < kernelSize; x++) {
                    for (int y = 0; y < kernelSize; y++) {
                        if(x == kernelOffset || y == kernelOffset)
                            kernelStructure[x][y] = 1;
                        else
                            kernelStructure[x][y] = 0;
                    }
                }
                break;
            case RHOMBUS:
                for (int x = 0; x < kernelSize; x++) {
                    for (int y = 0; y < kernelSize; y++) {
                        if(Math.abs(x - kernelOffset) + Math.abs(y - kernelOffset) <= kernelOffset)
                            kernelStructure[x][y] = 1;
                        else
                            kernelStructure[x][y] = 0;
                    }
                }
                break;
            case ELLIPSE:
                for (int x = 0; x < kernelSize; x++) {
                    for (int y = 0; y < kernelSize; y++) {
                        // –ассто€ние от центра до заданой точки должно быть меньше радиуса круга + погрешность
                        if(Math.sqrt(Math.pow(kernelOffset - x, 2) + Math.pow(kernelOffset - y, 2)) <= kernelOffset + ellipseError)
                            kernelStructure[x][y] = 1;
                        else
                            kernelStructure[x][y] = 0;
                    }
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown kernel type");
        }
    }

    Kernel(byte[][] hardCodded, Morph obj){
        kernelStructure = hardCodded;
        kernelSize = hardCodded.length;
        kernelOffset = kernelSize / 2;
    }

    public int getKernelSize() {
        return kernelSize;
    }

    public int getKernelOffset() {
        return kernelOffset;
    }

    public byte getKernelAt(int x, int y){
        return kernelStructure[x][y];
    }

    public static Kernel createKernel(kernelShape type, int size) {
        if (size % 2 == 0)
            throw new IllegalArgumentException("Kernel size must be odd");
        if (size == 1)
            throw new IllegalArgumentException("Kernel size should be larger");

        return new Kernel(type, size);
    }
}
