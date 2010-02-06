package nbeckman.jumpcar;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;

import edu.rice.cs.plt.tuple.Option;

/**
 * Each instance of the Car class holds information about
 * one model of car. This includes the function for determining
 * the height of the car at each offset, and also code for
 * loading all the cars in a certain directory. Cars are
 * comparable because they can be sorted by their models, names,
 * years.
 * 
 * @author Nels E. Beckman
 *
 */
public final class Car implements Comparable<Car> {

	private final String make;
	private final String model;
	private final int year;
	private final HeightFunction heightFn;
	private final MPH topSpead;
	
	private Car(String make, String model, int year, HeightFunction heightFn, MPH topSpead) {
		this.make = make;
		this.model = model;
		this.year = year;
		this.heightFn = heightFn;
		this.topSpead = topSpead;
	}

	@Override
	public int compareTo(Car c) {
		return 
			!make.equals(c.make) ? make.compareTo(c.make) :
			!model.equals(c.model) ? model.compareTo(c.model) :
			year - c.year;
	}
	
	/**
	 * Parses all of the *.spec files in a directory and returns
	 * a list of those spec files as cars, in alphabetical order.
	 * @param dirName
	 * @return
	 */
	public static List<Car> parseSpecFiles(String dirName) {
		File dir = new File(dirName);
		
		if( !dir.isDirectory() ) {
			System.err.println("parseSpecFiles was not given a directory.");
			return Collections.emptyList();
		}
		
		String[] spec_file_names =
		dir.list(new FilenameFilter() {
			@Override public boolean accept(File dir, String fname) {
				// don't care about directory. Does file end in spec?
				return fname.endsWith("spec");
			}});
		
		List<Car> result = new ArrayList<Car>(spec_file_names.length);
		for( String spec_file_name : spec_file_names ) {
			File spec_file = new File(dirName + "/" + spec_file_name);
			Option<Car> car_ = parseSpecFile(spec_file);
			
			if( car_.isNone() )
				System.err.println("Could not parse: " + spec_file.getName());
			else
				result.add(car_.unwrap());
		}
		return result;
	}
	
	private static void assertNonNull(Object... os) throws NullPointerException {
		for( Object o : os ) {
			if( o == null ) throw new NullPointerException();
		}
	}
	
	private static Option<Car> parseSpecFile(File spec_file) {
		try {
			Properties file_props = new Properties();
			file_props.load(new FileReader(spec_file));
			String make = file_props.getProperty("make");
			String model = file_props.getProperty("model");
			int year = Integer.parseInt(file_props.getProperty("year"));
			double length = Double.parseDouble(file_props.getProperty("length"));
			MPH speed = MPH.fromMilesPerHour(Integer.parseInt(file_props.getProperty("speed")));
			String image_file = file_props.getProperty("image");
			
			// Make sure all strings were in file.
			assertNonNull(make, model, image_file);
			
			BufferedImage input_image = ImageIO.read(new File(spec_file.getParent() + "/" + image_file));
			HeightFunction height_fn = HeightFunction.createHeightFn(input_image, length);
			
			return Option.some(new Car(make, model, year, height_fn, speed));
		} catch(Exception e) {
			e.printStackTrace();
			return Option.none();
		}
	}
	
	private static class HeightFunction {
		public HeightFunction(BufferedImage image) {
			
		}
		
		public static HeightFunction createHeightFn(BufferedImage image, double length) {
			throw new RuntimeException();
		}
	}
}
