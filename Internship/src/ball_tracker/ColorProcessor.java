package ball_tracker;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JSlider;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

/*
 * All the processing is done here. Isolate certain colors and detect the midpoints of the blobs.
 */
public class ColorProcessor {

	private Scalar min, max;
	private Mat img1, imghsv, imgbin;
	private Panel livePanel, binPanel;
	private VideoCapture webcam;
	private JSlider h_slider, s_slider, v_slider, h_slider2, s_slider2, v_slider2, blur_slider, erode_slider;
	private Rect obj_rect = new Rect(0,0,0,0);
	//	private int[] obj_arr = {0,0,0,0};
	private Mat erode_element;
	private int x,y,x2,y2,midX,midY;
	//	private MyListener myListener;
	private boolean foundBall = false;

	public ColorProcessor(VideoCapture webcam, boolean run, Panel livePanel, Panel binPanel, JSlider h_slider, 
			JSlider s_slider, JSlider v_slider, JSlider h_slider2, JSlider s_slider2, JSlider v_slider2, 
			JSlider blur_slider, JSlider erode_slider) {

		this.webcam = webcam;
		this.livePanel = livePanel;
		this.binPanel = binPanel;
		this.h_slider = h_slider;
		this.s_slider = s_slider;
		this.v_slider = v_slider;
		this.h_slider2 = h_slider2;
		this.s_slider2 = s_slider2;
		this.v_slider2 = v_slider2;
		this.blur_slider = blur_slider;
		this.erode_slider = erode_slider;
		//		this.myListener = myListener;
		img1 = new Mat();
		imghsv = new Mat();
		imgbin = new Mat();
		erode_element = new Mat();
	}

	public double[] process() {
		webcam.read(img1); //read one frame
		if(img1 != null && !img1.empty()) { //if a frame is captured
			Imgproc.cvtColor(img1, imghsv, Imgproc.COLOR_BGR2HSV); //convert BGR to HSV
			Imgproc.cvtColor(img1, imgbin, Imgproc.COLOR_BGR2GRAY); //convert BGR to grayscale
			Imgproc.blur(imghsv,imghsv,new Size(blur_slider.getValue(),blur_slider.getValue())); //BLER
			erode_element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2*erode_slider.getValue() + 1, 2*erode_slider.getValue() + 1), new Point(erode_slider.getValue(), erode_slider.getValue()));
			Imgproc.erode(imghsv,imghsv,erode_element); //EROAD
			Imgproc.dilate(imghsv,imghsv,erode_element); //DYLAIT

			min = new Scalar(h_slider.getValue(),s_slider.getValue(),v_slider.getValue(),0); //set value to slider value
			max = new Scalar(h_slider2.getValue(),s_slider2.getValue(),v_slider2.getValue(),0);

			Core.inRange(imghsv,min,max,imgbin); //imgbin becomes binary after inRange is called

			double[] point = findBall(imgbin,img1);
			//			findPlate(imgbin, img1);

			//show captured frame
			livePanel.setImageWithMat(img1);
			livePanel.repaint();
			binPanel.setImageWithMat(imgbin);
			binPanel.repaint();

