package com.bootcamp.expense.expense;

import org.springframework.data.repository.CrudRepository;

public interface ExpenseRepository extends CrudRepository<Expense, Integer> {
	Iterable<Expense> findByStatusAndEmployeeIdNot(String status, int employeeId);
}
