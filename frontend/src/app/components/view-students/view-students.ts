import { Component,OnInit } from '@angular/core';

import { Student } from '../../models/student';
import { StudentService } from '../../services/student';

@Component({
selector:'app-view-students',
templateUrl:'./view-students.html',
standalone:false
})
export class ViewStudentsComponent implements OnInit{

students:Student[]=[];

constructor(public ss:StudentService){}

ngOnInit(){
this.ss.getStudents().subscribe(data=>{
this.students=data;
});
}

}