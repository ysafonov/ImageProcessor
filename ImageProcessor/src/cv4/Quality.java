package cv4;

public class Quality {
	
	public double getMse(int[][] original, int[][] edited)
	{
		double MSE = 0;
		for(int i=0; i < (original.length-1); i++)
		{
			for(int j=0; j < (original[0].length-1); j++)
			{
				MSE += Math.pow((original[i][j]-edited[i][j]),2);
			}
		}
		return MSE/(original.length*original[0].length);
	}
	
	public double getPsnr(int[][] original, int[][] edited) 
	{
		return 10*Math.log10(((Math.pow(2, 8)-1)*(Math.pow(2, 8)-1))/this.getMse(original, edited));	
	}

}
