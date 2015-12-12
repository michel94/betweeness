import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;

import java.io.IOException;
import java.util.*;
import java.util.Iterator;


public class Betweenness{
	private Random random = new Random();
	private double[] C;

	int randomIndex(int size){
		return Math.abs(random.nextInt()) % size;
	}

	int popRandom(ArrayList<Integer> arr){
		return arr.remove(randomIndex(arr.size()));
	}

	public double[] getCount(){
		return C;
	}

	public Betweenness(Graph graph, ArrayList<Integer> availableNodes, int cores){
		int n = graph.numNodes();
		C = new double[n];
		
		ArrayList<Integer>[] nodesDist = new ArrayList[cores];
		Random random = new Random();

		for(int c=0; c<cores; c++)
			nodesDist[c] = new ArrayList<Integer>();


		for (int i=0; i<availableNodes.size(); i++) {
			nodesDist[i%cores].add( popRandom(availableNodes) );
			//System.out.println(i%cores + " " + nodesDist[i%cores].get(nodesDist[i%cores].size()-1 ) );
		}
		
		WorkThread[] threads = new WorkThread[cores];
		for(int i=0; i<cores; i++){
			threads[i] = new WorkThread(graph, C, nodesDist[i]);
		}

		for(int i=0; i<cores; i++)
			threads[i].start();
		
		System.out.println("Started");

		for(int i=0; i<cores; i++)
			try{
				threads[i].join();
			}catch(InterruptedException e){}

		for(int j=0; j<n; j++){
			for(int c=0; c<cores; c++){
				C[j] += threads[c].C[j];
			}
		}
		

	}

}

class NodeRank implements Comparable<NodeRank>{
	private int index;
	private double betweenness;
	public int getIndex(){
		return index;
	}
	public double getBetweenness(){
		return betweenness;
	}
	public NodeRank(int index, double betweenness){
		this.index = index;
		this.betweenness = betweenness;
	}

	public int compareTo(NodeRank b){
		if(betweenness < b.getBetweenness())
			return 1;
		else if(betweenness > b.getBetweenness())
			return -1;
		else
			return 0;
	}

}

class WorkThread extends Thread{
	private int start, end;
	private Graph graph;
	public double[] C;
	private ArrayList<Integer> elements;
	public WorkThread(Graph graph, double[] C, ArrayList<Integer> elements){
		this.graph = graph;
		this.C = C;
		this.elements = elements;
	}

	public void run(){
		for (int s=0; s<elements.size(); s++) {			
			bfs(elements.get(s));
		}
	}

	public void bfs(int start){
		int n = graph.numNodes();

		int[] distance = new int[n];
		for(int i=0; i<n; i++){
			distance[i] = -1;
		}
		int[] pathCount = new int[n];
		double[] accumPathCount = new double[n];

		int[] queue = new int[n];
		int[] stack = new int[n];
		int qs=0, ss=0;
		IntArrayList[] predec = new IntArrayList[n];
		for(int i=0; i<n; i++){
			predec[i] = new IntArrayList();
		}

		pathCount[start] = 1;
		queue[qs++] = start;
		distance[start] = 0;

		while(qs != 0){
			int node = queue[--qs];
			stack[ss++] = node;
			
			MyIterator successors = graph.successors(node);
			
			int neigh;
			while( (neigh = successors.next()) != -1 ){
				if(distance[neigh] == -1){
					distance[neigh] = distance[node] + 1;
					queue[qs++] = neigh;
				}
				if(distance[neigh] == distance[node] + 1){
					pathCount[neigh] += pathCount[node];
					predec[neigh].add(node);
				}
			}
			

		}

		while(ss != 0){
			int node = stack[--ss];
			for(int i=0; i<predec[node].size(); i++){
				int pred = predec[node].get(i);
				accumPathCount[pred] += ((float)pathCount[pred] / pathCount[node]) * (1 + accumPathCount[node]);
			}
			if(node != start)
				C[node] += accumPathCount[node];
		}



	}
}


class Graph{
	private int[][] adjacency;
	private int nNodes;
	public Graph(ImmutableGraph graph){
		nNodes = graph.numNodes();
		adjacency = new int[nNodes][];
		for(int i=0; i<graph.numNodes(); i++){
			adjacency[i] = graph.successorArray(i);
		}
	}
	public int numNodes(){
		return nNodes;
	}

	public MyIterator successors(int node){
		return new MyIterator(adjacency[node]);
	}
}

class MyIterator{
	private int[] vector;
	private int index;
	public MyIterator(int[] vector){
		this.vector = vector;
	}
	public int next(){
		if(index < vector.length){
			return vector[index++];
		}
		return -1;
		
	}
	public int size(){
		return vector.length;
	}
}