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

import java.util.UUID;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.flow.examples.common.ConfigHelper;

/**
 * This is used for launching a Workflow instance of FileProcessingWorkflowExample
 */
public class WorkflowExecutionStarter {
    
    private static AmazonSimpleWorkflow swfService;
    private static String domain;
    
    public static void main(String[] args) throws Exception {
    	
    	// Load configuration
    	ConfigHelper configHelper = ConfigHelper.createConfig();
        
        // Create the client for Simple Workflow Service
        swfService = configHelper.createSWFClient();
        domain = configHelper.getDomain();
        
        // Start Workflow instance
        String executionId = configHelper.getValueFromConfig(ImageProcessingConfigKeys.WORKFLOW_EXECUTION_ID_KEY) + UUID.randomUUID();
        String sourceBucketName = configHelper.getValueFromConfig(ImageProcessingConfigKeys.WORKFLOW_INPUT_SOURCEBUCKETNAME_KEY);
        String sourceFilename = configHelper.getValueFromConfig(ImageProcessingConfigKeys.WORKFLOW_INPUT_SOURCEFILENAME_KEY);
        String targetBucketName = configHelper.getValueFromConfig(ImageProcessingConfigKeys.WORKFLOW_INPUT_BUCKET_KEY);
        String imageProcessingOptionString = configHelper.getValueFromConfig(ImageProcessingConfigKeys.WORKFLOW_INPUT_IMAGEPROCESSINGOPTION_KEY);
        
        ImageProcessingOption imageProcessingOption = ImageProcessingOption.valueOf(imageProcessingOptionString);
        
        ImageProcessingWorkflowClientExternalFactory clientFactory = new ImageProcessingWorkflowClientExternalFactoryImpl(swfService, domain);
        ImageProcessingWorkflowClientExternal workflow = clientFactory.getClient(executionId);
        workflow.processImage(sourceBucketName, sourceFilename, targetBucketName, imageProcessingOption);
        System.exit(0);
    }    
}
