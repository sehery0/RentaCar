package com.kodlamaio.rentACar.business.concretes;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kodlamaio.rentACar.business.abstracts.AdditionalServiceItemService;
import com.kodlamaio.rentACar.business.request.additionalServiceItems.CreateAdditionalServiceItemRequest;
import com.kodlamaio.rentACar.business.request.additionalServiceItems.UpdateAdditionalServiceItemRequest;
import com.kodlamaio.rentACar.business.response.additionalServiceItems.AdditionalServiceItemResponse;
import com.kodlamaio.rentACar.core.utilities.mapping.ModelMapperService;
import com.kodlamaio.rentACar.core.utilities.results.DataResult;
import com.kodlamaio.rentACar.core.utilities.results.Result;
import com.kodlamaio.rentACar.core.utilities.results.SuccessDataResult;
import com.kodlamaio.rentACar.core.utilities.results.SuccessResult;
import com.kodlamaio.rentACar.dataAccess.abstracts.AdditionalServiceItemRepository;
import com.kodlamaio.rentACar.entities.concretes.AdditionalServiceItem;

@Service
public class AdditionalServiceItemManager implements AdditionalServiceItemService{
	
	private AdditionalServiceItemRepository additionalServiceItemRepository;
	private ModelMapperService modelMapperService;
	
	
	
    @Autowired
	public AdditionalServiceItemManager(AdditionalServiceItemRepository additionalServiceItemRepository,
			ModelMapperService modelMapperService) {
		super();
		this.additionalServiceItemRepository = additionalServiceItemRepository;
		this.modelMapperService = modelMapperService;
	}

	@Override
	public Result add(CreateAdditionalServiceItemRequest createAdditionalServiceItemRequest) {
		AdditionalServiceItem additionalServiceItem = this.modelMapperService.forRequest().map(createAdditionalServiceItemRequest, AdditionalServiceItem.class);
		this.additionalServiceItemRepository.save(additionalServiceItem);
		return new SuccessResult("ADDITIONALSERVİCEITEM.ADDED");
	}

	@Override
	public Result update(UpdateAdditionalServiceItemRequest updateAdditionalServiceItemRequest) {
		AdditionalServiceItem additionalServiceItem = this.modelMapperService.forRequest().map(updateAdditionalServiceItemRequest, AdditionalServiceItem.class);
		this.additionalServiceItemRepository.save(additionalServiceItem);
		return new SuccessResult("ADDITIONALSERVİCEITEM.UPDATED");
	}

	@Override
	public Result delete(int id) {
		this.additionalServiceItemRepository.deleteById(id);
		return new SuccessResult("ADDITIONALSERVİCEITEM.DELETED");
	}

	@Override
	public DataResult<AdditionalServiceItemResponse> findById(int id) {
		AdditionalServiceItem additionalServiceItem=additionalServiceItemRepository.findById(id).get();
		AdditionalServiceItemResponse additionalServiceItemResponse=modelMapperService.forResponse().map(additionalServiceItem, AdditionalServiceItemResponse.class);
		return new SuccessDataResult<AdditionalServiceItemResponse>(additionalServiceItemResponse);
	}

	@Override
	public DataResult<List<AdditionalServiceItemResponse>> getAll() {
		List<AdditionalServiceItem> additionalServiceItems = additionalServiceItemRepository.findAll();
		List<AdditionalServiceItemResponse> response = additionalServiceItems.stream()
				.map(additionalServiceItem -> this.modelMapperService.forResponse()
						.map(additionalServiceItem, AdditionalServiceItemResponse.class)).collect(Collectors.toList());
		return new SuccessDataResult<List<AdditionalServiceItemResponse>>(response);
	}

}
