package com.ant.crawler.core.content.relate;

public interface VectorDocIterator {

	public abstract VectorDoc prev();

	public abstract VectorDoc next();

	public abstract void remove();

	public abstract void close();

}