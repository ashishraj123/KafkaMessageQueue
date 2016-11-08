/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package kafkapushmessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;
import kafka.javaapi.message.ByteBufferMessageSet;
import kafka.message.Message;
import kafka.producer.KeyedMessage;
import kafka.producer.SyncProducerConfig;
import kafkachat.ChannelListener;
import kafkachat.User;

/**
 * 
 * @author ashish
 */
public class KafkaChatClient extends Thread {

	/**
	 * @param args
	 *            the command line arguments
	 */
	private static User user;
	private static ProducerConfig pc;
	private static String SAMPLE_FINAL_MESSAGE = "this is sample kafka message";

	private static Hashtable<String, ChannelListener> source = new Hashtable<String, ChannelListener>();
	private static HashMap<String, ChannelListener> ChannelMap = new HashMap(
			source);
	private static final List<String> defaultUsernames = new ArrayList<>(
			Arrays.asList("Kucing", "Sapi", "Rusa", "Kambing", "Platipus",
					"Kucing", "Naga", "Panda"));

	public KafkaChatClient() {
	}

	public KafkaChatClient(String Username) {
		user = new User();
		user.setName(Username);
		Properties ProducerProperties = new Properties();
		ProducerProperties.put("metadata.broker.list", "localhost:9092");
		ProducerProperties.put("serializer.class",
				"kafka.serializer.StringEncoder");
		pc = new ProducerConfig(ProducerProperties);
	}

	public void join(String username, String channelName) {
		ChannelMap.put(channelName, new ChannelListener(username, channelName));
		ChannelMap.get(channelName).start();
		String message = username + " has joined channel " + channelName;
		System.out.println(message);
	}

	public void leave(String channelName) {
		String message = "";
		if (ChannelMap.containsKey(channelName)) {
			ChannelMap.get(channelName).shutdown();
			ChannelMap.remove(channelName);
			message = user.getName() + " left channel " + channelName;
		} else {
			message = "You're not in channel " + channelName;
		}
		System.out.println(message);
	}

	public void send(String message, String channelName) {
		if (ChannelMap.containsKey(channelName)) {
			String msg = "[" + channelName + "] " + "(" + user.getName() + ") "
					+ message;
			kafka.javaapi.producer.Producer<String, String> producer = new kafka.javaapi.producer.Producer<String, String>(
					pc);
			SimpleDateFormat sdf = new SimpleDateFormat();
			KeyedMessage<String, String> km = new KeyedMessage<String, String>(
					channelName, msg);
			producer.send(km);
			producer.close();
		} else {
			System.out.println("You're not in channel " + channelName);
		}
	}

	public void broadcast(String username, String message) {
		kafka.javaapi.producer.Producer<String, String> producer = new kafka.javaapi.producer.Producer<String, String>(
				pc);
		for (String channelName : ChannelMap.keySet()) {
			String msg = "[" + channelName + "] " + "(" + username + ") "
					+ message;
			SimpleDateFormat sdf = new SimpleDateFormat();
			KeyedMessage<String, String> km = new KeyedMessage<String, String>(
					channelName, msg);
			producer.send(km);
		}
		producer.close();
	}

	@Override
	public void run() {

		KafkaChatClient kc = new KafkaChatClient();
		int messageCounter = 0;
		while (messageCounter < -1) {
			kc.broadcast(user.getName(), SAMPLE_FINAL_MESSAGE + " "
					+ messageCounter);
			messageCounter++;
		}
		kc.broadcast(user.getName(), "hi");
		for (ChannelListener channel : ChannelMap.values()) {
			channel.shutdown();
		}
	}

}
