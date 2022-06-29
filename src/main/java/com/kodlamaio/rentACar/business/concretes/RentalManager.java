package com.kodlamaio.rentACar.business.concretes;


import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.kodlamaio.rentACar.business.abstracts.CarService;
import com.kodlamaio.rentACar.business.abstracts.CityService;
import com.kodlamaio.rentACar.business.abstracts.CustomerService;
import com.kodlamaio.rentACar.business.abstracts.IndividualCustomerService;
import com.kodlamaio.rentACar.business.abstracts.RentalService;
import com.kodlamaio.rentACar.business.request.rentals.CreateRentalRequest;
import com.kodlamaio.rentACar.business.request.rentals.UpdateRentalRequest;
import com.kodlamaio.rentACar.business.response.rentals.GetAllRentalResponse;
import com.kodlamaio.rentACar.core.utilities.adapters.abstracts.FindexCheckService;
import com.kodlamaio.rentACar.core.utilities.exceptions.BusinessException;
import com.kodlamaio.rentACar.core.utilities.mapping.ModelMapperService;
import com.kodlamaio.rentACar.core.utilities.results.DataResult;
import com.kodlamaio.rentACar.core.utilities.results.Result;
import com.kodlamaio.rentACar.core.utilities.results.SuccessDataResult;
import com.kodlamaio.rentACar.core.utilities.results.SuccessResult;
import com.kodlamaio.rentACar.dataAccess.abstracts.RentalRepository;
import com.kodlamaio.rentACar.entities.concretes.Rental;

@Service
public class RentalManager implements RentalService {
	private RentalRepository rentalRepository;
	private CarService carService;
	private CustomerService customerService;
	private IndividualCustomerService individualCustomerService;
	private ModelMapperService modelMapperService;
	private FindexCheckService findexCheckService;
	private CityService cityService;

	//Bir servis sadece kendi repository'sini enjekte edebilir. 
	//İki servis birbirini kullanırsa lazy yap.
	public RentalManager(RentalRepository rentalRepository, CarService carService, CustomerService customerService,
			IndividualCustomerService individualCustomerService, ModelMapperService modelMapperService,
			FindexCheckService findexCheckService, CityService cityService) {
		this.rentalRepository = rentalRepository;
		this.carService = carService;
		this.customerService = customerService;
		this.individualCustomerService = individualCustomerService;
		this.modelMapperService = modelMapperService;
		this.findexCheckService = findexCheckService;
		this.cityService = cityService;
	}

	@Override
	public DataResult<List<GetAllRentalResponse>> getAll() {
		List<Rental> rentals = rentalRepository.findAll();
		List<GetAllRentalResponse> response = rentals.stream()
				.map(rental -> this.modelMapperService.forResponse().map(rental, GetAllRentalResponse.class))
				.collect(Collectors.toList());
		return new SuccessDataResult<List<GetAllRentalResponse>>(response, "DATA.LISTED.SUCCESSFULLY");

	}

	@Override
	public Result addForIndividualCustomer(CreateRentalRequest createRentalRequest) {
		checkIfCarExist(createRentalRequest.getCarId());
		checkIfCityExist(createRentalRequest.getPickUpCityId());
		checkIfCityExist(createRentalRequest.getReturnCityId());
		checkIfCustomerExist(createRentalRequest.getCustomerId());
		checkIfCarAlreadyRented(createRentalRequest.getCarId());
		checkIfCarInMaintenance(createRentalRequest.getCarId());
		checkDates(createRentalRequest.getPickUpDate(), createRentalRequest.getReturnDate());
		checkIfFindexIsEnough(createRentalRequest.getCarId(), createRentalRequest.getCustomerId());
		setStateAsRented(createRentalRequest.getCarId());
		setTotalDays(createRentalRequest);
		setTotalPrice(createRentalRequest);
		Rental rental = modelMapperService.forRequest().map(createRentalRequest, Rental.class);
		rentalRepository.save(rental);
		return new SuccessResult("RENTAL.ADDED.FOR.INDIVIDUAL.CUSTOMER");
	}

	@Override
	public Result addForCorporateCustomer(CreateRentalRequest createRentalRequest) {

		checkIfCarExist(createRentalRequest.getCarId());
		checkIfCityExist(createRentalRequest.getPickUpCityId());
		checkIfCityExist(createRentalRequest.getReturnCityId());
		checkIfCustomerExist(createRentalRequest.getCustomerId());
		checkIfCarAlreadyRented(createRentalRequest.getCarId());
		checkIfCarInMaintenance(createRentalRequest.getCarId());
		checkDates(createRentalRequest.getPickUpDate(), createRentalRequest.getReturnDate());
		setStateAsRented(createRentalRequest.getCarId());
		setTotalDays(createRentalRequest);
		setTotalPrice(createRentalRequest);
		Rental rental = modelMapperService.forRequest().map(createRentalRequest, Rental.class);
		rentalRepository.save(rental);
		return new SuccessResult("RENTAL.ADDED.FOR.CORPORATE.CUSTOMER");
	}

	@Override
	public Result delete(int id) {
		checkIfRentalExistById(id);
		rentalRepository.deleteById(id);
		return new SuccessResult("RENTAL.DELETED");
	}

