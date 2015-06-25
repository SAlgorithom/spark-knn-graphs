package info.debatty.spark.knngraphs.example;

import info.debatty.java.graphs.NeighborList;
import info.debatty.java.graphs.Node;
import info.debatty.java.graphs.build.NNDescent;
import info.debatty.java.stringsimilarity.KShingling;
import info.debatty.java.utils.SparseIntegerVector;
import info.debatty.spark.knngraphs.builder.LSHSuperBitSparseIntegerVector;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

/**
 * An example of how to use LSHSuperBit algorithm, with NN-Descent inside buckets,
 * to build a k-nn graph from a text dataset 
 * 
 * @author Thibault Debatty
 */
public class LSHSuperBitNNDescentTextExample {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        
        String file = args[0];
        
        // Read the file
        ArrayList<String> strings = readFile(file);
        
        // Convert the strings to nodes of SparseIntegerVectors
        // using k-shingling
        // we will split strings in 3-grams (sequences of 3 characters)
        KShingling ks = new KShingling(3);
        List<Node<SparseIntegerVector>> data = new ArrayList<Node<SparseIntegerVector>>();
        for (int i = 0; i < strings.size(); i++) {
            String s = strings.get(i);
            data.add(new Node<SparseIntegerVector>(
                    s,                      // id
                    ks.getProfile(s).getSparseVector()));   // value
        }
        
        // Number of different 3-grams that were found in the dataset
        int dim = ks.getDimension();
        
        
        // Configure spark instance
        SparkConf conf = new SparkConf();
        conf.setAppName("SparkTest");
        conf.setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        
        
        JavaRDD<Node<SparseIntegerVector>> nodes = sc.parallelize(data);
        
        
        // Configure LSHSuperBit graph builder
        LSHSuperBitSparseIntegerVector gbuilder = new LSHSuperBitSparseIntegerVector();
        gbuilder.setK(10);
        gbuilder.setStages(2);
        gbuilder.setBuckets(10);
        
        // LSH hashing requires the dimensionality
        gbuilder.setDim(dim);
        
        // Configure the serial nn-descent algorithm that will be used inside each bucket
        NNDescent<SparseIntegerVector> nndescent = new NNDescent<SparseIntegerVector>();
        nndescent.setDelta(0.01);
        nndescent.setRho(0.5);
        nndescent.setMaxIterations(10);
        gbuilder.setInnerGraphBuilder(nndescent);
        
        // By default, LSHSuperBit graph builder uses cosine similarity
        // but another similarity measure can be defined if needed...
        
        // Build the graph...
        JavaPairRDD<Node<SparseIntegerVector>, NeighborList> graph;
        try {
            graph = gbuilder.computeGraph(nodes);
            System.out.println(graph.first());
        
        } catch (Exception ex) {
            Logger.getLogger(LSHSuperBitSparseIntegerVectorExample.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static ArrayList<String> readFile(String path) throws IOException {
        
        File file = new File(path);
	BufferedReader br = new BufferedReader(new FileReader(file));
        
        ArrayList<String> r = new ArrayList<String>();
	String line;
	while ((line = br.readLine()) != null) {
		r.add(line);
	}
 
	br.close();
        return r;
    }
}