package rt.cameras;

import rt.Camera;
import rt.Ray;

import javax.vecmath.*;

/**
 * Created by Adrian on 25.02.2016.
 */
public class PinholeCamera implements Camera {

    public static final float DEFAULT_NEAR = 1;
    public static final float DEFAULT_FAR = 10;

    private Vector3f position;
    private Vector3f up;
    private Vector3f lookAt;
    private float verticalFOV;
    private float aspect;
    private int width;
    private int height;
    private float near;
    private float far;

    /**
     * Transformation from viewport to world
     */
    private Matrix4f m;

    public PinholeCamera(Vector3f position, Vector3f lookAt, Vector3f up, float verticalFOV, float aspect, int width, int height, float near, float far) {
        this.position = position;
        this.up = up;
        this.lookAt = lookAt;
        this.verticalFOV = verticalFOV;
        this.aspect = aspect;
        this.width = width;
        this.height = height;
        this.near = near;
        this.far = far;

        // Make the matrix c*p*v that transforms a viewport pixel coordinate
        // to a world space point
        Matrix4f v = getViewportMatrix();
        v.invert();
        Matrix4f p = getProjectionMatrix();
        p.invert();
        Matrix4f c = getCameraToWorldMatrix();

        p.mul(v);
        c.mul(p);
        m = c;
    }

    /**
     * Pinhole camera with default near- and far clipping planes.
     */
    public PinholeCamera(Vector3f position, Vector3f lookAt, Vector3f up, float verticalFOV, float aspect, int width, int height){
        this(position, lookAt, up, verticalFOV, aspect, width, height, DEFAULT_NEAR, DEFAULT_FAR);
    }

    public Matrix4f getCameraToWorldMatrix() {

        Vector3f w = new Vector3f(this.position);
        w.sub(lookAt);
        w.normalize();

        Vector3f u = new Vector3f();
        u.cross(this.up, w);
        u.normalize();

        Vector3f v = new Vector3f();
        v.cross(w, u);

        Matrix4f c = new Matrix4f();
        c.setColumn(0, u.x, u.y, u.z, 0);
        c.setColumn(1, v.x, v.y, v.z, 0);
        c.setColumn(2, w.x, w.y, w.z, 0);
        c.setColumn(3, this.position.x, this.position.y, this.position.z, 1);

        return c;
    }

    public Matrix4f getWorldToCameraMatrix() {
        Matrix4f c = getCameraToWorldMatrix();
        c.invert();
        return c;
    }

    public Matrix4f getProjectionMatrix() {

        Matrix4f p = new Matrix4f();
        p.setIdentity();
        float top = (float) Math.tan(Math.toRadians(this.verticalFOV / 2));
        float right = this.aspect * top;

        p.m00 = 1 / right;
        p.m11 = 1 / top;
        p.m22 = (this.near + this.far) / (this.near - this.far);
        p.m32 = -1;
        p.m23 = 2 * this.near * this.far / (this.near - this.far);
        p.m33 = 0;

        return p;
    }

    public Matrix4f getViewportMatrix() {

        Matrix4f v = new Matrix4f();
        v.setIdentity();

        v.m00 = this.width / 2;
        v.m11 = this.height / 2;
        v.m22 = 1;
        v.m03 = this.width / 2;
        v.m13 = this.height / 2;
        v.m23 = 0;

        return v;
    }

    @Override
    public Ray makeWorldSpaceRay(int i, int j, float[] sample) {
        // Make point on image plane in viewport coordinates, that is range [0,width-1] x [0,height-1]
        // The assumption is that pixel [i,j] is the square [i,i+1] x [j,j+1] in viewport coordinates
        Vector4f d = new Vector4f((float) i + sample[0], (float) j + sample[1], -1.f, 1.f);

        // Transform it back to world coordinates
        m.transform(d);

        // Make ray consisting of origin and direction in world coordinates
        Vector3f dir = new Vector3f();
        dir.sub(new Vector3f(d.x, d.y, d.z), this.position);
        Ray r = new Ray(new Point3f(this.position), dir);
        return r;
    }
}
