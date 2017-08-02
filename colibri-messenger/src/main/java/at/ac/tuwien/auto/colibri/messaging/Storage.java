package at.ac.tuwien.auto.colibri.messaging;

import java.util.HashMap;
import java.util.logging.Logger;

import at.ac.tuwien.auto.colibri.messaging.exceptions.AmbiguityException;
import at.ac.tuwien.auto.colibri.messaging.types.Message;

public class Storage
{
	/**
	 * Logger instance
	 */
	private static final Logger log = Logger.getLogger(Storage.class.getName());

	/**
	 * Map of sent and received messages
	 */
	private HashMap<String, Message> messages = null;

	public Storage()
	{
		log.info("Starting storage");

		// initialize collections
		this.messages = new HashMap<String, Message>();
	}

	/**
	 * Adds a message to the central storage.
	 * 
	 * @param message New message
	 */
	public synchronized void addMessage(Message message) throws AmbiguityException
	{
		// check message ID
		if (this.messages.containsKey(message.getMessageId()))
			throw new AmbiguityException("Message-Id is not unique (id = " + message.getMessageId() + ")", message);

		// store message ID
		this.messages.put(message.getMessageId(), message);

		log.info("Message stored (message-id = " + message.toString() + ")");
	}

	/**
	 * Returns a message with the given ID.
	 * 
	 * @param messageId Message ID
	 * @return Found message or null
	 */
	public synchronized Message getMessage(String messageId)
	{
		return this.messages.get(messageId);
	}
}
