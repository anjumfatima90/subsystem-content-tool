package subsystem.content.tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.aries.util.manifest.ManifestHeaderProcessor;
import nu.studer.java.util.OrderedProperties;

public class SubsystemContentTool {

	private static int count = 0;
	public static void main(String[] args) throws Exception {
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);

		System.out.println("Enter features directory:");

		String featuresDir = sc.nextLine();
		
		System.out.println("Enter temp directory to copy temp feature files:");
		
		String tempDir = sc.nextLine();

		Path dir = Paths.get(featuresDir);
		Files.walk(dir).forEach(path -> {
			try {
				process(path.toFile(), tempDir);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		System.out.println("TOTAL COUNT="+count);
	}

	private static void process(File file, String tempDir) throws Exception {

		if (file.isFile() && file.getAbsolutePath().endsWith(".feature")) {

			int tempCount =0;
			
			List<String> updated = updateFeatures(file);

			if (updated.size() > 0) {
				String tempFilePath = createTempFeature(updated, file, tempCount, tempDir);
				
				replaceExistingFeatureFile(tempFilePath, file.getAbsolutePath());
				count++;
			}
			
		}
	}

	private static void replaceExistingFeatureFile(String source, String destination) {
		FileInputStream instream = null;
		FileOutputStream outstream = null;
	 
	    	try{
	    	    File infile =new File(source);
	    	    File outfile =new File(destination);
	 
	    	    instream = new FileInputStream(infile);
	    	    outstream = new FileOutputStream(outfile);
	 
	    	    byte[] buffer = new byte[1024];
	 
	    	    int length;

	    	    while ((length = instream.read(buffer)) > 0){
	    	    	outstream.write(buffer, 0, length);
	    	    }

	    	    instream.close();
	    	    outstream.close();

	    	    System.out.println("File copied successfully!!");
	 
	    	}catch(IOException ioe){
	    		ioe.printStackTrace();
	    	 }
		
	}

	private static String createTempFeature(List<String> updated, File file, int count, String tempDir) {
		List<String> lines = new ArrayList<String>();
		BufferedReader reader;
		String line = null;
		
		String formatedFeature = formatFeature(updated);
		boolean visited = false;
		count ++;
		String tempFilePath = tempDir +"newFile" + count +".txt";

		try {
			reader = new BufferedReader(new FileReader(
					file.getAbsolutePath()));
			
			while ((line = reader.readLine()) != null) {
				if (line.contains("-features")) {
					if (line.contains("\\")) {
						visited = true;
					} else {
						visited = false;
					}
					lines.add(formatedFeature);
					continue;
				}
				if (visited && !line.contains("\\")) {
					visited = false;
					continue;
				}
				
				if (visited) {
					continue;
				}
				
				
				else {
					 lines.add(line);
				}
                    
               

			}
			reader.close();
			
			 FileWriter fw = new FileWriter(tempFilePath);
	         BufferedWriter out = new BufferedWriter(fw);
	         for(String s : lines) {
	        	 out.write(s);
	        	 out.write(System.lineSeparator());
	         }
	        	
	            out.flush();
	            out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return tempFilePath;
	}

	private static String formatFeature(List<String> updated) {
		String newFeature = "-features=";
		int count =0;
		for (String f: updated) {
			newFeature = newFeature + f;
			count++;
			if(count < updated.size()) {
				newFeature = newFeature + "," +" " + "\\" + System.lineSeparator() + "  ";
			}
			
		}
		return newFeature;
		
	}

	private static List<String> updateFeatures(File file) throws FileNotFoundException, IOException {
		OrderedProperties featureProps = new OrderedProperties();

		try (InputStream in = new FileInputStream(file.getAbsolutePath())) {
			featureProps.load(in);
		}
		String featureProp = featureProps.getProperty("-features");

		List<String> updated = new ArrayList<String>();

		if (featureProp != null) {
			Map<String, Map<String, String>> data = ManifestHeaderProcessor.parseImportString(featureProp);

			int count = 0;

			for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
				String updatedFeatures = entry.getKey();
				if (entry.getValue() != null && !entry.getValue().isEmpty()) {
					for (Map.Entry<String, String> e : entry.getValue().entrySet()) {
						updatedFeatures = updatedFeatures + "; ";
						updatedFeatures = e.getKey().contains("ibm.tolerates")
								? updatedFeatures + e.getKey() + "=" + "\"" + e.getValue() + "\""
								: updatedFeatures + e.getKey() + "=" + e.getValue();
					}

				}
				updated.add(updatedFeatures);
				count++;
				
			}
		}
		if (updated.size() == 1) {
			return new ArrayList<String>();
		}
		return updated;

	}

}
