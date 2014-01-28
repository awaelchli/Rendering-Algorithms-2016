package rt;
import javax.vecmath.*;

public class Camera {

	private Matrix4f m;
	private Vector3f eye;
	
	public Camera(Vector3f eye, Vector3f lookAt, Vector3f up, float fov, float aspect, int width, int height)
	{
		this.eye = new Vector3f(eye);
		
		// Make camera matrix
		Vector3f x, y, z;

		z = new Vector3f(eye);
		z.sub(lookAt);
		z.normalize();

		x = new Vector3f();
		x.cross(up, z);
		x.normalize();

		y = new Vector3f();
		y.cross(z, x);
		y.normalize();

		Matrix4f c = new Matrix4f(x.x, y.x, z.x, eye.x, 
								  x.y, y.y, z.y, eye.y,
								  x.z, y.z, z.z, eye.z,
								  0,   0,   0,   1);

		// Make projection matrix
		float left, right, top, bottom;
		float near, far;
		near = 1.f;
		far = 10.f;
		
		top = (float)((double)near * Math.tan(Math.toRadians((double)(fov/2.f))));
		right = top * aspect;
		bottom = -top;
		left = -right;
		
		Matrix4f p = new Matrix4f();
		p.setIdentity();
		p.setElement(0,0, (2*near)/(right-left));
		p.setElement(0,2, (right+left)/(right-left));
		p.setElement(1,1, (2*near)/(top-bottom));
		p.setElement(1,2, (top+bottom)/(top-bottom));
		p.setElement(2,2, -(far+near)/(far-near));
		p.setElement(2,3, -(2*far*near)/(far-near));
		p.setElement(3,2, -1.f);
		p.setElement(3,3, 0.f);		
		p.invert();
		
		// Make viewport matrix
		Matrix4f v = new Matrix4f();
		v.setIdentity();
		v.setM00((float)width/2.f);
		v.setM03((float)width/2.f);
		v.setM11((float)height/2.f);
		v.setM13((float)height/2.f);
		v.setM22(1.f);
		v.setM23(0.f);
		v.invert();
		
		// Make the matrix that transforms a viewport pixel coordinate
		// to a world space point
		p.mul(v);
		c.mul(p);
		m = c;
	}
	
	Ray makeWorldSpaceRay(float x, float y)
	{
		Vector4f d = new Vector4f(x,y,-1.f,1.f);
		m.transform(d);
		Vector3f dir = new Vector3f();
		dir.sub(new Vector3f(d.x, d.y, d.z), eye);
		Ray r = new Ray(new Vector3f(eye), dir);
		return r;
	}
}
