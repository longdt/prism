package com.ant.crawler.core.content.tag;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LoadDataFromFile implements DataLoader {
	public static final String FILE_DATA_FOLDER = "traindata";
	private List<StringBuilder> data ;
	
	public LoadDataFromFile() {
		data = new ArrayList<StringBuilder>();
	}
	@Override
	public List<StringBuilder> getContenData()  {
		File dataFolder = new File(FILE_DATA_FOLDER);
		if (!dataFolder.isDirectory()) 
			System.exit(-1);
		
		File [] listResource = dataFolder.listFiles();
		int i = 0;
		for (File file : listResource) {
			
			try {
				if (i < ApplicationConfig.getTotalResouce()) {
					Scanner in = new Scanner(new BufferedReader(new FileReader(file)));
					StringBuilder content = new StringBuilder();
					while (in.hasNext()) {
						content.append(in.next());
						content.append(" ");
					}
					data.add(content);
					 ++ i;
				}
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		return data;
	}
}
