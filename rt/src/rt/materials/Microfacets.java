package rt.materials;

import rt.HitRecord;
import rt.Material;
import rt.Spectrum;
import rt.StaticVecmath;

import javax.vecmath.Vector3f;

/**
 * Created by adrian on 14.04.16.
 */
public class Microfacets implements Material
{
    float smoothness;
    Spectrum eta;
    Spectrum k;

    /**
     *
     * @param refractiveIndex       Wavelength dependent refractive index of the conductor
     * @param absorption            Wavelength dependent absorption coefficient
     * @param smoothness            Spectral exponent
     */
    public Microfacets(Spectrum refractiveIndex, Spectrum absorption, float smoothness)
    {
        this.eta = refractiveIndex;
        this.k = absorption;
        this.smoothness = smoothness;
    }

    @Override
    public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut, Vector3f wIn)
    {
        Vector3f halfVector = computeHalfVector(wOut, wIn);
        Vector3f normal = hitRecord.normal;
        float e = this.smoothness;

        float nDotHalf = halfVector.dot(normal);
        float nDotOut = wOut.dot(normal);
        float nDotIn = wIn.dot(normal);
        float outDotHalf = halfVector.dot(wOut);

        // Microfacet Distribution (Beckmann Distribution)
        float d = (float) ((e + 2) * Math.pow(nDotHalf, e) / (2 * Math.PI));

        // Geometry term
        float g1 = 2 * nDotHalf * nDotOut / outDotHalf;
        float g2 = 2 * nDotHalf * nDotIn / outDotHalf;
        float g = Math.min(1, Math.min(g1, g2));

        // Fresnel term
        float f_r = fresnel_reflectance(this.eta.r, this.k.r, nDotIn);
        float f_g = fresnel_reflectance(this.eta.g, this.k.g, nDotIn);
        float f_b = fresnel_reflectance(this.eta.b, this.k.b, nDotIn);

        // Cosine terms
        float cosTerms = 4 * nDotOut * nDotIn;

        // Torrance Sparrow BRDF for Microfacets
        Spectrum brdf = new Spectrum(f_r, f_g, f_b);
        brdf.mult(d * g / cosTerms);

        return brdf;
    }

    @Override
    public Spectrum evaluateEmission(HitRecord hitRecord, Vector3f wOut)
    {
        return new Spectrum(0, 0, 0);
    }

    @Override
    public boolean hasSpecularReflection()
    {
        return false;
    }

    @Override
    public ShadingSample evaluateSpecularReflection(HitRecord hitRecord)
    {
        return null;
    }

    @Override
    public boolean hasSpecularRefraction()
    {
        return false;
    }

    @Override
    public ShadingSample evaluateSpecularRefraction(HitRecord hitRecord)
    {
        return null;
    }

    @Override
    public ShadingSample getShadingSample(HitRecord hitRecord, float[] sample)
    {
        return null;
    }

    @Override
    public ShadingSample getEmissionSample(HitRecord hitRecord, float[] sample)
    {
        return null;
    }

    @Override
    public boolean castsShadows()
    {
        return true;
    }

    private Vector3f computeHalfVector(Vector3f wOut, Vector3f wIn)
    {
        Vector3f halfVector = new Vector3f(wIn);
        halfVector.add(wOut);
        halfVector.normalize();
        return halfVector;
    }

    private float fresnel_reflectance(float eta, float k, float cosi)
    {
        return (rParl2(eta, k, cosi) + rPerp2(eta, k, cosi)) / 2;
    }

    private float rParl2(float eta, float k, float cosi)
    {
        float tmp1 = (eta * eta + k * k) * cosi * cosi;
        float tmp2 = 2 * eta * cosi;
        return (tmp1 - tmp2 + 1) / (tmp1 + tmp2 + 1);
    }

    private float rPerp2(float eta, float k, float cosi)
    {
        float tmp1 = eta * eta + k * k;
        float tmp2 = 2 * eta * cosi;
        float tmp3 = cosi * cosi;
        return (tmp1 - tmp2 + tmp3) / (tmp1 + tmp2 + tmp3);
    }
}
