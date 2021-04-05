package kale.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import kale.joint.EmbeddingSpace;
import kale.struct.Rule;
import kale.struct.Triple;

public class NegativeRuleGeneration {
	public Rule PositiveRule;
	public int iNumberOfRelations; //relation个数
	private EmbeddingSpace space;
	
	public NegativeRuleGeneration(Rule inPositiveRule,
			EmbeddingSpace space) {
		PositiveRule = inPositiveRule;
		//iNumberOfRelations = inNumberOfRelations;
		this.space = space;
		iNumberOfRelations = space.getRelationSet().size();
	}
	
	public Rule generateSndNegRule() throws Exception {
		HashSet<Integer> relationSet = space.getRelationSet();
		if(PositiveRule.trdTriple()==null){ //替换掉第二个triple的relation，生成否定rule
			Triple fstTriple = PositiveRule.fstTriple(); //第一个triple
			int iSndHead = PositiveRule.sndTriple().head(); //第2个triple的head
			int iSndTail = PositiveRule.sndTriple().tail(); //第2个triple的tail
			int iSndRelation = PositiveRule.sndTriple().relation(); //第2个triple的relation
			int iFstRelation = PositiveRule.fstTriple().relation(); //第1个triple的relation

			Triple sndTriple = new Triple(iSndHead, iSndTail, iSndRelation);
			Iterator<Integer> iterator = relationSet.iterator();
			while (iterator.hasNext()) {
				Integer next = iterator.next();
				if (next != iSndRelation && next != iFstRelation) {
					sndTriple = new Triple(iSndHead, iSndTail, next);
					break;
				}
			}
			Rule NegativeRule = new Rule(fstTriple, sndTriple);
			return NegativeRule;
		}
		else{
			Triple fstTriple = PositiveRule.fstTriple();
			Triple sndTriple = PositiveRule.sndTriple();
			int iTrdHead = PositiveRule.trdTriple().head();
			int iTrdTail = PositiveRule.trdTriple().tail();
			int iTrdRelation = PositiveRule.trdTriple().relation();
			int iFstRelation = PositiveRule.fstTriple().relation();
			int iSndRelation = PositiveRule.sndTriple().relation();

			Triple trdTriple = new Triple(iTrdHead, iTrdTail, iTrdRelation);
			Iterator<Integer> iterator = relationSet.iterator();
			while (iterator.hasNext()) {
				Integer next = iterator.next();
				if (next != iTrdRelation && next != iSndRelation && next != iFstRelation) {
					trdTriple = new Triple(iTrdHead, iTrdTail, next);
					break;
				}
			}
			Rule NegativeRule = new Rule(fstTriple, sndTriple, trdTriple);
			return NegativeRule;
		}
		
	}
	
	public Rule generateFstNegRule() throws Exception {
		Triple sndTriple = PositiveRule.sndTriple();
		int ifstHead = PositiveRule.fstTriple().head();
		int ifstTail = PositiveRule.fstTriple().tail();
		int iFstRelation = PositiveRule.fstTriple().relation();
		int iSndRelation = PositiveRule.sndTriple().relation();
		
		int iNegRelation = iFstRelation;
		Triple fstTriple = new Triple(ifstHead, ifstTail, iNegRelation);
		while (iNegRelation == iSndRelation || iNegRelation == iFstRelation) {
			iNegRelation = (int)(Math.random() * iNumberOfRelations);
			fstTriple = new Triple(ifstHead, ifstTail, iNegRelation);
		}
		Rule NegativeRule = new Rule(fstTriple, sndTriple);
		return NegativeRule;
	}
	
}
