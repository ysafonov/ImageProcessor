package cv3;

public class Quality {
	
	public double getMse(int[][] original, int[][] edited)
	{
		double MSE = 0;
		for(int i=0; i < (original.length-1); i++)
		{
			for(int j=0; j < (original[0].length-1); j++)
			{
				MSE += (original[i][j]-edited[i][j])*(original[i][j]-edited[i][j]);
			}
		}
		return 1/(original.length*original[0].length)*MSE;
	}
	
	public double getPsnr(int[][] original, int[][] edited) 
	{
		return 10*Math.log10(((Math.pow(2, 8)-1)*(Math.pow(2, 8)-1))/this.getMse(original, edited));	
	}

}
