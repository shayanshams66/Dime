package sshams2.cct.dime3;

import org.apache.hadoop.fs.FileStatus;

public class Repeat {

	public int numGroup = 0;
	public FileStatus[] arrFiles;
	public int sizePerGroup = 0;
	public FileStatus[] arrOutFiles;
	public int idx1;
	public int idx2;
	
	public Repeat(FileStatus[] _files, int _numGroup){
		arrFiles = new FileStatus[_files.length];
		for(int i=0;i<_files.length;i++){
			arrFiles[i] = _files[i];
		}
		numGroup = _numGroup;
		sizePerGroup = _files.length / _numGroup;
		idx1 = 0;
		idx2 = 0;
		arrOutFiles = new FileStatus[sizePerGroup*2];
	}
	
	public FileStatus[] get(){
		idx2++;
		if(idx2>=numGroup){
			idx1++;
			idx2 = idx1+1;
		}
		if(idx1>=(numGroup-1))return null;
		int idx = 0;
		for(int i=0;i<sizePerGroup;i++){
			arrOutFiles[idx] = arrFiles[i+idx1*sizePerGroup];
			idx++;
		}
		for(int i=0;i<sizePerGroup;i++){
			arrOutFiles[idx] = arrFiles[i+idx2*sizePerGroup];
			idx++;
		}
		return arrOutFiles;
	}
}
