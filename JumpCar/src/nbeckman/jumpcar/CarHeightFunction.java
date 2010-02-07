package nbeckman.jumpcar;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import edu.rice.cs.plt.tuple.Option;


/**
 * This class holds a non-uniform distribution of samples, and given an 
 * X value, it will return a y value, even if that means interpolating
 * from adjacent samples.
 * 
 * @author Nels E. Beckman
 *
 */
final class CarHeightFunction {
	
	// The samples. Maps from x value to y
	final private TreeMap<Double,Double> samples;
	
	// The default value to return for keys larger or
	// smaller than the given samples.
	final private Double defaultVal;
	
	private CarHeightFunction(TreeMap<Double,Double> samples, double defaultVal) {
		this.samples = samples;
		this.defaultVal = Double.valueOf(defaultVal);
	}
	
	public double get(double x_) {
		Double x = Double.valueOf(x_);
		if( samples.containsKey(x) )
			return samples.get(x);
		
		// else, we interpolate.
		Map.Entry<Double,Double> ceiling = samples.ceilingEntry(x);
		Map.Entry<Double, Double> floor = samples.floorEntry(x);
		
		// We may be outside the range of samples...
		// if so return default.
		if( ceiling == null || floor == null )
			return defaultVal;
		
		// We can interpolate!
		double c_x = ceiling.getKey().doubleValue();
		double c_y = ceiling.getValue().doubleValue();
		double f_x = floor.getKey().doubleValue();
		double f_y = floor.getValue().doubleValue();
		
		double slope = (c_y - f_y) / (c_x - f_x);
		double y_intercept = c_y - slope*c_x;
		
		return slope*x_ + y_intercept;
	}

	public static class BadBMPException extends Exception {
		private static final long serialVersionUID = 8721961327823086074L;
		public BadBMPException(String string) {
			super(string);
		}
	}
	
	/**
	 * Given an image containing two red dots (points the max length apart)
	 * and other black points representing samples, create a height function.
	 * The length is given is the length of the car, and is necessary to 
	 * translate the difference of the red pixels in pixels to a distance in
	 * meters. 
	 */
	public static CarHeightFunction createHeightFn(BufferedImage inputImage, double length) throws BadBMPException {
		Raster image_raster = inputImage.getData();
		
		int length_in_pixels = findLengthInPixels(image_raster);
		double pixel_multiplier = length / ((double)length_in_pixels);
		
		int starting_point = findFirstRedPixelFrom(image_raster, 0).unwrap().intValue(); // can't throw exn b/c we would have already. 
		int lowest_point = findLowestPoint(image_raster);
		
		TreeMap<Double,Double> result = new TreeMap<Double, Double>();
		for( int x = starting_point; x<image_raster.getWidth(); x++ ) {
			for( int y = 0; y < image_raster.getHeight(); y++ ) { 
				if( isBlack(x,y,image_raster) || isRed(x, y, image_raster) ) {
					// get actual x and y of this in terms of car meters
					int actual_offset = x - starting_point;
					int actual_height  = image_raster.getHeight() - y - lowest_point;
					
					result.put(Double.valueOf(pixel_multiplier * ((double)actual_offset)), 
							Double.valueOf(pixel_multiplier * ((double)actual_height)) );
				}
			}
		}
		
		return new CarHeightFunction(result, 0.0d);
	}

	// Given that there are two pixels in this image that are red, find
	// their distance apart on the x axis in pixels.
	private static int findLengthInPixels(Raster imageRaster) throws BadBMPException {
		Option<Integer> first_x_ = findFirstRedPixelFrom(imageRaster, 0);
		if( first_x_.isNone() ) throw new BadBMPException("No red pixels in image");
		
		int first_x = first_x_.unwrap();
		Option<Integer> second_x_ = findFirstRedPixelFrom(imageRaster, first_x+1);
		if( second_x_.isNone() ) throw new BadBMPException("Only one red pixel in image");
		int second_x = second_x_.unwrap();
		
		return second_x - first_x;
	}
	
	// Find the first red pixel starting at the given x offset, and
	// return the x offset of that pixel, or none if there is
	// not one.
	private static Option<Integer> findFirstRedPixelFrom(Raster r, int offset) {
		for( int x=offset; x<r.getWidth(); x++ ) {
			for( int y=0; y<r.getHeight(); y++ ) {
				if( isRed(x, y, r) ) {
					return Option.some(Integer.valueOf(x));
				}
			}
		}
		return Option.none();
	}
	
	private static boolean isRed(int x, int y, Raster r) {
		int red = r.getSample(x, y, 0);
		int grn = r.getSample(x, y, 1);
		int blu = r.getSample(x, y, 2);
		
		return red == 255 && grn == 0 && blu == 0;
	}
	
	// Find the y offset of the lowest green pixel in the image.
	private static int findLowestPoint(Raster r) throws BadBMPException {
		for( int y=r.getHeight()-1; y>=0; y-- ) {
			for( int x=0; x<r.getWidth(); x++ ) {
				if( isGreen(x,y,r) ) 
					return r.getHeight() - y;
			}
		}
		throw new BadBMPException("Image contains no non-white pixels!");
	}
	
	private static boolean isBlack(int x, int y, Raster r) {
		int red = r.getSample(x, y, 0);
		int grn = r.getSample(x, y, 1);
		int blu = r.getSample(x, y, 2);
		
		return red == 0 && grn == 0 && blu == 0;
	}
	
	private static boolean isGreen(int x, int y, Raster r) {
		int red = r.getSample(x, y, 0);
		int grn = r.getSample(x, y, 1);
		int blu = r.getSample(x, y, 2);
		
		return red == 0 && grn == 255 && blu == 0;
	}

	public static void main(String args[]) throws IOException, BadBMPException {  
		File carimage = new File("cardata/audi_a4_2008.bmp");
		BufferedImage input_image = ImageIO.read(carimage);
		final CarHeightFunction hf = CarHeightFunction.createHeightFn(input_image, 4.703d);

		for( double x=0.0d; x<=4.703d; x+=0.1d ) {
			System.out.println("x: " + x + ", y: " + hf.get(x));
		}
	}  

}