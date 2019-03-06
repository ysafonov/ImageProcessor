package cv5;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import Jama.Matrix;
import ij.ImagePlus;

public class ColorTransform {
	private int red[][];
	private int green[][];
	private int blue[][];

	private int imageHeight;
	private int imageWidth;

	private Matrix y;
	private Matrix cb;
	private Matrix cr;

	private QMatrix qMatrix = new QMatrix();
	public Matrix quantMatrix8Brightness = new Matrix(8, 8);
	public Matrix quantMatrix8Color = new Matrix(8, 8);

	private BufferedImage bImage;
	private ColorModel colorModel;

	public ColorTransform(BufferedImage bImage) {
		this.bImage = bImage;
		this.colorModel = bImage.getColorModel();
		this.imageHeight = bImage.getHeight();
		this.imageWidth = bImage.getWidth();

		red = new int[this.imageHeight][this.imageWidth];
		green = new int[this.imageHeight][this.imageWidth];
		blue = new int[this.imageHeight][this.imageWidth];

		y = new Matrix(this.imageHeight, this.imageWidth);
		cb = new Matrix(this.imageHeight, this.imageWidth);
		cr = new Matrix(this.imageHeight, this.imageWidth);
	}

	public void getRGB() {
		for (int i = 0; i < this.imageHeight; i++) {
			for (int j = 0; j < this.imageWidth; j++) {
				red[i][j] = colorModel.getRed(this.bImage.getRGB(j, i));
				green[i][j] = colorModel.getGreen(this.bImage.getRGB(j, i));
				blue[i][j] = colorModel.getBlue(this.bImage.getRGB(j, i));
			}
		}
	}

