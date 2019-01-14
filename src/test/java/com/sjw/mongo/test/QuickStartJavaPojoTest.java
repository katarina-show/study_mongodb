package com.sjw.mongo.test;
/**
 * 文档结构：
 * {        
 *  	"_id" : ObjectId("59f938235d93fc4af8a37114"),         
 * 		"username" : “lison",         
 * 		"country" : "China",         
 * 		"address" : {                
 * 						 "aCode" : "邮编",                 
 * 						 "add" : "d11pff"         
 * 						},         
 * 		"favourites" : {                 
 * 						"movies" : ["杀破狼2","毒液","雷神1"],                 
 * 						"cities" : ["1sh","1cs","1zz"]         },        
 * 		"age" : 18，        
 * 		"salary"：NumberDecimal("2.099"),        
 * 		"length" ：1.79 
 * }  
 */

import static com.mongodb.client.model.Filters.all;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Filters.or;
import static com.mongodb.client.model.Filters.regex;
import static com.mongodb.client.model.Updates.addEachToSet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.sjw.mongo.entity.Address;
import com.sjw.mongo.entity.Favourites;
import com.sjw.mongo.entity.User;

//原生java驱动 Pojo的操作方式 3.4版本后的新功能
public class QuickStartJavaPojoTest {

	private static final Logger logger = LoggerFactory.getLogger(QuickStartJavaPojoTest.class);
	
    private MongoDatabase db;
    
    //这里是User
    private MongoCollection<User> doc;
    
    private MongoClient client;
	
	
    @Before
    public void init(){
    	
    	//使用pojo操作，需要如下几个步骤
    	
    	//1.定义1个  编解码器的list  编解码器即  把java中各数据类型，转成mongodb中的数据类型
    	List<CodecRegistry> codecResgistes = new ArrayList<>();
    	
    	
    	//2.编解码器的list加入默认的编解码器结合 ，默认编解码器不包含对pojo的解析
    	codecResgistes.add(MongoClient.getDefaultCodecRegistry());
    	
    	//然后我们再 自己定义一个pojo的编解码器
    	//3.利用现成的PojoCodecProvider 来构建1个pojo编解码器
    	CodecRegistry pojoProviders = CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build());
    	
    	codecResgistes.add(pojoProviders);
    	
    	//4.通过编解码器的list生成1个  新的  带pojo解析的   编解码器注册中心
    	CodecRegistry registry = CodecRegistries.fromRegistries(codecResgistes);
    	
    	//5.把编解码器注册中心放入MongoClientOptions
    	MongoClientOptions build = MongoClientOptions.builder().codecRegistry(registry).build();

    	ServerAddress serverAddress = new ServerAddress("192.168.1.6",27017);

    	//6.使用这种构造函数传入值
		client = new MongoClient(serverAddress, build);
	    //client = new MongoClient("192.168.1.6",27017);
    	db =client.getDatabase("lison");
    	
    	//把users这个Collection对应到代码中的User对象
    	doc = db.getCollection("users",User.class);
    }
    
    @Test
    public void insertDemo(){
    	User user = new User();
    	user.setUsername("cang");
    	user.setCountry("USA");
    	user.setAge(20);
    	user.setLength(1.77f);
    	user.setSalary(new BigDecimal("6265.22"));
    	Address address1 = new Address();
    	address1.setaCode("411222");
    	address1.setAdd("sdfsdf");
    	user.setAddress(address1);
    	Favourites favourites1 = new Favourites();
    	favourites1.setCities(Arrays.asList("东莞","东京"));
    	favourites1.setMovies(Arrays.asList("西游记","一路向西"));
    	user.setFavourites(favourites1);
    	
    	
    	User user1 = new User();
    	user1.setUsername("chen");
    	user1.setCountry("China");
    	user1.setAge(30);
    	user1.setLength(1.77f);
    	user1.setSalary(new BigDecimal("6885.22"));
    	Address address2 = new Address();
    	address2.setaCode("411000");
    	address2.setAdd("我的地址2");
    	user1.setAddress(address2);
    	Favourites favourites2 = new Favourites();
    	favourites2.setCities(Arrays.asList("珠海","东京"));
    	favourites2.setMovies(Arrays.asList("东游记","一路向东"));
    	user1.setFavourites(favourites2);
    	
    	
    	//直接传入User
    	doc.insertMany(Arrays.asList(user,user1));
    	
    }
    
    @Test
    public void testDelete(){
    	
    	//delete from users where username = ‘lison’
    	
    	DeleteResult deleteMany = doc.deleteMany(eq("username", "lison"));
    	logger.info(String.valueOf(deleteMany.getDeletedCount()));
    	
    	//delete from users where age >8 and age <25
    	DeleteResult deleteMany2 = doc.deleteMany(and(gt("age",8),lt("age",25)));
    	logger.info(String.valueOf(deleteMany2.getDeletedCount()));
    }
    
    @Test
    public void testUpdate(){
    	//update  users  set age=6 where username = 'lison' 
    	UpdateResult updateMany = doc.updateMany(eq("username", "lison"), 
    			                  new Document("$set",new Document("age",6)));
    	logger.info(String.valueOf(updateMany.getModifiedCount()));
    	
    	//update users  set favourites.movies add "小电影2 ", "小电影3" where favourites.cites  has "东莞"
    	UpdateResult updateMany2 = doc.updateMany(eq("favourites.cites", "东莞"), 
    			                                  addEachToSet("favourites.movies", Arrays.asList( "小电影2 ", "小电影3")));
    	logger.info(String.valueOf(updateMany2.getModifiedCount()));
    }
    
    @Test
    public void testFind(){
    	
    	final List<User> ret = new ArrayList<>();
    	Block<User> printBlock = new Block<User>() {
			@Override
			public void apply(User t) {
//				logger.info();
				System.out.println(t.getUsername());
				System.out.println(t.getSalary());
				ret.add(t);
			}
    		
		};
		
    	//select * from users  where favourites.cites has "东莞"、"东京"
		FindIterable<User> find = doc.find(all("favourites.cites", Arrays.asList("东莞","东京")));
		find.forEach(printBlock);
		logger.info(String.valueOf(ret.size()));
		ret.removeAll(ret);
    	
    	
    	//select * from users  where username like '%s%' and (contry= English or contry = USA)
		String regexStr = ".*s.*";
		Bson regex = regex("username", regexStr);
		Bson or = or(eq("country","English"),eq("country","USA"));
		FindIterable<User> find2 = doc.find(and(regex,or));
		find2.forEach(printBlock);
		logger.info(String.valueOf(ret.size()));

    }
    
	
	
	

}
