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

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.Activity;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;
import com.amazonaws.services.simpleworkflow.flow.annotations.ExponentialRetry;
import com.amazonaws.services.simpleworkflow.flow.common.FlowConstants;

@Activities()
@ActivityRegistrationOptions(
		defaultTaskHeartbeatTimeoutSeconds = FlowConstants.NONE, 
        defaultTaskScheduleToCloseTimeoutSeconds = 300, 
        defaultTaskScheduleToStartTimeoutSeconds = FlowConstants.NONE, 
        defaultTaskStartToCloseTimeoutSeconds = 300)
public interface SimpleStoreActivities {
    /**
     * 
     * @param localName
     *          Name of the file to upload from temporary directory
     * @param remoteName
     *          Name of the file to use on S3 bucket after upload
     * @param fromBox
     *          Machine name which has the file that needs to be uploaded
     * @return
     */
	@Activity(name = "S3Upload", version = "1.0")
    @ExponentialRetry(initialRetryIntervalSeconds = 10,  maximumAttempts = 10) 
    public String upload(String bucketName, String localName, String targetName) throws Exception ;
    /**
     * 
     * @param remoteName 
     *          Name of the file to download from S3 bucket 
     * @param localName
     *          Name of the file used locally after download
     * @param toBox 
     *          This is an output parameter here.  
     *          Used to communicate the name of the box that runs download activity
     */
	@Activity(name = "S3Download", version = "1.0")
    @ExponentialRetry(initialRetryIntervalSeconds = 10, maximumAttempts = 10)
    public String download(String bucketName, String remoteName, String localName) throws Exception;
    /**
     * 
     * @param fileName 
     *          Name of file to delete from temporary folder
     * @param machineName
     *          Machine which has the file locally 
     * @return
     */
	@Activity(name = "DeleteLocalFile", version = "1.0")
    @ExponentialRetry(initialRetryIntervalSeconds=10)
    public void deleteLocalFile(String fileName);

}
