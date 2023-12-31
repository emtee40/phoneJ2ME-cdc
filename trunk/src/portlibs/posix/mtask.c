/*
 * @(#)mtask.c	1.37 06/10/10
 *
 * Copyright  1990-2006 Sun Microsystems, Inc. All Rights Reserved.  
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER  
 *   
 * This program is free software; you can redistribute it and/or  
 * modify it under the terms of the GNU General Public License version  
 * 2 only, as published by the Free Software Foundation.   
 *   
 * This program is distributed in the hope that it will be useful, but  
 * WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU  
 * General Public License version 2 for more details (a copy is  
 * included at /legal/license.txt).   
 *   
 * You should have received a copy of the GNU General Public License  
 * version 2 along with this work; if not, write to the Free Software  
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  
 * 02110-1301 USA   
 *   
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa  
 * Clara, CA 95054 or visit www.sun.com if you need additional  
 * information or have any questions. 
 *
 */

#include <stdio.h>
#include <stddef.h>
#include <stdlib.h>
#include <assert.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <signal.h>
#include <unistd.h>
#include <sys/time.h>
#include <sys/resource.h>
#include <sys/wait.h>

#include "javavm/export/jni.h"
#include "javavm/export/cvm.h"
#include "portlibs/ansi_c/java.h"
#include "portlibs/posix/mtask.h"

#include <jump_messaging.h>
#include <dirent.h>
#include <sys/ipc.h>
#include <sys/msg.h>
#include "porting/JUMPMessageQueue.h"
#include "porting/JUMPProcess.h"

#include "zip_util.h"

/*
 * Free (argc, argv[]) set
 */
static void
freeArgs(int argc, char** argv)
{
    int i;
    for (i = 0; i < argc; i++) {
	if (argv[i] != NULL) {
	    free(argv[i]);
	    argv[i] = NULL;
	}
    }
    free(argv);
}

static void
setupRequest(JNIEnv*env, int argc, char** argv, CVMInt32 clientId)
{
    char* appclasspathStr = NULL;
    char* bootclasspathStr = NULL;
    JavaVMInitArgs* clientArgs;
    char* parse;
    int cpadd;

    clientArgs = ANSIargvToJinitArgs(argc - 1, argv + 1, 
				     &appclasspathStr,
				     &bootclasspathStr);

    /* Now take this set of init args, and parse them */
    parse = CVMparseCommandLineOptions(env, clientArgs, clientArgs->nOptions);

    if (parse != NULL) {
	/* If the parse failed for some reason, the parser must have already
	   printed out the necessary message on the console. Exit with an
	   error code. */
	fprintf(stderr, "Exiting JVM\n");
	exit(1);
    }

    /* The JVM state now reflects the arguments. Also amend the classpath
       setting with the value that we read from appclasspathStr and
       bootclasspathStr */
    cpadd = CVMclassClassPathAppend(env, appclasspathStr, bootclasspathStr);
    if (!cpadd) {
	/* If the class path add failed for some reason, the system must have
	   already printed out the necessary message on the console. Exit with
	   an error code. */
	fprintf(stderr, "Exiting JVM\n");
	exit(1);
    }

    /* Initialize child VM */
    CVMmtaskReinitializeChildVM(env, clientId);

    /* Free up the (argc,arv[]) and jInitArgs that we have built */
    ANSIfreeJinitArgs(clientArgs);
    freeArgs(argc, argv);

    /* Now we are ready to execute code */
#ifdef CVM_TIMESTAMPING
    if (clientId != 0) {
	CVMmtaskTimeStampRecord(env, "Child Created", -1);
    }
#endif

}

static void child(int sig);

/*
 * Task management 
 */
typedef struct _TaskRec {
    int pid;
    char* command;

    /* testing mode specific snapshot of prefix */
    char* testingModeFilePrefixSnapshot;

    struct _TaskRec* next;
    /* Any other task info? */
} TaskRec;

/* Single process for the mtask server, so these globals are OK */
static int numTasks = 0;
static TaskRec* taskList = NULL;
static int reapChildrenFlag = 0;

static int executivePid = -1;
static int amExecutive = 0;
static char* executiveTestingModeFilePrefixSnapshot = NULL;

/*
 * Handle exiting children so they don't hang around as zombies.
 */
static void child(int sig)
{
    int pid = getpid();

    fprintf(stderr, "Received SIGCHLD in PID=%d\n", pid);
    reapChildrenFlag = 1;
}

static void 
dumpTaskOneWithFp(TaskRec* task, FILE *fp, int verbose)
{
    if (verbose) {
	fprintf(fp, 
		 "[Task pid=%d, command=\"%s\"(0x%x)]\n",
		 task->pid, task->command, 
		 (unsigned int)task->command);
    } else {
	fprintf(fp, "PID=%d COMMAND=\"%s\"\n", task->pid, task->command);
    }
}

