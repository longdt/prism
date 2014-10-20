package com.ant.crawler.core.content.relate;

import java.util.Collection;

public interface VectorDocDAO {
	public boolean storeVectorDoc(VectorDoc vector);
	
	public Collection<VectorDoc> getVectorDocs();
	
	public Collection<VectorDoc> getRecentVectorDocs(int num);
	
	public void close();

	public void sync();

	public long count();

	public void removeOldest(long num);
	
	public VectorDocIterator getIterator();
}
