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

import java.io.IOException;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.Activity;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;
import com.amazonaws.services.simpleworkflow.flow.annotations.ExponentialRetry;
import com.amazonaws.services.simpleworkflow.flow.common.FlowConstants;


/**
 * Contract for file processing activities
 */
@Activities
@ActivityRegistrationOptions(
		defaultTaskHeartbeatTimeoutSeconds = FlowConstants.NONE, 
        defaultTaskScheduleToCloseTimeoutSeconds = 300, 
        defaultTaskScheduleToStartTimeoutSeconds = FlowConstants.NONE, 
        defaultTaskStartToCloseTimeoutSeconds = 300)
public interface ImageProcessingActivities {
	
	@Activity(name = "GrayscaleTransform", version = "1.0")
	@ExponentialRetry(
			initialRetryIntervalSeconds=10,
			backoffCoefficient=1,
			maximumAttempts=5)
	public void convertToGrayscale(String inputFileName, String outputFileName) throws IOException;	
	
	@Activity(name = "SepiaTransform", version = "1.0")
	@ExponentialRetry(
			initialRetryIntervalSeconds=10,
			backoffCoefficient=1,
			maximumAttempts=5)
	public void convertToSepia(String inputFileName, String outputFileName) throws IOException;
}
