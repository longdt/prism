package com.ant.crawler.core.utils;

import java.io.IOException;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.process.ProcessStarter;

import com.ant.crawler.core.conf.PrismConfiguration;

public class ImageProccessor {
	public static final String defImagicPath = "tools\\ImageMagick-6.7.6-Q16";
	public static final String imagicPath = PrismConfiguration.getInstance()
			.getStrings(PrismConstants.CRAWL_IMAGIC_PATH, defImagicPath)[0];

	public static String resize(String inFile, String outFile, int width,
			int height) {
		try {
			/*Info info = new Info(inFile);
			long inputRatio = info.getImageWidth()/info.getImageHeight();
			long outputRatio = width/height;
			if(inputRatio > outputRatio){
				
			}*/
			ProcessStarter.setGlobalSearchPath(imagicPath);
			ConvertCmd cmd = new ConvertCmd();
			IMOperation op = new IMOperation();
			op.addImage(inFile);
			op.resize(width, height);
			op.addImage(outFile);

			cmd.run(op);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IM4JavaException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		ImageProccessor.resize("img_sample_2.jpg", "img_sample_2_small.jpg",
				480, 330);
	}
}
