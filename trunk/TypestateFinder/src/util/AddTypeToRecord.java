package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.rice.cs.plt.tuple.Option;

/**
 * This class is nothing more than some utility code. It is meant to
 * help me extract classes for the warnings that I found with the
 * typestate finder earlier.
 * @author Nels E. Beckman
 *
 */
public final class AddTypeToRecord {

	static class FalseInput extends InputRecord {
		public FalseInput() {
			super(-1, "", "");
		}
		@Override public boolean equals(Object o) { return o == this; }
		@Override public int hashCode() { return System.identityHashCode(this); }
	}
	
	static class OutputRecord extends InputRecord {
		private final String typeName;
		
		public OutputRecord(InputRecord input, String typeName) {
			super(input);
			this.typeName = typeName;
		}

		public OutputRecord(InputRecord input) {
			super(input);
			this.typeName = "XXX UNKNOWN XXX";
		}
		
		public String getType() { return this.typeName; }
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result
					+ ((typeName == null) ? 0 : typeName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			OutputRecord other = (OutputRecord) obj;
			if (typeName == null) {
				if (other.typeName != null)
					return false;
			} else if (!typeName.equals(other.typeName))
				return false;
			return true;
		}
	}
	
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

		protected InputRecord(InputRecord copy) {
			this.lineNo = copy.lineNo;
			this.packageName = copy.packageName;
			this.fileName = copy.fileName;
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
			result = prime * result + lineNo;
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
			if (lineNo != other.lineNo)
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
	
		
		Map<InputRecord, OutputRecord> input_records = buildInputRecords();
		// Now we've got all the input records.
		// build the output record.
		
		File classes_file = new File("C:\\Users\\nbeckman\\workspace\\TypestateFinder\\azureus_tf_run_r65_com_packages.txt");
		FileReader freader = new FileReader(classes_file);
		BufferedReader reader = new BufferedReader(freader);
		
		//List<String> classes_with_protocols = new LinkedList<String>();
		
		String cur = reader.readLine();
		while( cur != null ) {
			cur = cur.trim();
			if( !"".equals(cur) ) {
				InputRecord class_rec = buildInputRecordFromClass(cur);
				if( input_records.containsKey(class_rec) ) {
					// Remove it and add it to the output
//					input_records.remove(class_rec);
					try {
						String class_name = classNameFromClassRecord(cur);
						input_records.put(class_rec, new OutputRecord(class_rec, class_name));
					} catch( ArrayIndexOutOfBoundsException aioobe ) {/* Do nothing, probably a dup.*/ }
				}
			}
			
			cur = reader.readLine();
		}
		
		// Print all class names & number
		for( Map.Entry<InputRecord, OutputRecord> entry  : input_records.entrySet() ) {
			System.out.println(entry.getValue().getType().toString() + ", " + entry.getKey().toString());
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
		
	//	if( fields.length < 6 ) return Option.none();
		
		String package_name = fields[1].trim().toLowerCase();
		String file_name = fields[2].trim().toLowerCase();
		String trim = fields[3].replaceAll("li?(n|m)(e|d)", "").trim();
		int line_no;
//		try {
			line_no = Integer.parseInt(trim);
//		} catch(NumberFormatException nfe) {
//			return Option.none();
//		}
		
		if( fields[6].trim().toLowerCase().contains("yes") || 
			 fields[6].trim().toLowerCase().contains("n/a")) {
			InputRecord result = new InputRecord(line_no, package_name, file_name);
			return Option.some(result);
		}
		else {
			return Option.none();
		}
	}
	
	private static Map<InputRecord, OutputRecord> buildInputRecords() throws IOException {
		File results_file = new File("C:\\Users\\nbeckman\\workspace\\TypestateFinder\\azureus.csv");
		FileReader results_reader = new FileReader(results_file);
		BufferedReader results_reader_br = new BufferedReader(results_reader);
		
		Map<InputRecord, OutputRecord> input_records = new LinkedHashMap<InputRecord, OutputRecord>();
		
		// Read results.csv into the map
		String cur = results_reader_br.readLine();
		while( cur!=null ) {
			Option<InputRecord> record_ = inputLineToInputRecord(cur);
			if( record_.isNone() || input_records.containsKey(record_.unwrap()) ) {
				FalseInput falseInput = new FalseInput();
				input_records.put(falseInput, new OutputRecord(falseInput));				
			}
			else {
				InputRecord rec = record_.unwrap();
				input_records.put(rec, new OutputRecord(rec));
			}
			cur = results_reader_br.readLine();
		}
		return input_records;
	}
}
