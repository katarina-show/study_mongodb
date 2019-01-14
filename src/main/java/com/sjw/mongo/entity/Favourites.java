package com.sjw.mongo.entity;

import java.util.List;

public class Favourites {
	private List<String> movies;
	private List<String> cities;
	
	public List<String> getMovies() {
		return movies;
	}
	public void setMovies(List<String> movies) {
		this.movies = movies;
	}
	public List<String> getCities() {
		return cities;
	}
	public void setCities(List<String> cities) {
		this.cities = cities;
	}
	
}
