package sshams2.cct.dime5;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class Metis {
	
	private static Graph G = new Graph();
	
	public static int run(Weight[][] _arrEdge, HashSet<Integer> _hsCandi, 
			int[] _infoClus, int _idxClus, int _maxSize){
		G.readInput(_arrEdge, _hsCandi);
		G.writeFile("temp.txt", _arrEdge);
		int nParts = (int)Math.ceil(((double)_hsCandi.size())/((double)_maxSize));
		String strResult = runMetis("temp.txt", nParts);
		labelClus(G, _infoClus,_idxClus, strResult);
		return _idxClus+nParts;
	}
	
	public static int labelClus(Graph _g, int[] _infoClus, int _idxClus, String _fileName){
		try {
			BufferedReader br = new BufferedReader(new FileReader(_fileName));
			String inputLine;
			int idInner = 0;
			int idExter = 0;
			int idxClus;
			while((inputLine = br.readLine())!=null){
				idExter = _g.getExterId(idInner);
				idxClus = Integer.parseInt(inputLine);
				_infoClus[idExter] = _idxClus + idxClus;
				idInner++;
			}
			br.close();
			File file = new File(_fileName);
			file.delete(); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	public static String runMetis(String _fileName, int _nParts){
		Runtime rn = Runtime.getRuntime();
		Process p = null;
		String cmd = "./Programs/kmetis "+_fileName+" "+_nParts;
		try {
			p = rn.exec(cmd, null);
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String strResult = _fileName+".part."+_nParts;
		return strResult;
	}
	
	
	
	
}
