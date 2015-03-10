package com.ant.crawler.core.entity;

import org.apache.commons.beanutils.DynaBean;
import org.apache.ddlutils.dynabean.SqlDynaClass;
import org.apache.ddlutils.dynabean.SqlDynaProperty;

public abstract class EntityBuilderFactory {
	protected String entityIDField;
	protected String subEntityIDField;
	
	public static EntityBuilderFactory newInstance(Class entityClass) {
		return newInstance(entityClass, null);
	}
	
	public static EntityBuilderFactory newInstance(Class entityClass, Class subEntityClass) {
		return new PojoEntityBuilderFactory(entityClass, subEntityClass);
	}

	public static EntityBuilderFactory newInstance(SqlDynaClass entityClass) {
		return newInstance(entityClass, null);
	}
	
	public static EntityBuilderFactory newInstance(SqlDynaClass entityClass, SqlDynaClass subEntityClass) {
		return new DynaEntityBuilderFactory(entityClass, subEntityClass);
	}

	public EntityBuilder newEntityBuilder() {
		try {
			Object entity = newEntity();
			if (entityIDField == null) {
				entityIDField = getIDField(entity);
			}
			return new EntityBuilder(entity, entityIDField, this);
		} catch (InstantiationException | IllegalAccessException | UnsupportedEntityException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public EntityBuilder newSubEntityBuilder() {
		try {
			Object entity = newSubEntity();
			if (subEntityIDField == null) {
				subEntityIDField = getIDField(entity);
			}
			return new EntityBuilder(entity, subEntityIDField);
		} catch (InstantiationException | IllegalAccessException | UnsupportedEntityException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected abstract String getIDField(Object entity) throws UnsupportedEntityException;
	
	protected abstract Object newEntity() throws InstantiationException, IllegalAccessException;
	
	protected abstract Object newSubEntity() throws InstantiationException, IllegalAccessException;
}

class PojoEntityBuilderFactory extends EntityBuilderFactory {
	private Class entityClass;
	private Class subEntityClass;

	public PojoEntityBuilderFactory(Class entityClass, Class subEntityClass) {
		this.entityClass = entityClass;
		this.subEntityClass = subEntityClass;
	}

	@Override
	protected String getIDField(Object entity) {
		return null;
	}

	@Override
	protected Object newEntity() throws InstantiationException,
			IllegalAccessException {
		return entityClass.newInstance();
	}

	@Override
	protected Object newSubEntity() throws InstantiationException,
			IllegalAccessException {
		return subEntityClass.newInstance();
	}
}

class DynaEntityBuilderFactory extends EntityBuilderFactory {
	private SqlDynaClass entityClass;
	private SqlDynaClass subEntityClass;

	public DynaEntityBuilderFactory(SqlDynaClass entityClass, SqlDynaClass subEntityClass) {
		this.entityClass = entityClass;
		this.subEntityClass = subEntityClass;
	}

	@Override
	protected String getIDField(Object classObj) throws UnsupportedEntityException {
		SqlDynaProperty[] properties = ((SqlDynaClass)((DynaBean) classObj).getDynaClass()).getPrimaryKeyProperties();
		if (properties.length != 1) {
			throw new UnsupportedEntityException();
		}
		return properties[0].getName();
	}

	@Override
	protected Object newEntity() throws InstantiationException,
			IllegalAccessException {
		return entityClass.newInstance();
	}

	@Override
	protected Object newSubEntity() throws InstantiationException,
			IllegalAccessException {
		return subEntityClass.newInstance();
	}


}
