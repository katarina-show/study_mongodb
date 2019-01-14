package com.sjw.mongo.test;


import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.WriteResult;
import com.sjw.mongo.entity.Address;
import com.sjw.mongo.entity.Favourites;
import com.sjw.mongo.entity.User;

//spring 默认是Pojo的操作方式
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class QuickStartSpringPojoTest {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(QuickStartSpringPojoTest.class);
	
	//MongoTemplate在bean标签中被注入，是MongoOperations的实现类
	@Resource
	private MongoOperations tempelate;
   	
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
    	
    	//spring中叫insertAll
    	tempelate.insertAll(Arrays.asList(user,user1));
    }
    
    @Test
    public void testDelete(){
    	
    	//delete from users where username = ‘lison’
    	//spring中和原生的相比，更像是SQL语句
    	//使用的Query，而原生使用的是Filter
    	WriteResult remove = tempelate.remove(query(where("username").is("lison")), User.class);
    	System.out.println(remove.getN());
    	
    	//delete from users where age >8 and age <25
    	WriteResult remove2 = tempelate.remove(query(new Criteria().andOperator(where("age").gt(8),where("age").lt(25))), User.class);
    	System.out.println(remove2.getN());
    	
    }
    
    @Test
    public void testUpdate(){
    	//update  users  set age=6 where username = 'lison' 
    	WriteResult updateFirst = tempelate.updateMulti(query(where("username").is("lison")), update("age", 6), User.class);
    	System.out.println(updateFirst.getN());
    	//update users  set favourites.movies add "小电影2 ", "小电影3" where favourites.cites  has "东莞"
    	Query query = query(where("favourites.cites").is("东莞"));
		Update update = new Update().addToSet("favourites.movies").each("小电影2 ", "小电影3");
		WriteResult updateMulti = tempelate.updateMulti(query, update, User.class);
		System.out.println(updateMulti.getN());
    }
    
    @Test
    public void testFind(){
    	
    	//select * from users  where favourites.cites has "东莞"、"东京"
    	List<User> find = tempelate.find(query(where("favourites.cites").all(Arrays.asList("东莞","东京"))), User.class);
    	System.out.println(find.size());
    	
    	
    	//select * from users  where username like '%s%' and (contry= EngLish or contry = USA)
    	String regexStr = ".*s.*";
    	//username like '%s%'
    	Criteria regex = where("username").regex(regexStr);
    	//contry= EngLish
    	Criteria or1 = where("country").is("English");
    	//contry= USA
    	Criteria or2 = where("country").is("USA");
    	
    	Criteria or = new Criteria().orOperator(or1,or2);
    	
    	Query query = query(new Criteria().andOperator(regex,or));
    	
    	List<User> find2 = tempelate.find(query, User.class);
    	
    	System.out.println(find2.size());

    }
    
	    
	
	
	

}
