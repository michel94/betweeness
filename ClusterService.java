
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

import java.io.*;
import java.net.*;

public class ClusterService{
	public ServerSocket serverSocket;

	public ClusterService(){
		//wait for work
		try{
			serverSocket = new ServerSocket(8001);
			
			while(true){
				Socket connection = serverSocket.accept();
				ObjectInputStream inputObject = new ObjectInputStream(connection.getInputStream());

				Work work = null;
				try{
					work = (Work) inputObject.readObject();
				}catch(ClassNotFoundException e){

				}

				final ProgressLogger pl = new ProgressLogger();
				ImmutableGraph iGraph = null;

				Graph graph = null;
				try{
					graph = new Graph(ImmutableGraph.load(work.graphPath, pl));
				}catch(IOException e){
					System.out.println("File not found\n" + e);
					return;
				}
				
				double[] C = new Betweenness(graph, work.elements, Runtime.getRuntime().availableProcessors()).getCount();

				ObjectOutputStream outputObject = new ObjectOutputStream(connection.getOutputStream());
				outputObject.writeObject( new Result(C) );

			}

		}catch(IOException e){
			e.printStackTrace();
		}

	}

	public static void main(String[] args){
		new ClusterService();
	}

}
