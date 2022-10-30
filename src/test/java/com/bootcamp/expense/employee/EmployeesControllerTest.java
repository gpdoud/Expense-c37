package com.bootcamp.expense.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

@SpringBootTest
class EmployeesControllerTest {
	
	@Autowired
	EmployeesController empCtrl;

	@Test
	public void TestGetAllEmployeees() {
		assertNotNull(empCtrl);
		var empls = empCtrl.getEmployees();
		assertThat(empls).isInstanceOf(ResponseEntity.class);
	}
	
	@Test
	public void TestGetEmployeeByPk() {
		var empl = empCtrl.getEmployee(1);
		assertThat(empl.getBody()).isInstanceOf(Employee.class);
		empl = empCtrl.getEmployee(1000);
		assertEquals(empl.getStatusCodeValue(), 404);
	}

}
