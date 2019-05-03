package MPEG;

import Jama.Matrix;

/**
 * This java class represents abstract class containing ThreeStep search
 * technique.
 * 
 * @author Yehor Safonov; id: 185942
 */

public abstract class ThreeStepSearch {
	public static Matrix threeSearch(Matrix extend, Matrix second, int size) {
		int columDem = (int) (second.getRowDimension() / size) * (second.getColumnDimension() / size);
		Matrix movementPredictionMatrix = new Matrix(columDem, 2);
		int c = 0;

		for (int xpos = 0; xpos < second.getRowDimension(); xpos += size) {
			for (int ypos = 0; ypos < second.getColumnDimension(); ypos += size) {
				Matrix block = HelperClass.createMacroBlock(second, xpos, ypos, size);
				Matrix area = HelperClass.createMacroBlock(extend, xpos, ypos, 2 * size);
				searchMovementThreeStep(movementPredictionMatrix, area, block, size, c);
				c++;
			}
		}
		return movementPredictionMatrix;
	}

	private static void searchMovementThreeStep(Matrix vectors, Matrix area, Matrix block, int blockSize,
			int positionInVector) {
		int tmpBlock = blockSize * 2;
		int s = 4;
		int x_best = (int) tmpBlock / 2;
		int y_best = (int) tmpBlock / 2;

		while (s != 1) {
			double errDuringStep = Double.MAX_VALUE;

			int x_stred = x_best;
			int y_stred = y_best;
			int x_begin = (int) x_stred - s;
			int y_begin = (int) y_stred - s;

			for (int j = x_begin; j <= (x_begin + 2 * s); j += s) {
				for (int e = y_begin; e <= (y_begin + 2 * s); e += s) {

					double tmpErrOfBlock = HelperClass.getErrorOfBlock(blockSize, area, block, 0, j - blockSize / 2,
							e - blockSize / 2);
					if (errDuringStep > tmpErrOfBlock) {
						errDuringStep = tmpErrOfBlock;
						vectors.set(positionInVector, 0, j - blockSize / 2);
						vectors.set(positionInVector, 1, e - blockSize / 2);
						x_best = j;
						y_best = e;
					}
				}
			}

			s = s / 2;
		}
	}
}
