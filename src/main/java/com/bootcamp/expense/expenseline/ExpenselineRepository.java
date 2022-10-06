package com.bootcamp.expense.expenseline;

import org.springframework.data.repository.CrudRepository;

public interface ExpenselineRepository extends CrudRepository<Expenseline, Integer> {
	Iterable<Expenseline> findByExpenseId(int expenseId);
}
