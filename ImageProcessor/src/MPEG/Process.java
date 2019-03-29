package MPEG;

import java.awt.Button;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;
import Jama.Matrix;
import ij.ImagePlus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;

public class Process implements Initializable {

	private ImagePlus imagePlusFirst;
	private ImagePlus imagePlusSecond;
	private ToggleGroup group;
	
	private ColorTransform colorTransformFirst;
	private ColorTransform colorTransformSecond;
	
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
	
	@FXML
	private RadioButton radioButtonFirst;
	
	@FXML
	private RadioButton radioButtonSecond;
	
	@FXML
	private ImageView currentImageView;
	
	public static final int quantization_block_size = 8;
	public static final int RED = 1;
	public static final int GREEN = 2;
	public static final int BLUE = 3;
	public static final int Y = 4;
	public static final int CB = 5;
	public static final int CR = 6;
	
	public Matrix DCTmatrixY;
	public Matrix DCTmatrixcB;
	public Matrix DCTmatrixcR;
	
	public int qQuality;

	private TransformMatrix transformMatrix = new TransformMatrix();
	
	
	
	
	public void loadFrames(ActionEvent evt) {
	}
	
	
	public void rButtonPressed(ActionEvent evt) {	
		setImageView(getComponent(RED), this.currentImageView);
	}
	
	public void gButtonPressed(ActionEvent evt) {
		setImageView(getComponent(GREEN), this.currentImageView);
	}
	
	public void DpcmButtonPressed(ActionEvent event)
	{
	
	}


	public void bButtonPressed(ActionEvent evt) {
		setImageView(getComponent(BLUE), this.currentImageView);
	}
	
	public void yButtonPressed(ActionEvent evt) {
		setImageView(getComponent(Y), this.currentImageView);
	}
	
	public void cbButtonPressed(ActionEvent evt) {
		setImageView(getComponent(CB), this.currentImageView);
	}
	
	
	public void crButtonPressed(ActionEvent evt) {
		setImageView(getComponent(CR), this.currentImageView);
	}
	
	public void quantizationButton(ActionEvent evt) {
		// Quantization(quantization_block_size);
	}
	
	public void  iquantizationButton(ActionEvent evt) {
		// IQuantization(quantization_block_size);
	}
	
	
	public void dctButton(ActionEvent evt) {
		
	}
	public void idctButton(ActionEvent evt) {
		
	}

	
	
	public void processFirstIamge(ActionEvent evt) {
		Quantization(quantization_block_size, this.colorTransformFirst);
		setImageView(colorTransformFirst.convertYcBcRToRGB(), this.currentImageView);
	}
	
	public void  processSecondIamge(ActionEvent evt) {
		Quantization(quantization_block_size, this.colorTransformFirst);
		IQuantization(quantization_block_size, this.colorTransformFirst);
	}
	
	public void subtraction (ColorTransform first, ColorTransform second) {
		
		Matrix y_1 = new Matrix(first.getY().getArray());
		Matrix cB_1 = new Matrix(first.getCb().getArray());
		Matrix cR_1 = new Matrix(first.getCr().getArray());
		
		Matrix y_2 = new Matrix(second.getY().getArray());
		Matrix cB_2 = new Matrix(second.getCb().getArray());
		Matrix cR_2 = new Matrix(second.getCr().getArray());
		
		
	
	}
	
	
	
	private void Quantization (int n, ColorTransform colorTransform)
	{
		Matrix y = new Matrix(colorTransform.getY().getArray());
		Matrix cB = new Matrix(colorTransform.getCb().getArray());
		Matrix cR = new Matrix(colorTransform.getCr().getArray());
		
		y = QuantizationCycle(y,colorTransform.getQuantMatrix8Brightness(qQuality),n);
		cB = QuantizationCycle(cB,colorTransform.getQuantMatrix8Color(qQuality),n);
		cR = QuantizationCycle(cR,colorTransform.getQuantMatrix8Color(qQuality),n);
		setYcBcR(y, cB, cR, colorTransform);
	}

