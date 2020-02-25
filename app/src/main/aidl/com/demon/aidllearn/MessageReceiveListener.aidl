// MessageReceiveListener.aidl
package com.demon.aidllearn;
import com.demon.aidllearn.entity.Message;

// Declare any non-default types here with import statements

interface MessageReceiveListener {
    void onMessageReceived(in Message message);
}
