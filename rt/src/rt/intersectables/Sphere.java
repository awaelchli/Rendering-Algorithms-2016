package rt.intersectables;

import rt.*;
import rt.materials.Diffuse;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * Created by Adrian on 25.02.2016.
 */
public class Sphere implements Intersectable {

    public static final Point3f DEFAULT_CENTER = new Point3f(0, 0, 0);
    public static final float DEFAULT_RADIUS = 1;
    public static final Material DEFAULT_MATERIAL = new Diffuse(new Spectrum(1, 1, 1));

    public Point3f center;
    public float radius;
    public Material material = DEFAULT_MATERIAL;

    public Sphere(Point3f center, float radius) {
        this.center = center;
        this.radius = radius;
    }

    public Sphere(){
        this(DEFAULT_CENTER, DEFAULT_RADIUS);
    }

    @Override
    public HitRecord intersect(Ray r) {
        // Notation following http://www.siggraph.org/education/materials/HyperGraph/raytrace/rtinter1.htm

        Vector3f d = new Vector3f(r.direction);
        d.normalize();
        Point3f o = new Point3f(r.origin);

        float b = 2 * (d.x * (o.x - center.x) + d.y * (o.y - center.y) + d.z * (o.z - center.z));
        float c1 = (o.x - center.x);
        float c2 = (o.y - center.y);
        float c3 = (o.z - center.z);
        float c = c1 * c1 + c2 * c2 + c3 * c3 - radius * radius;

        // a = 1
        float discriminant = b * b - 4 * c;

        if (discriminant < 0){
            // No intersection
            return null;
        }

        float t;
        float t0 = (-b - (float) Math.sqrt(discriminant)) / 2;

        if(t0 > 0){
            t = t0;
        } else {
            float t1 = (-b + (float) Math.sqrt(discriminant)) / 2;
            if(t1 > 0){
                t = t1;
            } else {
                // No intersection
                return null;
            }
        }

        Point3f position = new Point3f(d);
        position.scale(t);
        position.add(o);

        Vector3f normal = new Vector3f(position);
        normal.sub(this.center);
        normal.scale(1 / this.radius);

        Vector3f w = new Vector3f(d);
        w.negate();

        float u = 0, v = 0;

        return new HitRecord(t, position, normal, w, this, material, u, v);
    }
}
