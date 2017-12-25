package worldbuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Worldbuilder {
	static double r = 6371; //Radius of earth in km
	static float N = 5000000; //5 millions point for this radius approx 10km resolution
	static int n = (int)Math.round(Math.sqrt(N*Math.PI)/2); //number of parallels
	static double h = r*Math.PI/n; //step
	static int ltdn; //number of points dependent on latitude
	static Point3D grid[][] = new Point3D[n][]; //the grid of points on the surface of the sphere
	static Point2D proj[][] = new Point2D[n][]; //the projected points, they will be colored
	static int i,j,k,swi;
	static Point3D temp = new Point3D(); //used in several points when working with arrays of points
	static double sint,cost; // necessary while using spherical coordinates
	static double water = 1260000000; // cubic km of water on earth, roughly
	static int nasteroids = 1000; //number of asteroids
	static double bucket = 0; //will be of use when we need to fill the oceans
	static double area = 4*(Math.PI)*r*r/N; //approx area per point
	static double sealevel, maxhgt, minhgt, relative; //will be used in the colouring process
	public static void main(String[] args){
		for(i=0; i<n; i++){ //creating the grid on the 3d sphere
			
			sint = (Math.sin(i * Math.PI/n));
			cost = (Math.cos(i * Math.PI/n));
			ltdn = (int) Math.ceil(2*n*Math.abs(sint));
			grid[i] = new Point3D[ltdn];
			for(j=0; j<ltdn; j++){
				temp.x = r*sint*(Math.cos((j * 2 * Math.PI)/ltdn));
				temp.y = r*sint*(Math.sin((j * 2 * Math.PI)/ltdn));
				temp.z = r*cost;
				temp.theta = (j * 2 * Math.PI)/ltdn;
				grid[i][j] = temp;
				System.out.println("Sin is " + sint + ", we have " + ltdn + " points in this parallel, and the i and j cycle numbers are " + i + " and " + j);
			}
				
		}
		for(k=0; k<nasteroids; k++){
			launchAsteroid();
		}
		fillTheOceans();
		for(i=0; i<n; i++){
			Point3D gridtemp[] = grid[i];
			for(j=0; j<gridtemp.length; j++){
				Point3D temp = grid[i][j];
				proj[i][j]=projectionMercator(temp);
			}	
		}
	
		Showtellpanel showp = new Showtellpanel(proj, n);
		Showandtell show = new Showandtell();
		show.setLocation(250, 150);
		show.getContentPane().add(showp);
	
	}
	
	public static void launchAsteroid(){
		Random rng = new Random();
		double rndradius = 20+(rng.nextGaussian())*10; //random radius of asteroid, distributed normally with 20mean and 10variance
		int rndinout = 1;
		boolean disc = rng.nextBoolean();//determines whether the asteroid is incoming or outgoing
		if (disc){
			rndinout = -1;
		}
		int rndpld = rng.nextInt(n); //random parallel of impact
		int maxrndtheta = (int) Math.ceil(2*n*(Math.abs(Math.sin((rndpld * Math.PI)/n))));
		int rndtheta = rng.nextInt(maxrndtheta); //random theta angle of impact
		Point3D impactPoint = grid[rndpld][rndtheta];
		double rndwidth = rndradius + rng.nextGaussian()*10; 
		double rndheight = rndradius + rng.nextGaussian()*10; // height and width are smeared so the asteroids will be elliptical
		int astrange = ((int)Math.round(rndwidth/h) + 1); //how many parallels will the asteroids impact, max
		for(i=rndpld-astrange; i<=rndpld+astrange; i++){
			Point3D gridtemp[] = grid[i];
			for(j=0; j<gridtemp.length; ++j){
				Point3D temp = grid[i][j];
				double dist = distance(temp, impactPoint);
				if (dist<rndwidth){
					temp.hgt = (rndinout)*rndheight*(Math.sqrt(1- (dist*dist)/(rndwidth*rndwidth)));
					grid[i][j] = temp;
				}	
			}
			
		}
	}
	
	public static void fillTheOceans(){
		ArrayList<Double> altitudes = new ArrayList<Double>();
		for(i=0; i<n; i++){
			Point3D gridtemp[] = grid[i];
			for(j=0; j<gridtemp.length; j++){
				Point3D temp = grid[i][j];
				altitudes.add(temp.hgt);
			}	
		}
		Collections.sort(altitudes);
		maxhgt=altitudes.get(altitudes.size()-1);
		minhgt=altitudes.get(0);
		k=0;
		while(water>0){
			bucket = altitudes.get(k)-altitudes.get(k+1);
			k++;
			water = water - k*bucket; //we'll run out of water eventually
		}
		sealevel = altitudes.get(k+1);
	}
	
	public static Point2D projectionMercator(Point3D a){
		Point2D temp2D = new Point2D();
		temp2D.y = a.z;
		if (temp2D.x>0){
			temp2D.x = Math.asin((a.x)/(a.y));
		} else {
			temp2D.x = Math.asin((a.x)/(a.y)) + Math.PI;
		}
		if (sealevel>a.hgt) { //relative will be between 0 an 1
			relative = (a.hgt - sealevel)/(maxhgt - sealevel);
			if (relative<=1)swi = 0;
			if (relative<0.8)swi = 1;
			if (relative<0.6)swi = 2;
			if (relative<0.4)swi = 3;
			if (relative<0.2)swi = 4;
			switch (swi){
				case 0: //saddlebrown
					temp2D.red = 139;
					temp2D.green = 69; 
					temp2D.blue = 19;
					break;
				case 1: //sienna
					temp2D.red = 160;
					temp2D.green = 82; 
					temp2D.blue = 45;
					break;
				case 2: //peru
					temp2D.red = 205;
					temp2D.green = 133; 
					temp2D.blue = 63;
					break;
				case 3: //olive
					temp2D.red = 128;
					temp2D.green = 128; 
					temp2D.blue = 0;
					break;
				case 4: //olivedrab
					temp2D.red = 107;
					temp2D.green = 142; 
					temp2D.blue = 33;
					break;
			}
		} else {
			relative = (sealevel - a.hgt)/(sealevel - minhgt);
			if (relative<=1)swi = 0;
			if (relative<0.8)swi = 1;
			if (relative<0.6)swi = 2;
			if (relative<0.4)swi = 3;
			if (relative<0.2)swi = 4;
			switch (swi) {
				case 0: //navy
					temp2D.red = 0;
					temp2D.green = 0; 
					temp2D.blue = 128;
					break;
				case 1: //darkblue
					temp2D.red = 0;
					temp2D.green = 0; 
					temp2D.blue = 139;
					break;
				case 2: //mediumblue
					temp2D.red = 0;
					temp2D.green = 0; 
					temp2D.blue = 205;
					break;
				case 3: //blue
					temp2D.red = 0;
					temp2D.green = 0; 
					temp2D.blue = 255;
					break;
				case 4: //royalblue
					temp2D.red = 65;
					temp2D.green = 105; 
					temp2D.blue = 225;
					break;
			}
		}
		return temp2D;
	}

	
	public static double distance(Point3D a, Point3D b){
		double dist = Math.sqrt((a.x - b.x)*(a.x - b.x) + (a.y - b.y)*(a.y - b.y) + (a.y - b.y)*(a.y - b.y));
		return dist;	
	}
}
