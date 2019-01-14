package com.sjw.mongo;


import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.Criteria.*;
import static org.springframework.data.mongodb.core.query.Query.*;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.Update.PushOperatorBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.sjw.mongo.entity.Comment;
import com.sjw.mongo.entity.User;
import com.mongodb.WriteResult;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class SpringTest {

	private static final Logger logger = LoggerFactory
			.getLogger(SpringTest.class);

	@Resource
	private MongoOperations tempelate;

	@Test
	// db.users.find({"comments":{"$elemMatch":{"author" : "lison5","content" :
	// "lison是苍老师的小迷弟"}}}) .pretty()
	public void testElemMatch() {
		Query query = query(where("comments").elemMatch(where("author").is("lison5").and("content").is("lison是苍老师的小迷弟")));
		List<User> find = tempelate.find(query, User.class);
		System.out.println(find.size());

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
	// 新增评论时，使用$sort运算符进行排序，插入评论后，再按照评论时间降序排序
	public void demoStep1() {
		Query query = query(where("username").is("lison"));
		Comment comment = new Comment();
		comment.setAuthor("cang");
		comment.setCommentTime(new Date());
		comment.setContent("lison是我的粉丝");

		Update update = new Update();
		PushOperatorBuilder pob = update.push("comments");
		pob.each(comment);
		pob.sort(new Sort(new Sort.Order(Direction.DESC, "commentTime")));
		
		System.out.println("---------------");
		WriteResult updateFirst = tempelate.updateFirst(query, update,User.class);
		System.out.println("---------------");
		System.out.println(updateFirst.getN());
	}

	@Test
	// 查看人员时加载最新的三条评论；
	// db.users.find({"username":"lison"},{"comments":{"$slice":[0,3]}}).pretty()
	public void demoStep2() {
		//{"username":"lison"}
		Query query = query(where("username").is("lison"));
		//{"comments":{"$slice":[0,3]}
		query.fields().include("comments").slice("comments", 0, 3);
		System.out.println("---------------");
		List<User> find = tempelate.find(query, User.class);
		System.out.println("---------------");
		System.out.println(find);
		
	}

	@Test
	// 点击评论的下一页按钮，新加载三条评论
	// db.users.find({"username":"lison"},{"comments":{"$slice":[3,3]},"$id":1}).pretty();
	public void demoStep3() {
		Query query = query(where("username").is("lison"));
		query.fields().include("comments").slice("comments", 3, 3)
				.include("id");
		System.out.println("---------------");
		List<User> find = tempelate.find(query, User.class);
		System.out.println("---------------");
		System.out.println(find);
	}

	
	/**
	 * db.users.aggregate([{"$match":{"username":"lison"}},
	                       {"$unwind":"$comments"},
	                       {$sort:{"comments.commentTime":-1}},
	                       {"$project":{"comments":1}},
	                       {"$skip":6},
	                       {"$limit":3}])
	                       
	 */
	// 如果有多种排序需求怎么处理,使用聚合
	@Test
	public void demoStep4() {
		Aggregation aggs = newAggregation(
				match(where("username").is("lison")),
				unwind("comments"),
				sort(Direction.ASC, "comments.commentTime"),
				project("comments"), 
				skip(6), 
				limit(3));
		System.out.println("---------------");
		AggregationResults<Object> aggregate = tempelate.aggregate(aggs, "users",	Object.class);
		System.out.println("---------------");
		List<Object> mappedResults = aggregate.getMappedResults();
		System.out.println(mappedResults.size());

	}
	
	@Test
	//(1)注意相关的实体bean要加上注解@document，@dbRef
	//(2)spring对dbRef进行了封装，发起了两次查询请求
	public void dbRefTest(){
		System.out.println("----------------------------");
		List<User> users = tempelate.findAll(User.class);
		System.out.println("----------------------------");
		System.out.println(users);
//		System.out.println(users.get(0).getComments());
	}



}
