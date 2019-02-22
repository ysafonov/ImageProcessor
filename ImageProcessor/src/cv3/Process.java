package cv3;

import java.awt.Button;
import java.awt.event.ActionListener;
import java.io.Console;
import java.net.URL;
import java.util.ResourceBundle;

import Jama.Matrix;
import ij.ImagePlus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

public class Process implements Initializable {

	private ImagePlus imagePlus;
	private ColorTransform colorTransform;
	private ColorTransform colorTransformOrig;
	private Quality quality;
	private int lastSample = B444;

	
	@FXML
	private Button button;
	
	@FXML
	private TextField MSE;
	
	@FXML
	private TextField PSNR;
	
	public static final int RED = 1;
	public static final int GREEN = 2;
	public static final int BLUE = 3;
	public static final int Y = 4;
	public static final int CB = 5;
	public static final int CR = 6;
	public static final int B444 = 7;
	public static final int B422 = 8;
	public static final int B420 = 9;
	public static final int B411 = 10;
	
	public void rButtonPressed(ActionEvent evt) {	
		
		getComponent(RED).show();
	}
	
	public void gButtonPressed(ActionEvent evt) {

		getComponent(GREEN).show();
	}
	
	public void bButtonPressed(ActionEvent evt) {

		getComponent(BLUE).show();
	}
	
	public void yButtonPressed(ActionEvent evt) {

		getComponent(Y).show();
	}
	
	public void cbButtonPressed(ActionEvent evt) {

		getComponent(CB).show();
	}
	
	public void crButtonPressed(ActionEvent evt) {

		getComponent(CR).show();
	}
	
	public void b444ButtonPressed(ActionEvent evt) {
		downSample(B444);
	}
	public void b422ButtonPressed(ActionEvent evt) {
		downSample(B422);
	}
	public void b420ButtonPressed(ActionEvent evt) {
		downSample(B420);
	}
	public void b411ButtonPressed(ActionEvent evt) {
		downSample(B411);
	}
	
	
	public void downSample(int opID) {
		switch (opID) {
		case B444:
			getComponent(Y).show();
			colorTransform.get444(colorTransform.getCb(), "cb").show();
			colorTransform.get444(colorTransform.getCr(), "cr").show();
			break;
		case B422:
			getComponent(Y).show();
			colorTransform.get422(colorTransform.getCb(), "cb").show();
			colorTransform.get422(colorTransform.getCr(), "cr").show();
			break;
		case B420:
			getComponent(Y).show();
			colorTransform.get420(colorTransform.getCb(), "cb").show();
			colorTransform.get420(colorTransform.getCr(), "cr").show();
			break;
		
		case B411:
			getComponent(Y).show();
			colorTransform.get411(colorTransform.getCb(), "cb").show();
			colorTransform.get411(colorTransform.getCr(), "cr").show();
			break;

		default:
			break;
		}
		lastSample = opID;
	}
	
	public void QualityButtonPressed(ActionEvent evt) {
		quality = new Quality();		
		double valueMSE = quality.getMse(colorTransformOrig.getRed(), colorTransform.getRed());
		valueMSE += quality.getMse(colorTransformOrig.getGreen(), colorTransform.getGreen());
		valueMSE += valueMSE + quality.getMse(colorTransformOrig.getBlue(), colorTransform.getBlue());
		valueMSE = valueMSE/3;
		MSE.setText(String.valueOf(valueMSE));
		
		double valuePSNR = quality.getPsnr(colorTransformOrig.getRed(), colorTransform.getRed());
		valuePSNR += quality.getPsnr(colorTransformOrig.getGreen(), colorTransform.getGreen());
		valuePSNR += valuePSNR + quality.getPsnr(colorTransformOrig.getBlue(), colorTransform.getBlue());
		valuePSNR = valuePSNR/3;
		PSNR.setText(String.valueOf(valuePSNR));
	}
	
	public void OverSampleButtonPressed(ActionEvent evt) {
		this.oversample(lastSample);
	}
	
	
	public ImagePlus getComponent(int component) {
		ImagePlus imagePlus = null;
		switch (component) {
		case RED:
			imagePlus = colorTransform.setImageFromRGB(colorTransform.getImageWidth(),
					colorTransform.getImageHeight(), colorTransform.getRed(), "RED");
			break;
			
		case GREEN:
			imagePlus = colorTransform.setImageFromRGB(colorTransform.getImageWidth(),
					colorTransform.getImageHeight(), colorTransform.getGreen(), "GREEN");
			break;
		case BLUE:
			imagePlus = colorTransform.setImageFromRGB(colorTransform.getImageWidth(),
					colorTransform.getImageHeight(), colorTransform.getBlue(), "BLUE");
			break;

		case Y:
			imagePlus = colorTransform.setImageFromRGB(colorTransform.getImageWidth(),
					colorTransform.getImageHeight(), colorTransform.getY(), "Y");
			break;
			
		case CB:
			imagePlus = colorTransform.setImageFromRGB(colorTransform.getImageWidth(),
					colorTransform.getImageHeight(), colorTransform.getCb(), "cb");
			break;
			
		case CR:
			imagePlus = colorTransform.setImageFromRGB(colorTransform.getImageWidth(),
					colorTransform.getImageHeight(), colorTransform.getCr(), "Cr");
			break;
		default:
			break;
		}
		return imagePlus;
	}
	
	public void oversample (int oversampleType){
		
		Matrix cB = new Matrix(colorTransform.getCb().getArray());
		Matrix cR = new Matrix(colorTransform.getCr().getArray());
		
		switch (oversampleType) {
		case B422:
			cB = colorTransform.overSample(cB);
			cR = colorTransform.overSample(cR);
			
			colorTransform.setCb(cB);
			colorTransform.setCr(cR);
			break;
		case B411:
			cB = colorTransform.overSample(cB);
			cB = colorTransform.overSample(cB);
			cR = colorTransform.overSample(cR);
			cR = colorTransform.overSample(cR);
			
			colorTransform.setCb(cB);
			colorTransform.setCr(cR);
			break;
		case B420:
			cB = colorTransform.overSample(cB);
			cR = colorTransform.overSample(cR);
			cB = cB.transpose();
			cR = cR.transpose();
			cB = colorTransform.overSample(cB);
			cR = colorTransform.overSample(cR);
			cB = cB.transpose();
			cR = cR.transpose();
			colorTransform.setCb(cB);
			colorTransform.setCr(cR);
			break;
		case B444:
		default:
			break;
		}
		getComponent(Y).show();
		colorTransform.get444(colorTransform.getCb(), "cb").show();
		colorTransform.get444(colorTransform.getCr(), "cr").show();
		colorTransform.convertYcBcRToRGB().show();
	}


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		loadOriginalImage();
		this.colorTransform.convertRgbToYcBcR();
		imagePlus.setTitle("Original image");
		imagePlus.show("Original image");
	}


	private void loadOriginalImage() {
		this.imagePlus = new ImagePlus("images/lena_std.jpg");
		this.colorTransformOrig = new ColorTransform(imagePlus.getBufferedImage());
		this.colorTransform = new ColorTransform(imagePlus.getBufferedImage());
		this.colorTransform.getRGB();
		this.colorTransformOrig.getRGB();
		
	}
}
