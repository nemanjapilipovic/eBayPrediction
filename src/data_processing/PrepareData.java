package data_processing;

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.Random;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;
import data.EbayDriverShoppingAPI;

public class PrepareData {

	public void createDataset() throws Exception {
		EbayDriverShoppingAPI ebds = new EbayDriverShoppingAPI();
		String data = ebds.getData();
		prepareDataset(data);

	}

	private void prepareDataset(String data) throws Exception {
		LinkedList<String> productFamily = new LinkedList<String>();
		LinkedList<String> operatingSystem = new LinkedList<String>();
		LinkedList<String> processorType = new LinkedList<String>();

		File dataFile = new File("data/dataset.arff");
		FileWriter writer = null;
		writer = new FileWriter(dataFile);
		writer.write("@relation applelaptops \n\n");

		String[] listOfItems = data.split("//");

		for (int i = 0; i < listOfItems.length; i++) {
			String[] specifics = listOfItems[i].split(",,,");

			if (!productFamily.contains(specifics[0])) {
				productFamily.add(specifics[0]);
			}
			if (!operatingSystem.contains(specifics[1])) {
				operatingSystem.add(specifics[1]);
			}
			if (!processorType.contains(specifics[2])) {
				processorType.add(specifics[2]);
			}

		}
		writer.write("@attribute productFamily {" + productFamily.get(0));

		for (int i = 1; i < productFamily.size(); i++) {
			writer.write("," + productFamily.get(i));
		}

		writer.write("}\n");

		writer.write("@attribute operatingSystem {" + operatingSystem.get(0));

		for (int i = 1; i < operatingSystem.size(); i++) {
			writer.write("," + operatingSystem.get(i));
		}

		writer.write("}\n");

		writer.write("@attribute processorType {" + processorType.get(0));

		for (int i = 1; i < processorType.size(); i++) {
			writer.write("," + processorType.get(i));
		}

		writer.write("}\n");

		writer.write("@attribute screenSize NUMERIC\n");

		writer.write("@attribute memory NUMERIC\n");

		writer.write("@attribute hardDriveCapacity NUMERIC\n");

		writer.write("@attribute processorSpeed NUMERIC\n");

		writer.write("@attribute productPrice NUMERIC\n\n@data");

		for (int i = 0; i < listOfItems.length; i++) {
			String[] specifics = listOfItems[i].split(",,,");
			writer.write("\n" + specifics[0] + "," + specifics[1] + ","
					+ specifics[2] + "," + specifics[3] + "," + specifics[4]
					+ "," + specifics[5] + "," + specifics[6] + ","
					+ specifics[7]);

		}

		writer.close();
		
		createTrainingAndTestFiles();

	}

	public void createTrainingAndTestFiles() throws Exception {
		DataSource loader = new DataSource("data/dataset.arff");
		Instances data = loader.getDataSet();

		data.randomize(new Random());
		int trainSize = data.numInstances() * 80 / 100;
		int testSize = data.numInstances() - trainSize;
		Instances train = new Instances(data, 0, trainSize);
		Instances test = new Instances(data, trainSize, testSize);

		ArffSaver saverTrain = new ArffSaver();
		saverTrain.setInstances(train);
		saverTrain.setFile(new File("data/training.arff"));
		saverTrain.writeBatch();
		
		ArffSaver saverTest = new ArffSaver();
		saverTest.setInstances(test);
		saverTest.setFile(new File("data/test.arff"));
		saverTest.writeBatch();
	}

}
