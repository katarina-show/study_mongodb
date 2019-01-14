package com.sjw.mongo;


import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Resource;

import org.bson.BSON;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.sjw.mongo.entity.Comment;
import com.sjw.mongo.entity.MyOrder;
import com.sjw.mongo.entity.Order;
import com.sjw.mongo.entity.User;


import com.mongodb.WriteResult;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.Criteria.*;
import static org.springframework.data.mongodb.core.query.Query.*;
import static org.springframework.data.mongodb.core.query.Update.*;

import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.Update.PushOperatorBuilder;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class SpringUpdateObjArray {

	private static final Logger logger = LoggerFactory.getLogger(SpringUpdateObjArray.class);
	
	@Resource
	private MongoOperations tempelate;
    
    
    
    //--------------------------------------insert demo--------------------------------------------------------------
    
    //给jack老师增加一条评论（$push）
    //db.users.updateOne({"username":"jack"},
//                       {"$push":{"comments":{"author":"lison23","content":"ydddyyytttt"}}})
    @Test
    public void addOneComment(){
    	Query query = query(Criteria.where("username").is("jack"));
    	Comment comment = new Comment();
    	comment.setAuthor("lison23");
    	comment.setContent("ydddyyytttt");
		Update push = new Update().push("comments", comment);
		WriteResult updateFirst = tempelate.updateFirst(query, push, User.class);
		System.out.println(updateFirst.getN());
    }
    
    
//    给jack老师批量新增两条评论（$push,$each）
//    db.users.updateOne({"username":"jack"},     
//           {"$push":{"comments":
//                      {"$each":[{"author":"lison33","content":"lison33lison33"},
//                                      {"author":"lison44","content":"lison44lison44"}]}}})
    @Test
    public void addManyComment(){
    	Query query = query(Criteria.where("username").is("jack"));
    	Comment comment1 = new Comment();
    	comment1.setAuthor("lison55");
    	comment1.setContent("lison55lison55");
    	Comment comment2 = new Comment();
    	comment2.setAuthor("lison66");
    	comment2.setContent("lison66lison66");
		Update push = new Update().pushAll("comments", new Comment[]{comment1,comment2});
		WriteResult updateFirst = tempelate.updateFirst(query, push, User.class);
		System.out.println(updateFirst.getN());   	
    }
    
//    给jack老师批量新增两条评论并对数组进行排序（$push,$eachm,$sort）
//    db.users.updateOne({"username":"jack"}, 
//          {"$push": {"comments":
//                    {"$each":[ {"author":"lison22","content":"yyyytttt"},
//                                    {"author":"lison23","content":"ydddyyytttt"} ], 
//                      $sort: {"author":1} } } })
    @Test
    public void addManySortComment(){
    	Query query = query(Criteria.where("username").is("jack"));
    	Comment comment1 = new Comment();
    	comment1.setAuthor("lison77");
    	comment1.setContent("lison55lison55");
    	Comment comment2 = new Comment();
    	comment2.setAuthor("lison88");
    	comment2.setContent("lison66lison66");
    	
    	
		Update update = new Update();
		PushOperatorBuilder pob = update.push("comments");
		pob.each(comment1,comment2);
		pob.sort(new Sort(new Sort.Order(Direction.DESC, "author")));
		
		System.out.println("---------------");
		WriteResult updateFirst = tempelate.updateFirst(query, update,User.class);
		System.out.println(updateFirst.getN());   
    }
 
    //--------------------------------------delete demo--------------------------------------------------------------
 
//    删除lison1对jack的所有评论 （批量删除）
//    db.users.update({"username":“jack"},
//                               {"$pull":{"comments":{"author":"lison23"}}})

    @Test
    public void deleteByAuthorComment(){
    	Query query = query(Criteria.where("username").is("jack"));
    	
    	Comment comment1 = new Comment();
    	comment1.setAuthor("lison55");
		Update pull = new Update().pull("comments",comment1);
		WriteResult updateFirst = tempelate.updateFirst(query, pull, User.class);
		System.out.println(updateFirst.getN());   	
    }
    
    
//    删除lison5对lison评语为“lison是苍老师的小迷弟”的评论（精确删除）
//    db.users.update({"username":"lison"},
//            {"$pull":{"comments":{"author":"lison5",
//                                  "content":"lison是苍老师的小迷弟"}}})
    @Test
    public void deleteByAuthorContentComment(){
    	Query query = query(Criteria.where("username").is("lison"));
    	Comment comment1 = new Comment();
    	comment1.setAuthor("lison5");
    	comment1.setContent("lison是苍老师的小迷弟");
		Update pull = new Update().pull("comments",comment1);
		WriteResult updateFirst = tempelate.updateFirst(query, pull, User.class);
		System.out.println(updateFirst.getN());  
    }
    
    //--------------------------------------update demo--------------------------------------------------------------
//    db.users.updateMany({"username":"jack","comments.author":"lison1"},
//            {"$set":{"comments.$.content":"xxoo",
//                        "comments.$.author":"lison10" }})
//    	含义：精确修改某人某一条精确的评论，如果有多个符合条件的数据，则修改最后一条数据。无法批量修改数组元素
  @Test
  public void updateOneComment(){
  		Query query = query(where("username").is("lison").and("comments.author").is("lison4"));
  		Update update = update("comments.$.content","xxoo").set("comments.$.author","lison11");
		WriteResult updateFirst = tempelate.updateFirst(query, update, User.class);
		System.out.println(updateFirst.getN());  
  }

//--------------------------------------findandModify demo--------------------------------------------------------------
//使用findandModify方法在修改数据同时返回更新前的数据或更新后的数据
//db.num.findAndModify({
//      	"query":{"_id":ObjectId("5a58cef99506c50abaeb4384")},
//		  	"update":{"$inc":{"saleOrder":1}},
//		  	"new":true
//	     })
	@Test
	public void findAndModifyTest(){
		Query query = query(where("_id").is(new ObjectId("5a5a3a87d7598152855d758e")));
		Update update = new Update().inc("saleOrder", 1);
		FindAndModifyOptions famo = FindAndModifyOptions.options().returnNew(true);
		
		MyOrder ret = tempelate.findAndModify(query, update,famo, MyOrder.class);
		System.out.println(ret.getSaleOrder());
	}
  
	//随机生成orderTest数据
		@Test
		public void batchInsertOrder() {
			String[] userCodes = new String[] { "jack", "james", "five", "six",
					"jason", "mark", "luffy", "sean", "tony", "star", "denny" };
			List<Order> list = new ArrayList<>();
			for (int i = 0; i <= 10000; i++) {
				Order order = new Order();
				Random rand = new Random();
				int num = rand.nextInt(11);
				order.setUseCode(userCodes[num]);
				order.setOrderCode(UUID.randomUUID().toString());
				order.setOrderTime(RondomDateTest.randomDate("2015-01-01",
						"2017-10-31"));
				order.setPrice(RondomDateTest.randomBigDecimal(10000, 1));
				list.add(order);
				System.out.println(i);
			}
			tempelate.insertAll(list);
		}
	
}
