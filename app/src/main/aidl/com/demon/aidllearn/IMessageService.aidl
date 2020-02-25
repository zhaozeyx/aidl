// IMessageService.aidl
package com.demon.aidllearn;
import com.demon.aidllearn.entity.Message;
import com.demon.aidllearn.MessageReceiveListener;

// Declare any non-default types here with import statements

interface IMessageService {
    void sendMessage(in Message message);
    void registerMessageReceiveListener(MessageReceiveListener listener);
    void unregisterMessageReceiveListener(MessageReceiveListener listener);
}
