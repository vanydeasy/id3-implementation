/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myclassifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.Id3;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.RemoveType;

/**
 *
 * @author Venny
 */
public class MyClassifier {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, Exception {
        Scanner scan = new Scanner(System.in);
        System.out.print("Insert filename: ");
        String filename = scan.nextLine();

        // Load data from ARFF or CSV
        Instances data = MyClassifier.loadData(filename);
        
        // Remove atribut
        List<Attribute> attr = Collections.list(data.enumerateAttributes());
        System.out.println("\nList of attributes\n-----------------");
        for(int i=0;i<attr.size();i++) {
            System.out.println(i+1 + ". " + attr.get(i).name());
        }
        System.out.print("Want to remove attribute (y/n)? ");
        if (scan.nextLine().equalsIgnoreCase("y")){
            System.out.print("Attribute to be removed: ");
            data = MyClassifier.removeAttribute(data, scan.nextLine());
        }
        
        // Build or Load model
        Classifier model;
        System.out.println("\nBuild or Load Model\n-----------------");
        System.out.println("1. Build Model");
        System.out.println("2. Load Existing Model");
        System.out.print("Choose: ");
        if (scan.nextLine().equals("1")){ // Build Model
            System.out.println("\nDecision Tree Classifiers\n-----------------");
            System.out.println("1. ID3");
            System.out.println("2. J48");
            System.out.print("Choose classifier: ");
            
            if (scan.nextLine().equals("1")){
                model = new Id3();
            } else {
                model = new J48();
            }
            model.buildClassifier(data);
            System.out.println(model.toString());

            // Save Model
            System.out.print("Want to save model (y/n)? ");
            if (scan.nextLine().equalsIgnoreCase("y")){
                System.out.print("filename: ");
                MyClassifier.SaveModel(model, scan.next());
                System.out.print("Model Saved!\n");
            }
        } else { // Load Model 
            System.out.print("\nLoad Model\n----------\nfilename: ");
            model = MyClassifier.LoadModel(scan.next());
            System.out.print("Model Loaded!\n");
        }
        
        // 10-fold Cross Validation Evaluation
        Evaluation eval = new Evaluation(data);
        eval.crossValidateModel(model, data, 10, new Random());
        System.out.println(eval.toSummaryString("\n\n\n\n10-Fold Cross Validation\n============", false));
        
        // Prediction using user input
        System.out.println("\nClassify Unseen Instance\n-------------------------");
        List<Attribute> attrNew = Collections.list(data.enumerateAttributes());
        Instance predInst = new Instance(attrNew.size());
        for(int i=0;i<attrNew.size();i++) {
            System.out.print("Data "+attrNew.get(i).name()+": ");
            if(attrNew.get(i).isNumeric())
                predInst.setValue(attrNew.get(i),scan.nextDouble());
            else
                predInst.setValue(attrNew.get(i),scan.next());
        }
        predInst.setDataset(data);
        String prediction = data.classAttribute().value((int)model.classifyInstance(predInst));
        System.out.println("The predicted value of instance is "+prediction);
    }
    
    public static Instances loadData(String filename) throws FileNotFoundException, IOException {
        Instances data;
        if (filename.substring(filename.lastIndexOf(".") + 1).equals("arff")){
            BufferedReader br = new BufferedReader(new FileReader(filename));
            ArffLoader.ArffReader arff = new ArffLoader.ArffReader(br);
            data = arff.getData();
        } else {
            CSVLoader loader = new CSVLoader();
            loader.setSource(new File(filename));
            data = loader.getDataSet();
        }
        data.setClassIndex(data.numAttributes() - 1);
        return data;
    }
    
    public static Instances removeAttribute (Instances data, String attr) throws Exception {
        Remove remove = new Remove();
        remove.setAttributeIndices(attr);
        remove.setInvertSelection(false);
        remove.setInputFormat(data);
        
        return Filter.useFilter(data, remove);
    }
    
    public static void SaveModel(Classifier model, String filename) throws Exception {
        weka.core.SerializationHelper.write(filename, model);
    }
    
    public static Classifier LoadModel(String filename) throws Exception {
        Classifier cls = (Classifier) weka.core.SerializationHelper.read(filename);
        return cls;
    }
}
