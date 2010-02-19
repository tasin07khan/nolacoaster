package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import edu.rice.cs.plt.tuple.Option;

/**
 * This class is nothing more than some utility code. It is meant to
 * help me extract classes for the warnings that I found with the
 * typestate finder earlier.
 * @author Nels E. Beckman
 *
 */
public final class UniqueClassCounter {

	static class InputRecord {
		private final int lineNo;
		private final String packageName;
		private final String fileName;

		public InputRecord(int lineNo, String packageName, String fileName) {
			super();
			this.lineNo = lineNo;
			this.packageName = packageName;
			this.fileName = fileName;
		}

		
		@Override
		public String toString() {
			return packageName +", " + fileName + ", " + lineNo;
		}



		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((fileName == null) ? 0 : fileName.hashCode());
			result = prime * result
					+ ((packageName == null) ? 0 : packageName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			InputRecord other = (InputRecord) obj;
			if (fileName == null) {
				if (other.fileName != null)
					return false;
			} else if (!fileName.equals(other.fileName))
				return false;
			if (packageName == null) {
				if (other.packageName != null)
					return false;
			} else if (!packageName.equals(other.packageName))
				return false;
			return true;
		}
	}
	
	public static void main(String[] args) throws IOException {
	
		
		HashSet<InputRecord> input_records = buildInputRecords();
		// Now we've got all the input records.
		// build the output record.
		
		File classes_file = new File("C:\\Users\\nbeckman\\workspace\\TypestateFinder\\standardio.txt");
		FileReader freader = new FileReader(classes_file);
		BufferedReader reader = new BufferedReader(freader);
		
		List<String> classes_with_protocols = new LinkedList<String>();
		
		String cur = reader.readLine();
		while( cur != null ) {
			cur = cur.trim();
			if( !"".equals(cur) ) {
				InputRecord class_rec = buildInputRecordFromClass(cur);
				if( input_records.contains(class_rec) ) {
					// Remove it and add it to the output
					input_records.remove(class_rec);
					String class_name = classNameFromClassRecord(cur);
					classes_with_protocols.add(class_name);
				}
			}
			
			cur = reader.readLine();
		}
		
		// Print all class names & number
		System.out.println("Classes with protocls (" + classes_with_protocols.size() +"):");
		for( String class_name : classes_with_protocols ) {
			System.out.println(class_name);
		}
		System.out.println("");
		System.out.println("");
		
		
		// Print all protocols that weren't found.
		System.out.println("Protocols that weren't found:");
		for( InputRecord rec : input_records ) {
			System.out.println(rec.toString());
		}
	}
	
	private static String classNameFromClassRecord(String line) {
		String[] fields = line.split(",");
		return fields[3];
	}
	
	private static InputRecord buildInputRecordFromClass(String line) {
		String[] fields = line.split(",");
		String package_name = fields[0].trim().toLowerCase();
		String file_name = fields[1].trim().toLowerCase();
		int line_no = Integer.parseInt(fields[2].trim());
		return new InputRecord(line_no, package_name, file_name);
	}
	
	private static Option<InputRecord> inputLineToInputRecord(String line) {
		String[] fields = line.split(",");
		
		if( fields.length < 6 ) return Option.none();
		
		String package_name = fields[2].trim().toLowerCase();
		String file_name = fields[3].trim().toLowerCase();
		String trim = fields[4].replaceAll("li(n|m)e", "").trim();
		int line_no;
		try {
			line_no = Integer.parseInt(trim);
		} catch(NumberFormatException nfe) {
			return Option.none();
		}
		
		if( fields[5].trim().toLowerCase().contains("yes") ) {
			InputRecord result = new InputRecord(line_no, package_name, file_name);
			return Option.some(result);
		}
		else {
			return Option.none();
		}
	}
	
	private static HashSet<InputRecord> buildInputRecords() throws IOException {
		File results_file = new File("C:\\Users\\nbeckman\\workspace\\TypestateFinder\\jsl.csv");
		FileReader results_reader = new FileReader(results_file);
		BufferedReader results_reader_br = new BufferedReader(results_reader);
		
		HashSet<InputRecord> input_records = new LinkedHashSet<InputRecord>();
		
		// Read results.csv into the map
		String cur = results_reader_br.readLine();
		while( cur!=null ) {
			Option<InputRecord> record_ = inputLineToInputRecord(cur);
			if( record_.isSome() ) {
				input_records.add(record_.unwrap());
			}
			cur = results_reader_br.readLine();
		}
		return input_records;
	}
}
