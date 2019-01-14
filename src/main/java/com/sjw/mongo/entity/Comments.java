package com.sjw.mongo.entity;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

//对应Collection为comments
@Document(collection="comments")
public class Comments {
	
	private List<Comment> lists;

	public List<Comment> getLists() {
		return lists;
	}

	public void setLists(List<Comment> lists) {
		this.lists = lists;
	}
	
	

}
