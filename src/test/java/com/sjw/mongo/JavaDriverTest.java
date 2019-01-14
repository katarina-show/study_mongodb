package com.sjw.mongo;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.*;
import static com.mongodb.client.model.Aggregates.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.bson.BSON;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.PushOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.operation.OrderBy;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class JavaDriverTest {

	private static final Logger logger = LoggerFactory.getLogger(JavaDriverTest.class);
	
    private MongoDatabase db;


    private MongoCollection<Document> collection;
    
    @Resource
    private MongoClient client;
    
    
    @Before
    public void init(){
	    	db = client.getDatabase("lison");
	    	collection=db.getCollection("users");
	    	//new MongoClient的步骤交给config包下的类做了
    }
    
    
    @Test
    //测试elemMatch操作符，数组中对象数据要符合查询对象里面所有的字段
    //查找lison5评语为“lison是苍老师的小迷弟”的人
    //db.users.find({"comments":{"$elemMatch":{"author" : "lison5","content" : "lison是苍老师的小迷弟"}}}) .pretty()
    public void testElemMatch(){
       	//定义数据的处理类
    	final List<Document> ret = new ArrayList<>();
    	Block<Document> printBlock = getBlock(ret);
    	//通过append方法拼接多个条件
    	Document filter = new Document().append("author","lison5")
    			                        					  .append("content","lison是苍老师的小迷弟");
		Bson elemMatch = Filters.elemMatch("comments",filter );

    	FindIterable<Document> find = collection.find(elemMatch);
    	
    	printOperation(ret, printBlock, find);
    	
    }
    
    /**
     * 			db.users.updateOne({"username":"lison",},
					{"$push": {
						 "comments": {
						   $each: [{
									"author" : "james",
									"content" : "lison是个好老师！",
									"commentTime" : ISODate("2018-01-06T04:26:18.354Z")
								}
							],
						   $sort: {"commentTime":-1}
						 }}});
     */
    
    
    @Test
    //新增评论时，使用$sort运算符进行排序，插入评论后，再按照评论时间降序排序
    public void demoStep1(){
    	Bson filter = eq("username", "lison");
    	Document comment = new Document().append("author","cang")
                						 .append("content","lison是我的粉丝")
                						 .append("commentTime", new Date());
    	//$sort: {"commentTime":-1}
    	Document sortDoc = new Document().append("commentTime", -1);
    	PushOptions sortDocument = new PushOptions().sortDocument(sortDoc);
    	// $each
		Bson pushEach = Updates.pushEach("comments", Arrays.asList(comment), sortDocument);
		
		
		UpdateResult updateOne = collection.updateOne(filter, pushEach);
		System.out.println(updateOne.getModifiedCount());
    }
    
    
    @Test
    //查看人员时加载最新的三条评论；
    //db.users.find({"username":"lison"},{"comments":{"$slice":[0,3]}}).pretty()
    public void demoStep2(){
    	final List<Document> ret = new ArrayList<>();
    	Block<Document> printBlock = getBlock(ret);
		
		FindIterable<Document> find = collection.find(eq("username", "lison"))
                								.projection(slice("comments", 0, 3));
		printOperation(ret, printBlock, find);
    }

    
    @Test
    //点击评论的下一页按钮，新加载三条评论
    //db.users.find({"username":"lison"},{"comments":{"$slice":[3,3]},"$id":1}).pretty();
    public void demoStep3(){
    	final List<Document> ret = new ArrayList<>();
    	Block<Document> printBlock = getBlock(ret);
		
		//{"username":"lison"}
		Bson filter = eq("username", "lison");
		//"$slice":[3,3]
		Bson slice = slice("comments", 3, 3);
		//"$id":1
		Bson includeID = include("id");
		
		//{"comments":{"$slice":[3,3]},"$id":1})
		Bson projection = fields(slice,includeID);
		
		FindIterable<Document> find = collection.find(filter)
                								.projection(projection);
		printOperation(ret, printBlock, find);
    }


    @Test
    /**
     * db.users.aggregate([{"$match":{"username":"lison"}},
                           {"$unwind":"$comments"},
	                       {$sort:{"comments.commentTime":-1}},
	                       {"$project":{"comments":1}},
	                       {"$skip":6},
	                       {"$limit":3}])
     */
    //如果有多种排序需求怎么处理,使用聚合
    public void demoStep4(){
    	final List<Document> ret = new ArrayList<>();
    	Block<Document> printBlock = getBlock(ret);
    	List<Bson> aggregates = new ArrayList<>();
    	
    	aggregates.add(match(eq("username","lison")));
    	aggregates.add(unwind("$comments"));
    	aggregates.add(sort(orderBy(ascending("comments.commentTime"))));
    	aggregates.add(project(fields(include("comments"))));
    	aggregates.add(skip(0));
    	aggregates.add(limit(3));
    	
    	AggregateIterable<Document> aggregate = collection.aggregate(aggregates);
    	
		printOperation(ret, printBlock, aggregate);
    }
    
    
    //dbRef测试
    //dbref其实就是关联关系的信息载体，本身并不会去关联数据
    @Test
    public void dbRefTest(){
    	final List<Document> ret = new ArrayList<>();
    	Block<Document> printBlock = getBlock(ret);
    	FindIterable<Document> find = collection.find(eq("username", "lison"));
		printOperation(ret, printBlock, find);
    }


//---------------------------------------------------------------------------

	private void printOperation(List<Document> ret, Block<Document> printBlock,
			AggregateIterable<Document> aggregate) {
		aggregate.forEach(printBlock);
		System.out.println(ret.size());
		ret.removeAll(ret);
		
	}


	private void printOperation(final List<Document> ret,
			Block<Document> printBlock, FindIterable<Document> find) {
		find.forEach(printBlock);
		System.out.println(ret.size());
		ret.removeAll(ret);
	}
	
	private Block<Document> getBlock(final List<Document> ret) {
		Block<Document> printBlock = new Block<Document>() {
			@Override
			public void apply(Document t) {
				logger.info("---------------------");
//				logger.info(t.toJson());
				Object object = t.get("comments");
				System.out.println(object);
				logger.info("---------------------");
				ret.add(t);
			}
		};
		return printBlock;
	}
    
    

}
