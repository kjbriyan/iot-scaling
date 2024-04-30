package com.example.iot_test
import android.util.Log
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException

class MqttClientManager(private val serverUri: String, private val clientId: String, private val topic: String) {

    private lateinit var mqttClient: MqttAsyncClient

    fun connectAndSubscribe(callback: (String) -> Unit) {
        mqttClient = MqttAsyncClient(serverUri, clientId)

        val options = MqttConnectOptions().apply {
            isCleanSession = true
            // Add any additional options here, such as authentication
        }

        mqttClient.connect(options, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                // Connection successful
                subscribeToTopic(callback)
                Log.d("MQTT LOGS", "Subscribed to topic: $topic")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                // Connection failed
            }
        })

        mqttClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String) {
                // Connection complete
            }

            override fun connectionLost(cause: Throwable?) {
                // Connection lost
            }

            override fun messageArrived(topic: String?, message: org.eclipse.paho.client.mqttv3.MqttMessage?) {
                // Message received
                val payload = message?.payload?.toString(Charsets.UTF_8)
                payload?.let { callback(it) }
            }

            override fun deliveryComplete(token: org.eclipse.paho.client.mqttv3.IMqttDeliveryToken?) {
                // Message delivery complete
            }
        })
    }

    private fun subscribeToTopic(callback: (String) -> Unit) {
        mqttClient.subscribe(topic, 0, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                // Subscription successful
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                // Subscription failed
            }
        })
    }
    fun disconnect() {
        try {
            mqttClient.disconnect()
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }
}