static void 
dumpTaskIntoMessage(JUMPOutgoingMessage m, TaskRec* task)
{
    char str[256];
    
    snprintf(str, 256, "PID=%d COMMAND=\"%s\"", task->pid, task->command);
    jumpMessageAddString(m, str);
}

static void
dumpTaskOne(TaskRec* task)
{
    // Verbose printout on stderr
    dumpTaskOneWithFp(task, stderr, 1);
}

static void
freeTask(TaskRec* task)
{
    fprintf(stderr, "FREEING TASK: ");
    dumpTaskOne(task);
    
    free(task->command);
    if (task->testingModeFilePrefixSnapshot != NULL) {
	free(task->testingModeFilePrefixSnapshot);
    }
    free(task);
    numTasks--;
}

/*
 * Find TaskRec corresponding to given pid
 */
static TaskRec* 
findTaskFromPid(int taskPid)
{
    TaskRec* task;

    for (task = taskList; task != NULL; task = task->next) {
	if (task->pid == taskPid) {
	    return task;
	}
    }
    return NULL;
}

/*
 * removeTask with a given pid
 *
 * Called after the SIGCHLD signal handler signaled that there are
 * children exiting
 */
static void
removeTask(int taskPid)
{
    TaskRec* task;
    TaskRec* taskPrev = NULL;

    for (task = taskList; task != NULL; taskPrev = task, task = task->next) {
	if (task->pid == taskPid) {
	    if (taskPrev == NULL) {
		/* The first item in the list */
		taskList = task->next;
	    } else {
		taskPrev->next = task->next;
	    }
	    freeTask(task);
	    return;
	}
    }
}

/*
 * Create log file off of 'prefix', named 'kind' for 'pid'
 * Return fd
 */
static int
createLogFile(char* prefix, char* kind, int pid)
{
    /* We want to open files for stdout, stderr, and exit
       codes. */
    int prefixLen = strlen(prefix) + 1;
    const int maxPidLen = 6;
    const int kindLength = strlen(kind) + 1;
    const int maxFileLen = 
	prefixLen +
	maxPidLen +
	kindLength;
    char* fileName = calloc(1, maxFileLen);
    int fd;

    if (fileName == NULL) {
	fprintf(stderr, "Out of memory trying to open log file %s\n",
		fileName);
	return -1;
    }
    sprintf(fileName, "%s/%s.%d", prefix, kind, pid);

    /*fprintf(stderr, "%s fileName = %s\n", kind, fileName);*/
    fd = open(fileName, O_WRONLY | O_CREAT | O_TRUNC, 0666);
    if (fd == -1) {
	perror(fileName);
    }
    return fd;
}

static FILE*
makeLineBufferedStream(int socket)
{
    FILE* stream;
    stream = fdopen(socket, "w");
    if (stream != NULL) {
	setvbuf(stream, NULL, _IOLBF, BUFSIZ);
    }
    return stream;
}
    
static void
printExitStatusString(FILE* out, int status)
{
    if (WIFEXITED(status)) {
	fprintf(out, "<exit code %d>\n", WEXITSTATUS(status));
    } else if (WIFSIGNALED(status)) {
	fprintf(out, "<uncaught signal %d>\n", WTERMSIG(status));
    } else if (WIFSTOPPED(status)) {
	fprintf(out, "<stopped with signal %d>\n", WSTOPSIG(status));
    } 
}

static void
cleanMessageQueuesOf(int cpid)
{
    struct dirent* ptr;
    DIR* dir;
    char prefix[40];
    char filename[60];
    int prefixLen;
    
    snprintf(prefix, 40, "jump-mq-%d-", cpid);
    prefixLen = strlen(prefix);
    
    dir = opendir("/tmp");
    if (dir == NULL) {
	perror("opendir");
	return;
    }
    
    while ((ptr = readdir(dir)) != NULL) {
  	if (!strcmp(ptr->d_name, ".") || !strcmp(ptr->d_name, "..")) {
	    continue;
	}
	if (!strncmp(ptr->d_name, prefix, prefixLen)) {
	    key_t k;
	    int mq;
	    
	    snprintf(filename, 60, "/tmp/%s", ptr->d_name);
	    printf("Exiting process %d has message queue %s\n",
		   cpid, filename);
	    if ((k = ftok(filename, 'Q')) == -1) {
		perror("ftok");
		continue;
	    } else {
		printf("  key=%d\n", k);
	    }
	    if ((mq = msgget(k, 0)) == -1) {
		perror("msgget");
		continue;
	    } else {
		printf("  mq=%d\n", mq);
	    }
	    if (msgctl(mq, IPC_RMID, NULL) == -1) {
		perror("mq remove");
		continue;
	    } else {
		printf("  removed mq %d\n", mq);
	    }
	    
	    if (remove(filename) == -1) {
		perror("mq file delete");
		continue;
	    } else {
		printf("  removed file %s\n", filename);
	    }
	}
    }
    closedir(dir);
}

