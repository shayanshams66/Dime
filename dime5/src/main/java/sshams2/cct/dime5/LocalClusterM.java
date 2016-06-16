package sshams2.cct.dime5;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Stack;

public class LocalClusterM {

	//public static int THRESHOLD = 0;
	public static int NUMCLUS = 10;
	private Weight[][] weightGraph;
	private int[] arrLen;
	private static String tab = "\t";
	private static char comm = ',';
	private int[] infoCluster;
	private HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>(NUMCLUS);
	private int low = 0;
	private int high = 0;
	private int mid = 0;
	private boolean flagEqual = false;
	
	public static void main(String[] args){
		String folder = "G:\\Research\\Results\\CloudAssembly\\Cloud\\SRR001069\\01_03_EdgesDumpSorted";
		//String fileLen = "G:\\Research\\Results\\CloudAssembly\\Cloud\\SRR001069\\01_04_Length\\part-r-00000";
		String fileClusInfo = "G:\\Research\\Results\\CloudAssembly\\Cloud\\SRR001069\\KmeanCluster\\Clus_"+
		NUMCLUS;
		String fileSequence = "G:\\Research\\Results\\CloudAssembly\\Cloud\\SRR001069\\SimLC-454.4caa27e.fna";
		String folderResult = "G:\\Research\\Results\\CloudAssembly\\Cloud\\SRR001069\\Clusters\\10\\";
		int numReads = 50211;//1225169;
		if(args.length==6){
			folder = args[0];
			//fileLen = args[1];
			fileClusInfo = args[1];
			numReads = Integer.parseInt(args[2]);
			NUMCLUS = Integer.parseInt(args[3]);
			fileSequence = args[4];
			folderResult = args[5];
		}else{
			System.out.println("EdgesFolder " +
					"DesignedClusterFile " +
					"NumReads " +
					"SequencesFile " +
					"DesignedResultFolder");
			return;
		}
		long begin = System.currentTimeMillis();
		LocalClusterM obj = new LocalClusterM();
		obj.run(folder, fileClusInfo, numReads, NUMCLUS, fileSequence, folderResult);
		System.out.println((System.currentTimeMillis()-begin)/1000+"s");
	}
	
	public void run(String _folder, String _fileClusInfo, 
			int _numReads, int _defNumClusters, String _fileSequence, String _folderResult){
		readEdges(_folder, _numReads);
		//readLen(_fileLen, _numReads);
		infoCluster = new int[_numReads+1];
		int idxClus = 1;
		int maxSizeCluster = (int) Math.ceil((((double)_numReads)/((double)_defNumClusters)));
		idxClus = this.fastAssignCluster(infoCluster, _defNumClusters, maxSizeCluster, _numReads, weightGraph, idxClus);
		this.metisCluster(infoCluster, _numReads, weightGraph, idxClus, maxSizeCluster);
		maxSizeCluster = (int) Math.ceil((((double)_numReads)/((double)_defNumClusters)));
		furtherMerge(infoCluster, _defNumClusters, maxSizeCluster);
		writeResult(_fileClusInfo, infoCluster);
		//readClusInfo(_fileClusInfo, infoCluster);
		writeCluster(infoCluster, _fileSequence, _folderResult);
	}
	
	public void metisCluster(int[] _infoCluster, int _numReads, Weight[][] _weightGraph, 
			int _idxClus, int _maxSize){
		int n = _numReads+1;
		Stack<Integer> stack = new Stack<Integer>();
		int iRead = 0;
		int iRead2 = 0;
		HashSet<Integer> hsTemp = new HashSet<Integer>();
		boolean[] arrVisited = new boolean[n];
		for(int i=1;i<n;i++){
			if(_infoCluster[i]>0){
				arrVisited[i] = true;
				continue;
			}
			if(arrVisited[i])continue;
			stack.clear();
			stack.add(i);
			hsTemp.clear();
			while(!stack.isEmpty()){
				iRead = stack.pop();
				if(hsTemp.contains(iRead))continue;
				hsTemp.add(iRead);
				for(int j=0;j<_weightGraph[iRead].length;j++){
					iRead2 = _weightGraph[iRead][j].getId(iRead);
					if(!hsTemp.contains(iRead2)){
						stack.add(iRead2);
					}	
				}
			}
			for(Iterator<Integer> ite = hsTemp.iterator();ite.hasNext();){
				iRead = ite.next();
				arrVisited[iRead] = true;
				
			}
			_idxClus = Metis.run(_weightGraph, hsTemp, _infoCluster, _idxClus, _maxSize);
			hsTemp.clear();
		}	
	}
	
