package cv1;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import Jama.Matrix;
import ij.ImagePlus;

public class ColorTransform {
	
	private BufferedImage bImage;
	private ColorModel colorModel;
	
	private int [][] red;
	private int [][] green;
	private int [][] blue;
	private int width;
	private int height;
	private Matrix y;
	private Matrix cB;
	private Matrix cR;
	
	public ColorTransform(BufferedImage bImage) {
		this.bImage = bImage;
		this.width = bImage.getWidth();
		this.height = bImage.getHeight();
		
		this.red =  new int [this.height][this.width];
		this.green =  new int [this.height][this.width];
		this.blue =  new int [this.height][this.width];
		
		y = new Matrix(this.height, this.width);
		cB = new Matrix(this.height, this.width);
		cR = new Matrix(this.height, this.width);
		
		this.colorModel = bImage.getColorModel();
	}
	
	public void getRGB() {
		for (int i = 0; i < this.getHeight(); i++) {
			for (int j = 0; j < this.getWidth(); j++) {
				red[i][j] = this.colorModel.getRed(this.bImage.getRGB(j, i));
				blue[i][j] = this.colorModel.getBlue(this.bImage.getRGB(j, i));
				green[i][j] = this.colorModel.getGreen(this.bImage.getRGB(j, i));
			}
		}
	}
	
	public ImagePlus setImageFromRGB(int width, int height, int[][] r, int[][] g, int[][] b) {
		BufferedImage bImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int rgb[][]  = new int [height][width];
		for (int i = 0; i < getHeight(); i++) {
			for (int j = 0; j < getWidth(); j++) {
				rgb[i][j] = new Color(r[i][j], g[i][j], b[i][j]).getRGB();
				bImage.setRGB(j, i, rgb[i][j]);
			}
		}
		return new ImagePlus("RGB", bImage);
	}
	
	

	public void RgbToYcBcR() {
		for (int i = 0; i < getHeight(); i++) {
			for (int j = 0; j < getWidth(); j++) {
				double tmpy = 0.257 * red[i][j] + 0.504 * green[i][j]  + 0.098 * blue[i][j] + 16;
				double tmpCb = - 0.148 * red[i][j] - 0.291 * green[i][j] + 0.439 * blue[i][j] + 128;
				double tmpCr = 0.439 * red[i][j] - 0.368 * green[i][j] - 0.071 * blue[i][j] + 128;
				this.y.set(i, j,  tmpy);
				this.cB.set(i, j, tmpCb);
				this.cR.set(i, j, tmpCr);
			}
		}
		
	}
	
	public int control(double imput) {
		if(imput >= 255) return 255;
		if(imput<0) return 0;
		return (int)Math.round(imput);
	}

	public void YcBcRToRgb() {
		for (int i = 0; i < this.getHeight(); i++) {
			for (int j = 0; j < this.getWidth(); j++) {
				red[i][j] = this.control(1.164 * (this.y.get(i, j) - 16) + 1.596 * (this.cR.get(i, j) - 128));
				blue[i][j] =  this.control(1.164 * (this.y.get(i, j) - 16)  + 2.018 * (this.cB.get(i, j) - 128));
				green[i][j] =  this.control(1.164 * (this.y.get(i, j) - 16) - 0.813 * (this.cR.get(i, j) - 128) - 0.391 * (this.cB.get(i, j) - 128));
			}
		}
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
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public Matrix getY() {
		return y;
	}
	public void setY(Matrix y) {
		this.y = y;
	}
	public Matrix getcB() {
		return cB;
	}
	public void setcB(Matrix cB) {
		this.cB = cB;
	}
	public Matrix getcR() {
		return cR;
	}
	public void setcR(Matrix cR) {
		this.cR = cR;
	}

}