/*
 * The signal handler indicated there are children to reap. Do it.
 * Return last status reaped.
 */
static int
doReapChildren(ServerState* state, int options)
{
    int cpid, status;
    struct rusage ru;

    /* Reset the flag early. If the signal handler re-raises this, we will
       discover this in the next iteration of reapChildren() */
    reapChildrenFlag = 0;
    while ((cpid = wait3(&status, options, &ru)) > 0) {
	int exitcodefd;

	/* This had better be one of ours */
	assert((executivePid == cpid) || (findTaskFromPid(cpid) != NULL));

	fprintf(stderr, "Reaping child process %d\n", cpid);

	/* TODO: Should this be part of the porting layer? 
	   Or is the fact that we are using the mtask.c code indicative
	   of Linux already? */
	cleanMessageQueuesOf(cpid);
	
	printExitStatusString(stderr, status);
	fprintf(stderr, "\t user time %ld.%02d sec\n",
		ru.ru_utime.tv_sec,
		(int)(ru.ru_utime.tv_usec / 1000 / 10));
	fprintf(stderr, "\t sys time %ld.%02d sec\n",
		ru.ru_stime.tv_sec,
		(int)(ru.ru_stime.tv_usec / 1000 / 10));
	fprintf(stderr, "\t max res set %ld\n",
		ru.ru_maxrss);
#if 0
	fprintf(stderr, "\t integral shared mem size %ld\n",
		ru.ru_ixrss);
	fprintf(stderr, "\t integral unshared data size %ld\n",
		ru.ru_idrss);
	fprintf(stderr, "\t integral unshared stack size %ld\n",
		ru.ru_isrss);
#endif
	fprintf(stderr, "\t page reclaims (minor faults) %ld\n",
		ru.ru_minflt);
	fprintf(stderr, "\t page faults (major) %ld\n",
		ru.ru_majflt);
	fprintf(stderr, "\t swaps %ld\n",
		ru.ru_nswap);
#if 0
	fprintf(stderr, "\t block input operations %ld\n",
		ru.ru_inblock);
	fprintf(stderr, "\t block output operations %ld\n",
		ru.ru_oublock);
#endif
	if (state->isTestingMode) {
	    TaskRec* task = findTaskFromPid(cpid);
	    char* testingModeFilePrefixSnapshot;
	    
	    /* Now make a record, but only if TESTING_MODE was executed
	       _prior_ to launching this currently reaped task. We can't
	       apply current testing mode to an app that did not know
	       about it */
	    if (executivePid == cpid) {
		testingModeFilePrefixSnapshot = 
		    executiveTestingModeFilePrefixSnapshot;
	    } else {
		testingModeFilePrefixSnapshot = 
		    task->testingModeFilePrefixSnapshot;
	    }
	    if (testingModeFilePrefixSnapshot != NULL) {
		/* Use the snapshot of the file prefix created at launch */
		exitcodefd = createLogFile(testingModeFilePrefixSnapshot,
					   "exitcode", cpid);
		if (exitcodefd == -1) {
		    perror("exitcodefile");
		} else {
		    FILE* exitcodefile = makeLineBufferedStream(exitcodefd);
		    printExitStatusString(exitcodefile, status);
		    fclose(exitcodefile);
		    close(exitcodefd);
		}
	    }
	}
	if (executivePid == cpid) {
	    /* Get rid of all traces */
	    executivePid = -1;
	    if (executiveTestingModeFilePrefixSnapshot != NULL) {
		free(executiveTestingModeFilePrefixSnapshot);
		executiveTestingModeFilePrefixSnapshot = NULL;
	    }
	} else {
	    removeTask(cpid);
	}
    }
    return status;
}

static void
reapChildren(ServerState* state)
{
    doReapChildren(state, WNOHANG);
}

/*
 * A new task has been created 
 */
static int
addTask(JNIEnv* env, ServerState* state, int taskPid, char* command)
{
    TaskRec* task;

    task = (TaskRec*)calloc(1, sizeof(TaskRec));
    if (task == NULL) {
	return JNI_FALSE;
    }
    task->pid = taskPid;
    task->command = command;
    /* Take a snapshot of the testing mode prefix here */
    if (state->isTestingMode) {
	task->testingModeFilePrefixSnapshot =
	    strdup(state->testingModeFilePrefix);
	if (task->testingModeFilePrefixSnapshot == NULL) {
	    fprintf(stderr, "Could not allocate file prefix snapshot\n");
	    free(task->command);
	    free(task);
	    return JNI_FALSE;
	}
    } else {
	task->testingModeFilePrefixSnapshot = NULL;
    }
#if 0
    fprintf(stderr, "Added task=0x%x: [pid=%d, \"%s\"(0x%x)]\n",
	    task, taskPid, task->command, (unsigned int)task->command);
#endif
    numTasks++;
    /* Insert at the head */
    task->next = taskList;
    taskList = task;
    return JNI_TRUE;
}

