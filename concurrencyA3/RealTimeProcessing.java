import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class RealTimeProcessing extends JFrame {
    private JLabel label;
    private BufferedImage frame;
    private int squareSize;
   
    public RealTimeProcessing(String imagePath, int squareSize, char processingMode) {
        this.squareSize = squareSize;

        frame = resizeImage(ImageUtils.openImage(imagePath), 900, 1000);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        label = new JLabel(new ImageIcon(frame));
        add(label);

        pack();
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveResult();
                System.exit(0); 
            }});

        if (processingMode == 'S') {
            processImageSingleThread();
        } else if (processingMode == 'M') {
            processImageMultiThread();
        } else {
            System.out.println("Invalid processing mode.");
            System.exit(1);
        }
    }

    private void processSquare(int x, int y) {
        int left = x;
        int upper = y;
        int right = Math.min(x + squareSize, frame.getWidth());
        int lower = Math.min(y + squareSize, frame.getHeight());

        BufferedImage squareRegion = frame.getSubimage(left, upper, right - left, lower - upper);

        int[] avgColor = averageColor(squareRegion);

        Runnable updateGUI = () -> {
            for (int sx = left; sx < right; sx++) {
                for (int sy = upper; sy < lower; sy++) {
                    frame.setRGB(sx, sy, new Color(avgColor[0], avgColor[1], avgColor[2]).getRGB());
                }
            }

            label.setIcon(new ImageIcon(frame));
            repaint();
        };

        SwingUtilities.invokeLater(updateGUI);

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private int[] averageColor(BufferedImage region) {
        int[] sum = new int[]{0, 0, 0};

        for (int x = 0; x < region.getWidth(); x++) {
            for (int y = 0; y < region.getHeight(); y++) {
                Color pixel = new Color(region.getRGB(x, y));
                sum[0] += pixel.getRed();
                sum[1] += pixel.getGreen();
                sum[2] += pixel.getBlue();
            }
        }

        int count = region.getWidth() * region.getHeight();
        return new int[]{sum[0] / count, sum[1] / count, sum[2] / count};
    }

    private void processImageSingleThread() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                int[] currentPosition = new int[]{0, 0};

                while (currentPosition[1] < frame.getHeight()) {
                    processSquare(currentPosition[0], currentPosition[1]);
                    currentPosition[0] += squareSize;

                    if (currentPosition[0] >= frame.getWidth()) {
                        currentPosition[0] = 0;
                        currentPosition[1] += squareSize;
                    }
                }

                
                return null;
            }

            @Override
            protected void done() {
                saveResult();

            }
        };

        worker.execute();
    }

    private void processRow(int y) {
        int cols = frame.getWidth() / squareSize;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                for (int j = 0; j < cols; j++) {
                    final int x = j * squareSize;
                    processSquare(x, y);
                }
                return null;
            }
        };

        worker.execute();
    }

private void processImageMultiThread() {
    int numThreads = Runtime.getRuntime().availableProcessors();
    ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

    int rowsPerThread = frame.getHeight() / numThreads;

    CountDownLatch latch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
        final int startY = i * rowsPerThread;
        final int endY = (i + 1) * rowsPerThread;

        executorService.submit(() -> {
            processRowsInRange(startY, endY); 
            latch.countDown();
        });
    }

    try {
        latch.await(); 
    } catch (InterruptedException e) {
        e.printStackTrace();
        
    } finally {
        executorService.shutdown();
        
    }

   
}
    private void processRowsInRange(int startY, int endY) {
        for (int y = startY; y < endY && y < frame.getHeight(); y += squareSize) {
            processRow(y);
        }
    }

    private void saveResult() {
        try {
            BufferedImage resultImage = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resultImage.createGraphics();
            g.drawImage(frame, 0, 0, null);
            g.dispose();

            ImageIO.write(resultImage, "jpg", new File("result.jpg"));
            System.out.println("Result saved to: result.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(resultingImage, 0, 0, null);
        g2d.dispose();
        return resizedImage;
    }


public static void main(String[] args) {
    try (Scanner scanner = new Scanner(System.in)) {
        
        System.out.println("Enter image file path, square size, and processing mode (S or M:");
        String[] input = scanner.nextLine().split(" ");

        String imagePath = input[0];
        int squareSize = Integer.parseInt(input[1]);
        char processingMode = input[2].toUpperCase().charAt(0);

        SwingUtilities.invokeLater(() -> {
            RealTimeProcessing realTimeProcessing = new RealTimeProcessing(imagePath, squareSize, processingMode);
            realTimeProcessing.setVisible(true);
        });
    }
}
}

class ImageUtils {
    public static BufferedImage openImage(String imagePath) {
        try {
            return ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}