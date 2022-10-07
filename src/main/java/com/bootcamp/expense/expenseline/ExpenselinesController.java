package com.bootcamp.expense.expenseline;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bootcamp.expense.employee.EmployeeRepository;
import com.bootcamp.expense.expense.ExpenseRepository;
import com.bootcamp.expense.expense.ExpensesController;

@CrossOrigin
@RestController
@RequestMapping("/api/expenselines")
public class ExpenselinesController {

	@Autowired
	private ExpenselineRepository explRepo;
	@Autowired
	private ExpenseRepository expRepo;
	@Autowired
	private EmployeeRepository empRepo;
	
	@SuppressWarnings("rawtypes")
	private ResponseEntity recalcExpenseTotal(int expenseId) throws Exception {
		var expOpt = expRepo.findById(expenseId);
		if(expOpt.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		var expense = expOpt.get();
		var expenseTotal = 0;
		Iterable<Expenseline> orderlines = explRepo.findByExpenseId(expense.getId());
		for(var expenseline : orderlines) {
			//var item = itemRepo.findById(expenseline.getItem().getId());
			//expenseline.setItem(item.get());
			expenseTotal += expenseline.getItem().getPrice() * expenseline.getQuantity();
		}
		expense.setTotal(expenseTotal);
		if(expense.getStatus().equals(ExpensesController.APPROVED)) {
			ExpensesController.ExpenseApprovedUnSet(expense, empRepo);
		}
		expense.setStatus(ExpensesController.MODIFIED);
		expRepo.save(expense);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping
	public ResponseEntity<Iterable<Expenseline>> getExpenselines() {
		var expenselines = explRepo.findAll();
		return new ResponseEntity<Iterable<Expenseline>>(expenselines, HttpStatus.OK);
	}
	
	@GetMapping("{id}")
	public ResponseEntity<Expenseline> getExpenseline(@PathVariable int id) {
		var expenseline = explRepo.findById(id);
		if(expenseline.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Expenseline>(expenseline.get(), HttpStatus.OK);
	}
	
	@PostMapping
	public ResponseEntity<Expenseline> postExpenseline(@RequestBody Expenseline expenseline) throws Exception {
		if(expenseline == null || expenseline.getId() != 0) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		var expl = explRepo.save(expenseline);
		var respEntity = this.recalcExpenseTotal(expl.getExpense().getId());
		if(respEntity.getStatusCode() != HttpStatus.OK) {
			throw new Exception("Recalculate request total failed!");
		}
		return new ResponseEntity<Expenseline>(expl, HttpStatus.CREATED);
	}
	
	@SuppressWarnings("rawtypes")
	@PutMapping("{id}")
	public ResponseEntity putExpenseline(@PathVariable int id, @RequestBody Expenseline expenseline) throws Exception {
		if(expenseline == null || expenseline.getId() == 0) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		var explOpt = explRepo.findById(expenseline.getId());
		if(explOpt.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		explRepo.save(expenseline);
		var respEntity = this.recalcExpenseTotal(expenseline.getExpense().getId());
		if(respEntity.getStatusCode() != HttpStatus.OK) {
			throw new Exception("Recalculate expense total failed!");
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	@SuppressWarnings("rawtypes")
	@DeleteMapping("{id}")
	public ResponseEntity deleteExpenseline(@PathVariable int id) throws Exception {
		var explOpt = explRepo.findById(id);
		if(explOpt.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		var expenseline = explOpt.get();
		explRepo.delete(expenseline);
		var respEntity = recalcExpenseTotal(expenseline.getExpense().getId());
		if(respEntity.getStatusCode() != HttpStatus.OK) {
			throw new Exception("Recalculate expense total failed!");
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	
}
