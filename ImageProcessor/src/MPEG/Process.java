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
	
	public Matrix y;
	public Matrix cB;
	public Matrix cR;
	
	
	public void loadFrames(ActionEvent evt) {
	}
	
	
	public void rButtonPressed(ActionEvent evt) {	
		setImageView(getComponent(RED), this.currentImageView);
	}
	
	public void gButtonPressed(ActionEvent evt) {
		setImageView(getComponent(GREEN), this.currentImageView);
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
		Quantization(quantization_block_size);
	}
	
	public void  iquantizationButton(ActionEvent evt) {
		IQuantization(quantization_block_size);
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
				tempMat = colorTransform.transform(n, transformMatrix.getDctMatrix(n), tempMat);
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
				tempMat = colorTransform.inverseTransform(n, transformMatrix.getDctMatrix(n) , tempMat);
				outputMat = colorTransform.setMatrix(inputMat, tempMat,i,j,n);
			}
		}
		return outputMat;
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


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		loadOriginalImages();
		
		this.colorTransform = new ColorTransform(imagePlusFirst.getBufferedImage());
		this.colorTransform.getRGB();
		
		ToggleGroup group = new ToggleGroup();
		radioButtonFirst.setToggleGroup(group);
		radioButtonFirst.setId("First");
		radioButtonFirst.setSelected(true);
		radioButtonSecond.setToggleGroup(group);
		radioButtonSecond.setId("Second");
		
		qQuality = 50;
		QualityLabel.setText(String.valueOf(qQuality));
		QualitySlider.setValue(50);
		
		
		this.colorTransform.convertRgbToYcBcR();
		colorTransform.getQuantMatrix8Brightness(qQuality);
		colorTransform.getQuantMatrix8Color(qQuality);
		TransformMatrix a = new TransformMatrix();
		a.getDctMatrix(8);
	}
	
	
	private ColorTransform getColorTransform() throws Exception {
		RadioButton selectedRadioButton = (RadioButton) group.getSelectedToggle();
		if (selectedRadioButton == null) throw new Exception("Toggle button is not selected!");
		switch (selectedRadioButton.getId()) {
		case "First":
			return this.colorTransformFirst;
		case "Second":
			return this.colorTransformSecond;
		}
		return null
	}
	
 	private void loadOriginalImages() {
		this.imagePlusFirst = new ImagePlus("images/lena_std.jpg");
		this.imagePlusSecond = new ImagePlus("images/lena_std.jpg");
		setImageView(imagePlusFirst, this.currentImageView);
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
