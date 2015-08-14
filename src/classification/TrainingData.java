package classification;

import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.REPTree;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.converters.ConverterUtils.DataSource;

public class TrainingData {
	Instances data;

	public void loadData() throws Exception {
		DataSource loader = new DataSource("data/applelaptops.arff");
		data = loader.getDataSet();
		data.setClassIndex(data.numAttributes() - 1);
	}

	public void kNearestNeighbours() throws Exception {

		System.out.println("k-Nearest-Neighbours-Results:");
		Classifier ibk = new IBk();
		ibk.buildClassifier(data);

		Evaluation eval = new Evaluation(data);
		eval.crossValidateModel(ibk, data, 10, new Random(1));
		System.out.println(eval.toSummaryString());

	}

	public void repTree() throws Exception {

		System.out.println("REPTree-Results:");
		REPTree rt = new REPTree();
		rt.buildClassifier(data);

		Evaluation eval = new Evaluation(data);
		eval.crossValidateModel(rt, data, 10, new Random(1));

		System.out.println(eval.toSummaryString());

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

	}

}
