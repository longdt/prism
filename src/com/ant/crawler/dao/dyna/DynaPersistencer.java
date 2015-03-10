package com.ant.crawler.dao.dyna;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ddlutils.DatabaseOperationException;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.dynabean.SqlDynaClass;
import org.apache.ddlutils.model.Database;
import org.apache.log4j.Logger;

import com.ant.crawler.core.conf.PrismConfiguration;
import com.ant.crawler.core.content.relate.DocSimilar;
import com.ant.crawler.core.utils.PrismConstants;
import com.ant.crawler.dao.BasePersistencer;

public class DynaPersistencer extends BasePersistencer {
	private static final Logger logger = Logger.getLogger(DynaPersistencer.class);
	private static final String DB_CONF_FILE = "conf/database/database.properties";
	public static final String DATASOURCE_PROPERTY_PREFIX = "datasource.";
	private static final String ENTITY_RELATE_INSERT_SQL = PrismConfiguration.getInstance().get(PrismConstants.ENTITY_RELATE_INSERT_SQL, "");
	private Platform platform;
	private Database database;

	public DynaPersistencer() throws IOException {
		Properties props = loadConf();
		DataSource dataSource = initDataSource(props);
		platform = PlatformFactory.createNewPlatformInstance(dataSource);
		database = platform.readModelFromDatabase(null);
	}

	private DataSource initDataSource(Properties props) {
		try {
			String dataSourceClass = props.getProperty(
					DATASOURCE_PROPERTY_PREFIX + "class",
					BasicDataSource.class.getName());
			DataSource dataSource = (DataSource) Class.forName(dataSourceClass)
					.newInstance();
			int prefixLen = DATASOURCE_PROPERTY_PREFIX.length();
			for (Iterator it = props.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String propName = (String) entry.getKey();

				if (propName.startsWith(DATASOURCE_PROPERTY_PREFIX)
						&& !propName.equals(DATASOURCE_PROPERTY_PREFIX
								+ "class")) {
					BeanUtils.setProperty(dataSource, propName
							.substring(prefixLen),
							entry.getValue());
				}
			}
			return dataSource;
		} catch (Exception ex) {
			throw new DatabaseOperationException(ex);
		}
	}

	private Properties loadConf() throws IOException {
		Reader in = null;
		try {
			in = new FileReader(DB_CONF_FILE);
			Properties props = new Properties();
			props.load(in);
			return props;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	@Override
	protected long insertEntity(Object entity, List<DocSimilar> relateEntities, String pkName) {
		Connection connection = platform.borrowConnection();
        try
        {
        	platform.insert(connection, database, (DynaBean)entity);
        	long pk = ((Number) PropertyUtils.getProperty(entity, pkName)).longValue();
        	if (!ENTITY_RELATE_INSERT_SQL.isEmpty()) {
        		insertRelate(connection, pk, relateEntities);
        	}
        	return pk;
        } catch (Exception e) {
			logger.error("can't insert entity: " + entity, e);
		}finally
        {
        	platform.returnConnection(connection);
        }
		return 0;
	}
	
	private void insertRelate(Connection connection, long pk,
			List<DocSimilar> relateEntities) throws SQLException {
		PreparedStatement preStat = connection.prepareStatement(ENTITY_RELATE_INSERT_SQL);
		for (DocSimilar doc : relateEntities) {
			preStat.setLong(1, pk);
			preStat.setLong(2, doc.getDocid());
			preStat.setFloat(3, doc.getPercent());
			preStat.executeUpdate();
		}
	}

	public SqlDynaClass createClass(String tableName) {
		return database.getDynaClassFor(tableName);
	}

}
