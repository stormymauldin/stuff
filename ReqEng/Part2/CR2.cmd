!create ut : University

!create sam : Student
!create fred : Student

!insert (sam,ut) into EnrolledAtUniversity
!insert (fred,ut) into EnrolledAtUniversity

!create EE302 : Course
!create CS306 : Course
!create BUS311 : Course
!create EE379K : Course
!create E306 : Course
!create EE338 : Course
!create EE323 : Course

!set EE302.isFull := false
!set CS306.isFull := false
!set BUS311.isFull := false
!set EE379K.isFull := false
!set E306.isFull := false
!set EE338.isFull := false

!insert (sam,EE302) into TakingCourse
!insert (sam,CS306) into TakingCourse
!insert (sam,BUS311) into TakingCourse
!insert (sam,EE323) into TakingCourse

!insert (fred,EE302) into TakingCourse
!insert (fred,EE379K) into TakingCourse
!insert (fred,E306) into TakingCourse
!insert (fred,EE338) into TakingCourse

!set EE302.isFull := true

!openter sam drop(EE302)
!delete (sam,EE302) from TakingCourse
!set EE302.isFull := false
!opexit