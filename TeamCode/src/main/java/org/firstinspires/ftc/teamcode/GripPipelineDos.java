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
import org.openftc.easyopencv.OpenCvPipeline;

/**
* GripPipelineDos class.
*
* <p>An OpenCV pipeline generated by GRIP.
*
* @author GRIP
*/
public class GripPipelineDos extends OpenCvPipeline {
	public double[] hsvThresholdHue={0,0};
	public double[] hsvThresholdSaturation={0,0};
	public double[] hsvThresholdValue={0,0};
	//Outputs
	private Mat hsvThresholdOutput = new Mat();
	private ArrayList<MatOfPoint> findContoursOutput = new ArrayList<MatOfPoint>();
	private ArrayList<MatOfPoint> filterContoursOutput = new ArrayList<MatOfPoint>();
	private double targetCenter;
	private double maxSize = -1;
	private int closest = -1;
	enum Stage
	{
		YCbCr_CHAN2,
		THRESHOLD,
		RAW_IMAGE,
	}

	private Stage stageToRenderToViewport = Stage.YCbCr_CHAN2;
	private Stage[] stages = Stage.values();

	@Override
	public void onViewportTapped()
	{
		/*
		 * Note that this method is invoked from the UI thread
		 * so whatever we do here, we must do quickly.
		 */

		int currentStageNum = stageToRenderToViewport.ordinal();

		int nextStageNum = currentStageNum + 1;

		if(nextStageNum >= stages.length)
		{
			nextStageNum = 0;
		}

		stageToRenderToViewport = stages[nextStageNum];
	}
	/**
	 * This is the primary method that runs the entire pipeline and updates the outputs.
	 */
	public Mat processFrame(Mat source0) {
		// Step HSV_Threshold0:
		Mat hsvThresholdInput = source0;
		hsvThresholdHue = new double[]{60.0, 100.0};
		//double[] hsvThresholdSaturation = {183.45323741007192, 255.0};
		//double[] hsvThresholdValue = {96.31294964028777, 255.0};
		hsvThresholdSaturation = new double[]{0, 255.0};
		hsvThresholdValue = new double[]{0, 255.0};
		hsvThreshold(hsvThresholdInput, hsvThresholdHue, hsvThresholdSaturation, hsvThresholdValue, hsvThresholdOutput);

		// Step Find_Contours0:
		Mat findContoursInput = hsvThresholdOutput;
		boolean findContoursExternalOnly = true;
		findContours(findContoursInput, findContoursExternalOnly, findContoursOutput);

		// Step Filter_Contours0:
		ArrayList<MatOfPoint> filterContoursContours = findContoursOutput;
		double filterContoursMinArea = 6500.0;
		double filterContoursMinPerimeter = 0;
		double filterContoursMinWidth = 0;
		double filterContoursMaxWidth = 1000;
		double filterContoursMinHeight = 0;
		double filterContoursMaxHeight = 1000;
		double[] filterContoursSolidity = {0, 100};
		double filterContoursMaxVertices = 1000000;
		double filterContoursMinVertices = 0;
		double filterContoursMinRatio = 0;
		double filterContoursMaxRatio = 1000;
		filterContours(filterContoursContours, filterContoursMinArea, filterContoursMinPerimeter, filterContoursMinWidth, filterContoursMaxWidth, filterContoursMinHeight, filterContoursMaxHeight, filterContoursSolidity, filterContoursMaxVertices, filterContoursMinVertices, filterContoursMinRatio, filterContoursMaxRatio, filterContoursOutput);
		if(!filterContoursOutput().isEmpty()){
			closest = -1;
			maxSize = -1;
			for(int i = 0; i < filterContoursOutput.size(); i++){
				Rect test = Imgproc.boundingRect(filterContoursOutput().get(i));
				if( test.width > maxSize ){
					maxSize = test.area();
					closest = i;
				}

			}

			Rect r = Imgproc.boundingRect(filterContoursOutput().get(closest));
			targetCenter = r.x + (r.width / 2);
			Imgproc.rectangle(
					source0,
					new Point(
							r.x, r.y),
					new Point(
							r.x + r.width,
							r.y + r.height),
					new Scalar(0, 255, 0), 4);
		}else{
			targetCenter=0;
		}
		switch (stageToRenderToViewport)
		{


			case THRESHOLD:
			{
				return hsvThresholdOutput;
			}



			case RAW_IMAGE:
			{
				return source0;
			}

			default:
			{
				return source0;
			}

	}}
	public double getCenterX(){
		return targetCenter;
	}
	public boolean haveTarget(){
		return !filterContoursOutput.isEmpty();
	}
	/**
	 * This method is a generated getter for the output of a HSV_Threshold.
	 * @return Mat output from HSV_Threshold.
	 */
	public Mat hsvThresholdOutput() {
		return hsvThresholdOutput;
	}