	public void IQuantization(int n, ColorTransform colorTransform)
	{		
		Matrix y = new Matrix(colorTransform.getY().getArray());
		Matrix cB = new Matrix(colorTransform.getCb().getArray());
		Matrix cR = new Matrix(colorTransform.getCr().getArray());
		
		y = IQuantizationCycle(y,colorTransform.getQuantMatrix8Brightness(qQuality),n);
		cB = IQuantizationCycle(cB,colorTransform.getQuantMatrix8Color(qQuality),n);
		cR = IQuantizationCycle(cR,colorTransform.getQuantMatrix8Color(qQuality),n);
		setYcBcR(y, cB, cR, colorTransform);
	}
	
	public void setYcBcR(Matrix y, Matrix cB, Matrix cR, ColorTransform colorTransform) {
		colorTransform.setY(y);
		colorTransform.setCb(cB);
		colorTransform.setCr(cR);
	}
	
	public void dctButton(ColorTransform colorTransform)
	{		
		
		Matrix y = new Matrix(colorTransform.getY().getArray());
		Matrix cB = new Matrix(colorTransform.getCb().getArray());
		Matrix cR = new Matrix(colorTransform.getCr().getArray());
		
		int size = y.getColumnDimension();
		Matrix A = transformMatrix.getDctMatrix(size);
		
		DCTmatrixY = colorTransform.transform(size, A, y);
		DCTmatrixcB = colorTransform.transform(size, A, cB);
		DCTmatrixcR = colorTransform.transform(size, A, cR);	
	}
	
	public void idctButton(ColorTransform colorTransform)
	{		
		// int size = y.getColumnDimension();
		// Matrix B = transformMatrix.getDctMatrix(size);
		// Matrix y = colorTransform.inverseTransform(size, B , DCTmatrixY);
		// Matrix cB = colorTransform.inverseTransform(size, B, DCTmatrixcB);
		//  cR = colorTransform.inverseTransform(size, B, DCTmatrixcR);	
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
		ColorTransform tmp = getColorTransform();
		tmp.getQuantMatrix8Brightness(qQuality);
		tmp.getQuantMatrix8Color(qQuality);
		
	}
	
	
	private Matrix QuantizationCycle(Matrix inputMat, Matrix quantMat , int n)
	{
		Matrix tempMat = new Matrix(n,n);
		Matrix outputMat = new Matrix(inputMat.getRowDimension(),inputMat.getColumnDimension());
		ColorTransform tmp = getColorTransform();
		
		for(int i = 0; i < inputMat.getColumnDimension(); i = i+n)
		{
			for(int j = 0; j < inputMat.getRowDimension(); j = j+n)
			{
				tempMat = tmp.getMatrix(inputMat,i,j,n);
				tempMat = tmp.transform(n, transformMatrix.getDctMatrix(n), tempMat);
				tempMat = tempMat.arrayRightDivide(quantMat);
				outputMat = tmp.nSamplesTogether(inputMat,tempMat,i,j,n);
			}
		}
		return outputMat;
	}
	


