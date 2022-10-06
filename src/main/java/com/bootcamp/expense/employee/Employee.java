package com.bootcamp.expense.employee;

import javax.persistence.*;

@Entity(name="employees")
@Table(uniqueConstraints=@UniqueConstraint(name="UIDX_Email", columnNames={"email"}))
public class Employee {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	@Column(length=30, nullable=false)
	private String name;
	@Column(length=50, nullable=false)
	private String email;
	@Column(length=30, nullable=false)
	private String password;
	//@Column(columnDefinition="bit not null")
	private boolean admin;
	@Column(columnDefinition="decimal(7,2) not null")
	private double expensesDue;
	@Column(columnDefinition="decimal(7,2) not null")
	private double expensesPaid;
	
	public Employee() {}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean setAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public double getExpensesDue() {
		return expensesDue;
	}

	public void setExpensesDue(double expensesDue) {
		this.expensesDue = expensesDue;
	}

	public double getExpensesPaid() {
		return expensesPaid;
	}

	public void setExpensesPaid(double expensesPaid) {
		this.expensesPaid = expensesPaid;
	}
	
	
}
