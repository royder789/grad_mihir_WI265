import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { AppRoutingModule } from './app-routing-module';
import { App } from './app';

import {  ViewStudentsComponent } from './components/view-students/view-students';
import { FormsModule } from '@angular/forms';
import { AddStudentComponent } from './components/add-student/add-student';

@NgModule({
  declarations: [App, AddStudentComponent, ViewStudentsComponent],
  imports: [BrowserModule, AppRoutingModule, HttpClientModule,FormsModule],
  providers: [provideBrowserGlobalErrorListeners()],
  bootstrap: [App],
})
export class AppModule {}
