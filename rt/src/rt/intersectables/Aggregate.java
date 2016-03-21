package rt.intersectables;

import java.util.Iterator;

import rt.HitRecord;
import rt.Intersectable;
import rt.Ray;
import rt.bsp.BoundingBox;

/**
 * A group of {@link Intersectable} objects.
 */
public abstract class Aggregate implements Intersectable {

	public HitRecord intersect(Ray r) {

		HitRecord hitRecord = null;
		float t = Float.MAX_VALUE;
		
		// Intersect all objects in group, return closest hit
		Iterator<Intersectable> it = iterator();
		while(it.hasNext())
		{
			Intersectable o = it.next();
			HitRecord tmp = o.intersect(r);
			if(tmp!=null && tmp.t<t)
			{
				t = tmp.t;
				hitRecord = tmp;
			}
		}
		return hitRecord;
	}
	
	public abstract Iterator<Intersectable> iterator();

	public int count()
	{
		int count = 0;
		Iterator<Intersectable> it = iterator();
		while(it.hasNext()) {
			count++;
		}
		return count;
	}

	@Override
	public BoundingBox getBoundingBox()
	{
		Iterator<Intersectable> it = iterator();

		if (!it.hasNext()) {
			return null;
		}

		BoundingBox boundingBox = it.next().getBoundingBox();

		while(it.hasNext())
		{
			boundingBox.add(it.next().getBoundingBox());
		}
		return boundingBox;
	}
}
