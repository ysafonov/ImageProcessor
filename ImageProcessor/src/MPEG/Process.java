package MPEG;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;

import Jama.Matrix;
import ij.ImagePlus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;

/**
 * This java class contains main controller's functions.
 * 
 * @author Yehor Safonov; id: 185942
 */

public class Process implements Initializable {

	private ImagePlus imagePlusFirst;
	private ImagePlus imagePlusSecond;
	private ToggleGroup groupShowImage;
	private ToggleGroup sizeOfBlock;
	private ToggleGroup searchTechniqie;
	private ToggleGroup groupShowImageDecoder;

	private ColorTransform colorTransformFirst;
	private ColorTransform colorTransformSecond;
	private ColorTransform colorTransformBuffer;
	private ColorTransform colorTransformFirstToSend;
	private ColorTransform colorTransformDecoderBuffer;

	@FXML
	private TextField MSE;

	@FXML
	private TextField PSNR;

	@FXML
	private Slider QualitySlider;

	@FXML
	private Label QualityLabel;

	@FXML
	private Label sadOriginal;

	@FXML
	private Label sadPrediction;

	@FXML
	private Button buttonY;

	@FXML
	private Button buttonCb;

	@FXML
	private Button buttonCr;

	@FXML
	private RadioButton radioButtonFirst;

	@FXML
	private RadioButton radioButtonSecond;

	@FXML
	private RadioButton radioButtonDecoderFirst;

	@FXML
	private RadioButton radioButtonDecoderSecond;

	@FXML
	private RadioButton size2x2;

	@FXML
	private RadioButton size4x4;

	@FXML
	private RadioButton size8x8;

	@FXML
	private RadioButton size16x16;

	@FXML
	private RadioButton fullSearch;

	@FXML
	private RadioButton halfSearch;

	@FXML
	private RadioButton treeSearch;

	@FXML
	private RadioButton oneSeach;

	@FXML
	private ImageView currentImageView;

	@FXML
	private ImageView decoderImageView;

	public static int quantization_block_size = 8;
	public static int size_of_block = 16;

	public static final int RED = 1;
	public static final int GREEN = 2;
	public static final int BLUE = 3;
	public static final int Y = 4;
	public static final int CB = 5;
	public static final int CR = 6;

	public Matrix DCTmatrixY;
	public Matrix DCTmatrixcB;
	public Matrix DCTmatrixcR;
	public Matrix extendMatrix;
	public Matrix movement;
	public Matrix[] odhad;

	public int qQuality;
	public boolean isCustomView = false;
	public boolean isDecoderView = false;

	private TransformMatrix transformMatrix = new TransformMatrix();

	public void rButtonPressed(ActionEvent evt) {
		setImageView(getComponent(RED, getColorTransform()), this.currentImageView);
	}

	public void gButtonPressed(ActionEvent evt) {
		setImageView(getComponent(GREEN, getColorTransform()), this.currentImageView);
	}

	public void bButtonPressed(ActionEvent evt) {
		setImageView(getComponent(BLUE, getColorTransform()), this.currentImageView);
	}

	public void yButtonPressed(ActionEvent evt) {
		System.out.println();
		setImageView(getComponent(Y, getColorTransform()),
				((Control) evt.getSource()).getId() == null ? this.currentImageView : this.decoderImageView);
	}

	public void cbButtonPressed(ActionEvent evt) {
		setImageView(getComponent(CB, getColorTransform()),
				((Control) evt.getSource()).getId() == null ? this.currentImageView : this.decoderImageView);
	}

	public void crButtonPressed(ActionEvent evt) {
		setImageView(getComponent(CR, getColorTransform()),
				((Control) evt.getSource()).getId() == null ? this.currentImageView : this.decoderImageView);
	}

	public void dpcmWithoutPrediction(ActionEvent evt) {

		isCustomView = true;
		// Zpracovani prvniho snimku jeho kvantizace a inverzni kvantizace pro
		// nasledujici odhad pohybu

		Quantization(quantization_block_size, this.colorTransformFirst);
		setImageView(colorTransformFirst.convertYcBcRToRGB(), this.currentImageView);
		IQuantization(quantization_block_size, this.colorTransformFirst);

		Matrix subY = HelperClass.dpcmToShow(new Matrix(colorTransformFirst.getY().getArray()),
				new Matrix(colorTransformSecond.getY().getArray()));
		Matrix subCb = HelperClass.dpcmToShow(new Matrix(colorTransformFirst.getCb().getArray()),
				new Matrix(colorTransformSecond.getCb().getArray()));
		Matrix subCr = HelperClass.dpcmToShow(new Matrix(colorTransformFirst.getCr().getArray()),
				new Matrix(colorTransformSecond.getCr().getArray()));

		colorTransformBuffer.setY(subY);
		colorTransformBuffer.setCb(subCb);
		colorTransformBuffer.setCr(subCr);

		setImageView(
				colorTransformBuffer.setImageFromRGB(colorTransformBuffer.getImageWidth(),
						colorTransformBuffer.getImageHeight(), colorTransformBuffer.getY(), "Y"),
				this.currentImageView);
	}

