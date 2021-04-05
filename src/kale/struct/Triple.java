package kale.struct;

import java.util.Objects;

public class Triple {
	private int iHeadEntity;
	private int iTailEntity;
	private int iRelation;
	
	public Triple() {
	}
	
	public Triple(int i, int j, int k) {
		iHeadEntity = i;
		iTailEntity = j;
		iRelation = k;
	}
	
	public int head() {
		return iHeadEntity;
	}
	
	public int tail() {
		return iTailEntity;
	}
	
	public int relation() {
		return iRelation;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Triple triple = (Triple) o;
		return iHeadEntity == triple.iHeadEntity &&
				iTailEntity == triple.iTailEntity &&
				iRelation == triple.iRelation;
	}

	@Override
	public int hashCode() {
		return Objects.hash(iHeadEntity, iTailEntity, iRelation);
	}
}
