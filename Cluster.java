
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;

import java.util.*;
import java.util.Iterator;
import java.net.*;
import java.io.*;


public class Cluster{
	private String[] serverList = new String[]{"193.136.128.109", "193.136.128.103", "193.136.128.104"};
	private int[] portList = new int[]{8001, 8001, 8001};
	private Socket[] sockets;
	private Random random = new Random();

	int randomIndex(int size){
		return Math.abs(random.nextInt()) % size;
	}

	int popRandom(ArrayList<Integer> arr){
		return arr.remove(randomIndex(arr.size()));
	}

	public Cluster(String[] args, int cores){
		final ProgressLogger pl = new ProgressLogger();
		ImmutableGraph iGraph = null;

		try{
			iGraph = ImmutableGraph.load(args[0], pl);
		}catch(IOException e){
			System.out.println("File not found\n" + e);
			return;
		}
		Graph graph = new Graph(iGraph);

		int n = graph.numNodes();
		
		System.out.println("number of nodes" + n);

		boolean clustering = (args.length >= 3 && (args[2].equals("-c") || args[2].equals("-cluster")) );
		
		int nClusters = 1;
		if(clustering)
			nClusters += serverList.length;
		
		double pIterations = Double.parseDouble(args[1]);
		int iterations = (int) (n * pIterations);

		ArrayList elements = new ArrayList<Integer>();
		for(int i=0; i<n; i++){
			elements.add(i);
		}

		ArrayList<Integer>[] clusterElements = new ArrayList[nClusters];
		for(int i=0; i<nClusters; i++)
			clusterElements[i] = new ArrayList<Integer>();

		for(int i=0; i<iterations; i++){
			clusterElements[i%nClusters].add( popRandom(elements) );
		}
		
		if(clustering){
			//send work to clusters
			sockets = new Socket[nClusters-1];
			for(int i=0; i<nClusters-1; i++){
				Work w = new Work(args[0], clusterElements[i]);
				sockets[i] = null;
				try{
					sockets[i] = new Socket(serverList[i], portList[i]);
					ObjectOutputStream outputObject = new ObjectOutputStream(sockets[i].getOutputStream());
					outputObject.writeObject(w);
				}catch(Exception e){
					System.out.println("Error Connecting to server");
					e.printStackTrace();
				}
			}
		}

		System.out.println(clusterElements[0].size() + "/" + iterations + " " + nClusters);
		double[] C = new Betweenness(graph, clusterElements[0], cores).getCount();


		if(clustering){
			int j=0;
			for(Socket socket : sockets){
				System.out.println("Reading from server " + (j++) );
				try{
					ObjectInputStream inputObject = new ObjectInputStream(socket.getInputStream());
					Result result = (Result) inputObject.readObject();
					for(int i=0; i<result.C.length; i++)
						C[i] += result.C[i];
				}catch(Exception e){
					System.out.println("Error Receiving from server");
					e.printStackTrace();
				}

			}
		}

		LinkedList<NodeRank> ranks = new LinkedList<NodeRank>();
		for(int i=0; i<n; i++){
			ranks.add(new NodeRank(i, C[i]));
		}
		Collections.sort(ranks);

		Iterator<NodeRank> it =  ranks.iterator();
		int i=0;
		ArrayList<Integer> arr = new ArrayList<Integer>();

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(args[0] + ".stats", true));
			while(it.hasNext() && i++ < 20){
				NodeRank r = it.next();
					bw.write(r.getIndex() + ", ");


				arr.add(r.getIndex());
			}
			bw.newLine();
			bw.flush();
			bw.close();
		
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		TreeMap<Integer, AvgBetweenness> tree = new TreeMap();
		for(i=0; i<n; i++){
			int deg = iGraph.outdegree(i);
			if(!tree.containsKey(deg))
				tree.put(deg, new AvgBetweenness());
			tree.get(deg).addBetweeness(C[i]);

			if(deg < 0)
				System.out.println("BUG");
		}

		double[] count_betw = new double[tree.lastKey()+1];

		for(Map.Entry<Integer, AvgBetweenness> entry : tree.entrySet()) {
			Integer key = entry.getKey();
			AvgBetweenness value = entry.getValue();
			count_betw[key] += value.getBetweenness();
		}
		for(i=0; i<count_betw.length; i++){
			System.out.println(i + ": " + count_betw[i]);
		}
		
	}

	public static void main(String[] args){
		System.out.println(args.length);
		new Cluster(args, Runtime.getRuntime().availableProcessors());

	}
}


class AvgBetweenness{
	int n_occur;
	double sum;
	public AvgBetweenness(){

	}
	public void addBetweeness(double v){
		sum += v;
		n_occur++;
	}
	public double getBetweenness(){
		return sum / n_occur;
	}
}
