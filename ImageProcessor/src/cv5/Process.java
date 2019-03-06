package cv5;

import java.awt.Button;

import java.net.URL;
import java.util.ResourceBundle;

import Jama.Matrix;
import ij.ImagePlus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

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
	
	@FXML
	private Slider QualitySlider;
	
	@FXML
	private Label QualityLabel;
	
	
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

	public Matrix DCTmatrixY;
	public Matrix DCTmatrixcB;
	public Matrix DCTmatrixcR;
	public Matrix WHTmatrixY;
	public Matrix WHTmatrixcB;
	public Matrix WHTmatrixcR;
	
	public int qQuality;

	private TransformMatrix transformMatrix = new TransformMatrix();
	
	public Matrix y;
	public Matrix cB;
	public Matrix cR;
	
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
	
	
	public void quantizationButton(ActionEvent evt) {
		Quantization(8);
	}
	
	public void  iquantizationButton(ActionEvent evt) {
		IQuantization(8);
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
	
	
	private void Quantization (int n)
	{
		y = new Matrix(colorTransform.getY().getArray());
		cB = new Matrix(colorTransform.getCb().getArray());
		cR = new Matrix(colorTransform.getCr().getArray());
		
		y = QuantizationCycle(y,colorTransform.getQuantMatrix8Brightness(qQuality),n);
		cB = QuantizationCycle(cB,colorTransform.getQuantMatrix8Color(qQuality),n);
		cR = QuantizationCycle(cR,colorTransform.getQuantMatrix8Color(qQuality),n);
	}

	public void IQuantization(int n)
	{		
		y = IQuantizationCycle(y,colorTransform.getQuantMatrix8Brightness(qQuality),n);
		cB = IQuantizationCycle(cB,colorTransform.getQuantMatrix8Color(qQuality),n);
		cR = IQuantizationCycle(cR,colorTransform.getQuantMatrix8Color(qQuality),n);
		
		colorTransform.setY(y);
		colorTransform.setCb(cB);
		colorTransform.setCr(cR);
		colorTransform.convertYcBcRToRGB().show();
	}
	
	public void qualitySliderMouseDrag(MouseEvent event)
	{
		qQuality = (int) Math.round(QualitySlider.getValue());
		QualityLabel.setText(String.valueOf(qQuality));
	}
	
	public void qualitySliderMouseRelease(MouseEvent event)
	{
		qQuality = (int) Math.round(QualitySlider.getValue());
		QualityLabel.setText(String.valueOf(qQuality));
		colorTransform.getQuantMatrix8Brightness(qQuality);
		colorTransform.getQuantMatrix8Color(qQuality);
		
	}
	
	
	private Matrix QuantizationCycle(Matrix inputMat, Matrix quantMat , int n)
	{
		Matrix tempMat = new Matrix(n,n);
		Matrix outputMat = new Matrix(inputMat.getColumnDimension(),inputMat.getColumnDimension());
		
		for(int i = 0; i < inputMat.getColumnDimension(); i = i+n)
		{
			for(int j = 0; j < inputMat.getRowDimension(); j = j+n)
			{
				tempMat = colorTransform.getMatrix(inputMat,i,j,n);
				tempMat = tempMat.arrayRightDivide(quantMat);
				outputMat = colorTransform.nSamplesTogether(inputMat,tempMat,i,j,n);
			}
		}
		return outputMat;
	}
	


	private Matrix IQuantizationCycle(Matrix inputMat, Matrix quantMat, int n)
	{
		Matrix tempMat = new Matrix(n,n);
		Matrix outputMat = new Matrix(inputMat.getColumnDimension(),inputMat.getColumnDimension());
		
		for(int i = 0; i < inputMat.getColumnDimension(); i = i+n)
		{
			for(int j = 0; j < inputMat.getRowDimension(); j = j+n)
			{
				tempMat = colorTransform.getMatrix(inputMat,i,j,n);
				tempMat = tempMat.arrayTimes(quantMat);
				outputMat = colorTransform.setMatrix(inputMat, tempMat,i,j,n);
			}
		}
		return outputMat;
	}
	
	
	public void downSample(int opID) {
		y = new Matrix(colorTransform.getY().getArray());
		cB = new Matrix(colorTransform.getCb().getArray());
		cR = new Matrix(colorTransform.getCr().getArray());
		
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
		valueMSE = valueMSE/3.0;
		MSE.setText(Double.toString(valueMSE));
		
		double valuePSNR = quality.getPsnr(colorTransformOrig.getRed(), colorTransform.getRed());
		valuePSNR += quality.getPsnr(colorTransformOrig.getGreen(), colorTransform.getGreen());
		valuePSNR += valuePSNR + quality.getPsnr(colorTransformOrig.getBlue(), colorTransform.getBlue());
		valuePSNR = valuePSNR/3.0;
		PSNR.setText(Double.toString(valuePSNR));
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
	
	
	
	
	
	public void dctButton()
	{		
		
		y = new Matrix(colorTransform.getY().getArray());
		cB = new Matrix(colorTransform.getCb().getArray());
		cR = new Matrix(colorTransform.getCr().getArray());
		
		int size = y.getColumnDimension();
		Matrix A = transformMatrix.getDctMatrix(size);
		
		DCTmatrixY = colorTransform.transform(size, A, y);
		DCTmatrixcB = colorTransform.transform(size, A, cB);
		DCTmatrixcR = colorTransform.transform(size, A, cR);	
	}
	
	public void idctButton()
	{		
		int size = y.getColumnDimension();
		Matrix B = transformMatrix.getDctMatrix(size);
		y = colorTransform.inverseTransform(size, B , DCTmatrixY);
		cB = colorTransform.inverseTransform(size, B, DCTmatrixcB);
		cR = colorTransform.inverseTransform(size, B, DCTmatrixcR);	
	}
	
	
	
	public void whtButton()
	{	
		y = new Matrix(colorTransform.getY().getArray());
		cB = new Matrix(colorTransform.getCb().getArray());
		cR = new Matrix(colorTransform.getCr().getArray());
		
		int size = y.getColumnDimension();
		Matrix A = transformMatrix.getWhtMatrix(size);

		
		DCTmatrixY = colorTransform.transform(size, A, y);
		DCTmatrixcB = colorTransform.transform(size, A, cB);
		DCTmatrixcR = colorTransform.transform(size, A, cR);	
	}
	
	public void iwhtButton()
	{		
		int size = y.getColumnDimension();
		Matrix H = transformMatrix.getWhtMatrix(size);
		y = colorTransform.inverseTransform(size, H, DCTmatrixY);
		cB = colorTransform.inverseTransform(size, H, DCTmatrixcB);
		cR = colorTransform.inverseTransform(size, H, DCTmatrixcR);	
	}
	
	
	
	public void show() {
		colorTransform.setY(y);
		colorTransform.setCb(cB);
		colorTransform.setCr(cR);
		colorTransform.convertYcBcRToRGB().show();
	}


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		loadOriginalImage();
		this.colorTransform.convertRgbToYcBcR();
		imagePlus.setTitle("Original image");
		imagePlus.show("Original image");
		qQuality = 50;
		QualityLabel.setText(String.valueOf(qQuality));
		QualitySlider.setValue(50);
		colorTransform.getQuantMatrix8Brightness(qQuality);
		colorTransform.getQuantMatrix8Color(qQuality);
	
	}


	private void loadOriginalImage() {
		this.imagePlus = new ImagePlus("images/lena_std.jpg");
		this.colorTransformOrig = new ColorTransform(imagePlus.getBufferedImage());
		this.colorTransform = new ColorTransform(imagePlus.getBufferedImage());
		this.colorTransform.getRGB();
		this.colorTransformOrig.getRGB();
		TransformMatrix a = new TransformMatrix();
		a.getDctMatrix(8);
		
	}
}
