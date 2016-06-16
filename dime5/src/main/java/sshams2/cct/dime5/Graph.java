package sshams2.cct.dime5;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

public class Graph {

	public int numV = 0;
	public int numE = 0;
	
	private static int[] arrMap = null;
	private static int[] arrMapTotal = null;
	
	public Graph(){
		
	}
	
	public Graph(int _n){
		arrMapTotal = new int[_n+1];
	}
	
	public int getInnerId(int _id){
		return arrMapTotal[_id];
	}
	
	public int getExterId(int _id){
		return arrMap[_id];
	}
	
	public void readInput(Weight[][] _arrEdges, HashSet<Integer> _hs){
		numV = _hs.size();
		numE = 0;
		if(arrMap==null||arrMap.length<_hs.size()){
			arrMap = new int[_hs.size()];
		}
		int val = 0;
		int index = 0;
		for(Iterator<Integer> ite = _hs.iterator();ite.hasNext();){
			val = ite.next();
			arrMap[index] = val;
			index++;
			numE += _arrEdges[val].length;
		}
		Arrays.sort(arrMap, 0, numV);
		arrMapTotal = new int[_arrEdges.length];
		for(int i=0;i<numV;i++){
			arrMapTotal[arrMap[i]] = i;
		}
	}
	
	public void writeFile(String _fileName, Weight[][] _arrEdge){
		try {
			int id1 = 0;
			int id2 = 0;
			FileWriter fw = new FileWriter(_fileName);
			fw.write(Integer.toString(numV)+" "+Integer.toString(numE/2)+" 001\n");
			for(int i=0;i<numV;i++){
				id1 = getExterId(i);
				for(int j=0;j<_arrEdge[id1].length;j++){
					id2 = getInnerId(_arrEdge[id1][j].getId(id1));
					fw.write(Integer.toString(id2+1)+" "+Byte.toString(_arrEdge[id1][j].getVal())+" ");
				}
				fw.write("\n");
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		
	}
}