			return point;				
		}
		return null;
	}

	public double[] findBall(Mat thresholdImage, Mat cameraFeed) {		
		Mat temp = new Mat();
		thresholdImage.copyTo(temp);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>(); //these two vectors needed for output of findContours
		MatOfInt4 hierarchy = new MatOfInt4();
		Imgproc.findContours(temp,contours,hierarchy,Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE );// retrieves external contours
		for( int i = 0; i< contours.size(); i++ ){
			Imgproc.drawContours(img1, contours, i, new Scalar(0,255,0), 2); //draw contours
		}

		if(contours.size() > 0) { //if contours vector is not empty, we have found some objects			
			foundBall = true;
			int index = findLargestContour(contours);
			obj_rect = Imgproc.boundingRect(contours.get(index)); //make bounding rect around largest vector
			x = obj_rect.x;
			y = obj_rect.y;
			x2 = obj_rect.x + obj_rect.width;
			y2 = obj_rect.y + obj_rect.height;
			//			obj_arr[0] = x; obj_arr[1] = y; obj_arr[2] = x2; obj_arr = y2;

			midX = (x + x2)/2;
			midY = (y + y2)/2;
			double[] obj = translatePoint(midX,midY);
			Imgproc.rectangle(cameraFeed, new Point(x, y), new Point(x2, y2), new Scalar(0,0,255),2); //draw rectangle around the object
			Imgproc.circle(cameraFeed, new Point(midX, midY), 4, new Scalar(255,255,0));
			Imgproc.putText(cameraFeed,"(" + obj[0] +","+ obj[1] + ")", new Point(x,y),2,1, new Scalar(255,0,0),2);

			return obj;
		} else {
			foundBall = false;
			return null;
		}
	}

	public void findPlate(Mat thresholdImage, Mat cameraFeed) {
		Mat temp = new Mat();
		thresholdImage.copyTo(temp);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>(); //these two vectors needed for output of findContours
		MatOfInt4 hierarchy = new MatOfInt4();
		Imgproc.findContours(temp,contours,hierarchy,Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE );// retrieves external contours

		if(contours.size() > 0) { 
			List<MatOfPoint> squareContours = getSquareContours(contours);
			System.out.println(squareContours.size());
			for( int i = 0; i < contours.size(); i++ ){
				Imgproc.drawContours(img1, contours, i, new Scalar(0,255,0), 2); //draw contours
			}

			x = obj_rect.x;
			y = obj_rect.y;
			x2 = obj_rect.x + obj_rect.width;
			y2 = obj_rect.y + obj_rect.height;
			//			obj_arr[0] = x; obj_arr[1] = y; obj_arr[2] = x2; obj_arr = y2;

			//		double[] obj = translatePoint(midX,midY);
			//		Imgproc.rectangle(cameraFeed, new Point(x, y), new Point(x2, y2), new Scalar(0,0,255),2); //draw rectangle around the object
			//		Imgproc.circle(cameraFeed, new Point(midX, midY), 4, new Scalar(255,255,0));
			//		Imgproc.putText(cameraFeed,"(" + obj[0] +","+ obj[1] + ")", new Point(x,y),2,1, new Scalar(255,0,0),2);
		}
	}

	public int findLargestContour(List<MatOfPoint> contours) {
		// Find max contour area
		double maxArea = 0;
		int generalIndex = 0;
		int contourIndex = 0;
		Iterator<MatOfPoint> each = contours.iterator();
		while (each.hasNext()) {
			MatOfPoint wrapper = each.next();
			double area = Imgproc.contourArea(wrapper);
			if (area > maxArea){
				maxArea = area;
				contourIndex = generalIndex;
			}
			generalIndex++;
		}
		return contourIndex;
	}

	public boolean isContourSquare(MatOfPoint thisContour) {
		Rect ret = null;

		MatOfPoint2f thisContour2f = new MatOfPoint2f();
		MatOfPoint approxContour = new MatOfPoint();
		MatOfPoint2f approxContour2f = new MatOfPoint2f();

		thisContour.convertTo(thisContour2f, CvType.CV_32FC2);

		Imgproc.approxPolyDP(thisContour2f, approxContour2f, 2, true);

		approxContour2f.convertTo(approxContour, CvType.CV_32S);

		if (approxContour.size().height == 4) {
			ret = Imgproc.boundingRect(approxContour);
		}

		return (ret != null);
	}

	public List<MatOfPoint> getSquareContours(List<MatOfPoint> contours) {
		List<MatOfPoint> squares = new ArrayList<MatOfPoint>();
		for (MatOfPoint c : contours) {
			if (isContourSquare(c)) {
				squares.add(c);
			}
		}
		return squares;
	}

	public double[] translatePoint(int x, int y) { //origin is at the bottom/middle of the frame
		double[] translated = {x - livePanel.getWidth()/2., livePanel.getHeight() - y};
		return translated;
	}

	public Mat getImg1() {
		return img1;
	}

	public boolean getFoundBall() {
		return foundBall;
	}
}