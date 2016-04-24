package com.pa.messaging.ui;

import javax.jms.Message;
import javax.jms.MessageListener;

/**
 *
 * @author paul.anderson
 */
public class SelectedTopicListener implements MessageListener{

	private final IMessageSink sink;
	
	public SelectedTopicListener(IMessageSink sink){
		this.sink = sink;
	}
	@Override
	public void onMessage(Message msg) {
		sink.receiveMessage(msg);
	}
	
}