	public void extendMatrix(ActionEvent evt) {

		this.extendMatrix = new Matrix(colorTransformFirst.getY().getArray());
		this.extendMatrix = HelperClass.extendMatrix(extendMatrix, size_of_block);
		System.out.println(
				"==================================== EXTENDED MATRIX =========================================");
		extendMatrix.print(1, 0);
		System.out.println(
				"================================== END EXTENDED MATRIX =======================================");

	}

	public void movementVectors(ActionEvent evt) {

		// Movement prediction
		Matrix second = new Matrix(colorTransformSecond.getY().getArray());
		if (isHalfPixelActive())
			extendMatrix = new Matrix(colorTransformFirst.getY().getArray());
		movement = this.applySearchTechnique(extendMatrix, second, size_of_block);
		System.out.println(
				"\n==================================== MOVEMENT VECTORS =========================================");
		movement.print(1, 0);
		System.out.println(
				"================================== END MOVEMENT VECTORS =======================================");

	}

	public void movementPrediction(ActionEvent evt) {
		isCustomView = true;
		odhad = isHalfPixelActive() ? HalfHelperClass.predictFromVectorsHalfPixel(size_of_block, movement,
				new Matrix(colorTransformFirst.getY().getArray()), new Matrix(colorTransformFirst.getCb().getArray()),
				new Matrix(colorTransformFirst.getCr().getArray()))
				: HelperClass.predictFromVectors(size_of_block, movement,
						new Matrix(colorTransformFirst.getY().getArray()),
						new Matrix(colorTransformFirst.getCb().getArray()),
						new Matrix(colorTransformFirst.getCr().getArray()));

		// Pocitani SAD a zobrazeni v GUI
		int predicted = (int) HelperClass.sumOfAllValues(colorTransformSecond.getY().minus(odhad[0]));
		int original = (int) HelperClass.sumOfAllValues(colorTransformSecond.getY().minus(colorTransformFirst.getY()));
		System.out.println(
				"\n==================================== SAD PREDICTION =========================================");
		System.out.println("Second - Odhad: " + predicted);
		System.out.println("Second - First: " + original);
		System.out.println(
				"================================== END SAD PREDICTION =======================================");
		sadPrediction.setText(Integer.toString(predicted));
		sadOriginal.setText(Integer.toString(original));

		// Zobrazeni odhadnuteho obrazku
		colorTransformBuffer = new ColorTransform(imagePlusSecond.getBufferedImage());
		colorTransformBuffer.setY(odhad[0]);
		colorTransformBuffer.setCb(odhad[1]);
		colorTransformBuffer.setCr(odhad[2]);
		setImageView(colorTransformBuffer.convertYcBcRToRGB(), this.currentImageView);
	}

	public void dpcmAfterPrediction(ActionEvent evt) {
		// Odecitani
		Matrix subY = HelperClass.dpcmToShow(new Matrix(colorTransformFirst.getY().getArray()), odhad[0]);
		Matrix subCb = HelperClass.dpcmToShow(new Matrix(colorTransformFirst.getCb().getArray()), odhad[1]);
		Matrix subCr = HelperClass.dpcmToShow(new Matrix(colorTransformFirst.getCr().getArray()), odhad[2]);

		// Zobrazeni po rozdilu kvantovaneho
		colorTransformBuffer.setY(subY);
		colorTransformBuffer.setCb(subCb);
		colorTransformBuffer.setCr(subCr);
		setImageView(colorTransformBuffer.convertYcBcRToRGB(), this.currentImageView);
	}

	public void sendToDecoder(ActionEvent evt) {
		isDecoderView = false;
		Quantization(quantization_block_size, this.colorTransformFirstToSend);
		Quantization(quantization_block_size, colorTransformBuffer);
		setImageView(colorTransformBuffer.convertYcBcRToRGB(), this.currentImageView);

	}