	private int fastAssignCluster(int[] _infoCluster, int _defNumCluster, int _defMaxCluster, 
			int _numReads, Weight[][] _weightGraph, int _stClusIdx){
		int n = _numReads+1;
		int countInClus = 0;
		int idxClus = _stClusIdx;
		Stack<Integer> stack = new Stack<Integer>();
		int iRead = 0;
		int iRead2 = 0;
		HashSet<Integer> hsTemp = new HashSet<Integer>();
		boolean[] arrVisited = new boolean[n];
		boolean flagBreak = false;
		int countTemp = 0;
		for(int i=1;i<n;i++){
			if(arrVisited[i])continue;
			if(_weightGraph[i]==null){
				_infoCluster[i] = idxClus;
				countTemp++;
				arrVisited[i] = true;
				countInClus++;
				if(countInClus>=_defMaxCluster){
					countInClus=0;
					idxClus++;
				}
				continue;
			}
			stack.clear();
			stack.add(i);
			hsTemp.clear();
			while(!stack.isEmpty()){
				iRead = stack.pop();
				if(hsTemp.contains(iRead))continue;
				hsTemp.add(iRead);
				if(hsTemp.size()>_defMaxCluster){
					for(Iterator<Integer> ite = hsTemp.iterator();ite.hasNext();){
						arrVisited[ite.next()] = true;
					}
					hsTemp.clear();
					stack.clear();
					break;
				}
				flagBreak = false;
				for(int j=0;j<_weightGraph[iRead].length;j++){
					iRead2 = _weightGraph[iRead][j].getId(iRead);
					if(arrVisited[iRead2]){
						stack.clear();
						for(Iterator<Integer> ite = hsTemp.iterator();ite.hasNext();){
							arrVisited[ite.next()] = true;
						}
						hsTemp.clear();
						flagBreak = true;
						break;
					}else{
						if(!hsTemp.contains(iRead2)){
							stack.add(iRead2);
						}
					}
				}
				if(flagBreak)break;
			}
			if(hsTemp.size()>0&&hsTemp.size()<_defMaxCluster){
				if(countInClus+hsTemp.size()<_defMaxCluster){
					for(Iterator<Integer> ite = hsTemp.iterator();ite.hasNext();){
						iRead2 = ite.next();
						arrVisited[iRead2] = true;
						_infoCluster[iRead2] = idxClus;
						countTemp++;
						countInClus++;
					}
				}else{
					idxClus++;
					countInClus = 0;
					for(Iterator<Integer> ite = hsTemp.iterator();ite.hasNext();){
						iRead2 = ite.next();
						arrVisited[iRead2] = true;
						_infoCluster[iRead2] = idxClus;
						countTemp++;
						countInClus++;
					}
				}
				hsTemp.clear();
			}
		}
		System.out.println("Processed \t"+countTemp);
		return idxClus;
	}
	
	private void furtherMerge(int[] _infoCluster, int _defNumCluster, int _defMaxCluster){
		hm.clear();
		int id = -1;
		int count = 0;
		for(int i=1;i<_infoCluster.length;i++){
			id = _infoCluster[i];
			if(hm.containsKey(id)){
				count = hm.get(id);
				count++;
				hm.put(id, count);
			}else{
				hm.put(id, 1);
			}
		}
		if(hm.size()>_defNumCluster){
			count = hm.size();
			BinaryMap bm = new BinaryMap(count);
			Entry<Integer, Integer> entry;
			for(Iterator<Entry<Integer, Integer>> ite = hm.entrySet().iterator();ite.hasNext();){
				entry = ite.next();
				bm.add(entry.getValue(), entry.getKey());
			}
			int[][] OldNewIdClusters = new int[count][2];
			int[] arrCount = new int[count];
			int[] arrIdx = new int[count];
			for(int i=0;i<bm.Size();i++){
				arrCount[i] = bm.getSorted(i);
				arrIdx[i] = bm.getIndexArray(i);
			}
			int idx = 0;
			while(count>_defNumCluster){
				for(int j = bm.Size()-1;j>0;j--){
					if(idx>=j){
						System.out.println("error");
					}
					if(arrCount[j]+arrCount[idx]<_defMaxCluster){
						arrCount[j] += arrCount[idx];
						OldNewIdClusters[idx][0] = arrIdx[idx];
						OldNewIdClusters[idx][1] = arrIdx[j];
						break;
					}
				}
				count--;
				idx++;
			}
			for(int i=0;i<_infoCluster.length;i++){
				for(int j=0;j<idx;j++){
					if(_infoCluster[i]==OldNewIdClusters[j][0]){
						_infoCluster[i] = (int) OldNewIdClusters[j][1];
						break;
					}
				}
			}
		}
	}
	
