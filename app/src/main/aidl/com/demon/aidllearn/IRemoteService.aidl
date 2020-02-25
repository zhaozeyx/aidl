// IRemoteService.aidl
package com.demon.aidllearn;

interface IRemoteService {
    oneway void connnect();
    void disconnect();
    boolean isConnected();
}
