package com.ant.crawler.core.entity;

import org.apache.commons.beanutils.DynaClass;

public abstract class EntityBuilderFactory {

	public static EntityBuilderFactory newInstance(Class entityClass) {
		return new PojoEntityBuilderFactory(entityClass);
	}

	public static EntityBuilderFactory newInstance(DynaClass entityClass) {
		return new DynaEntityBuilderFactory(entityClass);
	}

	public abstract EntityBuilder newEntityBuilder();

}

class PojoEntityBuilderFactory extends EntityBuilderFactory {
	private Class entityClass;

	public PojoEntityBuilderFactory(Class entityClass) {
		this.entityClass = entityClass;
	}

	@Override
	public EntityBuilder newEntityBuilder() {
		try {
			return new EntityBuilder(entityClass.newInstance());
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}

class DynaEntityBuilderFactory extends EntityBuilderFactory {
	private DynaClass entityClass;

	public DynaEntityBuilderFactory(DynaClass entityClass) {
		this.entityClass = entityClass;
	}

	@Override
	public EntityBuilder newEntityBuilder() {
		try {
			return new EntityBuilder(entityClass.newInstance());
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
