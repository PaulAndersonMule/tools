package com.pa.messaging.ui;

import javax.jms.Message;

/**
 *
 * @author paul.anderson
 */
public interface IMessageSink {
	void receiveMessage(Message msg);
}
