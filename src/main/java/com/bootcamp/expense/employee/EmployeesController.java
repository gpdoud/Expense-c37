package com.bootcamp.expense.employee;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/employees")
public class EmployeesController {
	
	@Autowired
	private EmployeeRepository emplRepo;
	
	@GetMapping
	public ResponseEntity<Iterable<Employee>> getEmployees() {
		var employees = emplRepo.findAll();
		return new ResponseEntity<Iterable<Employee>>(employees, HttpStatus.OK);
	}

	@GetMapping("{id}")
	public ResponseEntity<Employee> getEmployee(@PathVariable int id) {
		var employee = emplRepo.findById(id);
		if(employee.isEmpty())
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		return new ResponseEntity<Employee>(employee.get(), HttpStatus.OK);
	}

	@GetMapping("{email}/{password}")
	public ResponseEntity<Employee> loginEmployee(@PathVariable String email, @PathVariable String password) {
		var employee = emplRepo.findByEmailAndPassword(email, password);
		if(employee.isEmpty())
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		return new ResponseEntity<Employee>(employee.get(), HttpStatus.OK);
	}
	
	@PostMapping
	public ResponseEntity<Employee> postEmployee(@RequestBody Employee employee) {
		if(employee.getId() != 0)
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		emplRepo.save(employee);
		return new ResponseEntity<Employee>(employee, HttpStatus.CREATED);
	}
	
	@SuppressWarnings("rawtypes")
	@PutMapping("{employeeId}")
	public ResponseEntity putEmployee(@PathVariable int employeeId, @RequestBody Employee employee) {
		if(employeeId != employee.getId() || employee.getId() == 0) 
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		var empl = emplRepo.findById(employee.getId());
		if(empl.isEmpty())
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		emplRepo.save(employee);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@SuppressWarnings("rawtypes")
	@DeleteMapping("{employeeId}")
	public ResponseEntity deleteEmployee(@PathVariable int employeeId) {
		var empl = emplRepo.findById(employeeId);
		if(empl.isEmpty())
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		emplRepo.delete(empl.get());
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
