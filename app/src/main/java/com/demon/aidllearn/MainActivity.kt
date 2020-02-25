package com.demon.aidllearn

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.demon.aidllearn.entity.Message
import com.demon.aidllearn.service.RemoteService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var remoteService: IRemoteService
    private lateinit var messageService: IMessageService
    private lateinit var serviceManager: IServiceManager
    private lateinit var remoteMessenger: Messenger

    private val handler = Handler(Looper.getMainLooper()) {
        it.data.apply {
            classLoader = Message::class.java.classLoader
        }.let { bundle ->
            val data: Message? = bundle.getParcelable("message")
            data
        }?.apply {
            handleReceiveMessage(this)
        }
        true
    }


    private val messageReceiveListener: MessageReceiveListener =
        object : MessageReceiveListener.Stub() {
            override fun onMessageReceived(message: Message?) {
                handler.post {

                }
            }

        }

    private val replyMessenger = Messenger(handler)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startService()
        bindButton()

    }

    private fun handleReceiveMessage(message: Message) {

        handler.postDelayed({
            Toast.makeText(this@MainActivity, "${message?.content}", Toast.LENGTH_SHORT)
                .show()
        }, 1500)
    }

    private fun startService() {
        val intent = Intent(this, RemoteService::class.java)
        bindService(intent, object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {
            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                serviceManager = IServiceManager.Stub.asInterface(service)
                remoteService =
                    IRemoteService.Stub.asInterface(serviceManager.getService(IRemoteService::class.java.simpleName))
                messageService =
                    IMessageService.Stub.asInterface(serviceManager.getService(IMessageService::class.java.simpleName))
                remoteMessenger =
                    Messenger(serviceManager.getService(Messenger::class.java.simpleName))
            }

        }, Context.BIND_AUTO_CREATE)
    }

    private fun bindButton() {
        connect.setOnClickListener {
            remoteService.connnect()
        }

        disconnect.setOnClickListener {
            remoteService.disconnect()
        }

        isConnected.setOnClickListener {
            Toast.makeText(
                this,
                "service is connected ${remoteService.isConnected}",
                Toast.LENGTH_SHORT
            ).show()
        }

        sendMessage.setOnClickListener {
            val message = Message("this is form main", true)
            messageService.sendMessage(message)
        }

        register.setOnClickListener {
            messageService.registerMessageReceiveListener(messageReceiveListener)
        }

        unregister.setOnClickListener {
            messageService.unregisterMessageReceiveListener(messageReceiveListener)
        }

        send_by_messenger.setOnClickListener {
            val message = Message("this message is sent by Messenger")

            Bundle().apply { putParcelable("message", message) }.let {
                val handlerMessage = android.os.Message()
                handlerMessage.data = it
                handlerMessage.replyTo = replyMessenger
                handlerMessage
            }.run {
                remoteMessenger.send(this)
            }
        }
    }
}
