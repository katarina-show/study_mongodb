package com.sjw.mongo.test;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

//静态导入
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Filters.*;

//原生java驱动 document的操作方式
public class QuickStartJavaDocTest {

	private static final Logger logger = LoggerFactory.getLogger(QuickStartJavaDocTest.class);
	
    private MongoDatabase db;
    
    private MongoCollection<Document> doc;
    
    private MongoClient client;
	
	//初始化操作
    @Before
    public void init(){
    	client = new MongoClient("192.168.1.6",27017);
    	db =client.getDatabase("lison");
    	doc = db.getCollection("users");
    }
    
    //添加数据
    @Test
    public void insertDemo(){
    	Document doc1 = new Document();
    	//基本属性
    	doc1.append("username", "cang");
    	doc1.append("country", "USA");
    	doc1.append("age", 20);
    	doc1.append("length", 1.77f);
    	//BigDecimal类型会被转换成Decimal128
    	doc1.append("salary", new BigDecimal("6565.22"));
    	
    	//复合属性address
    	Map<String, String> address1 = new HashMap<String, String>();
    	address1.put("aCode", "0000");
    	address1.put("add", "xxx000");
    	doc1.append("address", address1);
    	
    	//复合属性favourites1
    	Map<String, Object> favourites1 = new HashMap<String, Object>();
    	//movies和cities是1个数组
    	favourites1.put("movies", Arrays.asList("aa","bb"));
    	favourites1.put("cities", Arrays.asList("东莞","东京"));
    	doc1.append("favourites", favourites1);
    	
    	//第二个人
    	Document doc2  = new Document();
    	
    	doc2.append("username", "lison");
    	doc2.append("country", "China");
    	doc2.append("age", 30);
    	doc2.append("length", 1.77f);
    	doc2.append("salary", new BigDecimal("8888.22"));
    	
    	Map<String, String> address2 = new HashMap<>();
    	address2.put("aCode", "411000");
    	address2.put("add", "我的地址2");
    	doc2.append("address", address2);
    	
    	Map<String, Object> favourites2 = new HashMap<>();
    	favourites2.put("movies", Arrays.asList("东游记","一路向东"));
    	favourites2.put("cities", Arrays.asList("珠海","东京"));
    	doc2.append("favourites", favourites2);
    	
    	//insertMany插入多条
    	doc.insertMany(Arrays.asList(doc1,doc2));
    	
    }
    
    //删除数据
    @Test
    public void testDelete(){
    	
    	//deleteMany删除多条，参数是一个Bson类型过滤器
    	//静态导入所以不用类.方法
    	//eq方法相当于 delete from users where username = ‘lison’
    	DeleteResult deleteMany = doc.deleteMany(eq("username", "lison"));
    	logger.info(String.valueOf("删除了" + deleteMany.getDeletedCount() + "条数据"));
    	
    	//and方法、gt(即大于)方法、It(即小于)方法 相当于 delete from users where age >8 and age <25
    	DeleteResult deleteMany2 = doc.deleteMany(and(gt("age",8),lt("age",25)));
    	logger.info(String.valueOf("删除了" + deleteMany2.getDeletedCount() + "条数据"));

    }
    
    //更新数据
    @Test
    public void testUpdate(){
    	//update  users  set age=6 where username = 'lison' 
    	//$set表示只改1个字段
    	//updateMany批量更新
    	UpdateResult updateMany = doc.updateMany(eq("username", "lison"), 
    			                  new Document("$set",new Document("age",6)));
    	logger.info(String.valueOf("修改了" + updateMany.getModifiedCount() + "条数据"));
    	
    	//update users  set favorites.movies add "小电影2 ", "小电影3" where favourites.cities  has "东莞"
    	UpdateResult updateMany2 = doc.updateMany(eq("favourites.cities", "东莞"), 
    			                                  addEachToSet("favourites.movies", Arrays.asList( "小电影2 ", "小电影3")));
    	logger.info(String.valueOf("修改了" + updateMany2.getModifiedCount() + "条数据"));
    }
    
    //查询数据
    @Test
    public void testFind(){
    	final List<Document> ret = new ArrayList<>();
    	Block<Document> printBlock = new Block<Document>() {
			@Override
			public void apply(Document t) {
				logger.info(t.toJson());
				ret.add(t);
			} 		
		};
		
		
    	//select * from users  where favourites.cities has "东莞" 和 "东京" 
		FindIterable<Document> find = doc.find(all("favourites.cities", Arrays.asList("东莞","东京")));
		find.forEach(printBlock);
		logger.info("查到了" + String.valueOf(ret.size() + "条数据"));
		ret.removeAll(ret);
    	
    	//模糊查询
		//like在mongodb中实际是靠正则实现的
    	//select * from users  where username like '%s%' and (contry= China or contry = USA)
		String regexStr = ".*s.*";
		Bson regex = regex("username", regexStr);
		Bson or = or(eq("country","China"),eq("country","USA"));
		FindIterable<Document> find2 = doc.find(and(regex,or));
		find2.forEach(printBlock);
		logger.info("查到了" + String.valueOf(ret.size() + "条数据"));

    }
    

}
