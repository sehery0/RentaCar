package com.kodlamaio.rentACar.api.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kodlamaio.rentACar.business.abstracts.CarService;
import com.kodlamaio.rentACar.business.request.cars.CreateCarRequest;
import com.kodlamaio.rentACar.business.request.cars.DeleteCarRequest;
import com.kodlamaio.rentACar.business.request.cars.UpdateCarRequest;
import com.kodlamaio.rentACar.business.response.cars.GetAllCarResponse;
import com.kodlamaio.rentACar.business.response.cars.GetCarResponse;
import com.kodlamaio.rentACar.core.utilities.results.DataResult;
import com.kodlamaio.rentACar.core.utilities.results.Result;

@RestController
@RequestMapping("/api/cars")
public class CarsController {

	@Autowired
	private CarService carService;

	@PostMapping("/add")
	public Result add(@RequestBody CreateCarRequest createCarRequest) {
		return this.carService.add(createCarRequest);
	}

	@PostMapping("/update")
	public Result update(@RequestBody UpdateCarRequest updateCarRequest) {
		return this.carService.update(updateCarRequest);
	}

	@DeleteMapping("/delete")
	public Result delete(@RequestBody DeleteCarRequest deleteCarRequest) {
		return this.carService.delete(deleteCarRequest);
	}
	
	@GetMapping("/getById")
	public DataResult<GetCarResponse> getById(@RequestParam int id) {
		return this.carService.getById(id);
		
	}
	
	@GetMapping("/getAll")
	public DataResult<List<GetAllCarResponse>> getAll()
	{
		return this.carService.getAll();
	}
	
}
