package org.firstinspires.ftc.teamcode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

import org.opencv.core.*;
import org.opencv.core.Core.*;

import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.*;
import org.opencv.objdetect.*;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;
import org.openftc.easyopencv.OpenCvWebcam;
/**
* GripPipeline class.
*
* <p>An OpenCV pipeline generated by GRIP.
*
* @author GRIP
*/
public class GripPipeline implements OpenCvPipeline{

	//Outputs
	private Mat rgbThresholdOutput = new Mat();
	private ArrayList<MatOfPoint> findContoursOutput = new ArrayList<MatOfPoint>();
	private double targetCenter;
	private double maxSize = -1;
	private int closest = -1;
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	/**
	 * This is the primary method that runs the entire pipeline and updates the outputs.
	 */
	public Mat processFrame(Mat source0) {
		// Step RGB_Threshold0:
		Mat rgbThresholdInput = source0;
		double[] rgbThresholdRed = {107.77877697841726, 255.0};
		double[] rgbThresholdGreen = {112.36510791366906, 255.0};
		double[] rgbThresholdBlue = {0.0, 80.93856655290104};
		rgbThreshold(rgbThresholdInput, rgbThresholdRed, rgbThresholdGreen, rgbThresholdBlue, rgbThresholdOutput);

		// Step Find_Contours0:
		Mat findContoursInput = rgbThresholdOutput;
		boolean findContoursExternalOnly = true;
		findContours(findContoursInput, findContoursExternalOnly, findContoursOutput);
		if(!findContoursOutput().isEmpty()){
			closest = -1;
			maxSize = -1;
			for(int i = 0; i < findContoursOutput.size(); i++){
				Rect test = Imgproc.boundingRect(findContoursOutput().get(i));
				if( test.width > maxSize ){
					maxSize = test.area();
					closest = i;
				}

			}

			Rect r = Imgproc.boundingRect(findContoursOutput().get(closest));
			targetCenter = r.x + (r.width / 2);
			Imgproc.rectangle(
					source0,
					new Point(
							r.x, r.y),
					new Point(
							r.x + r.width,
							r.y + r.height),
					new Scalar(0, 255, 0), 4);
		}
		return source0;
	}
	public double getCenterX(){
		return targetCenter;
	}
	/**
	 * This method is a generated getter for the output of a RGB_Threshold.
	 * @return Mat output from RGB_Threshold.
	 */
	public Mat rgbThresholdOutput() {
		return rgbThresholdOutput;
	}

	/**
	 * This method is a generated getter for the output of a Find_Contours.
	 * @return ArrayList<MatOfPoint> output from Find_Contours.
	 */
	public ArrayList<MatOfPoint> findContoursOutput() {
		return findContoursOutput;
	}


	/**
	 * Segment an image based on color ranges.
	 * @param input The image on which to perform the RGB threshold.
	 * @param red The min and max red.
	 * @param green The min and max green.
	 * @param blue The min and max blue.
	 * @ output The image in which to store the output.
	 */
	private void rgbThreshold(Mat input, double[] red, double[] green, double[] blue,
		Mat out) {
		Imgproc.cvtColor(input, out, Imgproc.COLOR_BGR2RGB);
		Core.inRange(out, new Scalar(red[0], green[0], blue[0]),
			new Scalar(red[1], green[1], blue[1]), out);
	}

	/**
	 * Sets the values of pixels in a binary image to their distance to the nearest black pixel.
	 * @ input The image on which to perform the Distance Transform.
	 * @ type The Transform.
	 * @ maskSize the size of the mask.
	 * @ output The image in which to store the output.
	 */
	private void findContours(Mat input, boolean externalOnly,
		List<MatOfPoint> contours) {
		Mat hierarchy = new Mat();
		contours.clear();
		int mode;
		if (externalOnly) {
			mode = Imgproc.RETR_EXTERNAL;
		}
		else {
			mode = Imgproc.RETR_LIST;
		}
		int method = Imgproc.CHAIN_APPROX_SIMPLE;
		Imgproc.findContours(input, contours, hierarchy, mode, method);
	}




}