static CVMBool
killTaskFromTaskRec(TaskRec* task)
{
#if 0
    /*
     * Message based exit
     * Not so robust, so commenting out for now.
     */
    return sendMessageToTask(task, "EXIT");
#else
    if (kill(task->pid, SIGKILL) == -1) {
	perror("kill");
	return CVM_FALSE;
    }
    /* Killing is "best effort" and needs to be verified via
       Java, preferably using Xlet.getState() api's */
    return CVM_TRUE;
#endif
}

static CVMBool
killTask(int taskPid)
{
    TaskRec* task;
    task = findTaskFromPid(taskPid);
    if (task == NULL) {
	return CVM_FALSE;
    }
    return killTaskFromTaskRec(task);
}

static CVMBool
killAllTasks()
{
    TaskRec* task;
    CVMBool result = CVM_TRUE;
    
    for (task = taskList; task != NULL; task = task->next) {
	if (!killTaskFromTaskRec(task)) {
	    result = CVM_FALSE;
	}
    }
    return result;
}

static void
closeAllFdsExcept(int fd)
{
    /* nothing to do */
}

static int
numberOfTasks()
{
    int num = 0;
    TaskRec* task;
    
    for (task = taskList; task != NULL; task = task->next) {
	num++;
    }
    return num;
    
}

/*
 * multi-string response to caller, packaged as
 * return message.
 */
static void
dumpTasksAsResponse(JUMPMessage command)
{
    JUMPOutgoingMessage m;
    JUMPMessageStatusCode code;
    
    TaskRec* task;
    int numTasks;
    
    m = jumpMessageNewOutgoingByRequest(command);

    numTasks = numberOfTasks();

    jumpMessageAddInt(m, numTasks);
    
    for (task = taskList; task != NULL; task = task->next) {
	dumpTaskIntoMessage(m, task);   /* Brief printout to caller */
	dumpTaskOne(task);                    /* Verbose printout to console */
    }

    jumpMessageSendAsyncResponse(m, &code);
}

/* 
 * pthread's will not survive across a fork. Therefore, allow stopping
 * and re-starting of system threads for sanity across app launching.
 */
static void
stopSystemThreads(ServerState* state)
{
    jclass referenceClass;
    jmethodID stopMethod;
    JNIEnv *env = state->env;
    
    referenceClass = state->referenceClass;
    stopMethod = state->stopSystemThreadsID;
    
    /*
     * Call sun.misc.ThreadRegistry.waitAllSystemThreadsExit()
     * to shutdown system threads
     */
    (*env)->CallStaticVoidMethod(env, referenceClass, stopMethod);
    
    if ((*env)->ExceptionOccurred(env)) {
	/*
	 * Ignore and clear the exception
	 */
	CVMconsolePrintf("Exception occurred during "
			 "ThreadRegistry.waitAllSystemThreadsExit()\n");
	(*env)->ExceptionDescribe(env);
	return;
    }
    /* Shut down the fd's of any zip files we've cached. We can't fork
       with open fd's. */
    ZIP_Closefds();

#if defined(CVM_HAVE_DEPRECATED) || defined(CVM_THREAD_SUSPENSION)
    CVMmtaskHandleSuspendChecker();
#endif
}

static jboolean
restartSystemThreads(JNIEnv* env, ServerState* state)
{
    jclass referenceClass;
    jmethodID restartMethod;
    
    /* Ready to execute code, so re-open cached zip fd's */
    if (!ZIP_Reopenfds()) {
	return JNI_FALSE;
    }

    referenceClass = state->referenceClass;
    restartMethod = state->restartSystemThreadsID;
    
    /*
     * Call sun.misc.ThreadRegistry.waitAllSystemThreadsExit()
     * to shutdown system threads
     */
    (*env)->CallStaticVoidMethod(env, referenceClass, restartMethod);
    
    if ((*env)->ExceptionOccurred(env)) {
	/*
	 * Ignore and clear the exception
	 */
	CVMconsolePrintf("Exception occurred during "
			 "java.lang.ref.Reference.restartSystemThreads()\n");
	(*env)->ExceptionDescribe(env);
	return JNI_FALSE;
    }
    return JNI_TRUE;
}

static void
dumpMessage(JUMPMessage m, char* intro)
{
    JUMPMessageReader r;
    JUMPPlatformCString* strings;
    uint32 len, i;
    
    jumpMessageReaderInit(&r, m);
    strings = jumpMessageGetStringArray(&r, &len);
    printf("%s\n", intro);
    for (i = 0; i < len; i++) {
	printf("    \"%s\"\n", strings[i]);
    }
}