	public void decondeFirstImage(ActionEvent evt) {
		isDecoderView = true;
		IQuantization(quantization_block_size, this.colorTransformFirstToSend);
		setImageView(this.colorTransformFirstToSend.convertYcBcRToRGB(), this.decoderImageView);
		radioButtonDecoderFirst.setDisable(false);
		radioButtonDecoderFirst.setSelected(true);

		buttonCb.setDisable(false);
		buttonY.setDisable(false);
		buttonCr.setDisable(false);
	}

	public void decondeSecondImage(ActionEvent evt) {
		isDecoderView = true;
		// Vypocitani odhadu na strane decoderu
		Matrix[] odhadDecoderu = isHalfPixelActive() ? HalfHelperClass.predictFromVectorsHalfPixel(size_of_block,
				movement, new Matrix(colorTransformFirst.getY().getArray()),
				new Matrix(colorTransformFirst.getCb().getArray()), new Matrix(colorTransformFirst.getCr().getArray()))
				: HelperClass.predictFromVectors(size_of_block, movement,
						new Matrix(colorTransformFirst.getY().getArray()),
						new Matrix(colorTransformFirst.getCb().getArray()),
						new Matrix(colorTransformFirst.getCr().getArray()));

		// Pricitani
		Matrix sumY = HelperClass.summarization(colorTransformDecoderBuffer.getY(), odhadDecoderu[0]);
		Matrix sumCb = HelperClass.summarization(colorTransformDecoderBuffer.getCb(), odhadDecoderu[1]);
		Matrix sumCr = HelperClass.summarization(colorTransformDecoderBuffer.getCr(), odhadDecoderu[2]);

		// Zobrazeni po rozdilu kvantovaneho
		colorTransformBuffer.setY(sumY);
		colorTransformBuffer.setCb(sumCb);
		colorTransformBuffer.setCr(sumCr);
		setImageView(colorTransformBuffer.convertYcBcRToRGB(), this.decoderImageView);
		radioButtonDecoderSecond.setDisable(false);
		radioButtonDecoderSecond.setSelected(true);

	}

	private void Quantization(int n, ColorTransform colorTransform) {
		Matrix y = new Matrix(colorTransform.getY().getArray());
		Matrix cB = new Matrix(colorTransform.getCb().getArray());
		Matrix cR = new Matrix(colorTransform.getCr().getArray());

		y = QuantizationCycle(y, colorTransform.getQuantMatrix8Brightness(qQuality), n);
		cB = QuantizationCycle(cB, colorTransform.getQuantMatrix8Color(qQuality), n);
		cR = QuantizationCycle(cR, colorTransform.getQuantMatrix8Color(qQuality), n);
		setYcBcR(y, cB, cR, colorTransform);
	}

	public void IQuantization(int n, ColorTransform colorTransform) {
		Matrix y = new Matrix(colorTransform.getY().getArray());
		Matrix cB = new Matrix(colorTransform.getCb().getArray());
		Matrix cR = new Matrix(colorTransform.getCr().getArray());

		y = IQuantizationCycle(y, colorTransform.getQuantMatrix8Brightness(qQuality), n);
		cB = IQuantizationCycle(cB, colorTransform.getQuantMatrix8Color(qQuality), n);
		cR = IQuantizationCycle(cR, colorTransform.getQuantMatrix8Color(qQuality), n);
		setYcBcR(y, cB, cR, colorTransform);
	}

	public void setYcBcR(Matrix y, Matrix cB, Matrix cR, ColorTransform colorTransform) {
		colorTransform.setY(y);
		colorTransform.setCb(cB);
		colorTransform.setCr(cR);
	}

	public void dctButton(ColorTransform colorTransform) {

		Matrix y = new Matrix(colorTransform.getY().getArray());
		Matrix cB = new Matrix(colorTransform.getCb().getArray());
		Matrix cR = new Matrix(colorTransform.getCr().getArray());

		int size = y.getColumnDimension();
		Matrix A = transformMatrix.getDctMatrix(size);

		DCTmatrixY = colorTransform.transform(size, A, y);
		DCTmatrixcB = colorTransform.transform(size, A, cB);
		DCTmatrixcR = colorTransform.transform(size, A, cR);
	}

	public void qualitySliderMouseDrag(MouseEvent event) {
		qQuality = (int) Math.round(QualitySlider.getValue());
		QualityLabel.setText(String.valueOf(qQuality));
	}

	public void qualitySliderMouseRelease(MouseEvent event) {
		qQuality = (int) Math.round(QualitySlider.getValue());
		QualityLabel.setText(String.valueOf(qQuality));
		ColorTransform tmp = getColorTransform();
		tmp.getQuantMatrix8Brightness(qQuality);
		tmp.getQuantMatrix8Color(qQuality);

	}