	/**
	 * This method is a generated getter for the output of a Find_Contours.
	 * @return ArrayList<MatOfPoint> output from Find_Contours.
	 */
	public ArrayList<MatOfPoint> findContoursOutput() {
		return findContoursOutput;
	}

	/**
	 * This method is a generated getter for the output of a Filter_Contours.
	 * @return ArrayList<MatOfPoint> output from Filter_Contours.
	 */
	public ArrayList<MatOfPoint> filterContoursOutput() {
		return filterContoursOutput;
	}


	/**
	 * Segment an image based on hue, saturation, and value ranges.
	 *
	 * @param input The image on which to perform the HSL threshold.
	 * @param hue The min and max hue
	 * @param sat The min and max saturation
	 * @param val The min and max value
	 *  The image in which to store the output.
	 */
	private void hsvThreshold(Mat input, double[] hue, double[] sat, double[] val,
	    Mat out) {
		Imgproc.cvtColor(input, out, Imgproc.COLOR_BGR2HSV);
		Core.inRange(out, new Scalar(hue[0], sat[0], val[0]),
			new Scalar(hue[1], sat[1], val[1]), out);
	}

	/**
	 * Sets the values of pixels in a binary image to their distance to the nearest black pixel.
	 * @param input The image on which to perform the Distance Transform.
	 *  type The Transform.
	 * maskSize the size of the mask.
	 * output The image in which to store the output.
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


	/**
	 * Filters out contours that do not meet certain criteria.
	 * @param inputContours is the input list of contours
	 * @param output is the the output list of contours
	 * @param minArea is the minimum area of a contour that will be kept
	 * @param minPerimeter is the minimum perimeter of a contour that will be kept
	 * @param minWidth minimum width of a contour
	 * @param maxWidth maximum width
	 * @param minHeight minimum height
	 * @param maxHeight maximimum height
	 *  the minimum and maximum solidity of a contour
	 * @param minVertexCount minimum vertex Count of the contours
	 * @param maxVertexCount maximum vertex Count
	 * @param minRatio minimum ratio of width to height
	 * @param maxRatio maximum ratio of width to height
	 */
	private void filterContours(List<MatOfPoint> inputContours, double minArea,
		double minPerimeter, double minWidth, double maxWidth, double minHeight, double
		maxHeight, double[] solidity, double maxVertexCount, double minVertexCount, double
		minRatio, double maxRatio, List<MatOfPoint> output) {
		final MatOfInt hull = new MatOfInt();
		output.clear();
		//operation
		for (int i = 0; i < inputContours.size(); i++) {
			final MatOfPoint contour = inputContours.get(i);
			final Rect bb = Imgproc.boundingRect(contour);
			if (bb.width < minWidth || bb.width > maxWidth) continue;
			if (bb.height < minHeight || bb.height > maxHeight) continue;
			final double area = Imgproc.contourArea(contour);
			if (area < minArea) continue;
			if (Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true) < minPerimeter) continue;
			Imgproc.convexHull(contour, hull);
			MatOfPoint mopHull = new MatOfPoint();
			mopHull.create((int) hull.size().height, 1, CvType.CV_32SC2);
			for (int j = 0; j < hull.size().height; j++) {
				int index = (int)hull.get(j, 0)[0];
				double[] point = new double[] { contour.get(index, 0)[0], contour.get(index, 0)[1]};
				mopHull.put(j, 0, point);
			}
			final double solid = 100 * area / Imgproc.contourArea(mopHull);
			if (solid < solidity[0] || solid > solidity[1]) continue;
			if (contour.rows() < minVertexCount || contour.rows() > maxVertexCount)	continue;
			final double ratio = bb.width / (double)bb.height;
			if (ratio < minRatio || ratio > maxRatio) continue;
			output.add(contour);
		}
	}




}

