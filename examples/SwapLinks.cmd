-- $ProjectHeader: use 2-1-0-release.1 Sun, 09 May 2004 13:57:11 +0200 mr $

!create a:A
!create b1:B
!create b2:B
!insert (a,b1) into R1
!insert (a,b2) into R2

-- swap links

!openter a swap_b()
!let tmp_b1 : B = self.rb1
!delete (self,self.rb1) from R1
!insert (self,self.rb2) into R1
!delete (self,self.rb2) from R2
!insert (self,tmp_b1) into R2
!opexit

