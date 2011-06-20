public class RCPoint {
	int x;
	int y; 		// coordinates on screen
	int realY;  // coordinates in task
	char letter;
	public RCPoint(int x, int y, int realY, char a) {
		this.x = x;
		this.y = y;
		this.realY = realY;
		this.letter = a;
	}
	public String toString() {
		return "Point " + this.letter + "  [" + this.x + ", " + this.realY + "]";
	}
}
