
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

import java.net.*;
import java.io.*;

public class Work implements Serializable{
	public String graphPath;
	public ArrayList<Integer> elements;

	public Work(String graphPath, ArrayList<Integer> elements){
		this.graphPath = graphPath;
		this.elements = elements;
	} 
}