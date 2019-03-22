package MPEG;

import Jama.Matrix;

public class TransformMatrix {

	public Matrix getDctMatrix(int size) {
		Matrix DCTmatrix = new Matrix(size, size);
		int i = 0;
		for (int j = 0; j < size; j++) {
			DCTmatrix.set(i, j, Math.sqrt(1.0 / size) * Math.cos(((2.0 * j + 1.0) * i * Math.PI) / (2.0 * size)));
		}
		for (i = 1; i < size; i++) {
			for (int j = 0; j < size; j++) {
				DCTmatrix.set(i, j, Math.sqrt(2.0 / size) * Math.cos(((2.0 * j + 1.0) * i * Math.PI) / (2.0 * size)));
			}
		}
		return DCTmatrix;
	}
	
	
	public Matrix getWhtMatrix (int size)
	{
		Matrix WHTmatrix = new Matrix(size,size);
		Matrix Init = new Matrix(1, 1);
		Init.set(0, 0, 1);
		for(int i=2; i<=size; i=2*i)
		{
			WHTmatrix.setMatrix(0,i/2-1,0,i/2-1,Init);
			WHTmatrix.setMatrix(0,i/2-1,i/2,i-1,Init);
			WHTmatrix.setMatrix(i/2,i-1,0,i/2-1,Init);
			WHTmatrix.setMatrix(i/2,i-1,i/2,i-1,Init.uminus());
			Init = WHTmatrix;
		}
		WHTmatrix = WHTmatrix.times(1.0/Math.sqrt(size));
		return WHTmatrix;
	}
}
