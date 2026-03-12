import { Component } from '@angular/core';
import { Student } from '../../models/student';
import { StudentService } from '../../services/student';


@Component({
selector:'app-add-student',
templateUrl:'./add-student.html',
standalone:false
})
export class AddStudentComponent{

student:Student=new Student();

constructor(public ss:StudentService){}

addStudent(){
this.ss.addStudent(this.student)
.subscribe(res=>{
console.log(res);
});
}

}