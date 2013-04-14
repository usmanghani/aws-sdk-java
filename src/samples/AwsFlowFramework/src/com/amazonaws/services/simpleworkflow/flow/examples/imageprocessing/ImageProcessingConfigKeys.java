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

class ImageProcessingConfigKeys {
	public static final String ACTIVITY_WORKER_LOCALFOLDER = "Activity.Worker.LocalFolder";
    public static final String ACTIVITY_WORKER_COMMON_TASKLIST = "Activity.Worker.CommonTaskList";
    public static final String WORKFLOW_WORKER_TASKLIST = "Workflow.Worker.Tasklist";
    
    
    public static final String WORKFLOW_EXECUTION_ID_KEY= "Workflow.Execution.Id";
    public static final String WORKFLOW_INPUT_SOURCEFILENAME_KEY = "Workflow.Input.SourceFileName";
    public static final String WORKFLOW_INPUT_SOURCEBUCKETNAME_KEY = "Workflow.Input.SourceBucketName";
    public static final String WORKFLOW_INPUT_BUCKET_KEY = "Workflow.Input.BucketName";
    public static final String WORKFLOW_INPUT_IMAGEPROCESSINGOPTION_KEY = "Workflow.Input.ImageProcessingOption";
}
