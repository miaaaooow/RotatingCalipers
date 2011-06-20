/**
 * Finding Minimal Bounding Box.
 * 
 * @author Maria Mateva, FMI, Sofia University
 */

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;



public class WhiteBoard extends JFrame{

	private static final long serialVersionUID = 1L;
	
	private final static int BOARDX 		= 1000;
	private final static int BOARDY 		= 600;
	private final static int BUTTONY 		= 60;
	private final static int POINT_SIZE 	= 10;
	private final static int MAX_RCPointS_NUMBER = 100;
	
	private final static String CONVEX_HULL = "CONVEX HULL";
	private final static String ROTATE_CALIPERS = "ROTATING CALIPERS";
	private final static String PLAY_MOTION = "PLAY";
	private final static String CLEAR_SCENE = "CLEAR SCENE";

	private Graphics background;
	private Image 	 backbuffer;
	private JPanel   buttons;
	private JPanel	 scene;
	
	private boolean startMode = true;
	private boolean convHullUpdated = false;
	
	private RCPoint [] RCPoints = new RCPoint [MAX_RCPointS_NUMBER];
	private RCPoint [] convexHull;
	
	private int RCPointIndex; // the RCPoints index to be filled
	private int chIndex; //last index of the convex hull
	
	
	public WhiteBoard() {
		super("Метод на шублера");
		setLocation(150, 100);
		RCPointIndex = 0;	
		chIndex = 0; 
		
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) { 
	            if (RCPointIndex < MAX_RCPointS_NUMBER) {
	            	convHullUpdated = false;
		            int x1 = me.getX();
		            int y1 = me.getY() - BUTTONY;
		            
		            char C = (char) (RCPointIndex + 'C');
		            drawChar(C, x1 + POINT_SIZE / 2, y1 + POINT_SIZE / 2);
		            
	            	RCPoint p = new RCPoint(x1, y1, me.getY(), C);
	            	background.fillRect(x1 - POINT_SIZE/2, y1 - POINT_SIZE/2, POINT_SIZE, POINT_SIZE);	            	
	            	RCPoints[RCPointIndex] = p;
	            
	            	if (RCPointIndex > 0)  {
	            		//background.setColor(Color.LIGHT_GRAY);
	            		//background.drawLine(x1, y1, RCPoints[RCPointIndex - 1].x, RCPoints[RCPointIndex - 1].y);
	            		background.setColor(Color.BLACK);
	            		
	            	}	            
	            	RCPointIndex++;
	            
	            	repaint();
	            	me.consume();
	            }	            
	        } 

		});
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(BOARDX, BOARDY + BUTTONY);	
		setVisible(true);
		setFocusable(true);
		requestFocus(); 
	}
	
	private void drawChar(char a, int x, int y) {
		char [] data = new char [1];
		data[0] = a;
		background.drawChars(data, 0, 1, x, y);
	}
	
	/*
	 * Convex Hull, Graham Scan
	 */
	public void findConvexHull() {
		if (RCPointIndex >= 3) {
			convexHull = new RCPoint [RCPointIndex + 1];
			
			if (RCPointIndex == 3){
				for (int i = 0; i < 3; i++) {
					convexHull[i] = RCPoints[i];
				}
				convexHull[3] = convexHull[0];
				chIndex = 3;
				paintConvexHull(3);
			}
			else if (RCPointIndex > 3) {
				int minY = Integer.MAX_VALUE;
				int minIndex = 0;
				// finding the lowest Y RCPoint - with index minIndex
				for (int i = 0; i < RCPointIndex; i++) {
					if (RCPoints[i].realY < minY || (RCPoints[i].realY == minY && RCPoints[i].x < RCPoints[minIndex].x)) {
						minIndex = i;
						minY = RCPoints[i].realY;
					}				
				}	
				RCPointComparator comparator = new RCPointComparator(RCPoints[minIndex]);
				
				convexHull[0] = RCPoints[minIndex]; 	//the first real RCPoint
				RCPoints[minIndex] = RCPoints[0];		//swap not to lose information
				RCPoints[0] = convexHull[0];			
				RCPoints[RCPointIndex] = RCPoints[0];		//close the RCPoints hull
				
				Arrays.sort(RCPoints, 1, RCPointIndex, comparator);
				
				convexHull[1] = RCPoints[1];
				convexHull[2] = RCPoints[2]; 				
				int convHullInd = 1;  
				
				for (int i = 2; i <= RCPointIndex; i++) {
					while (leftTurn(convexHull[convHullInd - 1], convexHull[convHullInd], RCPoints[i]) >= 0 ){ 
						if (convHullInd == 1) {
							convexHull[convHullInd] = RCPoints[i]; 
							i++;
						} else {
							convHullInd--;
						}
					} 
					convHullInd++;
					convexHull[convHullInd] = RCPoints[i];
				}			
				chIndex = convHullInd;
				paintConvexHull(chIndex);
			}
			convHullUpdated = true;
		}
		else {
			System.out.println("Трябват ви поне 3 точки за да търсите изпъкнала обвивка!");
		}
		
	}
	

	public static int leftTurn(RCPoint m1, RCPoint m2, RCPoint m3) {		
		return (m2.x - m1.x) * (m3.realY - m1.realY) - (m2.realY - m1.realY) * (m3.x - m1.x);
	}
	
	public void rotateCalipers() {
		if (!convHullUpdated) {
			findConvexHull();
		}
		
		double minS = Double.MAX_VALUE;
		RCPoint A = null, B = null, L = null, R = null, U = null;
		
		if (chIndex == 3) {
			A = L = convexHull[0];
			B = R = convexHull[1];
			U = convexHull[2];
			minS = vectorMultiplication(A, B, U) / distAB(A, B);
		}
		else {
			for (int j = 0; j < chIndex; j++) {
				RCPoint A1 = convexHull[j];    
				RCPoint B1 = convexHull[j + 1];
				/*
				 * find a rectangle for these A and B
				 */
				System.out.println("RC: " + A1.toString() + " " + B1.toString());
				int minInd = -1;
				int maxInd = -1;
				int UInd = -1;
				
				int minScMult = Integer.MAX_VALUE;
				int maxScMult = Integer.MIN_VALUE;
				double maxb = 0; // Distance between A, B and U; b side of the rectangle
				
				double AB = distAB(A1, B1);
				for (int i = 0; i <= chIndex; i++) {
					int temp = scalarMultiplication(A1, B1, A1, convexHull[i]);
					double dist = vectorMultiplication(A1, B1, convexHull[i]) / AB;
					if (temp < minScMult){
						minScMult = temp;
						minInd = i;
					}
					if (temp > maxScMult) {
						maxScMult = temp;
						maxInd = i;
					}
					if (dist > maxb) {
						maxb = dist;
						UInd = i;
					}
				}
				/*
				 * for that particular A and B we have the following R, L and U
				 */			
				RCPoint R1 = convexHull[maxInd];
				RCPoint L1 = convexHull[minInd];
				RCPoint U1 = convexHull[UInd];
				
				// a = Math.abs(scalarMultiplication(A1, B1, L1, R1) / AB);
				double currentS = Math.abs(scalarMultiplication(A1, B1, L1, R1) / AB) * maxb;
				
				if (currentS <= minS) {
					A = A1; B = B1; L = L1; R = R1; U = U1;
					minS = currentS;
				}
			}
		}
		
		System.out.println(minS);
		System.out.println("A= " + A.toString() + "B= " + B.toString());
		System.out.println("L= " + L.toString() + "R= " + R.toString() + "U= " + U.toString());
		
		/*
		 * Q-------------U----------P
		 * |                        |
		 * L                        |
		 * |                        R
		 * |                        |
		 * M--------A---B-----------N
		 * 
		 * MN = AB : ax + b = y
		 * QM : a1x + b1 = y
		 * PN : a2x + b2 = y
		 * QP : ax + b3 = y
		 * (MN || QP)
		 */
		
		double a1, a2, b1, b2, b3, a, b;
		double xm = 0, ym = 0, xn = 0, yn = 0, xp = 0, yp = 0, xq = 0, yq = 0;
		a = (double) (A.realY - B.realY) / ((double)(A.x - B.x));
		b = A.realY - a * A.x;

		if (a != 0) {
			a1 = - 1 / a;
			b1 = (double) L.realY - a1 * (double) L.x;
			xm = (b - b1) / (a1 - a);
			ym = a * xm + b;
			
			a2 = a1;
			b2 = (double) R.realY - a2 * (double) R.x;
			xn = (b - b2) / (a2 - a);
			yn = a * xn + b;
			
			b3 = (double) U.realY - a * (double) U.x;
			xq = (b1 - b3) / (a - a1);
			yq = a1 * xq + b1;
			
			xp = (b2 - b3) / (a - a2);
			yp = a2 * xp + b2;			
		} 
		else {
			// a = 0, AB || Ox'
			xm = L.x; ym = A.realY;
			xn = R.x; yn = ym;
			xq = xm; yq = U.realY;
			xp = xn; yp = yq;
		}
		
		RCPoint [] rectangle = new RCPoint[4];
		rectangle[0] = new RCPoint((int)xm, (int)ym - BUTTONY,(int) ym, 'M');
		rectangle[1] = new RCPoint((int)xn, (int)yn - BUTTONY,(int) yn, 'N');
		rectangle[2] = new RCPoint((int)xp, (int)yp - BUTTONY,(int) yp, 'P');		
		rectangle[3] = new RCPoint((int)xq, (int)yq - BUTTONY,(int) yq, 'Q');
		
		for (RCPoint p : rectangle) 
			System.out.println(p.toString());
		
		paintRectangle(rectangle);

	}
	
		
	
	/**
	 * Scalar multiplication of vectors AB and CD
	 */
	public static int scalarMultiplication (RCPoint A, RCPoint B, RCPoint C, RCPoint D) {
		return (A.x - B.x) * (C.x - D.x) + (A.realY - B.realY) * (C.realY - D.realY);
	}
	
	/**
	 * AB x AC
	 */	
	public static double vectorMultiplication(RCPoint A, RCPoint B, RCPoint C) {
		double AB = distAB(A, B);
		double BC = distAB(B, C);
		double CA = distAB(C, A);
		double p = (AB + BC + CA) / 2;
		double Sabc = Math.sqrt(p * (p - AB) * (p - BC) * (p - CA));
		return Sabc * 2;	
	}
	
	public static double distAB(RCPoint A, RCPoint B) {
		return Math.sqrt((A.x - B.x) * (A.x - B.x) + (A.realY - B.realY) * (A.realY - B.realY));
	}
	
	
	public void paintConvexHull (int convHullInd) {
		System.out.println("Convex Hull!");
		background.setColor(Color.BLUE);
		for (int i = 0; i < convHullInd - 1; i++) {
			background.drawLine(convexHull[i].x, convexHull[i].y, convexHull[i + 1].x, convexHull[i + 1].y);
			System.out.println(convexHull[i].toString());
		}
		background.drawLine(convexHull[0].x, convexHull[0].y, convexHull[convHullInd - 1].x, convexHull[convHullInd - 1].y);
		repaint();
		background.setColor(Color.BLACK);
	}
	
	public void paintRectangle(RCPoint [] rectangle) {
		background.setColor(Color.RED);
		for (int i = 0; i < 3; i++) {
			background.drawLine(rectangle[i].x, rectangle[i].y, rectangle[i + 1].x, rectangle[i + 1].y);
            drawChar(rectangle[i].letter, rectangle[i].x + POINT_SIZE / 2, rectangle[i].y + POINT_SIZE / 2);
		}
		background.drawLine(rectangle[3].x, rectangle[3].y, rectangle[0].x, rectangle[0].y);
		drawChar(rectangle[3].letter, rectangle[3].x + POINT_SIZE / 2, rectangle[3].y + POINT_SIZE / 2);
		System.out.println("Draw!");
		repaint();
		background.setColor(Color.BLACK);
	}
	
	public void clearScreen () {
		System.out.println("CLEAR!");
	    redraw();
	    repaint();
	    RCPoints = new RCPoint [MAX_RCPointS_NUMBER];
	    RCPointIndex = 0;
	    chIndex = 0;
	}

	
	public void paint(Graphics g) {
	    if (startMode)
	    	initGraphics(this.getContentPane());
	    g.drawImage(backbuffer, 0, BUTTONY, scene);
	}
	
	
	private void initGraphics (Container pane) {
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		
		backbuffer = createImage(BOARDX, BOARDY);
		background = backbuffer.getGraphics();
		redraw();
		
	    buttons = getButtonPanel();	    
	    scene = new JPanel();
		scene.setSize(BOARDX, BUTTONY);		
		pane.add(buttons);
		pane.add(scene);
		
		startMode = false;
	}
	
	protected void redraw() {
	    background.setColor(Color.WHITE);
	    background.fillRect(0, 0, BOARDX, BOARDY + BUTTONY);
	    background.setColor(Color.BLACK);
	}
	
	private JPanel getButtonPanel() {
		JPanel jpb = new JPanel();
		jpb.setSize(BOARDX, BUTTONY);
		ActionListener listener = new ActionListener() {
						
			@Override
			public void actionPerformed(ActionEvent e) {
				String action = e.getActionCommand();
				if (action.equals(ROTATE_CALIPERS)) {
					rotateCalipers();            	
				} else if (action.equals(CONVEX_HULL)) {
					findConvexHull();
	            } else if (action.equals(CLEAR_SCENE)) {
	            	clearScreen();
	            }
			}
		};
		
		addButton("Изпъкнала обвивка", CONVEX_HULL, listener, jpb);
		addButton("Метод на шублера", ROTATE_CALIPERS, listener, jpb);
		addButton("Изчисти!", CLEAR_SCENE, listener, jpb);
		
		return jpb;
	}
	
	private void addButton(String name, String caughtAction, ActionListener listener, JPanel container) {
		JButton button = new JButton(name);
		button.setActionCommand(caughtAction);
		button.addActionListener(listener);
		container.add(button);		
	}
	
	public static void main(String[] args) {
		new WhiteBoard();	
	}

}
