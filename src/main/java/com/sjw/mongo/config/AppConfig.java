package com.sjw.mongo.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

@Configuration
public class AppConfig {

  /*
   * Use the standard Mongo driver API to create a com.mongodb.MongoClient instance.
   */
	@Bean
   public MongoClient mongoClient() {
	   
//   	CodecRegistry registry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
//       CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));

		MongoClientOptions mco = MongoClientOptions.builder()
				.writeConcern(WriteConcern.ACKNOWLEDGED)
				.connectionsPerHost(100)
				.threadsAllowedToBlockForConnectionMultiplier(5)
			    //.readPreference(ReadPreference.secondary())   //可复制集读写分离
				.maxWaitTime(120000).connectTimeout(10000).build();
		
	   MongoClient client = new MongoClient(new ServerAddress("192.168.1.6", 27017), mco);
	   
		//可复制集配置
		/*List<ServerAddress> asList = Arrays.asList(
				new ServerAddress("192.168.1.6", 27017), 
				new ServerAddress("192.168.1.7", 27017), 
				new ServerAddress("192.168.1.8", 27017));*/
		
		//MongoClient client = new MongoClient(asList, mco);
	   
       return client;
   }
}
