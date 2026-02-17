package com.example.demo.repo.d2;

import com.example.demo.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface repopost extends JpaRepository<Student, Long> {
}