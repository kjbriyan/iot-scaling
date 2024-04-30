package com.example.iot_test

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage

class MainActivity : AppCompatActivity() {

    private lateinit var mqttClient: MqttAndroidClient
    private val mqttBrokerUrl = "tcp://172.21.24.16:1883"
    private val clientId = "AndroidClient"
    private val topic = "BeratKelapa"
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var statusTextView: TextView
    private lateinit var weight: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_activitys)

        loadingIndicator = findViewById(R.id.loadingIndicator)
        statusTextView = findViewById(R.id.statusTextView)
        weight = findViewById(R.id.etWeight)
        connectToMQTTBroker()
    }

    private fun connectToMQTTBroker() {
        loadingIndicator.visibility = View.VISIBLE
        statusTextView.visibility = View.VISIBLE
        statusTextView.text = "Connecting to MQTT broker..."


        mqttClient = MqttAndroidClient(applicationContext, mqttBrokerUrl, clientId)

        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isCleanSession = true

        mqttClient.connect(mqttConnectOptions, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                mqttClient.subscribe(topic, 0, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d("MQTT LOGS", "Subscribed to topic: $topic")
                        loadingIndicator.visibility = View.GONE
                        statusTextView.text = "Connected to MQTT broker"
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.e("MQTT LOGS", "Failed to subscribe to topic: $topic")
                        loadingIndicator.visibility = View.GONE
                        statusTextView.text = "Failed to connect to MQTT broker: ${exception?.message}"
                    }
                })

                mqttClient.setCallback(object : MqttCallback {
                    override fun connectionLost(cause: Throwable?) {}

                    override fun messageArrived(topic: String?, message: MqttMessage?) {
                        val payload = message?.payload?.toString(Charsets.UTF_8)
                        Log.d("MQTT LOGS", "Received message on topic $topic: $payload")
                        updatePieChart(payload?.toFloatOrNull() ?: 0f)
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    }
                })
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.e("MQTT LOGS", "Failed to connect to MQTT broker: $exception")
            }
        })
    }

    private fun updatePieChart(value: Float) {
        weight.text = "WEIGHT \n$value"
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mqttClient.disconnect()
        } catch (e: Exception) {
            Log.e("MQTT", "Error disconnecting MQTT client: $e")
        }
    }
}