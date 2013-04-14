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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.simpleworkflow.flow.worker.CurrentActivityExecutionContext;

/**
 * This is an S3 Store implementation which provides Activities to download/upload files from S3
 *
 */
public class SimpleStoreActivitiesS3Impl implements SimpleStoreActivities {
    private static final int HEARTBEAT_INTERVAL = 300000;
    
    private AmazonS3 s3Client;
    private final String localDirectory;
    private final String hostSpecificTaskList;
    
    
    public SimpleStoreActivitiesS3Impl(String localDirectory, String taskList) {
        this.localDirectory = localDirectory;
        this.hostSpecificTaskList = taskList;
    }
    
    @Override
    public String upload(String bucketName, String localName, String targetName) throws Exception {
    	try {
    		return uploadFileToS3(bucketName, localDirectory + localName, targetName);
    	} catch (Exception e) {
    		throw e;
    	}
    }

    /**
     * 
     * @param bucket
     *          Name of S3 bucket
     * @param localName
     *          Name of the file to upload
     * @param remoteName
     *          Key for the S3 object
     * @param fromBox
     *          The value for this parameter is used to control the scheduling of this Activity at runtime.
     *          In this case it is putting requirement to run this activity on the machine name passed in.
     *          We want to run this activity on the same box that ran the download. 
     * @return
     *          A Value object
     */

    private String uploadFileToS3(String bucket, String localName, String remoteName) {
        System.out.println("uploadToS3 begin bucket=" + bucket + " remoteName=" + remoteName + ", localName=" + localName);
        AmazonS3 storage = getS3Client();
        File f = new File(localName);
        try {
            storage.putObject(bucket, remoteName, f);
            Date expiration = new Date(new Date().getTime() + 1000 * 60 * 30);
            storage.generatePresignedUrl(bucket, remoteName, expiration);
            System.out.println("uploadToS3 done");
            return storage.generatePresignedUrl(bucket, remoteName, expiration).toString();
        }
        catch (AmazonServiceException e) {
            String message = "Failure uploading to S3";
            System.out.println(message);
            throw e;
        }
        catch (AmazonClientException e) {
            String message = "Failure uploading to S3";
            System.out.println(message);
            throw e;
        }
    }
    
    @Override
    public String download(String bucketName, String remoteName, String localName) throws Exception {
        return downloadFileFromS3(bucketName, remoteName, localDirectory + localName);
    }
    
    /**
     * 
     * @param bucketName
     *          Name of S3 bucket
     * @param remoteName
     *          Key to use for uploaded S3 object
     * @param localName
     *          Name of the file locally
     * @param toBox
     *          This is an output parameter here.  
     *          Used to communicate the name of the box that runs download activity
     * @return
     *      A Value object
     * @throws IOException 
     */


    private String downloadFileFromS3(String bucketName, String remoteName, String localName) throws IOException {
        System.out.println("downloadFileFromS3 begin remoteName=" + remoteName + ", localName=" + localName);
        AmazonS3 storage = getS3Client();
        try {
            FileOutputStream f = new FileOutputStream(localName);
            try {
                S3Object obj = storage.getObject(bucketName, remoteName);
                InputStream inputStream = obj.getObjectContent();
                long totalSize = obj.getObjectMetadata().getContentLength();

                try {
                    long totalRead = 0;
                    int read = 0;
                    byte[] bytes = new byte[1024];
                    long lastHeartbeatTime = System.currentTimeMillis();
                    while ((read = inputStream.read(bytes)) != -1) {
                        totalRead += read; 
                        f.write(bytes, 0, read);
                        int progress = (int)(totalRead/totalSize * 100);
                        lastHeartbeatTime = heartbeat(lastHeartbeatTime, progress);
                    }
                }
                finally {
                    inputStream.close();
                }
            }
            finally {
                f.close();
            }
        }
        catch (AmazonServiceException e) {
            String message = "Failure downloading from S3";
            System.out.println(message);
            throw e;
        }
        catch (AmazonClientException e) {
            String message = "Failure downloading from S3";
            System.out.println(message);
            throw e;
        }
        catch (IOException e) {
            String message = "Failure downloading from S3";
            System.out.println(message);
            throw e;
        }
        // Return hostname file was downloaded to        
        System.out.println("downloadFileFromS3 done");
        return hostSpecificTaskList;
        //todo: remove after testing
    }
    
    @Override
    public void deleteLocalFile(String fileName) {
        deleteLocalFiles(localDirectory + fileName);
    }

    /**
     * 
     * @param fileName
     *          Filename to delete locally
     * @param machineName
     *          The value for this parameter is used to control the scheduling of this Activity at runtime.
     *          In this case it is putting requirement to run this activity on the machine name passed in. 
     * @return
     */

    private void deleteLocalFiles(String fileName) {
        System.out.println("deleteLocalActivity begin fileName=" + fileName);
        File f = new File(fileName);
        f.delete();
        
        System.out.println("deleteLocalActivity done");
    }
    
    /**
     * Heartbeat every 5 minutes. It is not a good idea to heartbeat too
     * frequently as each noteActivityProgress event ends up eating history
     * events count.
     * 
     * @return time of the last heartbeat
     */
    private static long heartbeat(long lastHeartbeatTime, int progress) {
        if (System.currentTimeMillis() - lastHeartbeatTime > HEARTBEAT_INTERVAL) {
            CurrentActivityExecutionContext.get().recordActivityHeartbeat(Integer.toString((progress)));
            lastHeartbeatTime = System.currentTimeMillis();
        }
        return lastHeartbeatTime;
    }
    

	public void setS3Client(AmazonS3 s3Client) {
		this.s3Client = s3Client;
	}

	public AmazonS3 getS3Client() {
		return s3Client;
	}

}
