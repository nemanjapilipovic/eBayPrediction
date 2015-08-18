package main;

import classification.TrainingData;
import data.EbayDriverFindingAPI;
import data_processing.PrepareData;

public class Main {

	public static void main(String[] args) throws Exception {
		//PrepareData pd = new PrepareData();
		//pd.createDataset();
		
		TrainingData td = new TrainingData();
		td.loadData();
		
		td.kNearestNeighbours();
		td.repTree();
		td.supportVectorMachine();
		td.naiveBayes();
		
		

	}

}
