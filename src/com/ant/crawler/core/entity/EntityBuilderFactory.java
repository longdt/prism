package com.ant.crawler.core.entity;

import org.apache.commons.beanutils.DynaClass;

public abstract class EntityBuilderFactory {

	public static EntityBuilderFactory newInstance(Class entityClass) {
		return newInstance(entityClass, null);
	}
	
	public static EntityBuilderFactory newInstance(Class entityClass, Class subEntityClass) {
		return new PojoEntityBuilderFactory(entityClass, subEntityClass);
	}

	public static EntityBuilderFactory newInstance(DynaClass entityClass) {
		return newInstance(entityClass, null);
	}
	
	public static EntityBuilderFactory newInstance(DynaClass entityClass, DynaClass subEntityClass) {
		return new DynaEntityBuilderFactory(entityClass, subEntityClass);
	}

	public abstract EntityBuilder newEntityBuilder();
	
	public abstract EntityBuilder newSubEntityBuilder();

}

class PojoEntityBuilderFactory extends EntityBuilderFactory {
	private Class entityClass;
	private Class subEntityClass;

	public PojoEntityBuilderFactory(Class entityClass, Class subEntityClass) {
		this.entityClass = entityClass;
		this.subEntityClass = subEntityClass;
	}

	@Override
	public EntityBuilder newEntityBuilder() {
		try {
			return new EntityBuilder(entityClass.newInstance(), this);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public EntityBuilder newSubEntityBuilder() {
		try {
			return new EntityBuilder(subEntityClass.newInstance());
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
}

class DynaEntityBuilderFactory extends EntityBuilderFactory {
	private DynaClass entityClass;
	private DynaClass subEntityClass;

	public DynaEntityBuilderFactory(DynaClass entityClass, DynaClass subEntityClass) {
		this.entityClass = entityClass;
		this.subEntityClass = subEntityClass;
	}

	@Override
	public EntityBuilder newEntityBuilder() {
		try {
			return new EntityBuilder(entityClass.newInstance(), this);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public EntityBuilder newSubEntityBuilder() {
		try {
			return new EntityBuilder(subEntityClass.newInstance());
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

}
