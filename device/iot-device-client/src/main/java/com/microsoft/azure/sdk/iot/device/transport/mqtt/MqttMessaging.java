// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageProperty;

import java.io.IOException;

public class MqttMessaging extends Mqtt
{
    private String subscribeTopic;
    private String publishTopic;
    private String parseTopic;

    private static final char PROPERTY_SEPARATOR = '&';
    private static final char PAIR_SEPARATOR = '=';
    private static final String DEVICE_ID_TAG = "$.mid";

    public MqttMessaging(String serverURI, String deviceId, String userName, String password, IotHubSSLContext context) throws IOException
    {
        /*
        **Codes_SRS_MqttMessaging_25_001: [**The constructor shall throw InvalidParameter Exception if any of the parameters are null or empty .**]**
         */
        /*
        **Codes_SRS_MqttMessaging_25_002: [**The constructor shall use the configuration to instantiate super class and passing the parameters.**]**
         */
        super(serverURI, deviceId, userName, password, context);
        /*
        **Codes_SRS_MqttMessaging_25_003: [**The constructor construct publishTopic and subscribeTopic from deviceId.**]**
         */
        this.publishTopic = "devices/" + deviceId + "/messages/events/";
        this.subscribeTopic = "devices/" + deviceId + "/messages/devicebound/#";
        this.parseTopic = "devices/" + deviceId + "/messages/devicebound/";

    }

    public void start() throws IOException
    {
        /*
        **Codes_SRS_MqttMessaging_25_020: [**start method shall be call connect to establish a connection to IOT Hub with the given configuration.**]**

        **Codes_SRS_MqttMessaging_25_021: [**start method shall subscribe to messaging subscribe topic once connected.**]**
         */

        this.connect();
        this.subscribe(subscribeTopic);
    }

    public void stop() throws IOException
    {
       try
       {
           /*
           **Codes_SRS_MqttMessaging_25_022: [**stop method shall be call disconnect to tear down a connection to IOT Hub with the given configuration.**]**
            */

           this.disconnect();
       }
       finally
       {
            /*
            As MQTT connection is controlled by this class, it is important to restart
            base class on exit from this class.
            */
           this.restartBaseMqtt();
       }


    }

    public void send(Message message) throws IOException
    {
        if (message == null || message.getBytes() == null)
        {
            /*
            **Codes_SRS_MqttMessaging_25_025: [**send method shall throw an exception if the message is null.**]**
             */
            throw new IOException("Message cannot be null");
        }

        MessageProperty[] messageProperties = message.getProperties();
        String messagePublishTopic;
        if(messageProperties.length > 0)
        {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(this.publishTopic);
            boolean needAmpersand = false;
            for(MessageProperty property : message.getProperties())
            {
                if(needAmpersand)
                {
                    stringBuilder.append(PROPERTY_SEPARATOR);
                }
                /*
                **Codes_SRS_MqttMessaging_25_026: [**send method shall append the message properties to publishTopic before publishing.**]**
                 */
                stringBuilder.append(property.getName());
                stringBuilder.append(PAIR_SEPARATOR);
                stringBuilder.append(property.getValue());
                needAmpersand = true;
            }
            /*
             **Tests_SRS_MqttMessaging_21_027: [**send method shall append the messageid to publishTopic before publishing using the key name `$.mid`.**]**
             */
            if(message.getMessageId() != null)
            {
                stringBuilder.append(PROPERTY_SEPARATOR);
                stringBuilder.append(DEVICE_ID_TAG);
                stringBuilder.append(PAIR_SEPARATOR);
                stringBuilder.append(message.getMessageId());
            }
            messagePublishTopic = stringBuilder.toString();
        }
        else
        {
            messagePublishTopic = this.publishTopic;
        }
        /*
        **Codes_SRS_MqttMessaging_25_024: [**send method shall publish a message to the IOT Hub on the publish topic by calling method publish().**]**
         */
        this.publish(messagePublishTopic, message.getBytes());
    }
}
