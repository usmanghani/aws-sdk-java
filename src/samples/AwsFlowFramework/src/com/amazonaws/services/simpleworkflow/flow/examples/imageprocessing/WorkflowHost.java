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
import java.util.concurrent.TimeUnit;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.flow.WorkflowWorker;
import com.amazonaws.services.simpleworkflow.flow.examples.common.ConfigHelper;


/**
 * This is the process which hosts all SWF Deciders and Activities specified in this package
 */

public class WorkflowHost {  
    private static AmazonSimpleWorkflow swfService;
    private static String domain;
    private static WorkflowWorker executor;
    private static WorkflowHost host;

    // Factory method for Workflow worker
    public synchronized static WorkflowHost getWorkflowWorker() {
        if (host == null) {
            host = new WorkflowHost();
        }
        return host;
    }

    public static void main(String[] args) throws Exception {
    	ConfigHelper configHelper = loadConfiguration();
    	
        // Start Activity Executor Service
        getWorkflowWorker().startDecisionExecutor(configHelper);

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
        
    private void startDecisionExecutor(ConfigHelper configHelper) throws Exception {
        System.out.println("Starting Executor Host Service...");

        String taskList = configHelper.getValueFromConfig(ImageProcessingConfigKeys.WORKFLOW_WORKER_TASKLIST);
        executor = new WorkflowWorker(swfService, domain, taskList);
        executor.addWorkflowImplementationType(ImageProcessingWorkflowImpl.class);
        
        // Start Executor Service
        executor.start();

        System.out.println("Executor Host Service Started...");
    }

    private void stopHost() throws InterruptedException {
        System.out.println("Stopping Decision Executor Service...");
        executor.shutdownNow();
        swfService.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        System.out.println("Decision Executor Service Stopped...");
    }
    
    static ConfigHelper loadConfiguration() throws IllegalArgumentException, IOException{
        ConfigHelper configHelper = ConfigHelper.createConfig();

        // Create the client for Simple Workflow Service
        swfService = configHelper.createSWFClient();
        domain = configHelper.getDomain();
        configHelper.createS3Client();
        
        return configHelper;
    }
    
    static void addShutDownHook(){
  	  Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            public void run() {
                try {
                    getWorkflowWorker().stopHost();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));    	
  }
}
