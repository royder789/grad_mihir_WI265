import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

enum Gender {
    MALE,
    FEMALE
}

class Emp {

    String name;
    int age;
    Gender gender;
    int salary;
    String designation;
    String department;

    Emp(String n, int a, int s, Gender g, String desig, String dept) {
        this.name = n;
        this.age = a;
        this.salary = s;
        this.gender = g;
        this.designation = desig;
        this.department = dept;
    }

    @Override
    public String toString() {
        return "Emp{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", gender=" + gender +
                ", salary=" + salary +
                ", designation='" + designation + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}

public class StreamMain {

    public static void main(String[] args) {

        List<Emp> list = new ArrayList<>();

        list.add(new Emp("Arjun", 29, 34000, Gender.MALE, "PROGRAMMER", "IT"));
        list.add(new Emp("Vikram", 36, 47000, Gender.MALE, "MANAGER", "HR"));
        list.add(new Emp("Deepak", 31, 39000, Gender.MALE, "CLERK", "QA"));
        list.add(new Emp("Nitin", 27, 31000, Gender.MALE, "PROGRAMMER", "IT"));
        list.add(new Emp("Rohhan", 41, 52000, Gender.MALE, "MANAGER", "ADMIN"));

        list.add(new Emp("Aisha", 26, 29500, Gender.FEMALE, "CLERK", "QA"));
        list.add(new Emp("Ritu", 33, 43500, Gender.FEMALE, "MANAGER", "IT"));
        list.add(new Emp("Neha", 30, 36000, Gender.FEMALE, "PROGRAMMER", "IT"));
        list.add(new Emp("Tanvi", 39, 48500, Gender.FEMALE, "MANAGER", "FINANCE"));
        list.add(new Emp("Komal", 25, 27000, Gender.FEMALE, "CLERK", "CUSTOMER CARE"));

        list.add(new Emp("Siddharth", 28, 32500, Gender.MALE, "PROGRAMMER", "IT"));
        list.add(new Emp("Ankur", 34, 45000, Gender.MALE, "MANAGER", "IT"));
        list.add(new Emp("Prakash", 30, 35500, Gender.MALE, "CLERK", "QA"));
        list.add(new Emp("Manish", 37, 49500, Gender.MALE, "MANAGER", "SALES"));
        list.add(new Emp("Kartik", 24, 26500, Gender.MALE, "CLERK", "CUSTOMER CARE"));

        list.add(new Emp("Shruti", 32, 42000, Gender.FEMALE, "MANAGER", "HR"));
        list.add(new Emp("Ishita", 38, 47000, Gender.FEMALE, "MANAGER", "HR"));
        list.add(new Emp("Pallavi", 29, 34500, Gender.FEMALE, "PROGRAMMER", "IT"));
        list.add(new Emp("Divya", 35, 45500, Gender.FEMALE, "MANAGER", "QA"));
        list.add(new Emp("Sonal", 23, 25000, Gender.FEMALE, "CLERK", "IT"));

        list.add(new Emp("Aditya", 27, 30000, Gender.MALE, "CLERK", "QA"));
        list.add(new Emp("Ramesh", 46, 55000, Gender.MALE, "CEO", "OPERATIONS"));
        list.add(new Emp("Lokendra", 39, 50000, Gender.MALE, "MANAGER", "IT"));
        list.add(new Emp("Tarun", 28, 31500, Gender.MALE, "PROGRAMMER", "IT"));
        list.add(new Emp("Gaurav", 31, 37000, Gender.MALE, "CLERK", "FINANCE"));

        Emp highestSalaryEmp = list.stream()
                .max(Comparator.comparingInt(e -> e.salary))
                .orElse(null);

        System.out.println("Employee with highest salary : ");
        System.out.println(highestSalaryEmp);
        System.out.println("---------------------");

        Map<Gender, Long> genderCount = list.stream()
                .collect(Collectors.groupingBy(e -> e.gender, Collectors.counting()));

        System.out.println("Count of MALE & FEMALE Employee : " + genderCount);
        System.out.println("---------------------");

        Map<String, Integer> deptExpense = list.stream()
                .collect(Collectors.groupingBy(e -> e.department,
                        Collectors.summingInt(e -> e.salary)));

        System.out.println("Total expense Department wise : ");
        System.out.println(deptExpense);
        System.out.println("------------------------");

        List<Emp> seniorMost = list.stream()
                .sorted(Comparator.comparingInt((Emp e) -> e.age).reversed())
                .limit(5)
                .toList();

        System.out.println("5 senior most employee in the company :");
        System.out.println(seniorMost);
        System.out.println("------------------------");

        Predicate<Emp> isManager = e -> e.designation.equalsIgnoreCase("MANAGER");

        List<String> managers = list.stream()
                .filter(isManager)
                .map(e -> e.name)
                .toList();

        System.out.println("MANAGERS : " + managers);
        System.out.println("----------------------");

        list.stream()
                .filter(isManager.negate())
                .forEach(e -> e.salary += (int) (0.20 * e.salary));

        System.out.println("Hike applied to non-managers.");
        System.out.println("----------------------");

        long totalEmployees = list.stream().count();
        System.out.println("Total Employee : " + totalEmployees);
    }
}
