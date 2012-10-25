package kr.teamdeer.snap;

/* from another project : javaGeom
* File Vector3D.java 
*
* Project : Java Geometry Library
*
* ===========================================
* 
* This library is free software; you can redistribute it and/or modify it 
* under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 2.1 of the License, or (at
* your option) any later version.
*
* This library is distributed in the hope that it will be useful, but 
* WITHOUT ANY WARRANTY, without even the implied warranty of MERCHANTABILITY
* or FITNESS FOR A PARTICULAR PURPOSE.
*
* See the GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this library. if not, write to :
* The Free Software Foundation, Inc., 59 Temple Place, Suite 330,
* Boston, MA 02111-1307, USA.
*/

public class Vector2d {

	public final static double  ACCURACY  = 1e-12;
	
	public double x = 1;
	public double y = 0;
	
	public final static double dotProduct(Vector2d v1, Vector2d v2) {
		return v1.x*v2.x+v1.y*v2.y;
	}
	
	public final static Vector2d crossProduct(Vector2d v1, Vector2d v2) {
		return new Vector2d(v1.y*v2.x-v1.x*v2.y, v1.x*v2.y-v1.y*v2.x);
	}
	
	public final static boolean isColinear(Vector2d v1, Vector2d v2) {
        return Vector2d.crossProduct(v1.normalize(),
                v2.normalize()).norm()<ACCURACY;
    }
	
	public final static boolean isOrthogonal(Vector3d v1, Vector3d v2) {
        return Vector3d.dotProduct(v1.normalize(), v2
                .normalize())<ACCURACY;
    }

	public Vector2d() {
        this(1, 0);
    }
	
	public Vector2d(double x, double y) {
        this.x = x;
        this.y = y;
    }
	
	public Vector2d(Point2 point) {
        this(point.x, point.y);
    }
	
	public Vector2d(Point2 point1, Point2 point2) {
        this(point2.x-point1.x, point2.y-point1.y);
    }

	public Vector2d plus(Vector2d v) {
        return new Vector2d(x+v.x, y+v.y);
    }
	
	public Vector2d minus(Vector2d v) {
        return new Vector2d(x-v.x, y-v.y);
    }
	
	public Vector2d times(double k) {
        return new Vector2d(k*x, k*y);
    }
	
	public Vector2d opposite() {
        return new Vector2d(-x, -y);
    }
	
	public double norm() {
        return Math.hypot(x, y);
    }
	
	public double normSq() {
		return x * x + y * y;
    }
	
	public Vector2d normalize() {
        double r = this.norm();
        return new Vector2d(this.x/r, this.y/r);
    }
	
	@Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector2d))
            return false;

        Vector2d v = (Vector2d) obj;
        if (Math.abs(x-v.x)>ACCURACY)
            return false;
        if (Math.abs(y-v.y)>ACCURACY)
            return false;
        return true;
    }
	
	@Override
	public int hashCode() {
	    assert false : "Unexpected Function Call";
	    return 422; // Appropriate value
	}
	
}
