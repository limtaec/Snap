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

public class Vector3d {
	
	public final static double  ACCURACY  = 1e-12;
	
	public double x = 1;
	public double y = 0;
	public double z = 0;
	
	public final static double dotProduct(Vector3d v1, Vector3d v2) {
		return v1.x*v2.x+v1.y*v2.y+v1.z*v2.z;
	}
	
	public final static Vector3d crossProduct(Vector3d v1, Vector3d v2) {
		return new Vector3d(v1.y*v2.z-v1.z*v2.y, v1.z*v2.x-v1.x*v2.z, v1.x*v2.y
                -v1.y*v2.x);
	}
	
	public final static boolean isColinear(Vector3d v1, Vector3d v2) {
        return Vector3d.crossProduct(v1.normalize(),
                v2.normalize()).norm()<ACCURACY;
    }
	
	public final static boolean isOrthogonal(Vector3d v1, Vector3d v2) {
        return Vector3d.dotProduct(v1.normalize(), v2
                .normalize())<ACCURACY;
    }

	public Vector3d() {
        this(1, 0, 0);
    }
	
	public Vector3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
	
	public Vector3d(Point3 point) {
        this(point.x, point.y, point.z);
    }
	
	public Vector3d(Point3 point1, Point3 point2) {
        this(point2.x-point1.x, point2.y-point1.y, point2.z-point1.z);
    }

	public Vector3d plus(Vector3d v) {
        return new Vector3d(x+v.x, y+v.y, z+v.z);
    }
	
	public Vector3d minus(Vector3d v) {
        return new Vector3d(x-v.x, y-v.y, z-v.z);
    }
	
	public Vector3d times(double k) {
        return new Vector3d(k*x, k*y, k*z);
    }
	
	public Vector3d opposite() {
        return new Vector3d(-x, -y, -z);
    }
	
	public double norm() {
        return Math.hypot(Math.hypot(x, y), z);
    }
	
	public double normSq() {
		return x * x + y * y + z * z;
    }
	
	public Vector3d normalize() {
        double r = this.norm();
        return new Vector3d(this.x/r, this.y/r, this.z/r);
    }
	
	@Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector3d))
            return false;

        Vector3d v = (Vector3d) obj;
        if (Math.abs(x-v.x)>ACCURACY)
            return false;
        if (Math.abs(y-v.y)>ACCURACY)
            return false;
        if (Math.abs(z-v.z)>ACCURACY)
            return false;
        return true;
    }
	
	@Override
	public int hashCode() {
	    assert false : "Unexpected Function Call";
	    return 423; // Appropriate value
	}
	
}
