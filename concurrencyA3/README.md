# Features and purpose

Real-Time Processing: The program divides the input image into square regions and updates the display dynamically while processing it in real-time.

Single and Multithreaded Modes: Depending on their system's capabilities, users can select between single-threaded ('S') and multi-threaded ('M') processing modes to improve performance.

User Interaction: Using the console, the application asks the user to enter the processing mode, square size, and image file path.

Dynamic GUI: To provide a visual representation of the changes, the graphical user interface dynamically updates to reflect the ongoing image processing.I used Java Swing Package.

Image Resizing: The application resizes the original image to fit within a given width and height.

Saving the Output: The final image is saved as "result.jpg" in the project directory once processing is finished or closed by user.

## INSTRUCTIONS

Download the file and open it with your IDE. 
The main code is in the file named "RealTimeProcessing", run this file.

## HOW TO RUN THE CODE?

1. Copy these 2 lines and paste it to the terminal :
   
javac RealTimeProcessing.java   
java RealTimeProcessing   

2. Or run it manually
   
Open the RealProcessingTime.java code file on vscode
and there is a run command on the top right corner.
Choose "Run Java" if you run it for the first time, otherwise choose "Run Code" option.

## INPUT LINE 

First you declare imagepath,then the square size for pixelization process and finally the processing mode (S OR M)

Example: tiger.jpg 10 M 
or       mona.jpg 15 S


I included several images to the working directory for your experience or you can download any JPEG file. 
Do not forget to write the whole path to your image if you prefered to use your own image.


# Ending Process

Click the "X" button of GUI to close application. 

# See the result

Click on the result.jpg file.



## Key Components:

processSquare(int x, int y): Processes a square of the image and updates the GUI.

averageColor(BufferedImage region): Calculates the average color of a given region of the image.

processImageSingleThread(): Processes the entire image using a single thread.

processRow(int y): Processes a row of the image using a background thread.

processImageMultiThread(): Divides the image into rows and processes each row using multiple threads.

processRowsInRange(int startY, int endY): Processes a range of rows within a specific Y coordinate range.

saveResult(): Saves the processed image result to a file (result.jpg).

resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight): Resizes the given image to the specified dimensions.

main(String[] args): Takes user input for image file path, square size, and processing mode, then initiates the application.

## How Concurrency is Achieved in Multi-Threaded Mode:

Thread Pool Creation: ExecutorService is created with a fixed number of threads based on the available processors.

Image Division: The image is divided into rows, and each thread is assigned a range of rows to process.

CountDownLatch: A CountDownLatch is used to wait for all threads to finish processing before saving the result.

Thread Execution: Threads are submitted to the executor service, and each thread processes a range of rows by calling processRowsInRange.

Result Saving: After all threads finish processing, the result is saved.



