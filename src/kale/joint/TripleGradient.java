package kale.joint;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import kale.struct.Matrix;
import kale.struct.MatrixMap;
import kale.struct.Triple;

/**
 * triple梯度下降
 */
public class TripleGradient {
	public Triple PosTriple;
	public Triple NegTriple;
	public MatrixMap MatrixE;
	public MatrixMap MatrixR;
	public MatrixMap MatrixEGradient;
	public MatrixMap MatrixRGradient;
	public double dDelta;  //margin
	double dPosPi;
	double dNegPi;
	
	public TripleGradient(
			Triple inPosTriple,
			Triple inNegTriple,
			MatrixMap inMatrixE,
			MatrixMap inMatrixR,
			MatrixMap inMatrixEGradient,
			MatrixMap inMatrixRGradient,
			double inDelta) {
		PosTriple = inPosTriple;
		NegTriple = inNegTriple;
		MatrixE = inMatrixE;
		MatrixR = inMatrixR;
		MatrixEGradient = inMatrixEGradient;
		MatrixRGradient = inMatrixRGradient;
		dDelta = inDelta;
	}

	//梯度下降计算，计算真值
	public void calculateGradient() throws Exception {
		int iNumberOfFactors = MatrixE.columns(); //列
		int iPosHead = PosTriple.head(); //head
		int iPosTail = PosTriple.tail(); //tail
		int iPosRelation = PosTriple.relation(); //relation
		int iNegHead = NegTriple.head();
		int iNegTail = NegTriple.tail();
		int iNegRelation = NegTriple.relation();

		// 1 / (3 * √k)，其中k是向量维度，也就是列
		double dValue = 1.0 / (3.0 * Math.sqrt(iNumberOfFactors));
		dPosPi = 0.0;
		for (int p = 0; p < iNumberOfFactors; p++) {
			// -∑|E(head, p) + R(relation, p) - E(tail, p)|
			dPosPi -= Math.abs(MatrixE.get(iPosHead, p) + MatrixR.get(iPosRelation, p) - MatrixE.get(iPosTail, p));
		}
		// (-∑|E(head, p) + R(relation, p) - E(tail, p)|) * (1 / (3 * √k)
		dPosPi *= dValue;
		dPosPi += 1.0; //(-∑|E(head, p) + R(relation, p) - E(tail, p)|) * (1 / (3 * √k) + 1，值的范围是(0, 1)
		
		dNegPi = 0.0;
		for (int p = 0; p < iNumberOfFactors; p++) {
			dNegPi -= Math.abs(MatrixE.get(iNegHead, p) + MatrixR.get(iNegRelation, p) - MatrixE.get(iNegTail, p));
		}
		dNegPi *= dValue;
		dNegPi += 1.0;
         
		
		if (dDelta - dPosPi + dNegPi > 0.0) { //dDelta是不是γ
//		if (dDeltaAdapt - dPosPi + dNegPi > 0.0) {
			for (int p = 0; p < iNumberOfFactors; p++) {
				double dPosSgn = 0.0;
				if (MatrixE.get(iPosHead, p) + MatrixR.get(iPosRelation, p) - MatrixE.get(iPosTail, p) > 0) {
					dPosSgn = 1.0;
				} else if (MatrixE.get(iPosHead, p) + MatrixR.get(iPosRelation, p) - MatrixE.get(iPosTail, p) < 0) {
					dPosSgn = -1.0;
				}
				MatrixEGradient.add(iPosHead, p, dValue * dPosSgn);
				MatrixEGradient.add(iPosTail, p, -1.0 * dValue * dPosSgn);
				MatrixRGradient.add(iPosRelation, p, dValue * dPosSgn);
//				System.out.println("true0:"+  dValue * dPosSgn);
 				double dNegSgn = 0.0;
				if (MatrixE.get(iNegHead, p) + MatrixR.get(iNegRelation, p) - MatrixE.get(iNegTail, p) > 0) {
					dNegSgn = 1.0;
				} else if (MatrixE.get(iNegHead, p) + MatrixR.get(iNegRelation, p) - MatrixE.get(iNegTail, p) < 0) {
					dNegSgn = -1.0;
				}
				MatrixEGradient.add(iNegHead, p, -1.0 * dValue * dNegSgn);
				MatrixEGradient.add(iNegTail, p, dValue * dNegSgn);
				MatrixRGradient.add(iNegRelation, p, -1.0 * dValue * dNegSgn);
			}
		}
		
	}
}
