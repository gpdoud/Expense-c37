package com.bootcamp.expense.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/items")
public class ItemsController {

	@Autowired
	private ItemRepository prodRepo;

	@GetMapping
	public ResponseEntity<Iterable<Item>> getProducts() {
		var products = prodRepo.findAll();
		return new ResponseEntity<Iterable<Item>>(products, HttpStatus.OK);
	}
	
	@GetMapping("{id}")
	public ResponseEntity<Item> getProduct(@PathVariable int id) {
		var prod = prodRepo.findById(id);
		if(prod.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Item>(prod.get(), HttpStatus.OK);
	}
	
	@PostMapping
	public ResponseEntity<Item> postProduct(@RequestBody Item product) {
		if(product == null || product.getId() != 0) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		var prod = prodRepo.save(product);
		return new ResponseEntity<Item>(prod, HttpStatus.CREATED);
	}
	
	@SuppressWarnings("rawtypes")
	@PutMapping("{id}")
	public ResponseEntity putProduct(@PathVariable int id, @RequestBody Item product) {
		if(product == null || product.getId() == 0) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		var prod = prodRepo.findById(product.getId());
		if(prod.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		prodRepo.save(product);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	@SuppressWarnings("rawtypes")
	@DeleteMapping("{id}")
	public ResponseEntity deleteProduct(@PathVariable int id) {
		var prod = prodRepo.findById(id);
		if(prod.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		prodRepo.delete(prod.get());
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}