static char*
oneString(JUMPMessage m)
{
    char* string;
    JUMPMessageReader r;
    JUMPPlatformCString* strings;
    uint32 len, i;
    
    string = (char*)calloc(1024, 1);
    jumpMessageReaderInit(&r, m);
    strings = jumpMessageGetStringArray(&r, &len);
    for (i = 0; i < len; i++) {
	strcat(string, strings[i]);
	strcat(string, " ");
    }
    return string;
}

static JUMPMessage
readRequestMessage()
{
    JUMPMessage in;
    
    in = jumpMessageWaitFor("mvm/server", 0);
    dumpMessage(in, "Server received:");
    return in;
}

/*
 * Uniform response message from server to caller.
 * Always a string array for now
 */
static void
respondWith(JUMPMessage command, char* str)
{
    JUMPOutgoingMessage m;
    JUMPMessageStatusCode code;
    JUMPPlatformCString strs[1];
    
    m = jumpMessageNewOutgoingByRequest(command);
    strs[0] = (JUMPPlatformCString)str;
    jumpMessageAddStringArray(m, strs, 1);
    jumpMessageSendAsyncResponse(m, &code);
}

static void
respondWith2(JUMPMessage command, char* format, int v)
{
    char str[256];
    
    snprintf(str, 256, format, v);
    respondWith(command, str);
}

static void
respondWith2s(JUMPMessage command, char* format, char* arg)
{
    char str[256];
    
    snprintf(str, 256, format, arg);
    respondWith(command, str);
}

static void
tokenizeArgs(JUMPMessage m, int* argc, char*** argv)
{
    JUMPMessageReader r;
    JUMPPlatformCString* strings;
    uint32 len;
    
    jumpMessageReaderInit(&r, m);
    strings = jumpMessageGetStringArray(&r, &len);
    *argc = len;
    *argv = (char**)strings;
}

/*
 * A JVM server. Sleep waiting for new requests. As new ones come in,
 * fork off a process to handle each and go back to sleep. 
 *
 * return JNI_FALSE if a fork'ed child executes a request.
 * return JNI_TRUE if the parent sources the command
 */
