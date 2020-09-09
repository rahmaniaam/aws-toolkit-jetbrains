// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.services.sqs

import software.aws.toolkits.telemetry.SqsQueueType

const val MAX_NUMBER_OF_POLLED_MESSAGES = 10
const val MAX_LENGTH_OF_POLLED_MESSAGES = 1024
const val MAX_LENGTH_OF_FIFO_ID = 128
const val MAX_LENGTH_OF_QUEUE_NAME = 80

// Maximum length of queue name is 80, but the maximum will be 75 for FIFO queues due to '.fifo' suffix
const val MAX_LENGTH_OF_FIFO_QUEUE_NAME = 75

// Extension function to get telemetry type from Queue. It's weird to put it on the Queue (it pollutes
// the API), so make it an extension function
fun Queue.telemetryType() = if (isFifo) SqsQueueType.Fifo else SqsQueueType.Standard