	@Override
	public Result update(UpdateRentalRequest updateRentalRequest) {
		checkIfRentalExistById(updateRentalRequest.getId());
		checkIfCarExist(updateRentalRequest.getCarId());
		checkIfCityExist(updateRentalRequest.getPickUpCityId());
		checkIfCityExist(updateRentalRequest.getReturnCityId());
		checkIfCustomerExist(updateRentalRequest.getCustomerId());
		checkIfCarInMaintenance(updateRentalRequest.getCarId());
		updateTotalDays(updateRentalRequest);
		updateTotalPrice(updateRentalRequest);
		checkDates(updateRentalRequest.getPickUpDate(), updateRentalRequest.getReturnDate());
		checkIfFindexIsEnough(updateRentalRequest.getCarId(), updateRentalRequest.getCustomerId());
		Rental rental = modelMapperService.forRequest().map(updateRentalRequest, Rental.class);
		rentalRepository.save(rental);
		return new SuccessResult("RENTAL.UPDATED");

	}

	private void setTotalDays(CreateRentalRequest createRentalRequest) {
		modelMapperService.forRequest().map(createRentalRequest, Rental.class).setTotalDays(
				findDayDifference(createRentalRequest.getPickUpDate(), createRentalRequest.getReturnDate()));
	}

	private void setTotalPrice(CreateRentalRequest createRentalRequest) {
		Rental rental = modelMapperService.forRequest().map(createRentalRequest, Rental.class);
		double dailyPrice = getDailyPriceOfCar(createRentalRequest.getCarId());
		rental.setTotalPrice(calculateTotalPrice(rental.getTotalDays(), dailyPrice,
				createRentalRequest.getPickUpCityId(), createRentalRequest.getReturnCityId()));
	}

	private void updateTotalDays(UpdateRentalRequest updateRentalRequest) {
		modelMapperService.forRequest().map(updateRentalRequest, Rental.class).setTotalDays(
				findDayDifference(updateRentalRequest.getPickUpDate(), updateRentalRequest.getReturnDate()));
	}
	
	private void updateTotalPrice(UpdateRentalRequest updateRentalRequest) {
		Rental rental = modelMapperService.forRequest().map(updateRentalRequest, Rental.class);
		double dailyPrice = getDailyPriceOfCar(updateRentalRequest.getCarId());
		rental.setTotalPrice(calculateTotalPrice(rental.getTotalDays(), dailyPrice,
				updateRentalRequest.getPickUpCityId(), updateRentalRequest.getReturnCityId()));
	}

	
	private double calculateTotalPrice(int totalDays, double dailyPrice, int pickUpCityId, int returnCityId) {
		if (pickUpCityId == returnCityId)
			return totalDays * dailyPrice;
		return (totalDays * dailyPrice) + 750;

	}

	private int findDayDifference(LocalDate pickUpDate, LocalDate returnDate) {

		Period period = Period.between(pickUpDate, returnDate);
		return Math.abs(period.getDays());

	}

	private boolean checkIfFindexIsEnough(int carId, int customerId) {
		int score = getMinFindexScoreOfCar(carId);
		String identityNumber = getIdentityNumberOfIndividualCustomer(customerId);
		boolean state = false;
		if (findexCheckService.CheckFindexScore(identityNumber) > score) {
			state = true;
		} else {
			state = false;
		}
		return state;
	}

	private int getMinFindexScoreOfCar(int carId) {
		checkIfCarExist(carId);
		return carService.getCarById(carId).getMinFindexScore();

	}

	private double getDailyPriceOfCar(int carId) {
		checkIfCarExist(carId);
		return carService.getCarById(carId).getDailyPrice();
	}

	private String getIdentityNumberOfIndividualCustomer(int customerId) {
		String identityNumber = individualCustomerService.getIdentityNumber(customerId);
		if (identityNumber.equals(null))
			throw new BusinessException("IDENTITY.NUMBER.IS.NOT.EXIST");
		return identityNumber;
	}

	private int checkState(int carId) {
		checkIfCarExist(carId);
		return carService.getCarById(carId).getState();

	}

	private void setStateAsRented(int carId) {
		checkIfCarExist(carId);
		carService.getCarById(carId).setState(3);
	}

	private void checkIfCarInMaintenance(int carId) {
		int state = checkState(carId);
		if (state == 2)
			throw new BusinessException("CAR.IN.MAINTENANCE");

	}

	private void checkIfCarAlreadyRented(int carId) {
		int state = checkState(carId);
		if (state == 3)
			throw new BusinessException("CAR.IS.ALREADY.RENTED");
	}

	private void checkDates(LocalDate pickUpDate, LocalDate returnDate) {
		if (!returnDate.isAfter(pickUpDate)) {
			throw new BusinessException("RETURN.DATE.SHOULD.BE.AFTER.THE.PICK.UP.DATE");
		}
	}

	private void checkIfCustomerExist(int customerId) {
		if (!customerService.findCustomerById(customerId).isSuccess())
			throw new BusinessException("CUSTOMER.IS.NOT.EXIST");
	}

	private void checkIfCityExist(int cityId) {
		if (!cityService.getById(cityId).isSuccess())
			throw new BusinessException("CITY.IS.NOT.EXIST");

	}

	private void checkIfCarExist(int carId) {
		if (!carService.getById(carId).isSuccess())
			throw new BusinessException("CAR.IS.NOT.EXIST");

	}

	private void checkIfRentalExistById(int id) {
		Optional<Rental> rental = this.rentalRepository.findById(id);
		if (rental.get().equals(null))
			throw new BusinessException("RENTAL.IS.NOT.EXIST");
	}

	

}
	