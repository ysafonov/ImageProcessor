package MPEG;

import Jama.Matrix;

/**
 * This java class represents abstract class containing methods for MPEG
 * processing.
 * 
 * @author Yehor Safonov; id: 185942
 */

public abstract class HelperClass {

	public static Matrix createMacroBlock(Matrix yMatrix, int xpos, int ypos, int sizeOfMacroBlock) {

		Matrix matrix = yMatrix;
		Matrix macroblock = new Matrix(sizeOfMacroBlock, sizeOfMacroBlock);

		for (int i = 0; i < sizeOfMacroBlock; i++) {
			for (int j = 0; j < sizeOfMacroBlock; j++) {
				macroblock.set(i, j, matrix.get(xpos + i, ypos + j));
			}
		}
		return macroblock;
	}

	public static Matrix substract(Matrix first, Matrix second) {

		if (first.getRowDimension() != second.getRowDimension()
				|| first.getColumnDimension() != second.getColumnDimension())
			return null;
		Matrix tmp = new Matrix(first.getRowDimension(), first.getColumnDimension());
		for (int i = 0; i < first.getRowDimension(); i++) {
			for (int j = 0; j < first.getColumnDimension(); j++) {
				tmp.set(i, j, first.get(i, j) - second.get(i, j));
			}
		}
		return tmp;
	}

	public static Matrix dpcmToShow(Matrix matrix1, Matrix matrix2) {
		Matrix outputMat = new Matrix(matrix1.getColumnDimension(), matrix1.getColumnDimension());

		for (int i = 0; i < matrix1.getColumnDimension(); i++) {
			for (int j = 0; j < matrix1.getRowDimension(); j++) {
				double buffer = (matrix2.get(i, j) + 255) - (matrix1.get(i, j));
				buffer = buffer / 2;
				outputMat.set(i, j, buffer);
			}
		}
		return outputMat;
	}

	public static Matrix summarization(Matrix first, Matrix second) {

		if (first.getRowDimension() != second.getRowDimension()
				|| first.getColumnDimension() != second.getColumnDimension())
			return null;
		Matrix tmp = new Matrix(first.getRowDimension(), first.getColumnDimension());
		for (int i = 0; i < first.getRowDimension(); i++) {
			for (int j = 0; j < first.getColumnDimension(); j++) {
				tmp.set(i, j, first.get(i, j) + second.get(i, j));
			}
		}
		return tmp;
	}

	public static Matrix fullSearch(Matrix extend, Matrix second, int size) {

		int columDem = (int) (second.getRowDimension() / size) * (second.getColumnDimension() / size);
		Matrix movementPredictionMatrix = new Matrix(columDem, 2);
		int c = 0;

		for (int xpos = 0; xpos < second.getRowDimension(); xpos += size) {
			for (int ypos = 0; ypos < second.getColumnDimension(); ypos += size) {
				Matrix block = createMacroBlock(second, xpos, ypos, size);
				Matrix area = createMacroBlock(extend, xpos, ypos, 2 * size);
				searchMovement(movementPredictionMatrix, area, block, size, c);
				c++;
			}
		}

		return movementPredictionMatrix;
	}

	public static Matrix halfPixelSearch(Matrix extend, Matrix second, int blockSize) {
		return HalfHelperClass.countHalfPixelPredictionVectors(extend, second, blockSize);
	}

	public static Matrix treeSearch(Matrix extend, Matrix second, int blockSize) {
		return ThreeStepSearch.threeSearch(extend, second, blockSize);
	}

	public static Matrix oneAtTimeSearch(Matrix extend, Matrix second, int blockSize) {
		return SearchOneAtTime.oneAtTimeSearch(extend, second, blockSize);
	}

	private static void searchMovement(Matrix prediction, Matrix area, Matrix block, int blockSize, int c) {
		double errOfBlock = 0;
		double sad = Double.MAX_VALUE;
		for (int i = 0; i < area.getColumnDimension() - blockSize; i++) {
			for (int j = 0; j < area.getRowDimension() - blockSize; j++) {
				errOfBlock = getErrorOfBlock(blockSize, area, block, errOfBlock, i, j);
				if (sad > errOfBlock) {
					sad = errOfBlock;
					prediction.set(c, 0, i);
					prediction.set(c, 1, j);
				}
				errOfBlock = 0;
			}
		}
	}

	public static double getErrorOfBlock(int blockSize, Matrix area, Matrix block, double errOfBlock, int i, int j) {
		for (int k = 0; k < blockSize; k++) {
			for (int l = 0; l < blockSize; l++) {
				double areaValue = area.get(i + k, j + l);
				double blockValue = block.get(k, l);
				errOfBlock += Math.abs(areaValue - blockValue);
			}
		}
		return errOfBlock;
	}

