package com.example.demo.repo.d1;

import com.example.demo.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface repoh2 extends JpaRepository<Student, Long> {
}