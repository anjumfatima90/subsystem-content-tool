package subsystem.content.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import org.apache.aries.util.manifest.ManifestHeaderProcessor;

public class SubsystemContentTool {

	public static void main(String[] args) throws Exception {
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);

		System.out.println("Enter features directory");

		String featuresDir = sc.nextLine();

		Path dir = Paths.get(featuresDir);
		Files.walk(dir).forEach(path -> {
			try {
				process(path.toFile());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private static void process(File file) throws Exception {

		if (file.isFile() && file.getAbsolutePath().endsWith(".feature")) {

			Properties featureProps = new Properties();

			try (InputStream in = new FileInputStream(file.getAbsolutePath())) {
				featureProps.load(in);
			}
			String featureProp = featureProps.getProperty("-features");

			System.out.println("OLD FEATURES= " + featureProp);

			String updatedFeatures = updateFeatures(featureProp);

			if (updatedFeatures != "") {
				featureProps.setProperty("-features", updatedFeatures);
			}

			System.out.println("UPDATED FEATURES= " + featureProps.getProperty("-features"));

			try (OutputStream out = new FileOutputStream(file.getAbsolutePath())) {
				featureProps.store(out, "");
			}
		}
	}

	private static String updateFeatures(String featureProp) {

		String updatedFeatures = "";

		if (featureProp != null) {
			Map<String, Map<String, String>> data = ManifestHeaderProcessor.parseImportString(featureProp);

			int count = 0;

			for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
				updatedFeatures = updatedFeatures + entry.getKey();
				if (entry.getValue() != null && !entry.getValue().isEmpty()) {
					for (Map.Entry<String, String> e : entry.getValue().entrySet()) {
						updatedFeatures = updatedFeatures + ";";
						updatedFeatures = e.getKey().contains("ibm.tolerates")
								? updatedFeatures + e.getKey() + "=" + "\"" + e.getValue() + "\""
								: updatedFeatures + e.getKey() + "=" + e.getValue();
					}

				}
				count++;
				if (count < data.size()) {
					updatedFeatures = updatedFeatures + ",";
				}
			}
		}
		return updatedFeatures;

	}

}
