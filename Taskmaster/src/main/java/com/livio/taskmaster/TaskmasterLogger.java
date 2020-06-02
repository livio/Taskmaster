/*
 * Copyright (c) 2020 Livio, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of the Livio Inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.livio.taskmaster;

@SuppressWarnings("ALL")
class TaskmasterLogger {

    private static ITaskmasterLogger logger;
    private static boolean logsEnabled = false;

    TaskmasterLogger(ITaskmasterLogger logger) {
        if (TaskmasterLogger.logger != null) {
            throw new IllegalStateException("Already initialized with logger");
        } else {
            TaskmasterLogger.logger = logger;
        }
    }

    static void enableLogs(boolean enableLogs) {
        TaskmasterLogger.logsEnabled = enableLogs;
    }

    static void v(String tag, String message) {
        if (logsEnabled && logger != null) {
            logger.v(tag, message);
        }
    }

    static void d(String tag, String message) {
        if (logsEnabled && logger != null) {
            logger.d(tag, message);
        }
    }

    static void i(String tag, String message) {
        if (logsEnabled && logger != null) {
            logger.i(tag, message);
        }
    }

    static void w(String tag, String message) {
        if (logsEnabled && logger != null) {
            logger.w(tag, message);
        }
    }

    static void e(String tag, String message) {
        if (logsEnabled && logger != null) {
            logger.e(tag, message);
        }
    }

    static void e(String tag, String message, Exception e) {
        if (logsEnabled && logger != null) {
            logger.e(tag, message, e);
        }
    }
}
