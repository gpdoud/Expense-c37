package com.bootcamp.expense.expense;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bootcamp.expense.employee.Employee;
import com.bootcamp.expense.employee.EmployeeRepository;

@CrossOrigin
@RestController
@RequestMapping("/api/expenses")
public class ExpensesController {
	
	private final String NEW = "NEW";
	private final String REVIEW = "REVIEW";
	private final String APPROVED = "APPROVED";
	private final String REJECTED = "REJECTED";
	private final String PAID = "PAID";

	@Autowired
	private ExpenseRepository expRepo;
	@Autowired
	private EmployeeRepository empRepo;
	
	@GetMapping
	public ResponseEntity<Iterable<Expense>> getExpenses() {
		Iterable<Expense> expenses = expRepo.findAll();
		return new ResponseEntity<Iterable<Expense>>(expenses, HttpStatus.OK);
	}
	
	@GetMapping("{id}")
	public ResponseEntity<Expense> getExpense(@PathVariable int id) {
		Optional<Expense> expense = expRepo.findById(id);
		if(expense.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Expense>(expense.get(), HttpStatus.OK);
	}
	
	@GetMapping("reviews/{expenseId}")
	public ResponseEntity<Iterable<Expense>> getExpensesInReview(@PathVariable int employeeId) {
		Iterable<Expense> expenses = expRepo.findByStatusAndEmployeeIdNot(REVIEW, employeeId);
		return new ResponseEntity<Iterable<Expense>>(expenses, HttpStatus.OK);
	}
	
	@PostMapping
	public ResponseEntity<Expense> postExpense(@RequestBody Expense expense) {
		if(expense.getId() != 0) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		expense.setStatus(NEW);
		Expense newExpense = expRepo.save(expense);
		return new ResponseEntity<Expense>(newExpense, HttpStatus.CREATED);
	}
	
	@SuppressWarnings("rawtypes")
	@PutMapping("{id}")
	public ResponseEntity putExpense(@PathVariable int id, @RequestBody Expense expense) {
		if(expense.getId() == 0 || expense.getId() != id) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		var ord = expRepo.findById(expense.getId());
		if(ord.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		expRepo.save(expense);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	@SuppressWarnings("rawtypes")
	@PutMapping("review/{id}")
	public ResponseEntity reviewExpense(@PathVariable int id, @RequestBody Expense expense) throws Exception {
		// expense.Status = (expense.Total <= 200) ? "APPROVED" : "REVIEW";
		String newStatus = expense.getTotal() <= 200 ? APPROVED : REVIEW; 
		expense.setStatus(newStatus);
		var respEntity = putExpense(id, expense);
		if(expense.getStatus().equals(APPROVED))
			ExpenseApprovedSet(expense);
		return respEntity;
	}
	
	@SuppressWarnings("rawtypes")
	@PutMapping("approve/{id}")
	public ResponseEntity approveExpense(@PathVariable int id, @RequestBody Expense expense) throws Exception {
		expense.setStatus(APPROVED);
		var respEntity = putExpense(id, expense);
		ExpenseApprovedSet(expense);
		return respEntity;
	}

	@SuppressWarnings("rawtypes")
	@PutMapping("reject/{id}")
	public ResponseEntity rejectExpense(@PathVariable int id, @RequestBody Expense expense) throws Exception {
		var prevStatus = expense.getStatus();
		expense.setStatus(REJECTED);
		if(prevStatus.equals(APPROVED))
			ExpenseApprovedUnSet(expense);
		return putExpense(id, expense);
	}
	
	@SuppressWarnings("rawtypes")
	@PutMapping("pay/{expenseId}")
	public ResponseEntity payExpense(@PathVariable int expenseId) throws Exception {
		Optional<Expense> optExpense = expRepo.findById(expenseId);
		if(optExpense.isEmpty())
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		var expense = optExpense.get();
		if(!expense.getStatus().equals(APPROVED))
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		expense.setStatus(PAID);
		ExpensePaid(expense);
		expRepo.save(expense);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private void ExpenseApprovedSet(Expense expense) throws Exception {
		Optional<Employee> optEmployee = empRepo.findById(expense.getEmployee().getId());
		if(optEmployee.isEmpty())
			throw new Exception("Employee for expense not found");
		var employee = optEmployee.get();
		employee.setExpensesDue(employee.getExpensesDue() + expense.getTotal());
		empRepo.save(employee);
	}
	
	private void ExpenseApprovedUnSet(Expense expense)  throws Exception {
		Optional<Employee> optEmployee = empRepo.findById(expense.getEmployee().getId());
		if(optEmployee.isEmpty())
			throw new Exception("Employee for expense not found");
		var employee = optEmployee.get();
		employee.setExpensesDue(employee.getExpensesDue() - expense.getTotal());
		empRepo.save(employee);
	}

	private void ExpensePaid(Expense expense) throws Exception {
		Optional<Employee> optEmployee = empRepo.findById(expense.getEmployee().getId());
		if(optEmployee.isEmpty())
			throw new Exception("Employee for expense not found");
		var employee = optEmployee.get();
		employee.setExpensesPaid(employee.getExpensesPaid() + expense.getTotal());
		employee.setExpensesDue(employee.getExpensesDue() - expense.getTotal());
		empRepo.save(employee);
	}

	@SuppressWarnings("rawtypes")
	@DeleteMapping("{id}")
	public ResponseEntity deleteExpense(@PathVariable int id) {
		var ord = expRepo.findById(id);
		if(ord.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		expRepo.delete(ord.get());
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	
	
	
	
	
	
	
	
	
}