	private Weight locateEdge(int _id, Weight[] _perEdges){
		low = 0; high = _perEdges.length - 1;
		flagEqual = false;
		while(low<=high){
			mid = (low + high)/2;
			if(_perEdges[mid].getId2()<_id){
				low = mid + 1;
			}else if(_perEdges[mid].getId2()>_id){
				high = mid - 1;
			}else{
				flagEqual = true;
				break;
			}
		}
		if(flagEqual){
			return _perEdges[mid];
		}else{
			System.out.println("Error");
			System.exit(1);
			return null;
		}
	}
	
	private void writeResult(String _fileName, int[] _infoCluster){
		try {
			FileWriter fw = new FileWriter(_fileName);
			for(int i=1;i<_infoCluster.length;i++){
				fw.write(Integer.toString(_infoCluster[i]));
				fw.write("\n");
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void writeCluster(int[] _infoCluster,
			String _fileName, String _folder) {
		int idxFile = 0;
		HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
		for(int i=1;i<_infoCluster.length;i++){
			if(!hm.containsKey(_infoCluster[i])){
				hm.put(_infoCluster[i], idxFile);
				idxFile++;
			}
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(_fileName));
			FileWriter[] fw = new FileWriter[idxFile];
			for(int i=0;i<idxFile;i++){
				fw[i] = new FileWriter(_folder+"part_"+i);
			}
			int idxRead = 0;
			String inputLine = null;
			while((inputLine=br.readLine())!=null){
				if(inputLine.startsWith(">")){
					idxRead++;
					fw[hm.get(_infoCluster[idxRead])].write(">"+Integer.toString(idxRead)+"\n");
				}else{
					fw[hm.get(_infoCluster[idxRead])].write(inputLine+"\n");
				}
			}
			for(int i=0;i<fw.length;i++){
				fw[i].flush();
				fw[i].close();
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private int readEdges(String _folder, int _num){
		weightGraph = new Weight[_num+1][];
		File dir = new File(_folder);
		File file[] = dir.listFiles();
		int id = 0;
		int id2 = 0;
		int idxTab = 0;
		int st = 0;
		Edge edge = new Edge();
		StringBuffer sb = new StringBuffer();
		int numEdgesPerRead = 0;
		int numReadsWithEdges = 0;
		int largestSet = 0;
		for (int i = 0; i < file.length; i++) {
			System.out.println("Reading file "+file[i].getName());
			if (!file[i].isDirectory()){
				try {
					BufferedReader br = new BufferedReader(new FileReader(file[i]));
					String inputLine = null;
					while((inputLine = br.readLine())!=null){
						numReadsWithEdges++;
						idxTab = inputLine.indexOf(tab);
						numEdgesPerRead = 0;
						for(int j=idxTab+1;j<inputLine.length();j++){
							if(inputLine.charAt(j)==comm){
								numEdgesPerRead++;
							}
						}
						numEdgesPerRead++;
						id = Integer.parseInt(inputLine.substring(0, idxTab));
						weightGraph[id] = new Weight[numEdgesPerRead/7];
						st = idxTab+1;
						numEdgesPerRead = 0;
						while(st<inputLine.length()){
							st = edge.set(id, inputLine, st, comm, sb);
							id2 = edge.getId(id);
							if(id>id2){
								weightGraph[id][numEdgesPerRead] = 
										locateEdge(id, weightGraph[id2]);
							}else{
								weightGraph[id][numEdgesPerRead] = 
										new Weight(id, id2, Score.LogScore(edge));
							}
							numEdgesPerRead++;
						}
					}
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		System.out.println("largest: "+largestSet);
		return numReadsWithEdges;
	}
	
	public void readLen(String _file, int _num){
		arrLen = new int[_num+1];
		String[] strs;
		try {
			BufferedReader br = new BufferedReader(new FileReader(_file));
			String inputLine = null;
			while((inputLine = br.readLine())!=null){
				strs = inputLine.split(tab);
				arrLen[Integer.parseInt(strs[0])] = Integer.parseInt(strs[1]);
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void readClusInfo(String _file, int[] _infoCluster){
		try {
			BufferedReader br = new BufferedReader(new FileReader(_file));
			String inputLine = null;
			int idx = 1;
			while((inputLine = br.readLine())!=null){
				_infoCluster[idx] = Integer.parseInt(inputLine);
				idx++;
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
