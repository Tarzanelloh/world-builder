package worldbuilder;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import javax.swing.JPanel;

public class Showtellpanel extends JPanel 
{static double r=6371; //Radius of earth in km
static int u,v,n;
Point2D[][] points;
	public Showtellpanel(Point2D[][] a, int p)
	{points = a;
	n=p;
	}
	@Override
	public void paintComponent(Graphics g) 
	{
	super.paintComponent(g); //should clear? Man Im bad at this
	Graphics2D g2 = (Graphics2D) g;
	for(u=0; u<n; u++)
	{
	Point2D gridtemp[] = points[u];
	for(v=0; v<gridtemp.length; v++)
		{
		Point2D temp = points[u][v];
		int x =  Math.round((float) (1000*(temp.x + Math.PI/2)/(2*Math.PI)));
		int y =  Math.round((float) (500*(temp.x + Math.PI/2)/(2*r)));
		g2.setColor(new Color(temp.red, temp.green, temp.blue));
		g2.draw(new Line2D.Double(x, y, x, y));
		}	
	}
	}
}
