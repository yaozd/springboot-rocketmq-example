package rocketmq_example.simple;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageQueue;

public class PullConsumer {
	private static final Map<MessageQueue, Long> OFFSE_TABLE = new HashMap<MessageQueue, Long>();

	public static void main(String[] args) throws MQClientException {
		DefaultMQPullConsumer consumer = new DefaultMQPullConsumer("please_rename_unique_group_name_5");
		consumer.setNamesrvAddr("10.10.6.71:9876;10.10.6.72:9876");
		consumer.start();

		Set<MessageQueue> mqs = consumer.fetchSubscribeMessageQueues("broker-a");
		
		for (MessageQueue mq : mqs) {
			System.out.println(mq.getBrokerName());
			System.out.printf("Consume from the queue: %s%n", mq);
			SINGLE_MQ: while (true) {
				try {
					PullResult pullResult = consumer.pullBlockIfNotFound(mq, null, getMessageQueueOffset(mq), 32);
					System.out.printf("%s%n", pullResult);
					putMessageQueueOffset(mq, pullResult.getNextBeginOffset());
					switch (pullResult.getPullStatus()) {
					case FOUND:
						pullResult.getMsgFoundList().forEach(a->{
							System.out.println(a.getBody());
						});
						break;
					case NO_MATCHED_MSG:
						break;
					case NO_NEW_MSG:
						break SINGLE_MQ;
					case OFFSET_ILLEGAL:
						break;
					default:
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		consumer.shutdown();
	}

	private static long getMessageQueueOffset(MessageQueue mq) {
		Long offset = OFFSE_TABLE.get(mq);
		if (offset != null)
			return offset;

		return 0;
	}

	private static void putMessageQueueOffset(MessageQueue mq, long offset) {
		OFFSE_TABLE.put(mq, offset);
	}

}
