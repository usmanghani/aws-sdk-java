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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.flow.examples.common.ConfigHelper;
import com.amazonaws.services.simpleworkflow.flow.ActivityWorker;

/**
 * This is the process which hosts all Activities in this sample
 */
public class ActivityHost {    
    private static AmazonSimpleWorkflow swfService;
    private static String domain;
    private static AmazonS3 s3Client;
    
    private static ActivityWorker executorForCommonTaskList;
    private static ActivityWorker executorForHostSpecificTaskList;
    private static ActivityHost activityWorker;

    // ActivityWorker Factory method
    public synchronized static ActivityHost getActivityWorker() {
        if (activityWorker == null) {
            activityWorker = new ActivityHost();
        }
        return activityWorker;
    }

    public static void main(String[] args) throws Exception {
    	// Load configuration
    	ConfigHelper configHelper = loadConfig();

        // Start Activity Executor Services
        getActivityWorker().startExecutors(configHelper);
                        
        // Add a Shutdown hook to close ActivityExecutorService
        addShutDownHook();

        System.out.println("Please type 'exit' to terminate service.");
        try {
        	String CurLine = "";
        	InputStreamReader converter = new InputStreamReader(System.in);
        	BufferedReader in = new BufferedReader(converter);
        	while (!(CurLine!=null && CurLine.equals("exit"))){
    		  CurLine = in.readLine();
    		}
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        System.exit(0);

    }
    
    private void startExecutors(ConfigHelper configHelper) throws Exception {   
    	String localFolder = configHelper.getValueFromConfig(ImageProcessingConfigKeys.ACTIVITY_WORKER_LOCALFOLDER);
        String commonTaskList = configHelper.getValueFromConfig(ImageProcessingConfigKeys.ACTIVITY_WORKER_COMMON_TASKLIST);
        
    	// Create activity implementations
    	SimpleStoreActivitiesS3Impl storeActivityImpl = new SimpleStoreActivitiesS3Impl(localFolder, getHostName());
    	storeActivityImpl.setS3Client(s3Client);
    	ImageProcessingActivitiesImpl processorActivityImpl = new ImageProcessingActivitiesImpl(localFolder);
    	
    	// Start executor to poll the common task list
    	executorForCommonTaskList = createExecutor(commonTaskList, storeActivityImpl);
    	executorForCommonTaskList.start();
        System.out.println("Executor Host Service Started for Task List: " + commonTaskList);    	
    	
    	// Start executor to poll the host specific task list
    	executorForHostSpecificTaskList = createExecutor(getHostName(), storeActivityImpl, processorActivityImpl);
    	executorForHostSpecificTaskList.start();
        System.out.println("Executor Host Service Started for Task List: " + getHostName());    	
    	
	}

    private ActivityWorker createExecutor(String taskList, Object ...activityImplementations) throws Exception{        
        ActivityWorker worker = new ActivityWorker(swfService, domain, taskList);
    	for (Object activityImplementation: activityImplementations) {
    	    worker.addActivitiesImplementation(activityImplementation);
    	}
    	
        return worker;
    }

    private void stopExecutors() throws InterruptedException {
        System.out.println("Stopping Executor Services...");
        executorForCommonTaskList.shutdownNow();
        executorForHostSpecificTaskList.shutdownNow();
        swfService.shutdown();
        executorForCommonTaskList.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        System.out.println("Executor Services Stopped...");
    }
    
    
    static ConfigHelper loadConfig() throws IllegalArgumentException, IOException{
       	ConfigHelper configHelper = ConfigHelper.createConfig();

        // Create the client for Simple Workflow Service and S3 Service        
        swfService = configHelper.createSWFClient();
        domain = configHelper.getDomain();
        s3Client = configHelper.createS3Client();
        
        return configHelper;
    }
    
    static void addShutDownHook(){
    	  Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

              public void run() {
                  try {
                      getActivityWorker().stopExecutors();
                  }
                  catch (InterruptedException e) {
                      e.printStackTrace();
                  }
              }
          }));    	
    }
    
    static String getHostName() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostName();
        }
        catch (UnknownHostException e) {
            throw new Error(e);
        }
    }

}
