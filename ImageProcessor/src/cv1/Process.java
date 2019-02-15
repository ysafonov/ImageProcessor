package cv1;

import ij.ImagePlus;

public class Process {

	private ImagePlus imagePlus;
	private ColorTransform colorTransform;
	public Process(ImagePlus input) {
		this.imagePlus = input;
		this.colorTransform = new ColorTransform(input.getBufferedImage());
		testRGBMatrixs();
	}
	
	private void testRGBMatrixs() {
		this.imagePlus.show();
		colorTransform.getRGB();
		
		colorTransform.RgbToYcBcR();
		colorTransform.YcBcRToRgb();
		colorTransform.setImageFromRGB(colorTransform.getWidth(),
				colorTransform.getHeight(),
				colorTransform.getRed(), 
				colorTransform.getGreen(), 
				colorTransform.getBlue()).show();
	}
}
