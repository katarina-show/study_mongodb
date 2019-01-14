package com.sjw.mongo.entity;

import java.math.BigDecimal;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

//该注解告诉spring  User对象对应的是mongodb中名为users的Collection
@Document(collection="users")
public class User {
	
	//注意ID字段的类型
	private ObjectId id;
		
	private String username;
	
	private String country;
	
	private Address address;
	
	private Favourites favourites;
	
	private int age;
	
	private BigDecimal salary;
	
	private float length;
	
	private List<Comment> comments;
	
	//mongodb连表查询注解
	//@DBRef
	//private Comments comments;
	

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public Favourites getFavourites() {
		return favourites;
	}

	public void setFavourites(Favourites favourites) {
		this.favourites = favourites;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public BigDecimal getSalary() {
		return salary;
	}

	public void setSalary(BigDecimal salary) {
		this.salary = salary;
	}

	public float getLength() {
		return length;
	}

	public void setLength(float length) {
		this.length = length;
	}



	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
	
/*	public Comments getComments() {
		return comments;
	}

	public void setComments(Comments comments) {
		this.comments = comments;
	}*/

	@Override
	public String toString() {
		return "User [id=" + id + ", username=" + username + ", country=" + country + ", address=" + address
				+ ", favourites=" + favourites + ", age=" + age + ", salary=" + salary + ", length=" + length
				+ ", comments=" + comments + "]";
	}






	
	
	
	
}