static jboolean
waitForNextRequest(JNIEnv* env, ServerState* state)
{
    JUMPMessage command = NULL;
    CVMBool done;
    int   childrenExited = 0; /* No one has exited yet */
    
    done = CVM_FALSE;
    /* 
     * Set up the networking for the server
     * It might have been initialized already, in which case don't
     * touch the fd that was passed in.
     */
    assert(state->initialized);
    
    while (!done) {
	int argc;
	char** argv;
	int pid;
	
	/* Accepted a connection. Now accept commands from this 
	   connection */
	command = readRequestMessage();
	while (command != NULL) {
	    tokenizeArgs(command, &argc, &argv);
	    /*
	     * Check for children to reap before each command
	     * is executed 
	     */
	    while (reapChildrenFlag) {
		/* Remember we have detected dead children */
		childrenExited = 1; 
		reapChildren(state);
	    }
    
	    if (!strcmp(argv[0], "JEXIT")) {
		respondWith(command, "YES");
		jumpMessageShutdown();
		/* Simply exit here. That's what JEXIT is supposed to do. */
		exit(0);
	    } else if (!strcmp(argv[0], "SETENV")) {
		/* set environment variable */
		if (argc != 2) {
#define SETENV_USAGE   "Usage: SETENV <keyValuePair>"
#define SETENV_SUCCESS "SETENV succeeded"
		    respondWith(command, SETENV_USAGE);
		} else {
		    char* pair;
		    pair = strdup(argv[1]);
		    
		    if (pair == NULL) {
			/* setenv failed */
			respondWith(command, SETENV_USAGE);
		    } else {
			char* name = pair;
			char* value = strchr(pair, '=');
			if (value == NULL) {
			    /* setenv failed */
			    respondWith(command, SETENV_USAGE);
			} else {
			    *value = '\0';
			    value++;
			    fprintf(stderr, "setenv(%s, %s)\n", name, value);
			    if (*value == '\0') {
				/* unsetenv */
#ifdef __linux__
				unsetenv(name);
#else
				value[-1] = '=';
				putenv(name);
#endif
				/* setenv succeeded */
				respondWith(command, SETENV_SUCCESS);
			    } else {
				/* setenv */
#ifdef __linux__
				if (setenv(name, value, 1) == 0) {
#else
				value[-1] = '=';
				if (putenv(name) == 0) {
#endif
				    /* setenv succeeded */
				    respondWith(command, SETENV_SUCCESS);
				} else {
				    /* setenv failed */
				    respondWith(command, SETENV_USAGE);
				}
			    }
			}
		    }
		}
		/* The man page does not say whether setenv
		   makes a copy of the arguments. So I don't know
		   whether I can free the strdup'ed argv[1].
		   Be conservative and retain it here. */
		    freeArgs(argc, argv);
		/* Make sure */
		argc = 0;
		argv = NULL;
		free(command);
		command = readRequestMessage();
		continue;
	    } else if (!strcmp(argv[0], "LIST")) {
		/* Don't forget to free up all the (argc, argv[]) mem. */
		freeArgs(argc, argv);
		/* Make sure */
		argc = 0;
		argv = NULL;
		dumpTasksAsResponse(command);
		jumpMessageFree(command);
		command = readRequestMessage();
		continue;
	    } else if (!strcmp(argv[0], "CHILDREN_EXITED")) {
		/* Don't forget to free up all the (argc, argv[]) mem. */
		freeArgs(argc, argv);
		/* Make sure */
		argc = 0;
		argv = NULL;
		if (childrenExited) {
		    childrenExited = 0;
		    respondWith(command, "YES");
		} else {
		    respondWith(command, "NO");
		}
		jumpMessageFree(command);
		command = readRequestMessage();
		continue;
	    } else if (!strcmp(argv[0], "KILLALL") ||
		       !strcmp(argv[0], "KILLEMALL")) {
#define KILLALL_SUCCESS "KILL succeeded"
		killAllTasks();
		/* Kill succeeded. Say so */
		respondWith(command, KILLALL_SUCCESS);
		/* Don't forget to free up all the (argc, argv[]) mem. */
		freeArgs(argc, argv);
		/* Make sure */
		argc = 0;
		argv = NULL;
		jumpMessageFree(command);
		command = readRequestMessage();
		continue;
	    } else if (!strcmp(argv[0], "KILL")) {
		/* KILL a given task */
		if (argc != 2) {
#define KILL_USAGE   "Usage: KILL <taskId>"
#define KILL_SUCCESS "KILL succeeded"
		    respondWith(command, KILL_USAGE);
		} else {
		    char* pidStr = argv[1];
		    /* Try to convert to integer */
		    CVMInt32 pid = CVMoptionToInt32(pidStr);
		    
		    if (pid == -1) {
			/* Bad integer */
			respondWith(command, KILL_USAGE);
		    } else if (!killTask(pid)) {
			/* Kill failed for some reason */
			/* Just make a best effort here */
			respondWith(command, KILL_USAGE);
		    } else {
			/* Kill succeeded. Say so */
			respondWith(command, KILL_SUCCESS);
		    }
		}
		
		freeArgs(argc, argv);
		/* Make sure */
		argc = 0;
		argv = NULL;
		jumpMessageFree(command);
		command = readRequestMessage();
		continue;
	    } else if (!strcmp(argv[0], "TESTING_MODE")) {
		/* Set up testing mode, with a file prefix */
		if (argc != 2) {
#define TESTING_MODE_USAGE   "Usage: TESTING_MODE <prefix>"
#define TESTING_MODE_SUCCESS "TESTING_MODE succeeded"
		    respondWith(command, TESTING_MODE_USAGE);
		} else {
		    char* prefix = argv[1];
		    
		    state->isTestingMode = JNI_TRUE;
		    state->testingModeFilePrefix = strdup(prefix);
		    if (state->testingModeFilePrefix == NULL) {
			respondWith(command, TESTING_MODE_USAGE);
		    } else {
			respondWith(command, TESTING_MODE_SUCCESS);
		    }
		}
		
		freeArgs(argc, argv);
		/* Make sure */
		argc = 0;
		argv = NULL;
		jumpMessageFree(command);
		command = readRequestMessage();
		continue;
	    } else if (!strcmp(argv[0], "S")) {
		/* Always return a response to the connection. */
		dumpMessage(command, "SOURCING:");
		jumpMessageFree(command);
		command = NULL;
		/* In the parent process, setup request and return to caller */
		setupRequest(env, argc, argv, 0);
		/* Make sure */
		argc = 0;
		argv = NULL;
		/* Who would free up argc and argv? */
		return JNI_TRUE;
	    } else if (argv[0][0] != 'J') { /* Not equal to "J*" */
		/* If the command is not J, then it is not recognized. */
		/* Read next line and re-process. */
		respondWith2s(command, "Illegal command \"%s\"", argv[0]);
		/* Don't forget to free up all the (argc, argv[]) mem. */
		freeArgs(argc, argv);
		/* Make sure */
		argc = 0;
		argv = NULL;
		jumpMessageFree(command);
		command = readRequestMessage();
		continue;
	    }
	    {
		char* str = oneString(command);
		
		fprintf(stderr, "Executing new command: \"%s\"\n", str);
		free(str);
	    }
	    
	    if (!strcmp(argv[0], "JDETACH")) {
		amExecutive = CVM_TRUE;
	    }
#if 0
	    /* How to do sync? */
	    if (!strcmp(argv[0], "JSYNC")) {
		isSync = CVM_TRUE;
	    }
#endif

#if 1
	    {
		int j;
		for(j = 0; j < argc; j++) {
		    fprintf(stderr, "\tARGV[%d] = \"%s\"\n", j, argv[j]);
		}
	    }
#endif

	    /* Fork off a process, and handle the request */
	    if ((pid = fork()) == 0) {
		int mypid = getpid();

		/* Make sure each launched process knows the id of the
		   executive */
                if (amExecutive) {
                    jumpProcessSetExecutiveId(mypid);
                } else {
                    jumpProcessSetExecutiveId(executivePid);
                }
		
		/* Child process */
#ifdef CVM_TIMESTAMPING
		if (!CVMmtaskTimeStampReinitialize(env)) {
		    fprintf(stderr, 
			    "Could not reinitialize timestamping, exiting\n");
		    exit(1);
		}
#endif		
		/* Make sure all the fd's we inherit from the parent
		   get freed up */
		closeAllFdsExcept(-1);
		
		if (state->isTestingMode) {
		    char* prefix = state->testingModeFilePrefix;
		    /* We want to open files for stdout and stderr */
		    int outfd = createLogFile(prefix, "stdout", mypid);
		    int errfd = createLogFile(prefix, "stderr", mypid);
		    if ((outfd == -1) || (errfd == -1)) {
			/* Due to some error that was reported in
			   createLogFile() */
			fprintf(stderr, "MTASK: Could not set debug mode\n");
		    } else {
			/* Hook up stdout and stderr in the child process
			   to the right files */
			dup2(outfd, 1);
			dup2(errfd, 2);
			close(outfd);
			close(errfd);
		    }
		} 
		
#if 0
		/* how to do sync? */
                else if (isSync) {
		    /* If we are in JSYNC execution, route stdout
		       and stderr back where the request came from */
		    dup2(connfd, 1);
		    dup2(connfd, 2);
		}
#endif
		
		/* First, make sure that the child PID is communicated
		   to the client connection. */
		respondWith2(command, "CHILD PID=%d", mypid);
		
		/* No need for the connections in the child */
		jumpMessageFree(command);
		
		/* We need these threads in the child */
		if (!restartSystemThreads(env, state)) {
		    exit(1);
		}
		
		/* In the child process, setup request and return to caller */
		setupRequest(env, argc, argv, mypid);
		/* Make sure */
		argc = 0;
		argv = NULL;
		return JNI_FALSE;
	    } else if (pid == -1) {
		amExecutive = CVM_FALSE;
		perror("Fork");
		freeArgs(argc, argv);
		jumpMessageFree(command);
	    } else {
		amExecutive = CVM_FALSE;
		fprintf(stderr, "SPAWNED OFF PID=%d\n", pid);
		/* Add this new task into our "database" unless
		   launching via JDETACH */
		if (!strcmp(argv[0], "JDETACH")) {
		    /* Remember this as a special pid. */
		    executivePid = pid;
		    if (state->isTestingMode) {
			executiveTestingModeFilePrefixSnapshot =
			    strdup(state->testingModeFilePrefix);
		    }
		} else {
		    addTask(env, state, pid, oneString(command));
		}
		jumpMessageFree(command);
		/* The child is executing this command. The parent
		   can free the arguments */
		/* Don't forget to free up all the (argc, argv[]) mem. */
		freeArgs(argc, argv);
		/* Don't free 'command' here, since it's been put in
		   the task structure. We'll free it when we free the
		   task */
#if 0
		/* Is there a good way to do sync? */
		if (isSync) {
		    /* Wait for child to exit */
		    int status = doReapChildren(state, 0);
		    printExitStatusString(connfp, status);
		    isSync = CVM_FALSE;
		    /* This being a sync call, blow the caller off. */
		    command = NULL;
		    continue;
		}
#endif
	    }
	    command = readRequestMessage();
	}
	
	/* fprintf(stderr, "Quitting this connection, waiting for new one\n"); */
	
    }
    jumpMessageShutdown();
    
    /* Parent exits? Should sleep until the last child exits */
    exit(0);
}

int
MTASKnextRequest(ServerState *state)
{
    JNIEnv *env = state->env;

    /* Any other server specific initialization goes here */

    /* Sleep waiting for a request. The client process
       returns here, while the parent goes back to sleep. */
    
    /* There is one exception to this, and that is the "SOURCE"
       command. This allows the server to execute a program in its
       own address space rather than a child's, allowing it to
       expand its state */
    state->wasSourceCommand = waitForNextRequest(env, state);
    if (state->wasSourceCommand) {
	/* Prepare to process any weakrefs emerging from this
	   command */
	if (!restartSystemThreads(env, state)) {
	    return 0;
	}
    } else {
	/* In child */
	/* We first have to see if we are launched by JDETACH */
	/* If so, we don't call JUMPIsolateProcessImpl.start() */
	jmethodID createListenerID;
	jclass listenerClass;
	if (amExecutive) {
	    printf("In the executive, NOT launching JUMPIsolateProcessImpl\n");
	    /* We are the executive -- don't try to act like an isolate */
	    return 1;
	}
	
	listenerClass = (*env)->FindClass(env, "com/sun/jumpimpl/isolate/jvmprocess/JUMPIsolateProcessImpl");

	if ((listenerClass == NULL) ||
	    (*env)->ExceptionOccurred(env)) {
	    return 0;
	}
	createListenerID =
	    (*env)->GetStaticMethodID(env, listenerClass,
				      "start",
				      "()V");
	assert(createListenerID != NULL);
	
	if (createListenerID == NULL) {
	    (*env)->DeleteLocalRef(env, listenerClass);
	    return 0;
	}
	
	(*env)->CallStaticVoidMethod(env, listenerClass, createListenerID);
	(*env)->DeleteLocalRef(env, listenerClass);
	if ((*env)->ExceptionOccurred(env)) {
	    (*env)->ExceptionDescribe(env);
	}
    }
    return 1;
}

void
MTASKserverSourceReset(ServerState *state)
{
    /* Clear sun.misc.CVM's notion of what the main class was */
    (*state->env)->CallStaticVoidMethod(state->env, state->cvmClass, state->resetMainID);
    /* And stop threads */
    stopSystemThreads(state);
}

int
MTASKserverInitialize(ServerState* state,
    CVMParsedSubOptions* serverOpts,
    JNIEnv* env, jclass cvmClass)
{    
    const char* clist = CVMgetParsedSubOption(serverOpts, "initClasses");
    const char* mlist = CVMgetParsedSubOption(serverOpts, "precompileMethods");
    
    jumpMessageStart();
    
    /*
     * Set these up while server is being initialized.
     * This way we don't have to look up these JNI ID's again.
     */
    state->referenceClass =
	(*env)->FindClass(env, "java/lang/ref/Reference");
    assert(state->referenceClass != NULL);

    state->restartSystemThreadsID = 
	(*env)->GetStaticMethodID(env, state->referenceClass,
				  "restartReferenceThreads",
				  "()V");
    assert(state->restartSystemThreadsID != NULL);
    
    state->stopSystemThreadsID = 
	(*env)->GetStaticMethodID(env, state->referenceClass,
				  "stopReferenceThreads",
				  "()V");
    assert(state->stopSystemThreadsID != NULL);
    
    {
	struct sigaction sa;
	sa.sa_handler = child;
	sa.sa_flags = SA_RESTART;
#ifdef SA_NOCLDSTOP
	sa.sa_flags |= SA_NOCLDSTOP;
#endif
	sigemptyset(&sa.sa_mask);
	sigaction(SIGCHLD, &sa, NULL);
    }
    
    {
	extern void jumpProcessSetServerPid(int);
	jumpProcessSetServerPid(getpid());
    }
    
    fprintf(stderr, "Starting mTASK server at pid=%d  .... ", getpid());

    state->env = env;
    state->cvmClass = cvmClass;
    state->initialized = JNI_TRUE;
    state->isTestingMode = JNI_FALSE;
    state->testingModeFilePrefix = NULL;

     /*
      * Now let's warmup. Make sure we do this before stopSystemThreads(),
      * because stopSystemThreads() will also close all open fd's. We need the
      * fd's, becuase we may need to load classes.
      */
    if (clist != NULL || mlist != NULL) {
	jclass warmupClass;
        jmethodID runitID;
        jstring jclist;
        jstring jmlist;
        warmupClass = (*env)->FindClass(env, "sun/mtask/Warmup");
        if (warmupClass == NULL) {
	    goto error;
        }
        runitID = (*env)->GetStaticMethodID(env, warmupClass, 
                        "runit", "(Ljava/lang/String;Ljava/lang/String;)V");
        if (runitID == NULL) {
            goto error;
        }
        jclist = (clist == NULL) ? NULL : 
                                   (*env)->NewStringUTF(env, clist);
        if ((*env)->ExceptionOccurred(env)) {
            goto error;
        }
        jmlist = (mlist == NULL) ? NULL :
                                   (*env)->NewStringUTF(env, mlist);
        if ((*env)->ExceptionOccurred(env)) {
            goto error;
        }
        (*env)->CallStaticVoidMethod(env, warmupClass, runitID, jclist, jmlist);
    }

    stopSystemThreads(state);
    fprintf(stderr, "done!\n");

    state->resetMainID =
	(*env)->GetStaticMethodID(env, cvmClass,
				  "resetMain",
				  "()V");
    if (state->resetMainID == NULL) {
	goto error;
    }

    CVMdestroyParsedSubOptions(serverOpts);
    return 1;

error:
    CVMdestroyParsedSubOptions(serverOpts);
    return 0;
}
