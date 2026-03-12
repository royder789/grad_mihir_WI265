package com.example.demo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entities.Student;

public interface StuRepo extends JpaRepository<Student, Integer> {

    List<Student> findBySchool(String school);

    long countBySchool(String school);

    long countByStandard(int standard);

    List<Student> findByPercentageGreaterThanEqualOrderByPercentageDesc(int percentage);

    long countByGenderAndStandard(String gender, int standard);
}