	private Matrix QuantizationCycle(Matrix inputMat, Matrix quantMat, int n) {
		Matrix tempMat = new Matrix(n, n);
		Matrix outputMat = new Matrix(inputMat.getRowDimension(), inputMat.getColumnDimension());
		ColorTransform tmp = getColorTransform();

		for (int i = 0; i < inputMat.getColumnDimension(); i = i + n) {
			for (int j = 0; j < inputMat.getRowDimension(); j = j + n) {
				tempMat = tmp.getMatrix(inputMat, i, j, n);
				tempMat = tmp.transform(n, transformMatrix.getDctMatrix(n), tempMat);
				tempMat = tempMat.arrayRightDivide(quantMat);
				outputMat = tmp.nSamplesTogether(inputMat, tempMat, i, j, n);
			}
		}
		return outputMat;
	}

	private Matrix IQuantizationCycle(Matrix inputMat, Matrix quantMat, int n) {
		Matrix tempMat = new Matrix(n, n);
		Matrix outputMat = new Matrix(inputMat.getColumnDimension(), inputMat.getColumnDimension());
		ColorTransform tmp = getColorTransform();

		for (int i = 0; i < inputMat.getColumnDimension(); i = i + n) {
			for (int j = 0; j < inputMat.getRowDimension(); j = j + n) {
				tempMat = tmp.getMatrix(inputMat, i, j, n);
				tempMat = tempMat.arrayTimes(quantMat);
				tempMat = tmp.inverseTransform(n, transformMatrix.getDctMatrix(n), tempMat);
				outputMat = tmp.setMatrix(inputMat, tempMat, i, j, n);
			}
		}
		return outputMat;
	}

