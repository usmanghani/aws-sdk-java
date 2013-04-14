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
import java.io.IOException;

import com.amazonaws.services.simpleworkflow.flow.ActivitySchedulingOptions;
import com.amazonaws.services.simpleworkflow.flow.DecisionContextProviderImpl;
import com.amazonaws.services.simpleworkflow.flow.WorkflowContext;
import com.amazonaws.services.simpleworkflow.flow.annotations.Asynchronous;
import com.amazonaws.services.simpleworkflow.flow.core.Promise;
import com.amazonaws.services.simpleworkflow.flow.core.Settable;
import com.amazonaws.services.simpleworkflow.flow.core.TryFinally;

/**
 * This implementation of FileProcessingWorkflow downloads the file, zips it and uploads it back to S3 
 */
public class ImageProcessingWorkflowImpl implements ImageProcessingWorkflow {
	private final SimpleStoreActivitiesClient store;
	private final ImageProcessingActivitiesClient processor;
	private final WorkflowContext workflowContext;
	
	public ImageProcessingWorkflowImpl(){
		// Create activity clients
		this.store = new SimpleStoreActivitiesClientImpl();
		processor = new ImageProcessingActivitiesClientImpl(); 
		workflowContext = (new DecisionContextProviderImpl()).getDecisionContext().getWorkflowContext();
	}
	
    @Override
    public void processImage(final String sourceBucketName, final String sourceFilename, final String bucketName, final ImageProcessingOption option) throws IOException {    	
    	// Settable to store the worker specific task list returned by the activity
    	final Settable<String> taskList = new Settable<String>();
    	
    	// replace all "/" in runId, in case system will the path as sub-folder path.
    	String workflowRunId = workflowContext.getWorkflowExecution().getRunId().replace("/", "_");
    	File localSource = new File(sourceFilename);
        final String localSourceFilename = workflowRunId + "_" + localSource.getName(); 
        File localTarget = new File(sourceFilename.split("[.]")[0] + ".png");
        final String localTargetFilename = workflowRunId + "_" + localTarget.getName(); 
        final String remoteFilename = "converted_" + localTargetFilename; 
    	
    	new TryFinally() {
            @Override
            protected void doTry() throws Throwable {
            	// Call download activity to download the file
            	Promise<String> activityWorkerTaskList = store.download(sourceBucketName, sourceFilename, localSourceFilename);
            	// Chain the promise to the settable
            	taskList.chain(activityWorkerTaskList);
                // Call processFile activity to zip tthe file
            	Promise<Void> fileProcessed = processFileOnHost(option, localSourceFilename, localTargetFilename, activityWorkerTaskList);                
                // Call upload activity to upload zipped file
            	if (bucketName != null && bucketName.length() > 0) {
            		upload(bucketName, localTargetFilename, remoteFilename, taskList, fileProcessed);
            	}
            }

            @Override
            protected void doFinally() throws Throwable {
                if (taskList.isReady()) { // File was downloaded
                	                	
                	// Set option to schedule activity in worker specific task list
                	ActivitySchedulingOptions options = new ActivitySchedulingOptions().withTaskList(taskList.get());
                	
                	// Call deleteLocalFile activity using the host sepcific task list
                    store.deleteLocalFile(localSourceFilename, options);
                    store.deleteLocalFile(localTargetFilename, options);
                }
            }
        };
    }
    
    @Asynchronous
    private Promise<Void> processFileOnHost(ImageProcessingOption option, String fileToProcess, 
    		String localTargetFilename, Promise<String> tasklist) {    	
    	// Call the activity to process the file using worker specific task list
    	ActivitySchedulingOptions options = new ActivitySchedulingOptions().withTaskList(tasklist.get());
    	
    	switch(option) {
        case GRAY_SCALE:
            return processor.convertToGrayscale(Promise.asPromise(fileToProcess), Promise.asPromise(localTargetFilename), 
                options);
        case SEPIA:
            return processor.convertToSepia(Promise.asPromise(fileToProcess), Promise.asPromise(localTargetFilename), 
                options);
        }
    	
    	throw new IllegalArgumentException("Unknown image processing option specified.");
    }
    
    @Asynchronous
    private void upload(final String targetBucketName, final String localTargetFilename, final String targetFilename, 
            Promise<String> taskList, Promise<Void> fileProcessed) {
        ActivitySchedulingOptions options = new ActivitySchedulingOptions().withTaskList(taskList.get());
        store.upload(targetBucketName, localTargetFilename, targetFilename, options);
    }
    
}
