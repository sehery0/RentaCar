package com.kodlamaio.rentACar.business.concretes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kodlamaio.rentACar.business.abstracts.CarService;
import com.kodlamaio.rentACar.business.request.cars.CreateCarRequest;
import com.kodlamaio.rentACar.business.request.cars.UpdateCarRequest;
import com.kodlamaio.rentACar.business.response.cars.GetAllCarResponse;
import com.kodlamaio.rentACar.business.response.cars.GetCarResponse;
import com.kodlamaio.rentACar.core.utilities.exceptions.BusinessException;
import com.kodlamaio.rentACar.core.utilities.mapping.ModelMapperService;
import com.kodlamaio.rentACar.core.utilities.results.DataResult;
import com.kodlamaio.rentACar.core.utilities.results.Result;
import com.kodlamaio.rentACar.core.utilities.results.SuccessDataResult;
import com.kodlamaio.rentACar.core.utilities.results.SuccessResult;
import com.kodlamaio.rentACar.dataAccess.abstracts.CarRepository;
import com.kodlamaio.rentACar.entities.concretes.Car;

@Service
public class CarManager implements CarService {

	
	private CarRepository carRepository;
	private ModelMapperService modelMapperService;

	@Autowired
	public CarManager(CarRepository carRepository, ModelMapperService modelMapperService) {

		this.carRepository = carRepository;
		this.modelMapperService = modelMapperService;
	}

	@Override
	public DataResult<List<GetAllCarResponse>> getAll() {
		List<Car> cars = carRepository.findAll();
		List<GetAllCarResponse> response = cars.stream()
				.map(car -> this.modelMapperService.forResponse().map(car, GetAllCarResponse.class))
				.collect(Collectors.toList());

		return new SuccessDataResult<>(response, "DATA.LISTED.SUCCESSFULLY");
	}

	
	public DataResult<GetCarResponse> getById(int id) {
		checkIfCarExistById(id);
		Car car = carRepository.findById(id).get();
		return new SuccessDataResult<>(modelMapperService.forResponse().map(car, GetCarResponse.class));

	}

	@Override
	public Result add(CreateCarRequest createCarRequest) {
		checkIfLicensePlateExist(createCarRequest.getLicensePlate());
		Car car = this.modelMapperService.forRequest().map(createCarRequest, Car.class);
		checkNumberOfCars(car.getBrand().getId());
		carRepository.save(car);
		return new SuccessResult("CAR.ADDED");
	}

	@Override
	public Result update(UpdateCarRequest updateCarRequest) {
		checkIfCarExistById(updateCarRequest.getId());
		checkIfLicensePlateExist(updateCarRequest.getLicensePlate());
		Car car = modelMapperService.forRequest().map(updateCarRequest, Car.class);
		carRepository.save(car);
		return new SuccessResult("CAR.UPDATED");

	}

	@Override
	public Result deleteById(int id) {
		checkIfCarExistById(id);
		carRepository.deleteById(id);
		return new SuccessResult("CAR.DELETED");

	}

	private void checkNumberOfCars(int id) {
		List<Car> existsCars = carRepository.getByBrandId(id);
		if (existsCars.size() > 5)
			throw new BusinessException("CAR.NUMBER.IS.HIGHER.THAN.FIVE");

	}

	private void checkIfLicensePlateExist(String licensePlate) {
		Car car = carRepository.findByLicensePlate(licensePlate);
		if (car != null)
			throw new BusinessException("LICENSE.PLATE.EXIST");
	}

	private void checkIfCarExistById(int id) {
		Optional<Car> car = this.carRepository.findById(id);
		if (car.get().equals(null))
			throw new BusinessException("CAR.IS.NOT.EXIST");
	}

	
	public Car getCarById(int carId) {
		checkIfCarExistById(carId);
		return carRepository.findById(carId).get();

	}

	

	
}