	public static Matrix predictFromVectors(int blockSize, int columnDimension, int rowDimension,
			Matrix predictionVectors, Matrix stretched) {

		Matrix out = new Matrix(columnDimension, rowDimension);
		int c = 0;
		for (int i = 0; i < columnDimension; i += blockSize) {
			for (int j = 0; j < rowDimension; j += blockSize) {
				for (int k = 0; k < blockSize; k++) {
					for (int l = 0; l < blockSize; l++) {
						out.set(i + k, j + l, stretched.get(i + k + (int) predictionVectors.get(c, 0),
								j + l + (int) predictionVectors.get(c, 1)));
					}
				}
				c++;
			}
		}
		return out;
	}

	public static Matrix extendMatrix(Matrix matrix, int blockSize) {
		int extendedCols = matrix.getColumnDimension() + blockSize;
		int extendedRows = matrix.getRowDimension() + blockSize;
		int cols = matrix.getColumnDimension();
		int rows = matrix.getRowDimension();
		Matrix outputMatrix = new Matrix(extendedRows, extendedCols);
		for (int m = 0; m < rows; m++) {
			for (int n = 0; n < cols; n++) {

				if (m == 0) {
					for (int i = 0; i < blockSize / 2; i++) {
						outputMatrix.set(i, n + blockSize / 2, matrix.get(m, n));
					}
				}
				if (n == 0) {
					for (int i = 0; i < blockSize / 2; i++) {
						outputMatrix.set(m + blockSize / 2, i, matrix.get(m, n));
					}
				}
				if (m == rows - 1) {
					for (int i = 0; i < blockSize / 2; i++) {
						outputMatrix.set(i + rows + blockSize / 2, n + blockSize / 2, matrix.get(m, n));
					}
				}
				if (n == cols - 1) {
					for (int i = 0; i < blockSize / 2; i++) {
						outputMatrix.set(m + blockSize / 2, i + cols + blockSize / 2, matrix.get(m, n));
					}
				}

				if (m == 0 && n == 0) {
					for (int j = 0; j < blockSize / 2; j++) {
						for (int in = 0; in < blockSize / 2; in++) {
							outputMatrix.set(j, in, matrix.get(m, n));
						}
					}
				}

				if (m == 0 && n == cols - 1) {
					for (int j = 0; j < blockSize / 2; j++) {
						for (int in = 0; in < blockSize / 2; in++) {
							outputMatrix.set(j, in + cols + blockSize / 2, matrix.get(m, n));
						}
					}
				}

				if (m == rows - 1 && n == 0) {
					for (int j = 0; j < blockSize / 2; j++) {
						for (int in = 0; in < blockSize / 2; in++) {
							outputMatrix.set(j + rows + blockSize / 2, in, matrix.get(m, n));
						}
					}
				}

				if (m == rows - 1 && n == cols - 1) {
					for (int j = 0; j < blockSize / 2; j++) {
						for (int in = 0; in < blockSize / 2; in++) {
							outputMatrix.set(j + rows + blockSize / 2, in + cols + blockSize / 2, matrix.get(m, n));
						}
					}
				}

				outputMatrix.set(blockSize / 2 + m, blockSize / 2 + n, matrix.get(m, n));
			}
		}
		return outputMatrix;
	}

	public static Matrix[] predictFromVectors(int blockSize, Matrix predictionVectors, Matrix Y, Matrix Cb, Matrix Cr) {
		Matrix predictedY = predictFromVectors(blockSize, Y.getColumnDimension(), Y.getRowDimension(),
				predictionVectors, extendMatrix(Y, blockSize));
		Matrix predictedCb = predictFromVectors(blockSize, Cb.getColumnDimension(), Cb.getRowDimension(),
				predictionVectors, extendMatrix(Cb, blockSize));
		Matrix predictedCr = predictFromVectors(blockSize, Cr.getColumnDimension(), Cr.getRowDimension(),
				predictionVectors, extendMatrix(Cr, blockSize));

		Matrix[] out = new Matrix[3];
		out[0] = predictedY;
		out[1] = predictedCb;
		out[2] = predictedCr;
		return out;
	}

	public static double sumOfAllValues(Matrix a) {
		double out = 0;
		for (int i = 0; i < a.getColumnDimension(); i++) {
			for (int j = 0; j < a.getRowDimension(); j++) {
				out += Math.abs(a.get(i, j));
			}
		}
		return out;
	}

	public static Matrix getSubmatrix(int blockSize, Matrix mat, int i, int l) {
		Matrix block = new Matrix(blockSize, blockSize);
		for (int j = 0; j < blockSize; j++) {
			for (int k = 0; k < blockSize; k++) {
				block.set(j, k, mat.get(i + j, l + k));
			}
		}
		return block;
	}

}
