package classification;

import java.text.DecimalFormat;
import java.util.Random;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.REPTree;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.unsupervised.attribute.Discretize;

public class TrainingData {
	Instances data;
	Instances dataTest;

	public void loadData() throws Exception {
		DataSource loader = new DataSource("data/training.arff");
		data = loader.getDataSet();
		data.setClassIndex(data.numAttributes() - 1);

		DataSource loaderTest = new DataSource("data/test.arff");
		dataTest = loaderTest.getDataSet();
		dataTest.setClassIndex(dataTest.numAttributes() - 1);
	}

	public void kNearestNeighbours() throws Exception {

		System.out.println("k-Nearest-Neighbours-Results:");
		IBk ibk = new IBk();
		ibk.setOptions(new String[] { "-I" });
		ibk.buildClassifier(data);

		Evaluation eval = new Evaluation(data);
		eval.crossValidateModel(ibk, data, 10, new Random(1));
		System.out.println(eval.toSummaryString());

		for (int i = 0; i < dataTest.numInstances(); i++) {
			double price = ibk.classifyInstance(dataTest.instance(i));
			System.out.println("The predicted price for instance No."
					+ i
					+ " is: "
					+ Double.parseDouble(new DecimalFormat("#.##")
							.format(price)));
		}

	}

	public void repTree() throws Exception {

		System.out.println("REPTree-Results:");
		REPTree rt = new REPTree();
		rt.buildClassifier(data);

		Evaluation eval = new Evaluation(data);
		eval.crossValidateModel(rt, data, 10, new Random(1));

		System.out.println(eval.toSummaryString());

		for (int i = 0; i < dataTest.numInstances(); i++) {
			double price = rt.classifyInstance(dataTest.instance(i));
			System.out.println("The predicted price for instance No."
					+ i
					+ " is: "
					+ Double.parseDouble(new DecimalFormat("#.##")
							.format(price)));
		}

	}

	public void supportVectorMachine() throws Exception {

		System.out.println("SupportVectorMachine-Results:");

		LibSVM svm = new LibSVM();
		svm.setSVMType(new SelectedTag(LibSVM.SVMTYPE_EPSILON_SVR,
				LibSVM.TAGS_SVMTYPE));
		svm.buildClassifier(data);

		Evaluation eval = new Evaluation(data);
		eval.crossValidateModel(svm, data, 10, new Random(1));
		System.out.println(eval.toSummaryString());

		// Test data
		/*
		 * for (int i = 0; i < dataTest.numInstances(); i++) { double price =
		 * svm.classifyInstance(dataTest.instance(i));
		 * System.out.println("The predicted price for instance No." + i +
		 * " is: " + Double.parseDouble(new DecimalFormat("#.##")
		 * .format(price))); }
		 */

	}

	public void naiveBayes() throws Exception {

		Discretize discretizeFilter = new Discretize();
		discretizeFilter.setAttributeIndices("4,5,6,7,8");
		discretizeFilter.setInputFormat(data);
		discretizeFilter.setBins(10);
		discretizeFilter
				.setOptions(new String[] { "-unset-class-temporarily" });
		discretizeFilter.setUseEqualFrequency(true);

		NaiveBayes nbClassifier = new NaiveBayes();
		FilteredClassifier filteredClassifier = new FilteredClassifier();
		filteredClassifier.setClassifier(nbClassifier);
		filteredClassifier.setFilter(discretizeFilter);
		filteredClassifier.buildClassifier(data);

		Evaluation eval = new Evaluation(data);
		eval.crossValidateModel(filteredClassifier, data, 10, new Random(1));
		System.out.println(eval.toSummaryString());

		// Test data
		/*
		 * for (int i = 0; i < dataTest.numInstances(); i++) { double price =
		 * filteredClassifier.classifyInstance(dataTest .instance(i));
		 * System.out.println("The predicted price for instance No." + i +
		 * " is: " + Double.parseDouble(new DecimalFormat("#.##")
		 * .format(price))); }
		 */

	}

}