	private Matrix IQuantizationCycle(Matrix inputMat, Matrix quantMat, int n)
	{
		Matrix tempMat = new Matrix(n,n);
		Matrix outputMat = new Matrix(inputMat.getColumnDimension(),inputMat.getColumnDimension());
		ColorTransform tmp = getColorTransform();
		
		for(int i = 0; i < inputMat.getColumnDimension(); i = i+n)
		{
			for(int j = 0; j < inputMat.getRowDimension(); j = j+n)
			{
				tempMat = tmp.getMatrix(inputMat,i,j,n);
				tempMat = tempMat.arrayTimes(quantMat);
				tempMat = tmp.inverseTransform(n, transformMatrix.getDctMatrix(n) , tempMat);
				outputMat = tmp.setMatrix(inputMat, tempMat,i,j,n);
			}
		}
		return outputMat;
	}
	
	
	public ImagePlus getComponent(int component) {
		ImagePlus imagePlus = null;
		ColorTransform tmp = this.getColorTransform();
		switch (component) {
		case RED:
			imagePlus = tmp.setImageFromRGB(tmp.getImageWidth(),
					tmp.getImageHeight(), tmp.getRed(), "RED");
			break;
			
		case GREEN:
			imagePlus = tmp.setImageFromRGB(tmp.getImageWidth(),
					tmp.getImageHeight(), tmp.getGreen(), "GREEN");
			break;
		case BLUE:
			imagePlus = tmp.setImageFromRGB(tmp.getImageWidth(),
					tmp.getImageHeight(), tmp.getBlue(), "BLUE");
			break;

		case Y:
			imagePlus = tmp.setImageFromRGB(tmp.getImageWidth(),
					tmp.getImageHeight(), tmp.getY(), "Y");
			break;
			
		case CB:
			imagePlus = tmp.setImageFromRGB(tmp.getImageWidth(),
					tmp.getImageHeight(), tmp.getCb(), "cb");
			break;
			
		case CR:
			imagePlus = tmp.setImageFromRGB(tmp.getImageWidth(),
					tmp.getImageHeight(), tmp.getCr(), "Cr");
			break;
		default:
			break;
		}
		return imagePlus;
	}	


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		loadOriginalImages();
		setRadioButton();
		setQuality();
		colorTransformFirst.getQuantMatrix8Brightness(qQuality);
		colorTransformFirst.getQuantMatrix8Color(qQuality);
		// TransformMatrix a = new TransformMatrix();
		// a.getDctMatrix(8);
	}
	
	
 	private void loadOriginalImages() {
		this.imagePlusFirst = new ImagePlus("images/pomaly.jpg");
		this.imagePlusSecond = new ImagePlus("images/pomaly2.jpg");
		setImageView(imagePlusFirst, this.currentImageView);
		this.colorTransformFirst = new ColorTransform(imagePlusFirst.getBufferedImage());
		this.colorTransformSecond = new ColorTransform(imagePlusSecond.getBufferedImage());
		this.colorTransformFirst.getRGB();
		this.colorTransformSecond.getRGB();
		this.colorTransformFirst.convertRgbToYcBcR();
		this.colorTransformSecond.convertRgbToYcBcR();
	}
 	
 	private void setRadioButton () {
 		group = new ToggleGroup();
		radioButtonFirst.setToggleGroup(group);
		radioButtonFirst.setId("First");
		radioButtonFirst.setSelected(true);
		radioButtonSecond.setToggleGroup(group);
		radioButtonSecond.setId("Second");
		
		radioButtonFirst.setOnAction((ActionEvent e) -> {
			setImageView(imagePlusFirst, this.currentImageView);
		});
		
		radioButtonSecond.setOnAction((ActionEvent e) -> {
			setImageView(imagePlusSecond, this.currentImageView);
		});
 	}
 	
	private void setQuality() {
		qQuality = 50;
		QualityLabel.setText(String.valueOf(qQuality));
		QualitySlider.setValue(50);
	}


	
	
	
	
	
	
	
	
	
	
	
	
	private ColorTransform getColorTransform()  {
		RadioButton selectedRadioButton = (RadioButton) group.getSelectedToggle();
		if (selectedRadioButton == null) return null;
		switch (selectedRadioButton.getId()) {
		case "First":
			return this.colorTransformFirst;
		case "Second":
			return this.colorTransformSecond;
		}
		return null;
	}

	
	private void setImageView(ImagePlus imagePlus, ImageView imageView) {
		BufferedImage bf = imagePlus.getBufferedImage();
		WritableImage wr = null;
        if (bf != null) {
            wr = new WritableImage(bf.getWidth(), bf.getHeight());
            PixelWriter pw = wr.getPixelWriter();
            for (int x = 0; x < bf.getWidth(); x++) {
                for (int y = 0; y < bf.getHeight(); y++) {
                    pw.setArgb(x, y, bf.getRGB(x, y));
                }
            }
        }
        imageView.setImage(wr);
	}
}
