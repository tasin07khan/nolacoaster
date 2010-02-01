package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Eh... this code is garbage right now.
 * @author nbeckman
 *
 */
public final class UniqueClassCounter {

	public static void main(String[] args) throws IOException {
		File input = new File("output_1264997698224.txt");
		FileReader reader = new FileReader(input);
		BufferedReader br = new BufferedReader(reader);
		
		String cur = br.readLine();
		while( cur!=null ) {
			cur.split(",");
			cur = br.readLine();
		}
		
	}
}
