package com.example.demo.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entities.Student;
import com.example.demo.repositories.StuRepo;

@RestController
@RequestMapping("/students")
public class StuController {

    @Autowired
    private StuRepo stu;

    // ✅ GET /students
    @GetMapping
    public List<Student> getAllStudents() {
        return stu.findAll();
    }

    // ✅ GET /students/{regNo}
    @GetMapping("/{regNo}")
    public Optional<Student> getStudentByRegNo(@PathVariable int regNo) {
        return stu.findById(regNo);
    }

    // ✅ POST /students
    @PostMapping
    public String insertStudent(@RequestBody Student s) {

        if (stu.existsById(s.getRegNo())) {
            return "Student already exists";
        }

        stu.save(s);
        return "Student inserted successfully";
    }

    // ✅ PUT /students/{regNo}
    @PutMapping("/{regNo}")
    public String updateStudent(@PathVariable int regNo,
                                @RequestBody Student s) {

        if (!stu.existsById(regNo)) {
            return "Student not found";
        }

        s.setRegNo(regNo);
        stu.save(s);
        return "Student updated successfully";
    }

    // ✅ PATCH /students/{regNo}
    @PatchMapping("/{regNo}")
    public String patchStudent(@PathVariable int regNo,
                               @RequestBody Map<String, Object> updates) {

        Optional<Student> optional = stu.findById(regNo);

        if (optional.isEmpty()) {
            return "Student not found";
        }

        Student student = optional.get();

        updates.forEach((key, value) -> {
            switch (key) {
                case "name" -> student.setName((String) value);
                case "school" -> student.setSchool((String) value);
                case "gender" -> student.setGender((String) value);
                case "standard" -> student.setStandard((Integer) value);
                case "percentage" -> student.setPercentage((Integer) value);
            }
        });

        stu.save(student);
        return "Student partially updated";
    }

    // ✅ DELETE /students/{regNo}
    @DeleteMapping("/{regNo}")
    public String deleteStudent(@PathVariable int regNo) {

        if (!stu.existsById(regNo)) {
            return "Student not found";
        }

        stu.deleteById(regNo);
        return "Student deleted successfully";
    }

    // ✅ GET /students/school?name=KV
    @GetMapping("/school")
    public List<Student> getBySchool(@RequestParam String name) {
        return stu.findBySchool(name);
    }

    // ✅ GET /students/school/count?name=DPS
    @GetMapping("/school/count")
    public long countBySchool(@RequestParam String name) {
        return stu.countBySchool(name);
    }

    // ✅ GET /students/school/standard/count?class=5
    @GetMapping("/school/standard/count")
    public long countByStandard(@RequestParam("class") int standard) {
        return stu.countByStandard(standard);
    }

    // ✅ GET /students/result?pass=true
    @GetMapping("/result")
    public List<Student> getResult(@RequestParam boolean pass) {

        if (pass) {
            return stu.findByPercentageGreaterThanEqualOrderByPercentageDesc(40);
        } else {
            return stu.findAll().stream()
                    .filter(s -> s.getPercentage() < 40)
                    .toList();
        }
    }

    // ✅ GET /students/strength?gender=MALE&standard=5
    @GetMapping("/strength")
    public long getStrength(@RequestParam String gender,
                            @RequestParam int standard) {

        return stu.countByGenderAndStandard(gender, standard);
    }
}
