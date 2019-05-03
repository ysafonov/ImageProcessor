package MPEG;

import Jama.Matrix;

/**
 * This java class represents abstract class containing SearchOneAtTime search
 * technique.
 * 
 * @author Yehor Safonov; id: 185942
 */

public abstract class SearchOneAtTime {

	public static Matrix oneAtTimeSearch(Matrix extend, Matrix second, int size) {
		int columDem = (int) (second.getRowDimension() / size) * (second.getColumnDimension() / size);
		Matrix movementPredictionMatrix = new Matrix(columDem, 2);
		int c = 0;

		for (int xpos = 0; xpos < second.getRowDimension(); xpos += size) {
			for (int ypos = 0; ypos < second.getColumnDimension(); ypos += size) {
				Matrix block = HelperClass.createMacroBlock(second, xpos, ypos, size);
				Matrix area = HelperClass.createMacroBlock(extend, xpos, ypos, 2 * size);
				searchMovementOneAtTime(movementPredictionMatrix, area, block, size, c);
				c++;
			}
		}
		return movementPredictionMatrix;
	}

	private static void searchMovementOneAtTime(Matrix vectors, Matrix area, Matrix block, int blockSize, int c) {
		int tmpBlock = blockSize * 2;
		int x_center = (int) tmpBlock / 2;
		int y_center = (int) tmpBlock / 2;
		int counter = blockSize / 2;

		System.out.println("x_center " + x_center);
		System.out.println("x_center " + y_center);
		int x_best = positionOfTheBestX(x_center, y_center, blockSize, area, block, counter);
		counter = blockSize / 2;
		int y_best = positionOfTheBestY(x_center, y_center, blockSize, area, block, counter);
		vectors.set(c, 0, x_best);
		vectors.set(c, 1, y_best);

	}

	private static int positionOfTheBestX(int x, int y, int blockSize, Matrix area, Matrix block, int counter) {

		if (counter <= 0)
			return (x - blockSize / 2);

		double errDuringStep = Double.MAX_VALUE;
		int position = 0;

		int startPosition = x - 1;
		for (int i = 0; i < 3; i++) {
			double tmpErr = HelperClass.getErrorOfBlock(blockSize, area, block, 0, startPosition - blockSize / 2,
					y - blockSize / 2);
			if (errDuringStep > tmpErr) {
				errDuringStep = tmpErr;
				position = i;
			}
			startPosition++;
		}
		switch (position) {
		case 0:
			return positionOfTheBestX(--x, y, blockSize, area, block, --counter);
		case 1:
			return (x - blockSize / 2);
		case 2:
			return positionOfTheBestX(++x, y, blockSize, area, block, --counter);
		}
		return 0;
	}

	private static int positionOfTheBestY(int x, int y, int blockSize, Matrix area, Matrix block, int counter) {

		if (counter <= 0)
			return (y - blockSize / 2);

		double errDuringStep = Double.MAX_VALUE;
		int position = 0;
		int startPosition = y - 1;
		for (int i = 0; i < 3; i++) {
			double tmpErr = HelperClass.getErrorOfBlock(blockSize, area, block, 0, x - blockSize / 2,
					startPosition - blockSize / 2);
			if (errDuringStep > tmpErr) {
				errDuringStep = tmpErr;
				position = i;
			}
			startPosition++;
		}
		switch (position) {
		case 0:
			return positionOfTheBestY(x, --y, blockSize, area, block, --counter);
		case 1:
			return (y - blockSize / 2);
		case 2:
			return positionOfTheBestY(x, ++y, blockSize, area, block, --counter);
		}
		return 0;
	}
}
