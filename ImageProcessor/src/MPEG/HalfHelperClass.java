package MPEG;

import Jama.Matrix;

/**
 * This java class represents abstract class containing HalfPixel search
 * technique.
 * 
 * @author Yehor Safonov; id: 185942
 */

public abstract class HalfHelperClass {

	private static Matrix extendHalfPixelMatrix(Matrix m) {
		int cols = m.getColumnDimension();
		int rows = m.getRowDimension();
		Matrix out = new Matrix(cols * 2 - 2, rows * 2 - 2);
		for (int i = 0; i < cols - 1; i++) {
			for (int j = 0; j < rows - 1; j++) {
				double averageX = (m.get(i, j) + m.get(i + 1, j)) / 2.0;
				double averageY = (m.get(i, j) + m.get(i, j + 1)) / 2.0;
				double averageXY = (m.get(i, j) + m.get(i + 1, j + 1)) / 2.0;
				out.set(i * 2, j * 2, m.get(i, j));
				out.set(i * 2 + 1, j * 2, averageX);
				out.set(i * 2, j * 2 + 1, averageY);
				out.set(i * 2 + 1, j * 2 + 1, averageXY);
			}
		}
		return HelperClass.extendMatrix(out, 2);
	}

	public static Matrix countHalfPixelPredictionVectors(Matrix present, Matrix second, int blockSize) {
		Matrix extended = extendHalfPixelMatrix(present);
		extended = HelperClass.extendMatrix(extended, blockSize * 2);

		int columDem = (int) (second.getRowDimension() / blockSize) * (second.getColumnDimension() / blockSize);
		Matrix movementPredictionMatrix = new Matrix(columDem, 2);
		int c = 0;

		int areaSize = blockSize * 2 * 2;
		for (int i = 0; i < second.getColumnDimension(); i += blockSize) {
			for (int l = 0; l < second.getRowDimension(); l += blockSize) {
				Matrix block = getSubmatrix(blockSize, second, i, l);
				Matrix area = getSubmatrix(areaSize, extended, i * 2, l * 2);
				findBlockInArea(movementPredictionMatrix, blockSize, area, block, c);
				c++;
			}
		}

		return movementPredictionMatrix;
	}

	private static Matrix getSubmatrix(int blockSize, Matrix mat, int i, int l) {
		Matrix block = new Matrix(blockSize, blockSize);
		for (int j = 0; j < blockSize; j++) {
			for (int k = 0; k < blockSize; k++) {
				block.set(j, k, mat.get(i + j, l + k));
			}
		}
		return block;
	}

	private static void findBlockInArea(Matrix movement, int blockSize, Matrix area, Matrix block, int c) {
		double errOfBlock = 0;
		double sad = Double.MAX_VALUE;
		for (int i = 0; i < area.getColumnDimension() - 2 * blockSize; i++) {
			for (int j = 0; j < area.getRowDimension() - 2 * blockSize; j++) {
				errOfBlock = getErrorOfBlock(blockSize, area, block, errOfBlock, i, j);
				if (sad > errOfBlock) {
					sad = errOfBlock;
					movement.set(c, 0, i);
					movement.set(c, 1, j);
				}
				errOfBlock = 0;
			}
		}
	}

	private static double getErrorOfBlock(int blockSize, Matrix area, Matrix block, double errOfBlock, int i, int j) {
		for (int k = 0; k < blockSize; k++) {
			for (int l = 0; l < blockSize; l++) {
				double areaValue = area.get(i + k * 2, j + l * 2);
				double blockValue = block.get(k, l);
				errOfBlock += Math.abs(areaValue - blockValue);
			}
		}
		return errOfBlock;
	}

	public static Matrix predictImageFromHalfPixelMovementVectors(int blockSize, int columnDimension, int rowDimension,
			Matrix predictionVectors, Matrix stretched) {
		int c = 0;
		Matrix out = new Matrix(columnDimension, rowDimension);
		for (int i = 0; i < columnDimension; i += blockSize) {
			for (int j = 0; j < rowDimension; j += blockSize) {
				for (int k = 0; k < blockSize; k++) {
					for (int l = 0; l < blockSize; l++) {
						out.set(i + k, j + l, stretched.get(i * 2 + k * 2 + (int) predictionVectors.get(c, 0),
								j * 2 + l * 2 + (int) predictionVectors.get(c, 1)));
					}
				}
				c++;
			}
		}
		return out;
	}

	public static Matrix[] predictFromVectorsHalfPixel(int blockSize, Matrix predictionVectors, Matrix Y, Matrix Cb,
			Matrix Cr) {
		Matrix predictedY = predictImageFromHalfPixelMovementVectors(blockSize, Y.getColumnDimension(),
				Y.getRowDimension(), predictionVectors,
				HelperClass.extendMatrix(extendHalfPixelMatrix(Y), blockSize * 2));
		Matrix predictedCb = predictImageFromHalfPixelMovementVectors(blockSize, Cb.getColumnDimension(),
				Cb.getRowDimension(), predictionVectors,
				HelperClass.extendMatrix(extendHalfPixelMatrix(Cb), blockSize * 2));
		Matrix predictedCr = predictImageFromHalfPixelMovementVectors(blockSize, Cr.getColumnDimension(),
				Cr.getRowDimension(), predictionVectors,
				HelperClass.extendMatrix(extendHalfPixelMatrix(Cr), blockSize * 2));
		Matrix[] out = new Matrix[3];
		out[0] = predictedY;
		out[1] = predictedCb;
		out[2] = predictedCr;
		return out;
	}
}
