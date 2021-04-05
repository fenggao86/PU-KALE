package kale.struct;

//规则
public class Rule {
	private Triple FstTriple=null; //第一个三元组
	private Triple SndTriple=null; //第二个三元组
	private Triple TrdTriple=null; //第三个三元组
	
	public Rule(Triple inFstTriple, Triple inSndTriple) {
		FstTriple = inFstTriple;
		SndTriple = inSndTriple;
	}
	public Rule(Triple inFstTriple, Triple inSndTriple,Triple inTrdTriple) {
		FstTriple = inFstTriple;
		SndTriple = inSndTriple;
		TrdTriple = inTrdTriple;
	}
	
	public Triple fstTriple() {
		return FstTriple;
	}
	
	public Triple sndTriple() {
		return SndTriple;
	}
	
	public Triple trdTriple() {
		return TrdTriple;
	}
	
}
