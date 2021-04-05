package kale.util;

import kale.joint.EmbeddingSpace;
import kale.struct.Triple;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

//生成错误三元组，方法是：替换掉头实体head或者尾实体tail
public class NegativeTripleGeneration {
	public Triple PositiveTriple;
	public int iNumberOfEntities;
	public int iNumberOfRelation;
	public EmbeddingSpace space;
	
	public NegativeTripleGeneration(Triple inPositiveTriple,
			int inNumberOfEntities, int inNumberOfRelation) {
		PositiveTriple = inPositiveTriple;
		iNumberOfEntities = inNumberOfEntities;
		iNumberOfRelation = inNumberOfRelation;
	}

	public NegativeTripleGeneration(Triple inPositiveTriple, EmbeddingSpace space) {
		PositiveTriple = inPositiveTriple;
		iNumberOfEntities = space.getEntitySet().size();
		iNumberOfRelation = space.getRelationSet().size();
		this.space = space;
	}
	
	public Triple generateHeadNegTriple() throws Exception {
		int iPosHead = PositiveTriple.head();
		int iPosTail = PositiveTriple.tail();
		int iPosRelation = PositiveTriple.relation();

		/*Triple NegativeTriple = new Triple(iNegHead, iPosTail, iPosRelation);
		while (iNegHead == iPosHead) {
			iNegHead = (int)(Math.random() * iNumberOfEntities);
			NegativeTriple = new Triple(iNegHead, iPosTail, iPosRelation);
		}
		return NegativeTriple;*/
		//获取当前嵌入空间的实体
		HashSet<Integer> entitySet = space.getEntitySet();
		Iterator<Integer> iterator = entitySet.iterator();
		while (iterator.hasNext()) {
			Integer next = iterator.next();
			if (next != iPosHead) {
				return new Triple(next, iPosHead, iPosRelation);
			}
		}
		return new Triple(iPosHead, iPosTail, iPosRelation);
	}
	
	public Triple generateTailNegTriple() throws Exception {
		int iPosHead = PositiveTriple.head();
		int iPosTail = PositiveTriple.tail();
		int iPosRelation = PositiveTriple.relation();
		
		/*int iNegTail = iPosTail;
		Triple NegativeTriple = new Triple(iPosHead, iNegTail, iPosRelation);
		while (iNegTail == iPosTail) {
			iNegTail = (int)(Math.random() * iNumberOfEntities);
			NegativeTriple = new Triple(iPosHead, iNegTail, iPosRelation);
		}
		return NegativeTriple;*/
		HashSet<Integer> entitySet = space.getEntitySet();
		Iterator<Integer> iterator = entitySet.iterator();
		while (iterator.hasNext()) {
			Integer next = iterator.next();
			if (next != iPosHead) {
				return new Triple(iPosHead, next, iPosRelation);
			}
		}
		return new Triple(iPosHead, iPosTail, iPosRelation);
	}
	
	public Triple generateRelNegTriple() throws Exception {
		int iPosHead = PositiveTriple.head();
		int iPosTail = PositiveTriple.tail();
		int iPosRelation = PositiveTriple.relation();
		
		int iNegRelation = iPosRelation;
		Triple NegativeTriple = new Triple(iPosHead, iPosTail, iNegRelation);
		while (iNegRelation == iPosRelation) {
			iNegRelation = (int)(Math.random() * iNumberOfRelation);
			NegativeTriple = new Triple(iPosHead, iPosTail, iNegRelation);
		}
		return NegativeTriple;
	}
}