	public ImagePlus getComponent(int component, ColorTransform tmp) {
		ImagePlus imagePlus = null;
		switch (component) {
		case RED:
			imagePlus = tmp.setImageFromRGB(tmp.getImageWidth(), tmp.getImageHeight(), tmp.getRed(), "RED");
			break;
		case GREEN:
			imagePlus = tmp.setImageFromRGB(tmp.getImageWidth(), tmp.getImageHeight(), tmp.getGreen(), "GREEN");
			break;
		case BLUE:
			imagePlus = tmp.setImageFromRGB(tmp.getImageWidth(), tmp.getImageHeight(), tmp.getBlue(), "BLUE");
			break;

		case Y:
			imagePlus = tmp.setImageFromRGB(tmp.getImageWidth(), tmp.getImageHeight(), tmp.getY(), "Y");
			break;

		case CB:
			imagePlus = tmp.setImageFromRGB(tmp.getImageWidth(), tmp.getImageHeight(), tmp.getCb(), "cb");
			break;

		case CR:
			imagePlus = tmp.setImageFromRGB(tmp.getImageWidth(), tmp.getImageHeight(), tmp.getCr(), "Cr");
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
	}

	private void loadOriginalImages() {
		this.imagePlusFirst = new ImagePlus("images/pomaly.jpg");
		this.imagePlusSecond = new ImagePlus("images/pomaly2.jpg");
		setImageView(imagePlusFirst, this.currentImageView);
		this.colorTransformFirst = new ColorTransform(imagePlusFirst.getBufferedImage());
		this.colorTransformSecond = new ColorTransform(imagePlusSecond.getBufferedImage());
		this.colorTransformBuffer = new ColorTransform(imagePlusSecond.getBufferedImage());
		this.colorTransformFirstToSend = new ColorTransform(imagePlusFirst.getBufferedImage());
		this.colorTransformDecoderBuffer = new ColorTransform(imagePlusFirst.getBufferedImage());
		this.colorTransformFirstToSend.getRGB();
		this.colorTransformFirst.getRGB();
		this.colorTransformSecond.getRGB();
		this.colorTransformFirst.convertRgbToYcBcR();
		this.colorTransformSecond.convertRgbToYcBcR();
		this.colorTransformFirstToSend.convertRgbToYcBcR();
		this.colorTransformBuffer.getRGB();
		this.colorTransformBuffer.convertRgbToYcBcR();
	}

	private void setRadioButton() {

		sizeOfBlock = new ToggleGroup();
		searchTechniqie = new ToggleGroup();
		groupShowImage = new ToggleGroup();
		groupShowImageDecoder = new ToggleGroup();

		radioButtonFirst.setToggleGroup(groupShowImage);
		radioButtonFirst.setId("First");
		radioButtonFirst.setSelected(true);
		radioButtonSecond.setToggleGroup(groupShowImage);
		radioButtonSecond.setId("Second");

		radioButtonDecoderFirst.setToggleGroup(groupShowImageDecoder);
		radioButtonDecoderFirst.setId("FirstDecoder");
		radioButtonDecoderFirst.setDisable(true);
		radioButtonDecoderSecond.setToggleGroup(groupShowImageDecoder);
		radioButtonDecoderSecond.setId("SecondDecoder");
		radioButtonDecoderSecond.setDisable(true);

		radioButtonFirst.setOnAction((ActionEvent e) -> {
			isCustomView = false;
			setImageView(imagePlusFirst, this.currentImageView);
		});

		radioButtonSecond.setOnAction((ActionEvent e) -> {
			isCustomView = false;
			setImageView(imagePlusSecond, this.currentImageView);
		});

		radioButtonDecoderFirst.setOnAction((ActionEvent e) -> {
			setImageView(this.colorTransformFirstToSend.convertYcBcRToRGB(), this.decoderImageView);
		});

		radioButtonDecoderSecond.setOnAction((ActionEvent e) -> {
			setImageView(this.colorTransformBuffer.convertYcBcRToRGB(), this.decoderImageView);
		});

		size2x2.setToggleGroup(sizeOfBlock);
		size4x4.setToggleGroup(sizeOfBlock);
		size8x8.setToggleGroup(sizeOfBlock);
		size16x16.setToggleGroup(sizeOfBlock);
		size16x16.setSelected(true);

		size2x2.setOnAction((ActionEvent e) -> {
			size_of_block = 2;
			System.out.println("Block size is: " + size_of_block);
		});

		size4x4.setOnAction((ActionEvent e) -> {
			size_of_block = 4;
			System.out.println("Block size is: " + size_of_block);
		});

		size8x8.setOnAction((ActionEvent e) -> {
			size_of_block = 8;
			System.out.println("Block size is: " + size_of_block);
		});

		size16x16.setOnAction((ActionEvent e) -> {
			size_of_block = 16;
			System.out.println("Block size is: " + size_of_block);
		});

		fullSearch.setToggleGroup(searchTechniqie);
		fullSearch.setId("fullSearch");
		fullSearch.setSelected(true);
		halfSearch.setToggleGroup(searchTechniqie);
		halfSearch.setId("halfSearch");
		treeSearch.setToggleGroup(searchTechniqie);
		treeSearch.setId("treeSearch");
		oneSeach.setToggleGroup(searchTechniqie);
		oneSeach.setId("oneSeach");
	}

	private void setQuality() {
		qQuality = 50;
		QualityLabel.setText(String.valueOf(qQuality));
		QualitySlider.setValue(50);
	}

	private ColorTransform getColorTransform() {
		if (isCustomView)
			return this.colorTransformBuffer;
		if (isDecoderView) {
			RadioButton selectedRadioButton = (RadioButton) groupShowImageDecoder.getSelectedToggle();
			if (selectedRadioButton == null)
				return null;
			switch (selectedRadioButton.getId()) {
			case "FirstDecoder":
				return this.colorTransformFirstToSend;
			case "SecondDecoder":
				return this.colorTransformBuffer;
			}
			return null;
		}
		RadioButton selectedRadioButton = (RadioButton) groupShowImage.getSelectedToggle();
		if (selectedRadioButton == null)
			return null;
		switch (selectedRadioButton.getId()) {
		case "First":
			return this.colorTransformFirst;
		case "Second":
			return this.colorTransformSecond;
		}
		return null;
	}

	private Matrix applySearchTechnique(Matrix extendMatrix, Matrix second, int size_of_block) {
		RadioButton selectedRadioButton = (RadioButton) searchTechniqie.getSelectedToggle();
		if (selectedRadioButton == null)
			return null;
		switch (selectedRadioButton.getId()) {
		case "halfSearch":
			return HelperClass.halfPixelSearch(extendMatrix, second, size_of_block);
		case "treeSearch":
			return HelperClass.treeSearch(extendMatrix, second, size_of_block);
		case "fullSearch":
			return HelperClass.fullSearch(extendMatrix, second, size_of_block);
		case "oneSeach":
			return HelperClass.oneAtTimeSearch(extendMatrix, second, size_of_block);
		}
		return null;
	}

	private boolean isHalfPixelActive() {
		RadioButton selectedRadioButton = (RadioButton) searchTechniqie.getSelectedToggle();
		if (selectedRadioButton == null)
			return false;
		switch (selectedRadioButton.getId()) {
		case "halfSearch":
			return true;
		default:
			return false;
		}
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