	public ImagePlus setImageFromRGB(int width, int height, int[][] r, int[][] g, int[][] b) {

		BufferedImage bImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int[][] rgb = new int[height][width];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				rgb[i][j] = new Color(r[i][j], g[i][j], b[i][j]).getRGB();
				bImage.setRGB(j, i, rgb[i][j]);
			}
		}

		return new ImagePlus("RGB", bImage);
	}

	public ImagePlus setImageFromRGB(int width, int height, int[][] x, String component) {

		BufferedImage bImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int[][] rgb = new int[height][width];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				rgb[i][j] = new Color(x[i][j], x[i][j], x[i][j]).getRGB();
				bImage.setRGB(j, i, rgb[i][j]);
			}
		}

		return new ImagePlus(component, bImage);
	}

	public ImagePlus setImageFromRGB(int width, int height, Matrix x, String component) {

		BufferedImage bImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int[][] rgb = new int[height][width];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				rgb[i][j] = new Color((int) x.get(i, j), (int) x.get(i, j), (int) x.get(i, j)).getRGB();
				bImage.setRGB(j, i, rgb[i][j]);
			}
		}

		return new ImagePlus(component, bImage);
	}

	public void convertRgbToYcBcR() {

		for (int i = 0; i < imageHeight; i++) {
			for (int j = 0; j < imageWidth; j++) {
				y.set(i, j, 0.257 * red[i][j] + 0.504 * green[i][j] + 0.098 * blue[i][j] + 16);
				cb.set(i, j, -0.148 * red[i][j] - 0.291 * green[i][j] + 0.439 * blue[i][j] + 128);
				cr.set(i, j, 0.439 * red[i][j] - 0.368 * green[i][j] - 0.071 * blue[i][j] + 128);
			}
		}
	}

	public ImagePlus convertYcBcRToRGB() {
		int[][] r = new int[imageHeight][imageWidth];
		int[][] g = new int[imageHeight][imageWidth];
		int[][] b = new int[imageHeight][imageWidth];
		for (int i = 0; i < imageHeight; i++) {
			for (int j = 0; j < imageWidth; j++) {
				r[i][j] = this.control(1.164 * (y.get(i, j) - 16) + 1.596 * (cr.get(i, j) - 128));
				g[i][j] = this.control(
						1.164 * (y.get(i, j) - 16) - 0.813 * (cr.get(i, j) - 128) - 0.391 * (cb.get(i, j) - 128));
				b[i][j] = this.control(1.164 * (y.get(i, j) - 16) + 2.018 * (cb.get(i, j) - 128));
			}
		}
		return setImageFromRGB(imageWidth, imageHeight, r, g, b);
	}

	public Matrix downSample(Matrix mat) {
		Matrix temp = mat.copy();
		Matrix output = new Matrix(mat.getRowDimension(), mat.getColumnDimension() / 2);

		for (int column = 0; column < temp.getColumnDimension() / 2; column++) {
			int[] columnOrig = { column * 2 };
			output.setMatrix(0, output.getRowDimension() - 1, column, column,
					temp.getMatrix(0, output.getRowDimension() - 1, columnOrig));
		}

		return output;
	}

	public ImagePlus get444(Matrix x, String component) {
		return setImageFromRGB(x.getColumnDimension(), x.getRowDimension(), x, component);
	}

	public ImagePlus get422(Matrix x, String component) {
		Matrix outputMatrix = downSample(x);
		return setImageFromRGB(outputMatrix.getColumnDimension(), outputMatrix.getRowDimension(), outputMatrix,
				component);
	}

	public ImagePlus get420(Matrix x, String component) {
		Matrix temp = downSample(x.transpose());
		Matrix outputMatrix = downSample(temp.transpose());
		return setImageFromRGB(outputMatrix.getColumnDimension(), outputMatrix.getRowDimension(), outputMatrix,
				component);
	}

	public ImagePlus get411(Matrix x, String component) {
		Matrix temp = downSample(x);
		Matrix outputMatrix = downSample(temp);
		return setImageFromRGB(outputMatrix.getColumnDimension(), outputMatrix.getRowDimension(), outputMatrix,
				component);
	}

	public Matrix overSample(Matrix mat) {
		Matrix newMat = new Matrix(mat.getRowDimension(), mat.getColumnDimension() * 2);
		for (int i = 0; i < mat.getColumnDimension() * 2; i = i + 2) {
			for (int j = 0; j < mat.getRowDimension(); j++) {
				int buffer = this.control(mat.get(j, i / 2));
				newMat.set(j, i, buffer);
				newMat.set(j, i + 1, buffer);
			}
		}
		return newMat;
	}

	public Matrix transform(int size, Matrix transformMatrix, Matrix inputMatrix) {
		Matrix tmp = transformMatrix.times(inputMatrix);
		tmp = tmp.times(transformMatrix.transpose());
		return tmp;
	}

	public Matrix inverseTransform(int size, Matrix transformMatrix, Matrix inputMatrix) {
		Matrix tmp = transformMatrix.transpose().times(inputMatrix);
		tmp = tmp.times(transformMatrix);
		return tmp;
	}

	public Matrix getQuantMatrix8Brightness(int quality) {
		int column = quantMatrix8Brightness.getColumnDimension();
		int row = quantMatrix8Brightness.getRowDimension();
		for (int i = 0; i < column; i++) {
			for (int j = 0; j < row; j++) {
				double tmp = controlQuality(Math.round(qMatrix.getQuantizationMatrixBrightness()[i][j]), quality);
				quantMatrix8Brightness.set(j, i, control(tmp));
			}
		}
		return quantMatrix8Brightness;
	}

	public Matrix  getQuantMatrix8Color(int quality) {
		int column = quantMatrix8Color.getColumnDimension();
		int row = quantMatrix8Color.getRowDimension();
		for (int i = 0; i < column; i++) {
			for (int j = 0; j < row; j++) {
				double tmp = controlQuality(Math.round(qMatrix.getQuantizationMatrixColor()[i][j]), quality);
				quantMatrix8Color.set(j, i, tmp);
			}
		}
		return quantMatrix8Color;
	}

	public Matrix getMatrix(Matrix x, int k, int l, int n) {
		Matrix newMat = new Matrix(n, n);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				double tmp = x.get(l + j, k + i);
				newMat.set(j, i, tmp);
			}
		}
		return newMat;
	}

	public Matrix setMatrix(Matrix newMat, Matrix x, int k, int l, int n) {
		for(int i = 0; i < n; i++)
		{
			for(int j = 0; j < n; j++)
			{
				double tmp = x.get(j,i);
				newMat.set(l+j,k+i,tmp);
			}
		}		
		return newMat;
	}
	
	public Matrix nSamplesTogether(Matrix newMat, Matrix x, int k, int l, int n) {
		
		for(int i = 0; i < n; i++){
			for(int j = 0; j < n; j++){
				double buffer = x.get(j,i);
				if (Math.abs(buffer) < 1) buffer = 0;
				newMat.set(l+j,k+i,buffer);
			}
		}		
		return newMat;
	}
	
	
	public double controlQuality(double quantizationMatrixValue, int quality) {
		if (quality == 100) {
			quantizationMatrixValue = 1;
		} else if (quality >= 1 && quality < 50) {
			quantizationMatrixValue = quantizationMatrixValue * (50 / quality);
		} else if (quality >= 50 && quality <= 99) {
			quantizationMatrixValue = quantizationMatrixValue * (2.0 - ((2.0 * quality) / 100));
		}
		return quantizationMatrixValue;
	}

	public int control(double imput) {
		if (imput >= 255)
			return 255;
		if (imput < 0)
			return 0;
		return (int) Math.round(imput);
	}

	public int[][] getRed() {
		return red;
	}

	public void setRed(int[][] red) {
		this.red = red;
	}

	public int[][] getGreen() {
		return green;
	}

	public void setGreen(int[][] green) {
		this.green = green;
	}

	public int[][] getBlue() {
		return blue;
	}

	public void setBlue(int[][] blue) {
		this.blue = blue;
	}

	public int getImageHeight() {
		return imageHeight;
	}

	public int getImageWidth() {
		return imageWidth;
	}

	public Matrix getY() {
		return y;
	}

	public void setY(Matrix y) {
		this.y = y;
	}

	public Matrix getCb() {
		return cb;
	}

	public void setCb(Matrix cb) {
		this.cb = cb;
	}

	public Matrix getCr() {
		return cr;
	}

	public void setCr(Matrix cr) {
		this.cr = cr;
	}

}
