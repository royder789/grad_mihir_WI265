import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Student } from '../models/student';

@Injectable({
  providedIn: 'root'
})
export class StudentService {

url="http://localhost:8186/students";

constructor(private http:HttpClient){}

getStudents(){
return this.http.get<Student[]>(this.url);
}


// student.service.ts

addStudent(s: Student) {
  // Add the third argument { responseType: 'text' }
  return this.http.post(this.url, s, { responseType: 'text' });
}

deleteStudent(regNo: number) {
  return this.http.delete(this.url + "/" + regNo, { responseType: 'text' });
}

updateStudent(regNo: number, s: Student) {
  return this.http.put(this.url + "/" + regNo, s, { responseType: 'text' });
}

}