package com.ant.crawler.core.content.relate.global;

public interface VectorDocIterator extends AutoCloseable {

	public abstract VectorDoc prev();

	public abstract VectorDoc next();

	public abstract void remove();

}