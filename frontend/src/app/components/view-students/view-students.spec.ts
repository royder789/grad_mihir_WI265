import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewStudents } from './view-students';

describe('ViewStudents', () => {
  let component: ViewStudents;
  let fixture: ComponentFixture<ViewStudents>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ViewStudents],
    }).compileComponents();

    fixture = TestBed.createComponent(ViewStudents);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
