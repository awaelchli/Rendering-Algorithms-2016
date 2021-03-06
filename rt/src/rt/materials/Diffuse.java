package rt.materials;

import javax.vecmath.Vector3f;

import rt.*;

/**
 * A basic diffuse material.
 */
public class Diffuse implements Material {

	Spectrum kd;
	
	/**
	 * Note that the parameter value {@param kd} is the diffuse reflectance,
	 * which should be in the range [0,1], a value of 1 meaning all light
	 * is reflected (diffusely), and none is absorbed. The diffuse BRDF
	 * corresponding to {@param kd} is actually {@param kd}/pi.
	 * 
	 * @param kd the diffuse reflectance
	 */
	public Diffuse(Spectrum kd)
	{
		this.kd = new Spectrum(kd);
		// Normalize
		this.kd.mult(1/(float)Math.PI);
	}
	
	/**
	 * Default diffuse material with reflectance (1,1,1).
	 */
	public Diffuse()
	{
		this(new Spectrum(1.f, 1.f, 1.f));
	}

	/**
	 * Returns diffuse BRDF value, that is, a constant.
	 * 
	 *  @param wOut outgoing direction, by convention towards camera
	 *  @param wIn incident direction, by convention towards light
	 *  @param hitRecord hit record to be used
	 */
	public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut, Vector3f wIn) {
		return new Spectrum(kd);
	}

	public boolean hasSpecularReflection()
	{
		return false;
	}
	
	public ShadingSample evaluateSpecularReflection(HitRecord hitRecord)
	{
		return null;
	}
	public boolean hasSpecularRefraction()
	{
		return false;
	}

	public ShadingSample evaluateSpecularRefraction(HitRecord hitRecord)
	{
		return null;
	}

	public ShadingSample getShadingSample(HitRecord hitRecord, float[] sample)
	{
		// Construct random direction over hemisphere
		float phi = (float) (2 * Math.PI * sample[1]);
		Vector3f direction = new Vector3f();
		float tmp = (float) Math.sqrt(sample[0]);
		direction.x = (float) (Math.cos(phi) * tmp);
		direction.y = (float) (Math.sin(phi) * tmp);
		direction.z = (float) (Math.sqrt(1 - sample[0]));

		// Convert the sampled direction to tangent space coordinates
		hitRecord.toTangentSpace(direction);

		// Probability according to the cosine distribution
		float p = getProbability(hitRecord, direction);

		// Create the shading sample
		ShadingSample shadingSample = new ShadingSample();
		shadingSample.w = direction;
		shadingSample.p = p;
		shadingSample.brdf = evaluateBRDF(hitRecord, direction, hitRecord.w);
		shadingSample.isSpecular = false;
		shadingSample.emission = evaluateEmission(hitRecord, hitRecord.w);

		return shadingSample;
	}
		
	public boolean castsShadows()
	{
		return true;
	}

	@Override
	public float getProbability(HitRecord hitRecord, Vector3f direction)
	{
		return (float) (hitRecord.normal.dot(direction) / Math.PI);
	}

	public Spectrum evaluateEmission(HitRecord hitRecord, Vector3f wOut) {
		return new Spectrum(0.f, 0.f, 0.f);
	}

	public ShadingSample getEmissionSample(HitRecord hitRecord, float[] sample) {
		return new ShadingSample();
	}
	
}
