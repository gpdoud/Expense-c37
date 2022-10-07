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
	
	public static final String NEW = "NEW";
	public static final String MODIFIED = "MODIFIED";
	public static final String REVIEW = "REVIEW";
	public static final String APPROVED = "APPROVED";
	public static final String REJECTED = "REJECTED";
	public static final String PAID = "PAID";

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
	public ResponseEntity putExpense(@PathVariable int id, @RequestBody Expense expense) throws Exception {
		if(expense.getId() == 0 || expense.getId() != id) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		var exp = expRepo.findById(expense.getId());
		if(exp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if(ExpenseIsPaid(expense.getId()))
			return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
		expRepo.save(expense);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	@SuppressWarnings("rawtypes")
	@PutMapping("review/{id}")
	public ResponseEntity reviewExpense(@PathVariable int id, @RequestBody Expense expense) throws Exception {
		// expense.Status = (expense.Total <= 200) ? "APPROVED" : "REVIEW";
		if(ExpenseIsPaid(expense.getId()))
			return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
		String newStatus = expense.getTotal() <= 200 ? APPROVED : REVIEW; 
		expense.setStatus(newStatus);
		var respEntity = putExpense(id, expense);
		if(expense.getStatus().equals(APPROVED))
			ExpenseApprovedSet(expense, empRepo);
		return respEntity;
	}
	
	@SuppressWarnings("rawtypes")
	@PutMapping("approve/{id}")
	public ResponseEntity approveExpense(@PathVariable int id, @RequestBody Expense expense) throws Exception {
		if(ExpenseIsPaid(expense.getId()))
			return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
		expense.setStatus(APPROVED);
		var respEntity = putExpense(id, expense);
		ExpenseApprovedSet(expense, empRepo);
		return respEntity;
	}

	@SuppressWarnings("rawtypes")
	@PutMapping("reject/{id}")
	public ResponseEntity rejectExpense(@PathVariable int id, @RequestBody Expense expense) throws Exception {
		if(ExpenseIsPaid(expense.getId()))
			return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
		var prevStatus = expense.getStatus();
		expense.setStatus(REJECTED);
		if(prevStatus.equals(APPROVED))
			ExpenseApprovedUnSet(expense, empRepo);
		return putExpense(id, expense);
	}
	
	@SuppressWarnings("rawtypes")
	@PutMapping("pay/{expenseId}")
	public ResponseEntity payExpense(@PathVariable int expenseId) throws Exception {
		if(ExpenseIsPaid(expenseId))
			return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
		Optional<Expense> optExpense = expRepo.findById(expenseId);
		if(optExpense.isEmpty())
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		var expense = optExpense.get();
		if(!expense.getStatus().equals(APPROVED))
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		expense.setStatus(PAID);
		ExpensePaid(expense, empRepo);
		expRepo.save(expense);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	public static void ExpenseApprovedSet(Expense expense, EmployeeRepository empRepo) throws Exception {
		Optional<Employee> optEmployee = empRepo.findById(expense.getEmployee().getId());
		if(optEmployee.isEmpty())
			throw new Exception("Employee for expense not found");
		var employee = optEmployee.get();
		employee.setExpensesDue(employee.getExpensesDue() + expense.getTotal());
		empRepo.save(employee);
	}
	
	public static void ExpenseApprovedUnSet(Expense expense, EmployeeRepository empRepo)  throws Exception {
		Optional<Employee> optEmployee = empRepo.findById(expense.getEmployee().getId());
		if(optEmployee.isEmpty())
			throw new Exception("Employee for expense not found");
		var employee = optEmployee.get();
		employee.setExpensesDue(employee.getExpensesDue() - expense.getPrevTotal());
		empRepo.save(employee);
	}

	public static void ExpensePaid(Expense expense, EmployeeRepository empRepo) throws Exception {
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
	public ResponseEntity deleteExpense(@PathVariable int id) throws Exception {
		var exp = expRepo.findById(id);
		if(exp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if(ExpenseIsPaid(exp.get().getId()))
			return new ResponseEntity<>(HttpStatus.LOCKED);
		expRepo.delete(exp.get());
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	private boolean ExpenseIsPaid(int expenseId) throws Exception {
		var expense = expRepo.findById(expenseId);
		if(expense.isEmpty())
			throw new Exception("Expense not found");
		return expense.get().getStatus().equals(PAID);
	}
}
