package com.ant.crawler.core.content.relate.global;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.ant.crawler.core.utils.PrismConstants;
import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class VectorDocDAOImpl implements VectorDocDAO {
	private static final String CLASS_DB = "classdb";
	private Environment env;
	private Database vecDB;
	private TupleBinding<Long> keyBinding = TupleBinding
			.getPrimitiveBinding(Long.class);
	private Database classDB;
	private StoredClassCatalog classCatalog;
	private static EntryBinding<VectorDoc> dataBinding;

	public VectorDocDAOImpl() throws Exception {
		File vectorHome = new File(PrismConstants.VECTOR_HOME);
		if (!vectorHome.isDirectory()) {
			if (!vectorHome.mkdir()) {
				throw new Exception("can't create vector home:" + vectorHome);
			}
		}

		EnvironmentConfig envConf = null;
		File dbvecConf = new File(PrismConstants.VECTOR_STORAGE_CONF);
		if (dbvecConf.isFile()) {
			Properties dbProp = new Properties();
			FileReader dbConfReader = null;
			try {
				dbConfReader = new FileReader(dbvecConf);
				dbProp.load(dbConfReader);
				envConf = new EnvironmentConfig(dbProp);
			} finally {
				if (dbConfReader != null) {
					dbConfReader.close();
				}
			}
		} else {
			envConf = new EnvironmentConfig();
		}
		envConf.setAllowCreate(true);
		env = new Environment(vectorHome, envConf);
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setAllowCreate(true);
		vecDB = env.openDatabase(null, PrismConstants.VECTOR_STORE, dbConfig);
		classDB =  env.openDatabase(null, CLASS_DB, dbConfig);
		classCatalog = new StoredClassCatalog(classDB);
		dataBinding = new SerialBinding<VectorDoc>(classCatalog,
				VectorDoc.class);
	}

	@Override
	public Collection<VectorDoc> getVectorDocs() {
		Set<VectorDoc> vectorDocs = new LinkedHashSet<VectorDoc>();
		Cursor cursor = null;
		try {
			cursor = vecDB.openCursor(null, null);
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();
			VectorDoc vector = null;
			while (cursor.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				System.out.println(keyBinding.entryToObject(key));
				vector = dataBinding.entryToObject(data);
				vectorDocs.add(vector);
			}
		} finally {
			// Always make sure the cursor is closed when we are done with it.
			if (cursor != null) {
				cursor.close();
			}
		}
		return vectorDocs;
	}

	@Override
	public boolean storeVectorDoc(VectorDoc vector) {
		DatabaseEntry key = new DatabaseEntry();
		keyBinding.objectToEntry(vector.getDocid(), key);
		DatabaseEntry data = new DatabaseEntry();
		dataBinding.objectToEntry(vector, data);
		vecDB.put(null, key, data);
		return true;
	}

	public void close() {
		vecDB.close();
		classDB.close();
		env.cleanLog();
		env.close();
	}

	@Override
	public void sync() {
		env.sync();
	}

	@Override
	public long count() {
		return vecDB.count();
	}

	@Override
	public Collection<VectorDoc> getRecentVectorDocs(int num) {
		List<VectorDoc> vectorDocs = new ArrayList<VectorDoc>();
		Cursor cursor = null;
		try {
			cursor = vecDB.openCursor(null, null);
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();
			VectorDoc vector = null;
			int counter = 0;
			while (counter < num && cursor.getPrev(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				keyBinding.entryToObject(key);
				vector = dataBinding.entryToObject(data);
				vectorDocs.add(vector);
				++counter;
			}
		} finally {
			// Always make sure the cursor is closed when we are done with it.
			if (cursor != null) {
				cursor.close();
			}
		}
		Set<VectorDoc> result = new LinkedHashSet<VectorDoc>();
		for (int i = vectorDocs.size() - 1; i >= 0; --i) {
			result.add(vectorDocs.get(i));
		}
		return result;
	}

	@Override
	public void removeOldest(long num) {
		Cursor cursor = null;
		try {
			cursor = vecDB.openCursor(null, null);
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();
			int counter = 0;
			while (counter < num && cursor.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				cursor.delete();
				++counter;
			}
		} finally {
			// Always make sure the cursor is closed when we are done with it.
			if (cursor != null) {
				cursor.close();
			}
		}
	}
	
	@Override
	public VectorDocIterator getIterator() {
		return new VectorDocIteratorImpl(vecDB);
	}

	public static class VectorDocIteratorImpl implements VectorDocIterator {
		private Cursor cursor;
		
		VectorDocIteratorImpl(Database vecDB) {
			cursor = vecDB.openCursor(null, null);
		}
		
		/* (non-Javadoc)
		 * @see com.ant.crawler.core.similar.VectorDocIterator#prev()
		 */
		@Override
		public VectorDoc prev() {
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();
			VectorDoc vector = null;
			if (cursor.getPrev(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				vector = dataBinding.entryToObject(data);
				return vector;
			}
			return null;
		}

		
		/* (non-Javadoc)
		 * @see com.ant.crawler.core.similar.VectorDocIterator#next()
		 */
		@Override
		public VectorDoc next() {
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();
			VectorDoc vector = null;
			if (cursor.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				vector = dataBinding.entryToObject(data);
				return vector;
			}
			return null;
		}

		
		/* (non-Javadoc)
		 * @see com.ant.crawler.core.similar.VectorDocIterator#remove()
		 */
		@Override
		public void remove() {
			cursor.delete();
		}
		
		/* (non-Javadoc)
		 * @see com.ant.crawler.core.similar.VectorDocIterator#close()
		 */
		@Override
		public void close() {
			if (cursor != null) {
				cursor.close();
			}
		}
		
	}

}
