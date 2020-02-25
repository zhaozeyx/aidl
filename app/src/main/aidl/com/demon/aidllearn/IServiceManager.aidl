// IServiceManager.aidl
package com.demon.aidllearn;

// Declare any non-default types here with import statements

interface IServiceManager {
    IBinder getService(String serviceName);
}
