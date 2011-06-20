import java.util.Comparator;


public class RCPointComparator implements Comparator<RCPoint> {
	
	RCPoint A;
	public RCPointComparator(RCPoint somePoint) {
		this.A = somePoint;
	} 
	
	public double cosAN(RCPoint N) throws IllegalArgumentException{
		double NAx = N.x  - A.x;
		double NAy = N.realY - A.realY; // A has the lowest Y, so this is non-negative
		return NAx / Math.sqrt(NAx * NAx + NAy * NAy);
	}
	
	@Override
	public int compare(RCPoint B, RCPoint C) {
		double diff = cosAN(B) - cosAN(C);
		if (diff > 0)
			return 1;
		else if (diff < 0)
			return -1;		
		return 0;
	}

}
