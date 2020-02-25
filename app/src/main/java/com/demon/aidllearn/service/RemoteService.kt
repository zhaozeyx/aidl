package com.demon.aidllearn.service

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.Toast
import com.demon.aidllearn.IMessageService
import com.demon.aidllearn.IRemoteService
import com.demon.aidllearn.IServiceManager
import com.demon.aidllearn.MessageReceiveListener
import com.demon.aidllearn.entity.Message
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * <p>
 * [类说明]
 * </p>
 *
 * @author zhaozeyang
 * @since 2020-02-19
 */
class RemoteService : Service() {

    private var connected = false
    private val handler = Handler(Looper.getMainLooper()) {
        it.data.apply {
            classLoader = Message::class.java.classLoader
        }.let { bundle ->
            val data: Message? = bundle.getParcelable("message")
            data
        }?.apply {
            Toast.makeText(this@RemoteService, this.content, Toast.LENGTH_SHORT).show()
            replyToMainProcess(it.replyTo)
        }
        true
    }
    private val messageListenerArray = RemoteCallbackList<MessageReceiveListener>()
    private var messageScheduledFuture: ScheduledFuture<*>? = null

    private lateinit var scheduledThreadPoolExecutor: ScheduledThreadPoolExecutor

    private var messenger: Messenger = Messenger(handler)

    private val remoteService: IRemoteService = object : IRemoteService.Stub() {
        override fun connnect() {
            TimeUnit.SECONDS.sleep(5)
            handler.post {
                Toast.makeText(this@RemoteService, "connected", Toast.LENGTH_SHORT).show()
            }
            connected = true
            emulateReceiveMessage()
        }

        override fun disconnect() {
            handler.post {
                Toast.makeText(this@RemoteService, "disconnect", Toast.LENGTH_SHORT).show()
            }
            connected = false
            messageScheduledFuture?.cancel(true)
        }

        override fun isConnected(): Boolean {
            return connected
        }

    }

    private fun emulateReceiveMessage() {
        Log.d("YZZ", "start thread")
        messageScheduledFuture = scheduledThreadPoolExecutor.scheduleAtFixedRate({
            Log.d("YZZ", "run emulateReceiveMessage")
            try {
                val size = messageListenerArray.beginBroadcast()
                for (i in 0..size) {
                    val message = Message("this message from remote", false)
                    messageListenerArray.getBroadcastItem(i).onMessageReceived(message)
                    messageListenerArray.finishBroadcast()
                }
            } catch (e: Exception) {
                Log.e("YZZ", e.message)
            }

        }, 5000, 5000, TimeUnit.MILLISECONDS)
    }

    private val messageService: IMessageService = object : IMessageService.Stub() {
        override fun sendMessage(message: Message?) {
            handler.post {
                Toast.makeText(this@RemoteService, "$message", Toast.LENGTH_SHORT).show()
            }
            message?.isSuccess = connected
        }

        override fun registerMessageReceiveListener(listener: MessageReceiveListener?) {
            listener?.apply { messageListenerArray.register(this) }
        }

        override fun unregisterMessageReceiveListener(listener: MessageReceiveListener?) {
            listener?.apply { messageListenerArray.unregister(this) }
        }

    }

    private val serviceManager = object : IServiceManager.Stub() {
        override fun getService(serviceName: String?): IBinder? {
            return when (serviceName) {
                IRemoteService::class.java.simpleName -> remoteService.asBinder()
                IMessageService::class.java.simpleName -> messageService.asBinder()
                Messenger::class.java.simpleName -> messenger.binder
                else -> null
            }
        }

    }

    override fun onCreate() {
        super.onCreate()
        scheduledThreadPoolExecutor = ScheduledThreadPoolExecutor(1)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return serviceManager.asBinder()
    }

    private fun replyToMainProcess(messenger: Messenger) {
        val message = Message("this message is sent by Remote Messenger")

        Bundle().apply { putParcelable("message", message) }.let {
            val handlerMessage = Message()
            handlerMessage.data = it
            handlerMessage
        }.run {
            messenger.send(this)
        }
    }
}