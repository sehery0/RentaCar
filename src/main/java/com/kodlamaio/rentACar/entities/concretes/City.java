package com.kodlamaio.rentACar.entities.concretes;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity

public class City {
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		@Column(name= "id")
		private int id;
		
		
		@Column(name = "name")
		private String name;
		
		
		
		@OneToMany(mappedBy = "pickUpCityId")
		private List<Rental> pickUpCityRentals;
		
		@OneToMany(mappedBy = "returnCityId")
		private List<Rental> returnCityRentals;

}
