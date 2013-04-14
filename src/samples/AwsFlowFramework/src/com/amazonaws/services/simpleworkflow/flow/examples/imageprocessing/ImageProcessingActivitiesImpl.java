/*
 * Copyright 2012 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazonaws.services.simpleworkflow.flow.examples.imageprocessing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


/**
 * This implementation of FileProcessingActivities converts the file
 */
public class ImageProcessingActivitiesImpl implements ImageProcessingActivities{
	private final String localDirectory;
	
	/*This constructor will create a implementation of ImageProcessingActrivities
	* @param localDirectory
    *          Path to the local directory containing the file to convert
    */
	public ImageProcessingActivitiesImpl(String localDirectory) {
		this.localDirectory = localDirectory;
	}
                
    /**
     * This is the Activity implementation that does the convert of a file to Grayscale
     * @param inputFileName
     *          Name of file to convert
     * @param targetFileName
     *          Filename after convert
     */	
	@Override    
    public void convertToGrayscale(String inputFileName, String targetFileName) throws IOException {
		doConversion(inputFileName, targetFileName, BufferedImage.TYPE_BYTE_GRAY);
	}
	
	/**
     * This is the Activity implementation that does the convert of a file to Sepia
     * @param inputFileName
     *          Name of file to convert
     * @param targetFileName
     *          Filename after convert
     */	
	@Override
	public void convertToSepia(String inputFileName, String targetFileName) throws IOException {
		doConversion(inputFileName, targetFileName, BufferedImage.TYPE_INT_ARGB);
	}
	
	private void doConversion(String inputFileName, String targetFileName, int option) throws IOException {
		String fileNameFullPath = localDirectory + inputFileName;
		String processedFileNameFullPath = localDirectory + targetFileName;
		BufferedImage srcImage = null;
		BufferedImage destImage = null;
		System.out.println("processImage activity begin.  fileName= "
				+ fileNameFullPath + ", processedFileName= "
				+ processedFileNameFullPath);

        try {
        	srcImage = ImageIO.read(new File(fileNameFullPath));
        	destImage = new BufferedImage(srcImage.getWidth(), srcImage.getHeight(), option);
    		if (option == BufferedImage.TYPE_BYTE_GRAY) {
    			destImage.getGraphics().drawImage(srcImage, 0, 0, null);
    		} else if (option == BufferedImage.TYPE_INT_ARGB) {
    			Graphics tempGraphics = destImage.getGraphics();
        		tempGraphics.setColor(new Color(120, 120, 0, 120));
        		tempGraphics.drawImage(srcImage, 0, 0, null);
        		tempGraphics.fillRect(0, 0, destImage.getWidth(), destImage.getHeight());
    		}
    		
    		ImageIO.write(destImage, "PNG", new File(processedFileNameFullPath));
    		System.out.println("convertFileActivity done.");
        }
        catch (IOException e) {
            String message = "Processing image: " + inputFileName + " failed";
            System.out.println(message);
            throw e;
        }
	}

}
