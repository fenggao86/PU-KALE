package kale.util;

import java.util.HashMap;
import java.util.HashSet;

import kale.joint.EmbeddingSpace;
import kale.struct.Matrix;
import kale.struct.MatrixMap;
import kale.struct.TripleSet;

public class MetricMonitor {
	public TripleSet lstValidateTriples;
	public HashMap<String, Boolean> lstTriples;
	public MatrixMap MatrixE;
	public MatrixMap MatrixR;
	public double dMeanRank;
	public double dMRR;
	public double dHits;
	public EmbeddingSpace space;
	
	public MetricMonitor(TripleSet inLstValidateTriples,
			HashMap<String, Boolean> inlstTriples,
			MatrixMap inMatrixE,
			MatrixMap inMatrixR) {
		lstValidateTriples = inLstValidateTriples;
		lstTriples = inlstTriples;
		MatrixE = inMatrixE;
		MatrixR = inMatrixR;
	}

	public MetricMonitor(TripleSet inLstValidateTriples,
						 HashMap<String, Boolean> inlstTriples,
						 MatrixMap inMatrixE,
						 MatrixMap inMatrixR,
						 EmbeddingSpace space) {
		lstValidateTriples = inLstValidateTriples;
		lstTriples = inlstTriples;
		MatrixE = inMatrixE;
		MatrixR = inMatrixR;
		this.space = space;
	}
	
	public int calculateMetrics() throws Exception {
		//矩阵E的行和列
		//int iNumberOfEntities = MatrixE.rows();
		int iNumberOfFactors = MatrixE.columns();
		
		int iCnt = 0;
		double avgMeanRank = 0.0;
		double avgMRR = 0.0;
		int avgHits = 0;
		HashSet<Integer> entitySet = space.getEntitySet();
		HashSet<Integer> relationSet = space.getRelationSet();
		int mycount = 0;
		for (int iID = 0; iID < lstValidateTriples.triples(); iID++) {//对每一个triple
			int iRelationID = lstValidateTriples.get(iID).relation(); //获取triple对应的relationId
			int iSubjectID = lstValidateTriples.get(iID).head(); //获取tripe的head
			int iObjectID = lstValidateTriples.get(iID).tail(); //获取triple对应的tail
			if (!entitySet.contains(iSubjectID) || !entitySet.contains(iObjectID) || !relationSet.contains(iRelationID)) {
				continue;
			}
			mycount++;
			double dTargetValue = 0.0;
			for (int p = 0; p < iNumberOfFactors; p++) { //对每一列
				// |Matrix<headId, p> + Matrix<relationId, p> - Matrix<tailId, p>|
				//执行矩阵运算|h+r-t|
				dTargetValue -= Math.abs(MatrixE.get(iSubjectID, p) + MatrixR.get(iRelationID, p) - MatrixE.get(iObjectID, p));
			}
			
			int iLeftRank = 1;
			int iLeftIdentical = 0;
			//for (int iLeftID = 0; iLeftID < iNumberOfEntities; iLeftID++) {
			for (int iLeftID : space.getEntitySet()) {
				double dValue = 0.0;
				//破坏triple，这里是替换掉了头实体head
				String negTiple = iLeftID + "\t" + iRelationID + "\t" +iObjectID;
				//破坏的triple不是一个正确的triple（即它是negative的），即它不在lstTriple中
				if(!lstTriples.containsKey(negTiple)){
					//计算|h+r-t|
					for (int p = 0; p < iNumberOfFactors; p++) {
						dValue -= Math.abs(MatrixE.get(iLeftID, p) + MatrixR.get(iRelationID, p) - MatrixE.get(iObjectID, p));
					}
					if (dValue > dTargetValue) {
						iLeftRank++;
					}
					if (dValue == dTargetValue) {
						iLeftIdentical++;
					}
				}
			}
			
			double dLeftRank = iLeftRank;
			int iLeftHitsAt10 = 0;
			if (dLeftRank <= 10.0) {
				iLeftHitsAt10 = 1;
			}
			avgMeanRank += dLeftRank;
			avgMRR += 1.0/(double)dLeftRank;
			avgHits += iLeftHitsAt10;
			iCnt++;
			
			int iRightRank = 1;
			int iRightIdentical = 0;
			//for (int iRightID = 0; iRightID < iNumberOfEntities; iRightID++) {
			for (int iRightID : space.getEntitySet()) {
				double dValue = 0.0;
				String negTiple = iSubjectID + "\t" + iRelationID + "\t" +iRightID;
				if(!lstTriples.containsKey(negTiple)){
					for (int p = 0; p < iNumberOfFactors; p++) {
						dValue -= Math.abs(MatrixE.get(iSubjectID, p) + MatrixR.get(iRelationID, p) - MatrixE.get(iRightID, p));
					}
					if (dValue > dTargetValue) {
						iRightRank++;
					}
					if (dValue == dTargetValue) {
						iRightIdentical++;
					}
				}
			}

			double dRightRank = iRightRank;
			int iRightHitsAt10 = 0;
			if (dRightRank <= 10.0) {
				iRightHitsAt10 = 1;
			}
			avgMeanRank += dRightRank;
			avgMRR += 1.0/(double)dRightRank;
			avgHits += iRightHitsAt10;
			iCnt++;	
		}
		
		dMRR = avgMRR / (double)(iCnt);
		dHits = (double)avgHits / (double)(iCnt);
		System.out.println("avgMRR:" + avgMRR + "\t" + "avgHits:" + avgHits);
		System.out.println("MRR:" + dMRR + "\t" + "Hits:" + dHits);
		System.out.println("mycount=============" + mycount);
		return mycount;
	}
}
